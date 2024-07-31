package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.FindpasswordParamsDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.UserInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserInfoServiceImpl implements UserInfoService {

	@Autowired
	private CheckCodeClient checkCodeClient;
	@Autowired
	private XcUserMapper xcUserMapper;
	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * 找回密码操作
	 * @param findpasswordParamsDto
	 */
	public void findPassword(FindpasswordParamsDto findpasswordParamsDto){
		//1、校验验证码是否正确
		//获取用户输入的验证码以及key
		String checkcodeKey = findpasswordParamsDto.getCheckcodekey();
		String checkcode = findpasswordParamsDto.getCheckcode();
		Boolean verify = checkCodeClient.verify(checkcodeKey, checkcode);
		if(!verify){
			XueChengPlusException.cast("验证码错误");
		}

		//2、判断两次密码是否一致
		String confirmpwd = findpasswordParamsDto.getConfirmpwd();
		String password = findpasswordParamsDto.getPassword();
		if(!confirmpwd.equals(password)){
			XueChengPlusException.cast("两次密码不一致");
		}

		//3、根据手机号或者邮箱号查找用户
		String cellphone = findpasswordParamsDto.getCellphone();
		String email = findpasswordParamsDto.getEmail();
		LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();
		if(!StringUtils.isBlank(cellphone)){
			queryWrapper.eq(XcUser::getCellphone, cellphone);
		}else {
			queryWrapper.eq(XcUser::getEmail, email);
		}
		XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
		if(xcUser == null){
			XueChengPlusException.cast("用户不存在");
		}

		//4、如果找到用户更新为新密码
		String encode = passwordEncoder.encode(password);
		xcUser.setPassword(encode);
		xcUserMapper.updateById(xcUser);
	}
}
