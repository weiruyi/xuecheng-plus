package com.xuecheng.ucenter.feignclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CheckCodeClientFallbackFactory implements FallbackFactory<CheckCodeClient> {
	@Override
	public CheckCodeClient create(Throwable cause) {
		return new CheckCodeClient() {

			@Override
			public Boolean verify(String key, String code) {
				log.error("验证码服务远程调用熔断处理");
				return false;
			}
		};
	}
}
