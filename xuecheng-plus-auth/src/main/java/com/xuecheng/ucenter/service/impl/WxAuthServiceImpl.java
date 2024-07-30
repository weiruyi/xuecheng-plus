package com.xuecheng.ucenter.service.impl;


import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.stereotype.Component;

/**
 * 微信扫码登录实现方式
 */
@Component("wx_authservice")
public class WxAuthServiceImpl implements AuthService {
	@Override
	public XcUserExt execute(AuthParamsDto authParamsDto) {
		return null;
	}
}
