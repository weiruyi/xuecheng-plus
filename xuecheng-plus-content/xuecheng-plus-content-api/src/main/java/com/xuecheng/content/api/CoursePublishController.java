package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;


@Slf4j
@Controller
public class CoursePublishController {
	@Autowired
	private CoursePublishService coursePublishService;

	@GetMapping("/coursepreview/{courseId}")
	public ModelAndView preview(@PathVariable("courseId") Long courseId){

		ModelAndView modelAndView = new ModelAndView();
		//查询课程信息作为模型数据
		CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
		//指定模型
		modelAndView.addObject("model",coursePreviewInfo);
		//指定模版
		modelAndView.setViewName("course_template");
		return modelAndView;
	}

	@ResponseBody
	@PostMapping("/courseaudit/commit/{courseId}")
	public void commitAudit(@PathVariable("courseId") Long courseId){
		log.info("提交审核，courseId={}", courseId);
		// TODO: companyId
		Long companyId = 1232141425L;
		coursePublishService.commitAudit(companyId, courseId);
	}

	@ResponseBody
	@PostMapping("/coursepublish/{courseId}")
	public void coursepublish(@PathVariable("courseId") Long courseId){
		log.info("课程发布，courseId={}", courseId);
		// TODO: companyId
		Long companyId = 1232141425L;

		coursePublishService.publish(companyId, courseId);
	}


}
