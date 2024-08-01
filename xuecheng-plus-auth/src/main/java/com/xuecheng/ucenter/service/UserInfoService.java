package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.FindpasswordParamsDto;
import com.xuecheng.ucenter.model.dto.RegisterparamsDto;

public interface UserInfoService {

	/**
	 * 找回密码操作
	 * @param findpasswordParamsDto
	 */
	public void findPassword(FindpasswordParamsDto findpasswordParamsDto);

	/**
	 * 注册
	 * @param registerparamsDto
	 */
	public void register(RegisterparamsDto registerparamsDto);
}
