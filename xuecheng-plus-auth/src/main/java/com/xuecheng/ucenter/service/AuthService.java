package com.xuecheng.ucenter.service;


import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * 统一认证接口
 */
public interface AuthService {

	/**
	 * 认证方法
	 * @param authParamsDto
	 * @return
	 */
	XcUserExt execute(AuthParamsDto authParamsDto);
}
