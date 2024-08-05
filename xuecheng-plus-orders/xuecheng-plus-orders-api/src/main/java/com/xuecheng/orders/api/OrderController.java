package com.xuecheng.orders.api;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@RestController
public class OrderController {
	@Value("${pay.alipay.APP_ID}")
	String APP_ID;
	@Value("${pay.alipay.APP_PRIVATE_KEY}")
	String APP_PRIVATE_KEY;

	@Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
	String ALIPAY_PUBLIC_KEY;

	@Autowired
	private OrderService orderService;

	//生成订单支付二维码
	@PostMapping("/generatepaycode")
	public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {

		SecurityUtil.XcUser user = SecurityUtil.getUser();
		String userId = user.getId();

		//调用service，完成插入订单信息，插入支付记录，生成支付二维码
		PayRecordDto payRecordDto = orderService.createOrder(userId, addOrderDto);

		return payRecordDto;
	}

	@GetMapping("/requestpay")
	public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException, AlipayApiException {
		XcPayRecord xcPayRecord = orderService.getPayRecordByPayno(payNo);
		if(xcPayRecord == null) {
			XueChengPlusException.cast("支付记录不存在");
		}
		if(xcPayRecord.getStatus().equals("601002")){
			XueChengPlusException.cast("无需重复支付");
		}

		//请求支付宝下单
		AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
//		alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
		alipayRequest.setNotifyUrl("http://56cfb33a.r21.cpolar.top/api/orders/paynotify");//在公共参数中设置回跳和通知地址
		alipayRequest.setBizContent("{" +
				"    \"out_trade_no\":\""+ payNo +"\"," +
				"    \"total_amount\":"+ xcPayRecord.getTotalPrice() +"," +
				"    \"subject\":\""+ xcPayRecord.getOrderName() +"\"," +
				"    \"product_code\":\"QUICK_WAP_WAY\"" +
				"  }");//填充业务参数
		AlipayTradeWapPayResponse response = alipayClient.pageExecute(alipayRequest);
		String form = response.getBody(); //调用SDK生成表单
		httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
		httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
		httpResponse.getWriter().flush();
		if (response.isSuccess()) {
			System.out.println("调用成功");
		} else {
			System.out.println("调用失败");
		}
	}

	/**
	 * 查询支付结果
	 * @param payNo
	 * @return
	 */
	@GetMapping("/payresult")
	public PayRecordDto payresult(String payNo){
		log.info("查询支付状态：payNo={}", payNo);
		PayRecordDto payRecordDto = orderService.queryPayResult(payNo);
		return payRecordDto;
	}

	/**
	 * 接收支付宝支付成功消息
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws AlipayApiException
	 */
	@PostMapping("/paynotify")
	public void paynotify(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, AlipayApiException {
		log.info("收到支付宝支付消息");
		//获取支付宝POST过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
			params.put(name, valueStr);
		}
		boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");

		if (verify_result) {//验证成功
			//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
			//商户订单号
			String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
			//支付宝交易号
			String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
			//交易状态
			String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
			//交易金额
			String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");
			if (trade_status.equals("TRADE_SUCCESS")) {
				//更新支付记录表状态
				PayStatusDto payStatusDto = new PayStatusDto();
				payStatusDto.setTrade_status(trade_status);
				payStatusDto.setOut_trade_no(out_trade_no);
				payStatusDto.setTrade_no(trade_no);
				payStatusDto.setTotal_amount(total_amount);
				payStatusDto.setApp_id(APP_ID);
				orderService.saveAlipayStatus(payStatusDto);
			}
			response.getWriter().write("success");
		} else {//验证失败
			response.getWriter().write("fail");

		}
	}


}
