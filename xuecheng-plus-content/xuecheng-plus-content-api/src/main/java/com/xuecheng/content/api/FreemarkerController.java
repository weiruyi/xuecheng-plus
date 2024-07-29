package com.xuecheng.content.api;


import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignClient.MediaServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;

@Controller
public class FreemarkerController {

	@Autowired
	MediaServiceClient mediaServiceClient;

	@GetMapping("/testfreemarker")
	public ModelAndView test(){
		ModelAndView modelAndView = new ModelAndView();
		//设置模型数据
		modelAndView.addObject("name","小明");
		//设置模板名称
		modelAndView.setViewName("test");

		//将file类型转成multipartFile
		File file = new File("D:\\hnu\\html\\1.html");
//		File file = new File("/Users/lance/Downloads/1.html");
		MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);

		mediaServiceClient.uploadFile(multipartFile, "course/11.html");

		return modelAndView;
	}
}
