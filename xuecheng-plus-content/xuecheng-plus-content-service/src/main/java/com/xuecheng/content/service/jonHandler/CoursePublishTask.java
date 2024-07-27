package com.xuecheng.content.service.jonHandler;


import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

	/**
	 * 任务调度入口
	 * @throws Exception
	 */
	@XxlJob("CoursePublishHandler")
	public void coursePublishHandler()  throws Exception{
		//分片参数
		int shardIndex = XxlJobHelper.getShardIndex();
		int shardTotal = XxlJobHelper.getShardTotal();
		//调用抽象类执行任务
		process(shardIndex,shardTotal,"course_publish", 30, 60);
	}


	@Override
	public boolean execute(MqMessage mqMessage) {
		//从mqMessage拿到课程id
		Long courseId = Long.parseLong(mqMessage.getBusinessKey1());

		//课程静态化上传到minio
		generateCourseHtml(mqMessage, courseId);

		//向es写数据
		saveCourseIndex(mqMessage, courseId);

		//向redis写缓存
		saveCourseCache(mqMessage, courseId);
		//返回true表示任务完成
		return false;
	}

	//生成课程静态化页面并上传文件系统
	private void generateCourseHtml(MqMessage mqMessage, Long courseId) {
		Long taskId = mqMessage.getId();
		MqMessageService mqMessageService = this.getMqMessageService();

		//任务幂等性处理，取出该阶段的执行状态
		int stageOne = mqMessageService.getStageOne(taskId);
		if(stageOne > 0){
			log.debug("课程静态化任务已完成，无需处理");
			return;
		}

		//开始进行课程静态化
		int i= 1/0;

		//更改任务状态为已完成
		mqMessageService.completedStageOne(taskId);
	}

	//保存索引信息
	private void saveCourseIndex(MqMessage mqMessage, Long courseId) {
		//任务id
		Long taskId = mqMessage.getId();
		MqMessageService mqMessageService = this.getMqMessageService();
		//取出第二个任务状态
		int stageTwo = mqMessageService.getStageTwo(taskId);
		if(stageTwo > 0){
			log.debug("已经向ES写入成功，无需再次写入");
			return;
		}

		//开始向ES写入数据，调用搜索服务


		//保存完成信息
		mqMessageService.completedStageTwo(taskId);
	}

	//写入redis缓存
	private void saveCourseCache(MqMessage mqMessage, Long courseId) {
		//任务id
		Long taskId = mqMessage.getId();
		MqMessageService mqMessageService = this.getMqMessageService();
		//取出第三个任务状态
		int stageThree = mqMessageService.getStageThree(taskId);
		if(stageThree > 0){
			log.debug("缓存已经写入成功，无需再次写入");
			return;
		}

		//开始向ES写入数据


		//保存完成信息
		mqMessageService.completedStageThree(taskId);
	}

}
