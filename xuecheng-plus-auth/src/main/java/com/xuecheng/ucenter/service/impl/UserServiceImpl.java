package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.po.XcUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Component
public class UserServiceImpl implements UserDetailsService {
	@Autowired
	private XcUserMapper xcUserMapper;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//根据username查询数据库
		XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
		//查询到用户不存在，返回NUll
		if (xcUser == null) {
			return null;
		}

		//查询到了用户，拿到正确的密码，最终封装成UserDetails对象给spring security框架返回，由框架进行密码解析
		String password = xcUser.getPassword();
		//权限
		String[] authorities = {"test"};
		//将用户信息转json
		xcUser.setPassword(null);
		String userJson = JSON.toJSONString(xcUser);
		UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities).build();
		return userDetails;
	}
}
