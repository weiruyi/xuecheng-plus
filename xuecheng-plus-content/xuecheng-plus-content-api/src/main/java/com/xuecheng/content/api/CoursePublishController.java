package com.xuecheng.content.api;

import com.alibaba.fastjson.JSON;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;


@Slf4j
@RestController
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

	@PostMapping("/courseaudit/commit/{courseId}")
	public void commitAudit(@PathVariable("courseId") Long courseId){
		log.info("提交审核，courseId={}", courseId);
		// TODO: companyId
		Long companyId = 1232141425L;
		coursePublishService.commitAudit(companyId, courseId);
	}


	@PostMapping("/coursepublish/{courseId}")
	public void coursepublish(@PathVariable("courseId") Long courseId){
		log.info("课程发布，courseId={}", courseId);
		// TODO: companyId
		Long companyId = 1232141425L;

		coursePublishService.publish(companyId, courseId);
	}


	@ApiOperation("查询课程发布信息")
	@GetMapping("/r/coursepublish/{courseId}")
	public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId) {
		CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
		return coursePublish;
	}


	//获取课程发布信息
	@GetMapping("/course/whole/{courseId}")
	public CoursePreviewDto getCoursePublish(@PathVariable("courseId") Long courseId){
		CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
		//查询课程发布表
		CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
		if(coursePublish == null){
			return  coursePreviewDto;
		}
		//封装CourseBaseInfoDto
		CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
		BeanUtils.copyProperties(coursePublish,courseBaseInfoDto);
		//封装课程计划信息
		//获取课程信息json
		String teachplanJson = coursePublish.getTeachplan();
		//转成List<TeachplanDto>
		List<TeachplanDto> teachplanDtos = JSON.parseArray(teachplanJson, TeachplanDto.class);

		coursePreviewDto.setCourseBase(courseBaseInfoDto);
		coursePreviewDto.setTeachplans(teachplanDtos);
		return coursePreviewDto;
	}

}
