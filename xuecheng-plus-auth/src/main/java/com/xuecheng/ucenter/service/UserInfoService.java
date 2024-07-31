package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.FindpasswordParamsDto;

public interface UserInfoService {

	/**
	 * 找回密码操作
	 * @param findpasswordParamsDto
	 */
	public void findPassword(FindpasswordParamsDto findpasswordParamsDto);
}
