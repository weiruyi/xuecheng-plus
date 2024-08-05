package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class MyCourseTableServiceImpl implements MyCourseTableService {

	private final XcChooseCourseMapper xcChooseCourseMapper;
	private final XcCourseTablesMapper xcCourseTablesMapper;
	private final ContentServiceClient contentServiceClient;

	/**
	 * 添加选课
	 * @param userId
	 * @param courseId
	 * @return
	 */
	@Transactional
	@Override
	public XcChooseCourseDto addChoose(String userId, Long courseId){

		//1、选课调用内容管理查询课程收费规则
		CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
		if(coursepublish == null){
			XueChengPlusException.cast("课程信息不存在");
		}
		//收费规则
		String charge = coursepublish.getCharge();
		XcChooseCourse xcChooseCourse = null;
		if(charge.equals("201000")){
			//2、如果课程免费，会向选课记录表，我的课程表写数据
			//向选课记录表写
			xcChooseCourse = addFreeCourse(userId, coursepublish);
			//向我的课程表写
			XcCourseTables xcCourseTables = addCourseTable(xcChooseCourse);
		}else{
			//3、如果是收费课程，会向选课记录表写入数据
			xcChooseCourse = addChargeCourse(userId, coursepublish);
		}

		//4、查询学生的学习资格
		XcCourseTablesDto learningStatus = getLearningStatus(userId, courseId);
		XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
		BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
		xcChooseCourseDto.setLearnStatus(learningStatus.getLearnStatus());
		return xcChooseCourseDto;
	}


	/**
	 * 获取学习资格
	 * @param userId
	 * @param courseId
	 * @return 学习资格，[{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
	 */
	@Override
	public XcCourseTablesDto getLearningStatus(String userId, Long courseId){
		XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
		//查询我的课程表
		XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
		if(xcCourseTables == null){
			// {"code":"702002","desc":"没有选课或选课后没有支付"}
			xcCourseTablesDto.setLearnStatus("702002");
			return xcCourseTablesDto;
		}
		//判断是否过期
		boolean before = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
		if(before){
			// {"code":"702003","desc":"已过期需要申请续期或重新支付"}
			xcCourseTablesDto.setLearnStatus("702003");
			return xcCourseTablesDto;
		}
		//"code":"702001","desc":"正常学习"
		BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
		xcCourseTablesDto.setLearnStatus("702001");
		return  xcCourseTablesDto;
	}

	//添加免费课程
	public XcChooseCourse addFreeCourse(String userId, CoursePublish coursePublish){
		Long courseId = coursePublish.getId();
		//如果存在选课记录，直接返回
		LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(XcChooseCourse::getCourseId, courseId)
				.eq(XcChooseCourse::getUserId, userId)
				.eq(XcChooseCourse::getOrderType, "700001") //免费课程
				.eq(XcChooseCourse::getStatus, "701001");  //选课成功
		List<XcChooseCourse> xcChooseCourseList = xcChooseCourseMapper.selectList(queryWrapper);
		if(xcChooseCourseList.size()>0){
			return xcChooseCourseList.get(0);
		}
		//向选课记录表写入数据
		XcChooseCourse xcChooseCourse = new XcChooseCourse();
		xcChooseCourse.setCourseId(courseId);
		xcChooseCourse.setCourseName(coursePublish.getName());
		xcChooseCourse.setUserId(userId);
		xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
		xcChooseCourse.setOrderType("700001"); //免费课程
		xcChooseCourse.setCreateDate(LocalDateTime.now());
		xcChooseCourse.setCoursePrice(coursePublish.getPrice());
		xcChooseCourse.setValidDays(365);
		xcChooseCourse.setStatus("701001");//选课成功
		xcChooseCourse.setValidtimeStart(LocalDateTime.now()); //有效期开始时间
		xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365)); //有效期结束时间

		int insert = xcChooseCourseMapper.insert(xcChooseCourse);
		if(insert < 0){
			XueChengPlusException.cast("添加选课表失败");
		}

		return xcChooseCourse;
	}

	//添加收费课程
	public XcChooseCourse addChargeCourse(String userId, CoursePublish coursepublish){
		//如果存在待支付记录直接返回
		LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
				.eq(XcChooseCourse::getCourseId, coursepublish.getId())
				.eq(XcChooseCourse::getOrderType, "700002")//收费订单
				.eq(XcChooseCourse::getStatus, "701002");//待支付
		List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
		if (xcChooseCourses != null && xcChooseCourses.size()>0) {
			return xcChooseCourses.get(0);
		}

		XcChooseCourse xcChooseCourse = new XcChooseCourse();
		xcChooseCourse.setCourseId(coursepublish.getId());
		xcChooseCourse.setCourseName(coursepublish.getName());
		xcChooseCourse.setCoursePrice(coursepublish.getPrice());
		xcChooseCourse.setUserId(userId);
		xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
		xcChooseCourse.setOrderType("700002");//收费课程
		xcChooseCourse.setCreateDate(LocalDateTime.now());
		xcChooseCourse.setStatus("701002");//待支付

		xcChooseCourse.setValidDays(coursepublish.getValidDays());
		xcChooseCourse.setValidtimeStart(LocalDateTime.now());
		xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));
		xcChooseCourseMapper.insert(xcChooseCourse);
		return xcChooseCourse;
	}

	//添加课程到我的课程表
	public XcCourseTables addCourseTable(XcChooseCourse xcChooseCourse){
		//选课记录完成且未过期可以添加课程到课程表
		String status = xcChooseCourse.getStatus();
		if (!"701001".equals(status)){
			XueChengPlusException.cast("选课未成功，无法添加到课程表");
		}
		//查询我的课程表
		XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
		if(xcCourseTables!=null){
			return xcCourseTables;
		}
		XcCourseTables xcCourseTablesNew = new XcCourseTables();
		xcCourseTablesNew.setChooseCourseId(xcChooseCourse.getId());
		xcCourseTablesNew.setUserId(xcChooseCourse.getUserId());
		xcCourseTablesNew.setCourseId(xcChooseCourse.getCourseId());
		xcCourseTablesNew.setCompanyId(xcChooseCourse.getCompanyId());
		xcCourseTablesNew.setCourseName(xcChooseCourse.getCourseName());
		xcCourseTablesNew.setCreateDate(LocalDateTime.now());
		xcCourseTablesNew.setValidtimeStart(xcChooseCourse.getValidtimeStart());
		xcCourseTablesNew.setValidtimeEnd(xcChooseCourse.getValidtimeEnd());
		xcCourseTablesNew.setCourseType(xcChooseCourse.getOrderType());
		xcCourseTablesMapper.insert(xcCourseTablesNew);

		return xcCourseTablesNew;
	}


	/**
	 * @description 根据课程和用户查询我的课程表中某一门课程
	 * @param userId
	 * @param courseId
	 * @return com.xuecheng.learning.model.po.XcCourseTables
	 */
	public XcCourseTables getXcCourseTables(String userId,Long courseId){
		XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));
		return xcCourseTables;

	}


	/**
	 * 保存选课成功
	 * @param chooseCourseId
	 * @return
	 */
	@Override
	public boolean saveChooseCourseSuccess(String chooseCourseId){

		//根据choosecourseId查询选课记录
		XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectById(chooseCourseId);
		if(xcChooseCourse == null){
			log.debug("收到支付结果通知没有查询到关联的选课记录,choosecourseId:{}",chooseCourseId);
			return false;
		}
		String status = xcChooseCourse.getStatus();
		if("701001".equals(status)){
			//添加到课程表
			addCourseTable(xcChooseCourse);
			return true;
		}
		//待支付状态才处理
		if ("701002".equals(status)) {
			//更新为选课成功
			xcChooseCourse.setStatus("701001");
			int update = xcChooseCourseMapper.updateById(xcChooseCourse);
			if(update>0){
				log.debug("收到支付结果通知处理成功,选课记录:{}",xcChooseCourse);
				//添加到课程表
				addCourseTable(xcChooseCourse);
				return true;
			}else{
				log.debug("收到支付结果通知处理失败,选课记录:{}",xcChooseCourse);
				return false;
			}
		}

		return false;
	}

	/**
	 * 课程表查询
	 * @param params
	 * @return
	 */
	public PageResult<XcCourseTables> mycourseTables(MyCourseTableParams params){
		//当前页码
		int pageNo = params.getPage();
		//每页记录数
		int size = params.getSize();
		Page<XcCourseTables> xcCourseTablesPage = new Page<>(pageNo, size);

		LambdaQueryWrapper<XcCourseTables> lambdaQueryWrapper = new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, params.getUserId());

		//查询
		Page<XcCourseTables> result = xcCourseTablesMapper.selectPage(xcCourseTablesPage, lambdaQueryWrapper);
		List<XcCourseTables> records = result.getRecords();
		//记录总数
		long total = result.getTotal();
		PageResult<XcCourseTables> courseTablesResult = new PageResult<>(records, total, pageNo, size);
		return courseTablesResult;
	}

}
