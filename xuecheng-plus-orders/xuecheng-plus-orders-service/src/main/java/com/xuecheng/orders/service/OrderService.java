package com.xuecheng.orders.service;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;

public interface OrderService {

	/**
	 * 创建商品订单
	 * @param userId
	 * @param addOrderDto
	 * @return
	 */
	public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

	/**
	 * 根据支付记录号查询支付记录
	 * @param payNo
	 * @return
	 */
	public XcPayRecord getPayRecordByPayno(String payNo);

	/**
	 * 请求支付宝查询支付结果
	 * @param payNo
	 * @return
	 */
	public PayRecordDto queryPayResult(String payNo);


	/**
	 * 保存支付宝支付记录
	 * @param payStatusDto
	 */
	public void saveAlipayStatus(PayStatusDto payStatusDto);

	/**
	 * 发送通知结果
	 * @param message
	 */
	public void notifyPayResquest(MqMessage message);
}
