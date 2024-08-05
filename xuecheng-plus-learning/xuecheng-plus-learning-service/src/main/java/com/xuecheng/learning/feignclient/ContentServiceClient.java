package com.xuecheng.learning.feignclient;

import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.Teachplan;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;



@FeignClient(value = "content-api",fallbackFactory = ContentServiceClientFallbackFactory.class)
public interface ContentServiceClient {

    @ResponseBody
    @GetMapping("/content/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId);

    @GetMapping("/content/teachplan/{courseId}/{teachplanId}")
    public Teachplan getTeachPlanByCourseId(@PathVariable("courseId") Long courseId,
                                            @PathVariable("teachplanId") Long teachplanId);

}
