package com.xuecheng.content.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程计划管理
 */
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@Slf4j
@RestController
@RequestMapping("/teachplan")
@RequiredArgsConstructor
public class TeachplanController {

    private final TeachplanService teachplanService;
    private final TeachplanMapper teachplanMapper;

    //查询课程计划
    @ApiOperation("查询课程计划树形结构")
    @GetMapping("{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable("courseId") Long courseId) {
        List<TeachplanDto> treeNode = teachplanService.getTreeNode(courseId);
        return treeNode;
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping()
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        log.info("课程计划创建或修改：{}", teachplan.toString());
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("{id}")
    public void deleteTeachplan(@PathVariable Long id){
        log.info("删除课程计划,id=：{}",id);
        teachplanService.deleteTeachplan(id);
    }

    @ApiOperation("课程计划上移")
    @PostMapping("moveup/{id}")
    public void moveup(@PathVariable Long id){
        log.info("向上移动课程计划，id={}",id);
        teachplanService.changeOrderBy(id, true);
    }

    @ApiOperation("课程计划下移")
    @PostMapping("movedown/{id}")
    public void movedown(@PathVariable Long id){
        log.info("向下移动课程计划，id={}",id);
        teachplanService.changeOrderBy(id, false);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        log.info("课程和媒资关系绑定，{}",bindTeachplanMediaDto);
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation(value = "课程计划和媒资信息解除绑定")
    @DeleteMapping("/association/media/{teachPlanId}/{mediaId}")
    public void unassociationMedia(@PathVariable Long teachPlanId,@PathVariable String mediaId){
        log.info("课程和媒资关系解除绑定，teachPlanId={},mediaId={}",teachPlanId,mediaId);
        teachplanService.unassociationMedia(teachPlanId, mediaId);

    }

    @GetMapping("/{courseId}/{teachplanId}")
    public Teachplan getTeachPlanByCourseId(@PathVariable("courseId") Long courseId, @PathVariable("teachplanId") Long teachplanId){
        Teachplan teachplan = teachplanMapper.selectOne(new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getCourseId, courseId)
                .eq(Teachplan::getId, teachplanId));
        return teachplan;
    }

}
