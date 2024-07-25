package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VideoTask {

	@Autowired
	private MediaFileProcessService mediaFileProcessService;
	@Autowired
	private MediaFileService mediaFileService;

	//FFmpeg路径
	@Value("${videoprocess.ffmpegpath}")
	private String ffmpegPath;

	@XxlJob("videoJobHandler")
	public void videoJobHandler() throws Exception {
		// 分片参数
		int shardIndex = XxlJobHelper.getShardIndex();  //执行器的序号，0开始
		int shardTotal = XxlJobHelper.getShardTotal();  //执行器的总数
		log.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);

		//确定cpu核心数
		int processors = Runtime.getRuntime().availableProcessors();

		//查询待处理任务
		List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
		int size = mediaProcessList.size();
		log.debug("取到的视频处理任务数：{}", size);
		if (size <= 0) {
			return;
		}

		//创建一个线程池
		ExecutorService executorService = Executors.newFixedThreadPool(size);

		//使用计时器
		CountDownLatch countDownLatch = new CountDownLatch(size);
		mediaProcessList.forEach(mediaProcess -> {
			executorService.execute(() -> {
				try {
					//任务id
					Long taskId = mediaProcess.getId();
					//开启任务,争抢任务，数据库乐观锁
					boolean b = mediaFileProcessService.startTask(taskId);
					if (!b) {
						log.debug("抢占视频处理任务失败，任务id:{}", taskId);
						return;
					}

					//执行视频转码
					String fileId = mediaProcess.getFileId();

					//下载minio视频
					String bucket = mediaProcess.getBucket();
					String objectName = mediaProcess.getFilePath();
					File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
					if (file == null) {
						log.error("下载视频出错，任务id:{}", taskId);
						mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频出错");
						return;
					}
					//源avi视频的路径
					String videoPath = file.getAbsolutePath();
					//转换后mp4文件的名称
					String mp4Name = fileId.concat(".mp4");

					//转换后mp4文件的路径
					File mp4File = null;
					try {
						mp4File = File.createTempFile("minio", ".mp4");
					} catch (IOException e) {
						log.error("创建临时文件异常,{}", e.getMessage());
						//保存任务失败
						mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
						return;
					}
					String mp4Path = mp4File.getAbsolutePath();
					//创建工具类对象
					Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, videoPath, mp4Name, mp4Path);
					//开始视频转换，成功将返回success
					String result = videoUtil.generateMp4();
					if (!result.equals("success")) {
						//保存任务状态为失败
						log.error("视频转码失败，bucket:{},objectName:{},原因：{}", bucket, objectName, result);
						//保存任务失败
						mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频转码失败");
					}

					//上传到minio
					String newFilePath = getFilePathByMd5(fileId, ".mp4");
					boolean b1 = mediaFileService.addMediaFilesToMinio(mp4Path, "video/mp4", bucket, newFilePath);
					if (!b1) {
						log.error("上传mp4到minio失败,taskId:{}", taskId);
						//保存任务失败
						mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传mp4到minio失败");
					}
					//url
					String newUrl = "/" + bucket + "/" + newFilePath;
					//保存任务处理结果
					mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, newUrl, null);
				} finally {
					countDownLatch.countDown();
					log.debug("处理完成,md5:{}",mediaProcess.getFileId());
				}

			});
		});
		//阻塞,指定最大限度等待时间
		countDownLatch.await(10, TimeUnit.MINUTES);

	}


	private String getFilePathByMd5(String fileMd5, String fileExt) {
		return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
	}

}
