package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
		//获取课程预览信息
		CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
		return coursePreviewInfo;
	}


}
