package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;

/**
 * 课程基本信息管理业务接口
 */
public interface CourseBaseInfoService  {

    /*
     * @description 课程查询接口
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 条件条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDTO queryCourseParamsDto);

    /**
     * 新增课程
     * @param companyId 机构id
     * @param addCourseDto 课程信息
     * @return
     */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * 根据id查询课程详细信息
     * @param id
     * @return
     */
    public CourseBaseInfoDto getCourseBaseInfoById(Long id);

    /**
     * 修改课程信息
     * @param companyId
     * @param editCourseDto
     * @return
     */
    public CourseBaseInfoDto updateCourseBaseInfo(Long companyId, EditCourseDto editCourseDto);

 }