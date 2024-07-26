package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

public interface CoursePublishService {

	/**
	 * 获取课程预览信息
	 * @param courseId
	 * @return
	 */
	public CoursePreviewDto getCoursePreviewInfo(Long courseId);

	/**
	 * 课程提交审核
	 * @param companyId
	 * @param courseId
	 */
	public void commitAudit(Long companyId, Long courseId);
}
