package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignClient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


@SpringBootTest
public class FeignUploadTest {
	@Autowired
	MediaServiceClient mediaServiceClient;

	@Test
	public void test() {
		//将file类型转成multipartFile
		File file = new File("D:\\hnu\\html\\1.html");
//		File file = new File("/Users/lance/Downloads/1.html");
		MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);

		mediaServiceClient.uploadFile(multipartFile, "course/13.html");
	}
}
