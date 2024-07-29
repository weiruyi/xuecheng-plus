package com.xuecheng.content.feignClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {

	@Override
	public SearchServiceClient create(Throwable cause) {
		return new SearchServiceClient() {
			@Override
			public Boolean add(CourseIndex courseIndex) {
				log.error("添加课程索引发生熔断，索引信息：{}， 异常信息：{}", courseIndex.getName(), cause.toString());
				return false;
			}
		};
	}
}
