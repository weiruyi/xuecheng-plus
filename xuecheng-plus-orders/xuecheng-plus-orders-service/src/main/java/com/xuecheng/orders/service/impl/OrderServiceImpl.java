package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.config.PayNotifyConfig;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final XcOrdersMapper xcOrdersMapper;
	private final XcOrdersGoodsMapper xcOrdersGoodsMapper;
	private final XcPayRecordMapper xcPayRecordMapper;
	@Autowired
	@Lazy
	private OrderServiceImpl currentProxy;
	private final RabbitTemplate rabbitTemplate;
	private final MqMessageService mqMessageService;

	@Value("${pay.qrcodeurl}")
	String qrcodeUrl;
	@Value("${pay.alipay.APP_ID}")
	String APP_ID;
	@Value("${pay.alipay.APP_PRIVATE_KEY}")
	String APP_PRIVATE_KEY;
	@Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
	String ALIPAY_PUBLIC_KEY;

	/**
	 * 创建商品订单
	 * @param userId
	 * @param addOrderDto
	 * @return
	 */
	@Override
	public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto){

		//插入订单表，进行幂等性判断,订单明细表
		XcOrders xcOrders = saveXcOrders(userId, addOrderDto);

		//插入支付记录
		XcPayRecord payRecord = createPayRecord(xcOrders);
		Long payNo = payRecord.getPayNo();

		//生成二维码
		QRCodeUtil qrCodeUtil = new QRCodeUtil();
		String url = String.format(qrcodeUrl, payNo);
		String qrCode = null;
		try {
			qrCode = qrCodeUtil.createQRCode(url, 200, 200);
		} catch (IOException e) {
			XueChengPlusException.cast("生成二维码出错");
		}
		PayRecordDto payRecordDto = new PayRecordDto();
		BeanUtils.copyProperties(payRecord, payRecordDto);
		payRecordDto.setQrcode(qrCode);
		return payRecordDto;
	}

	/**
	 * 插入支付记录
	 * @param xcOrders
	 * @return
	 */
	public XcPayRecord createPayRecord(XcOrders xcOrders){
		//订单id
		Long ordersId = xcOrders.getId();
		XcOrders xcOrdersDb = xcOrdersMapper.selectById(ordersId);
		//如果此订单不存在，不添加支付记录
		if(xcOrdersDb == null){
			XueChengPlusException.cast("订单不存在");
		}
		//订单状态
		String status = xcOrdersDb.getStatus();
		if("601002".equals(status)){//支付成功
			//如果此订单支付结果为成功，补再添加支付记录，避免重复支付
			XueChengPlusException.cast("此订单已支付");
		}

		//添加支付记录
		XcPayRecord xcPayRecord = new XcPayRecord();
		//支付记录号，将来传给支付宝
		long payNo = IdWorkerUtils.getInstance().nextId();
		xcPayRecord.setPayNo(payNo);
		xcPayRecord.setOrderId(ordersId);
		xcPayRecord.setOrderName(xcOrdersDb.getOrderName());
		xcPayRecord.setTotalPrice(xcOrdersDb.getTotalPrice());
		xcPayRecord.setCreateDate(LocalDateTime.now());
		xcPayRecord.setCurrency("CNY");
		xcPayRecord.setStatus("601001");//未支付
		xcPayRecord.setUserId(xcOrdersDb.getUserId());

		int insert = xcPayRecordMapper.insert(xcPayRecord);
		if(insert < 0) {
			XueChengPlusException.cast("插入支付记录失败");
		}
		return xcPayRecord;
	}

	/**
	 * 保存订单信息
	 * @param userId
	 * @param addOrderDto
	 * @return
	 */
	public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto){
		//进行幂等性判断，同一个选课记录表只能有一个订单
		XcOrders xcOrders = getOrderByBussinessId(addOrderDto.getOutBusinessId());
		if(xcOrders != null){
			return xcOrders;
		}

		//插入订单表
		xcOrders = new XcOrders();
		//用雪花算法生成订单号
		long orderId = IdWorkerUtils.getInstance().nextId();
		xcOrders.setId(orderId);
		xcOrders.setTotalPrice(addOrderDto.getTotalPrice());
		xcOrders.setCreateDate(LocalDateTime.now());
		xcOrders.setStatus("600001");//未支付
		xcOrders.setUserId(userId);
		xcOrders.setOrderName(addOrderDto.getOrderName());
		xcOrders.setOrderType("60201");//订单类型，购买课程
		xcOrders.setOrderDescrip(addOrderDto.getOrderDescrip());
		xcOrders.setOrderDetail(addOrderDto.getOrderDetail());
		xcOrders.setOutBusinessId(addOrderDto.getOutBusinessId());//如果是选课折里记录选课表id

		int insert = xcOrdersMapper.insert(xcOrders);
		if(insert <= 0){
			XueChengPlusException.cast("添加订单失败");
		}

		//插入订单明细表
		//将前端传入的json转为json
		String orderDetailJson = addOrderDto.getOrderDetail();
		List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
		//遍历xcOrdersGoods插入订单明细表
		xcOrdersGoods.forEach(xcOrdersGood -> {
			xcOrdersGood.setOrderId(orderId);
			int insert1 = xcOrdersGoodsMapper.insert(xcOrdersGood);
			if(insert1 <= 0){
				XueChengPlusException.cast("订单明细插入失败");
			}
		});

		return xcOrders;
	}

	/**
	 * 根据业务id查询订单,业务id为选课记录表id
	 * @param bussinessId
	 * @return
	 */
	public XcOrders getOrderByBussinessId(String bussinessId){
		XcOrders xcOrders = xcOrdersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, bussinessId));
		return xcOrders;
	}

	/**
	 * 根据支付记录号查询支付记录
	 * @param payNo
	 * @return
	 */
	@Override
	public XcPayRecord getPayRecordByPayno(String payNo){
		XcPayRecord xcPayRecord = xcPayRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
		return xcPayRecord;
	}


	/**
	 * 请求支付宝查询支付结果并更新支付状态
	 * @param payNo
	 * @return
	 */
	@Override
	public PayRecordDto queryPayResult(String payNo){
		//查询支付结果
		PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
		//更新支付记录表和订单表支付状态
		currentProxy.saveAlipayStatus(payStatusDto);
		//返回最新的支付记录信息
		XcPayRecord payRecordByPayno = getPayRecordByPayno(payNo);
		PayRecordDto payRecordDto = new PayRecordDto();
		BeanUtils.copyProperties(payRecordByPayno, payRecordDto);
		return payRecordDto;
	}

	/**
	 * 调用支付宝查询支付结果
	 * @param payNo
	 * @return
	 */
	public PayStatusDto queryPayResultFromAlipay(String payNo){
		AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
		// 构造请求参数以调用接口
		AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
		JSONObject bizContent = new JSONObject();
		bizContent.put("out_trade_no", payNo);
		//bizContent.put("trade_no", "2014112611001004680073956707");
		request.setBizContent(bizContent.toString());
		String resultJson = null;
		try {
			AlipayTradeQueryResponse response = alipayClient.execute(request);
			if(!response.isSuccess()){
				XueChengPlusException.cast("未支付");
			}
			resultJson = response.getBody();
		} catch (AlipayApiException e) {
			XueChengPlusException.cast("请求支付宝查询支付结果异常");
		}

		Map resultMap = JSON.parseObject(resultJson, Map.class);
		Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
		//支付结果
		String trade_status = (String) alipay_trade_query_response.get("trade_status");

		PayStatusDto payStatusDto = new PayStatusDto();
		payStatusDto.setOut_trade_no(payNo);
		payStatusDto.setTrade_no((String) alipay_trade_query_response.get("trade_no"));//支付宝交易号
		payStatusDto.setTrade_status(trade_status);
		payStatusDto.setApp_id(APP_ID);
		payStatusDto.setTotal_amount((String) alipay_trade_query_response.get("total_amount"));

		return payStatusDto;
	}

	/**
	 * 保存支付宝支付记录
	 * @param payStatusDto
	 */
	@Transactional
	@Override
	public void saveAlipayStatus(PayStatusDto payStatusDto) {
		//支付记录号
		String payNo = payStatusDto.getOut_trade_no();
		XcPayRecord payRecordByPayno = getPayRecordByPayno(payNo);
		if(payRecordByPayno == null){
			XueChengPlusException.cast("支付记录不存在");
		}
		//相关联的订单号
		Long orderId = payRecordByPayno.getOrderId();
		XcOrders xcOrders = xcOrdersMapper.selectById(orderId);
		if(xcOrders == null){
			XueChengPlusException.cast("找不到相关订单");
		}

		String statusDb = payRecordByPayno.getStatus();
		if("601002".equals(statusDb)){
			//已经成功
			return;
		}

		String tradeStatus = payStatusDto.getTrade_status();
		//如果支付成功
		if(tradeStatus.equals("TRADE_SUCCESS")){
			//更新支付记录表状态为支付成功
			payRecordByPayno.setStatus("601002"); //交易成功
			payRecordByPayno.setOutPayNo(payStatusDto.getTrade_no()); //支付宝订单号
			payRecordByPayno.setOutPayChannel("Alipay");
			payRecordByPayno.setPaySuccessTime(LocalDateTime.now());
			xcPayRecordMapper.updateById(payRecordByPayno);
			//更新订单表状态为支付成功
			xcOrders.setStatus("601002");
			xcOrdersMapper.updateById(xcOrders);

			//将消息写到数据库
			MqMessage mqMessage = mqMessageService.addMessage("payresult_notify", xcOrders.getOutBusinessId(), xcOrders.getOrderType(), null);
			//发送消息
			notifyPayResquest(mqMessage);
		}
	}


	/**
	 * 发送通知结果
	 * @param message
	 */
	@Override
	public void notifyPayResquest(MqMessage message){
		//1、消息体，转json
		String msg = JSON.toJSONString(message);
		//设置消息持久化
		Message msgObj = MessageBuilder.withBody(msg.getBytes(StandardCharsets.UTF_8))
				.setDeliveryMode(MessageDeliveryMode.PERSISTENT)
				.build();
		// 2.全局唯一的消息ID，需要封装到CorrelationData中
		Long id = message.getId();
		CorrelationData correlationData = new CorrelationData(id.toString());
		//优先使用correlationData指定回调方法
		// 3.添加callback
		correlationData.getFuture().addCallback(
				result -> {
					if(result.isAck()){
						// 3.1.ack，消息成功
						log.debug("通知支付结果消息发送成功, ID:{}", correlationData.getId());
						//删除消息表中的记录
						mqMessageService.completed(message.getId());
					}else{
						// 3.2.nack，消息失败
						log.error("通知支付结果消息发送失败, ID:{}, 原因{}",correlationData.getId(), result.getReason());
					}
				},
				ex -> log.error("消息发送异常, ID:{}, 原因{}",correlationData.getId(),ex.getMessage())
		);
		// 4.发送消息
		rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", msgObj,correlationData);
	}

}
