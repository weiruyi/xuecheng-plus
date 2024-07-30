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

//		String username = authParamsDto.getUsername();
//		//根据username查询数据库
//		XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
//		//查询到用户不存在，返回NUll
//		if (xcUser == null) {
//			return null;
//		}
//
//		//查询到了用户，拿到正确的密码，最终封装成UserDetails对象给spring security框架返回，由框架进行密码解析
//		String password = xcUser.getPassword();
//		//权限
//		String[] authorities = {"test"};
//		//将用户信息转json
//		xcUser.setPassword(null);
//		String userJson = JSON.toJSONString(xcUser);
//		UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities).build();
//		return userDetails;
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
