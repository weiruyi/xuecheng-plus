package com.xuecheng.content.service.impl;

import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {
	@Autowired
	private CourseBaseInfoService courseBaseInfoService;

	@Autowired
	private TeachplanService teachplanService;

	/**
	 * 获取课程预览信息
	 * @param courseId
	 * @return
	 */
	@Override
	public CoursePreviewDto getCoursePreviewInfo(Long courseId){
		CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBaseInfoById(courseId);
		List<TeachplanDto> teachplanDtoList = teachplanService.getTreeNode(courseId);

		CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
		coursePreviewDto.setCourseBase(courseBaseInfoDto);
		coursePreviewDto.setTeachplans(teachplanDtoList);

		return coursePreviewDto;
	}
}
