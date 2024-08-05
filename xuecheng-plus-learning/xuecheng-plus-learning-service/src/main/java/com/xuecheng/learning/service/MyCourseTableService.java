package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;

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

	/**
	 * 保存选课成功
	 * @param chooseCourseId
	 * @return
	 */
	public boolean saveChooseCourseSuccess(String chooseCourseId);

	/**
	 * 课程表查询
	 * @param myCourseTableParams
	 * @return
	 */
	public PageResult<XcCourseTables> mycourseTables(MyCourseTableParams myCourseTableParams);
}
