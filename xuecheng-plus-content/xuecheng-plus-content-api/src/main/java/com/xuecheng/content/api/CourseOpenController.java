package com.xuecheng.content.api;

import com.alibaba.fastjson.JSON;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Api(value = "课程公开查询接口")
@Slf4j
@RestController
@RequestMapping("/open")
@RequiredArgsConstructor
public class CourseOpenController {
	private final CourseBaseInfoService courseBaseInfoService;
	private final CoursePublishService coursePublishService;

	@GetMapping("/course/whole/{courseId}")
	public CoursePreviewDto getPreviewInfo(@PathVariable("courseId") Long courseId) {
//		//获取课程预览信息
//		CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
//		return coursePreviewInfo;
		//查询课程发布信息
		CoursePublish coursePublish = coursePublishService.getCoursePublishCache(courseId);
//        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
		if(coursePublish==null){
			return new CoursePreviewDto();
		}

		//课程基本信息
		CourseBaseInfoDto courseBase = new CourseBaseInfoDto();
		BeanUtils.copyProperties(coursePublish, courseBase);
		//课程计划
		List<TeachplanDto> teachplans = JSON.parseArray(coursePublish.getTeachplan(), TeachplanDto.class);
		CoursePreviewDto coursePreviewInfo = new CoursePreviewDto();
		coursePreviewInfo.setCourseBase(courseBase);
		coursePreviewInfo.setTeachplans(teachplans);
		return coursePreviewInfo;
	}


}
