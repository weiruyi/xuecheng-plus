package com.xuecheng.content.feignClient;

import com.xuecheng.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;


@FeignClient(value = "media-api", configuration = MultipartSupportConfig.class, fallbackFactory = MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient {

	@PostMapping(value = "/media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	String uploadFile(@RequestPart("filedata") MultipartFile upload, @RequestParam(value = "objectName",required=false) String objectName);

}
