package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Component
public class UserServiceImpl implements UserDetailsService {
	@Autowired
	private XcUserMapper xcUserMapper;
//	@Autowired
//	private AuthService aUthService;
	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
		//将传入的json转成AuthParamsDto对象
		AuthParamsDto authParamsDto = null;
		try {
			authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
		} catch (Exception e) {
			throw new RuntimeException("请求认证参数不符合要求");
		}
		//认证类型
		String authType = authParamsDto.getAuthType();
		String beanName = authType + "_authservice";
		AuthService authService =  applicationContext.getBean(beanName,AuthService.class);
		//调用
		XcUserExt user = authService.execute(authParamsDto);
		return getUserPrincipal(user);

	}

	/**
	 * @description 查询用户信息
	 * @param user  用户id，主键
	 * @return com.xuecheng.ucenter.model.po.XcUser 用户信息
	 */
	public UserDetails getUserPrincipal(XcUserExt user){
		//用户权限,如果不加报Cannot pass a null GrantedAuthority collection
		String[] authorities = {"p1"};
		String password = user.getPassword();
		//为了安全在令牌中不放密码
		user.setPassword(null);
		//将user对象转json
		String userString = JSON.toJSONString(user);
		//创建UserDetails对象
		UserDetails userDetails = User.withUsername(userString).password(password ).authorities(authorities).build();
		return userDetails;
	}
}
