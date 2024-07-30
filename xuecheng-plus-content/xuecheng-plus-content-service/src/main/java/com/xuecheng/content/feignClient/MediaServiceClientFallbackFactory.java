package com.xuecheng.content.feignClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.web.multipart.MultipartFile;


public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {

	private static final Logger log = LoggerFactory.getLogger(MediaServiceClientFallbackFactory.class);

	@Override
	public MediaServiceClient create(Throwable cause) {
		return new MediaServiceClient() {
			@Override
			public String uploadFile(MultipartFile upload, String objectName) {
				log.debug("调用上传文件接口发生熔断，{}",cause.toString());
				return null;
			}
		};
	}
}
