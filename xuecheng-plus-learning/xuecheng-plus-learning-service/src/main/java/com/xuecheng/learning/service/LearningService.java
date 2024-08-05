package com.xuecheng.learning.service;

import com.xuecheng.base.model.RestResponse;

public interface LearningService {

	/**
	 * 获取学习视频
	 * @param userId
	 * @param courseId
	 * @param teachplanId
	 * @param mediaId
	 * @return
	 */
	public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}
