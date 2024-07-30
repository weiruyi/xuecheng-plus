package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@SpringBootTest
@Slf4j
public class FreemarkerTest {
	@Autowired
	private CoursePublishService coursePublishService;

	@Test
	public void testGenerateHtmlTemplate() throws IOException, TemplateException {
		Configuration configuration = new Configuration(Configuration.getVersion());

		//拿到calsspath
//		String classpath = coursePublishService.getClass().getResource("/").getPath();
		String classpath = "D:\\hnu\\java_project\\xuecheng-plus\\xuecheng-plus-content\\xuecheng-plus-content-service\\src\\test\\resources";
		//指定模版目录
		configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
		//指定编码
		configuration.setDefaultEncoding("UTF-8");
		//得到模版
		Template template = configuration.getTemplate("course_template.ftl");
		//准备数据
		CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(1L);
		HashMap<String, Object> map =  new HashMap<>();
		map.put("model", coursePreviewInfo);

		String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

		//输入流
		InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
		//输出文件
		FileOutputStream fileOutputStream = new FileOutputStream(new File("D:\\hnu\\html\\1.html"));
		IOUtils.copy(inputStream, fileOutputStream);
	}
}
