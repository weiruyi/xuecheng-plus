package com.xuecheng.ucenter.service.impl;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.stereotype.Service;


/**
 * 账号密码登录实现
 */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {
	@Override
	public XcUserExt execute(AuthParamsDto authParamsDto) {
		return null;
	}
}
