package com.xuecheng.learning.service;

import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;

public interface MyCourseTableService {

	/**
	 * 添加选课
	 * @param userId
	 * @param courseId
	 * @return
	 */
	public XcChooseCourseDto addChoose(String userId, Long courseId);

	/**
	 * 获取学习资格
	 * @param userId
	 * @param courseId
	 * @return
	 */
	public XcCourseTablesDto getLearningStatus(String userId, Long courseId);
}
