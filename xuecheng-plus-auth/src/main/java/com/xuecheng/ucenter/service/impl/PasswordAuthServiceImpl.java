package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * 账号密码登录实现
 */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {
	@Autowired
	private XcUserMapper xcUserMapper;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private CheckCodeClient checkCodeClient;

	@Override
	public XcUserExt execute(AuthParamsDto authParamsDto) {
		//调用验证码服务接口去校验验证码
		//输入的验证码
		String checkcode = authParamsDto.getCheckcode();
		//验证码对应的key
		String checkcodekey = authParamsDto.getCheckcodekey();
		Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
		if(!verify){
			throw new RuntimeException("验证码输入错误");
		}

		//账号
		String username = authParamsDto.getUsername();

		//根据username查询数据库，判断账号是否存在
		XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
		//查询到用户不存在，返回NUll
		if (xcUser == null) {
			throw new RuntimeException("账号不存在");
		}

		//验证密码是否正确
		//用户正确的密码
		String passwordDb = xcUser.getPassword();
		//拿到用户输入的密码
		String passwordForm = authParamsDto.getPassword();
		//校验密码
		boolean matches = passwordEncoder.matches(passwordForm, passwordDb);
		if(!matches){
			throw new RuntimeException("账号或密码错误");
		}

		XcUserExt xcUserExt = new XcUserExt();
		BeanUtils.copyProperties(xcUser, xcUserExt);
		return xcUserExt;
	}
}
