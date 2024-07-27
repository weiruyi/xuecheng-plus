package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {
	@Autowired
	private CourseBaseInfoService courseBaseInfoService;
	@Autowired
	private TeachplanService teachplanService;
	@Autowired
	private CourseMarketMapper courseMarketMapper;
	@Autowired
	private CourseTeacherService courseTeacherService;
	@Autowired
	private CoursePublishPreMapper coursePublishPreMapper;
	@Autowired
	private CourseBaseMapper courseBaseMapper;
	@Autowired
	private CoursePublishMapper coursePublishMapper;
	@Autowired
	private MqMessageService mqMessageService;

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


	/**
	 * 课程提交审核
	 * @param companyId
	 * @param courseId
	 */
	@Override
	@Transactional
	public void commitAudit(Long companyId, Long courseId){
		//课程基本信息
		CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBaseInfoById(courseId);
		if(courseBaseInfoDto == null){
			XueChengPlusException.cast("课程找不到");
		}

		//机构
		if(!courseBaseInfoDto.getCompanyId().equals(companyId)){
			XueChengPlusException.cast("只能提交本机构的课程");
		}

		//审核状态
		String auditStatus = courseBaseInfoDto.getAuditStatus();
		//如果课程审核状态为已提交，则不允许提交
		if(auditStatus.equals("202003")){
			XueChengPlusException.cast("课程已提交，请等待审核");
		}

		//课程图片，计划信息没有填写也不允许提交
		String pic = courseBaseInfoDto.getPic();
		if(StringUtils.isEmpty(pic)){
			XueChengPlusException.cast("请上传课程图片");
		}

		//查询课程计划信息
		List<TeachplanDto> teachplanDtoList = teachplanService.getTreeNode(courseId);
		if(teachplanDtoList == null || teachplanDtoList.size() == 0){
			XueChengPlusException.cast("请编写课程计划");
		}

		//查询课程基本信息，营销信息，课程计划，师资信息，插入到预发布表
		CoursePublishPre coursePublishPre = new CoursePublishPre();
		BeanUtils.copyProperties(courseBaseInfoDto, coursePublishPre);
		//营销信息
		CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
		//转json
		String courseMarketJson = JSON.toJSONString(courseMarket);
		coursePublishPre.setMarket(courseMarketJson);
		//课程计划
		String teachplanJson = JSON.toJSONString(teachplanDtoList);
		coursePublishPre.setTeachplan(teachplanJson);
		//教师信息
		List<CourseTeacher> courseTeacherList = courseTeacherService.getCourseTeacherListByCourseId(courseId);
		String teachersJson = JSON.toJSONString(courseTeacherList);
		coursePublishPre.setTeachers(teachersJson);
		//状态为已提交
		coursePublishPre.setStatus("202003");
		//提交时间
		coursePublishPre.setCreateDate(LocalDateTime.now());

		//插入。如果已经有先删除
		CoursePublishPre coursePublishPreObj = coursePublishPreMapper.selectById(courseId);
		if(coursePublishPreObj == null){
			//插入
			coursePublishPreMapper.insert(coursePublishPre);
		}else {
			//更新
			coursePublishPreMapper.updateById(coursePublishPre);
		}

		//修改课程审核状态为已提交
		CourseBase courseBase = new CourseBase();
		courseBase.setAuditStatus("202003");
		courseBase.setChangeDate(LocalDateTime.now());
		LambdaQueryWrapper<CourseBase> courseBaseWrapper = new LambdaQueryWrapper<>();
		courseBaseWrapper.eq(CourseBase::getId, courseId);
		courseBaseMapper.update(courseBase, courseBaseWrapper);

	}


	/**
	 * @description 课程发布接口
	 * @param companyId 机构id
	 * @param courseId 课程id
	 * @return void
	 */
	@Override
	@Transactional
	public void publish(Long companyId,Long courseId){
		//查询课程预发布信息
		CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
		if(coursePublishPre == null){
			XueChengPlusException.cast("请先提交课程审核，审核通过才能发布");
		}
		if(!coursePublishPre.getCompanyId().equals(companyId)){
			XueChengPlusException.cast("不允许提交其他机构的课程");
		}
		String status = coursePublishPre.getStatus();
		if(!status.equals("202004")){
			XueChengPlusException.cast("审核未通过，不允许发布");
		}

		//向课程发布表写入数据
		CoursePublish coursePublish = new CoursePublish();
		BeanUtils.copyProperties(coursePublishPre, coursePublish);
		//先查询课程发布
		CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
		if(coursePublishObj == null){
			coursePublishMapper.insert(coursePublish);
		}else {
			coursePublishMapper.updateById(coursePublish);
		}

		//向消息表写入数据
		saveCoursePublishMessage(courseId);

		//将预发布表删除
		coursePublishPreMapper.deleteById(courseId);

	}

	/**
	 * @description 保存消息表记录
	 * @param courseId  课程id
	 * @return void
	 */
	private void saveCoursePublishMessage(Long courseId){
		MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
		if(mqMessage==null){
			XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
		}
	}
}
