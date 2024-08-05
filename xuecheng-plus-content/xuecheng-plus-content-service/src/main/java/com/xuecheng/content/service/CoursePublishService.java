package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

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

	/**
	 * @description 课程发布接口
	 * @param companyId 机构id
	 * @param courseId 课程id
	 * @return void
	 */
	public void publish(Long companyId,Long courseId);

	/**
	 * 课程静态化
	 * @param courseId
	 * @return
	 */
	public File generateCourseHtml(Long courseId);


	/**
	 * 上传课程静态化页面
	 * @param courseId
	 * @param courseHtml
	 */
	public void uploadCourseHtml(Long courseId, File courseHtml);

	/**
	 * 获取课程发布信息
	 * @param courseId
	 * @return
	 */
	public CoursePublish getCoursePublish(Long courseId);
}
