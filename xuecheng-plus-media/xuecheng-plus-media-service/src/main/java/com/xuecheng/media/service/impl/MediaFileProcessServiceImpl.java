package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

	@Autowired
	private MediaProcessMapper mediaProcessMapper;
	@Autowired
	private MediaFilesMapper mediaFilesMapper;
	@Autowired
	MediaProcessHistoryMapper mediaProcessHistoryMapper;

	@Override
	public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {

		List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
		return mediaProcesses;
	}

	@Override
	public boolean startTask(Long id) {
		int result = mediaProcessMapper.startTask(id);
		return result <= 0 ? false : true;
	}

	@Override
	@Transactional
	public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
		//查出任务，如果不存在则直接返回
		MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
		if(mediaProcess == null) {
			return;
		}

		//处理失败，更新任务处理结果
		if(status.equals("3")){
			//更新mediaProcess表状态
//			mediaProcess.setStatus(status);
//			mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
//			mediaProcess.setErrormsg(errorMsg);
//			mediaProcessMapper.updateById(mediaProcess);

			//更高效的更新方式
			LambdaQueryWrapper<MediaProcess> wrapper = new LambdaQueryWrapper<>();
			wrapper.eq(MediaProcess::getId, taskId);
			MediaProcess mediaProcessU = new MediaProcess();
			mediaProcessU.setStatus(status);
			mediaProcessU.setErrormsg(errorMsg);
			mediaProcessU.setFailCount(mediaProcess.getFailCount() + 1);
			mediaProcessMapper.update(mediaProcessU, wrapper);

			return;
		}

		//任务处理成功
		MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
		//更新mediaFile表url
		mediaFiles.setUrl(url);
		mediaFilesMapper.updateById(mediaFiles);

		//更新mediaProcess表状态
		mediaProcess.setStatus(status);
		mediaProcess.setFinishDate(LocalDateTime.now());
		mediaProcess.setUrl(url);
		mediaProcessMapper.updateById(mediaProcess);

		//插入mediaPrccessHistory
		MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
		BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
		mediaProcessHistoryMapper.insert(mediaProcessHistory);

		//从mediaProcess表删除当前任务
		mediaProcessMapper.deleteById(taskId);

	}
}
