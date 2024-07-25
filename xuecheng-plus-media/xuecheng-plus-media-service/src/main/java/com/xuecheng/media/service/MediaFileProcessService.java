package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

public interface MediaFileProcessService {

	public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

	/**
	 *  开启一个任务
	 * @param id 任务id
	 * @return true开启任务成功，false开启任务失败
	 */
	public boolean startTask(Long id);

	/**
	 * 保存任务结果
	 * @param taskId
	 * @param status
	 * @param fileId
	 * @param url
	 * @param errorMsg
	 */
	public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);
}
