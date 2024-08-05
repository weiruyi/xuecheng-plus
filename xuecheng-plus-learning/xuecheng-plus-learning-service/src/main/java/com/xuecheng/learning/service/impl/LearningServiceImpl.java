package com.xuecheng.learning.service.impl;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 在线学习相关接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {

	private final MyCourseTableService myCourseTableService;
	private final ContentServiceClient contentServiceClient;
	private final MediaServiceClient mediaServiceClient;

	/**
	 * 获取学习视频
	 * @param userId
	 * @param courseId
	 * @param teachplanId
	 * @param mediaId
	 * @return
	 */
	@Override
	public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId){
		//查询课程信息
		CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
		//判断如果课程信息为null说明课程不存在
		if(coursepublish == null){
			return RestResponse.validfail("课程信息不存在");
		}

		//根据teachplanId查询课程计划信息，如果is_preview的值为1，支持试学
		Teachplan teachPlanByCourseId = contentServiceClient.getTeachPlanByCourseId(courseId, teachplanId);
		if(teachPlanByCourseId!= null && teachPlanByCourseId.getIsPreview().equals("1")){
			RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
			return playUrlByMediaId;
		}

		//用户登录
		if(StringUtils.isNotEmpty(userId)){
			//通过课程表获取学习资格
			XcCourseTablesDto xcCourseTablesDto = myCourseTableService.getLearningStatus(userId, courseId);
			String learnStatus = xcCourseTablesDto.getLearnStatus();  //正常学习：702001
			if("702002".equals(learnStatus)){
				return RestResponse.validfail("无法学习，因为没有选课或者没有支付");
			}else if("702003".equals(learnStatus)){
				return RestResponse.validfail("已过期需要申请续期或者重新支付");
			}else {
				//返回视频的播放地址
				// 远程调用媒资服务获取视频地址
				RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
				return playUrlByMediaId;
			}
		}
		//如果用户没有登录
		//取出收费规则
		String charge = coursepublish.getCharge();
		if(charge.equals("201000")){
			//有资格学习
			// 远程调用媒资服务获取视频地址
			RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
			return playUrlByMediaId;
		}

		return RestResponse.validfail("没有权限学习");
	}

}
