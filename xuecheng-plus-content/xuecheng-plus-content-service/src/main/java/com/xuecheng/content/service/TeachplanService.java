package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

public interface TeachplanService {

    /**
     * 查询课程计划树形结构
     * @param courseId
     * @return
     */
    public List<TeachplanDto> getTreeNode(Long courseId);

    /**
     * @description 保存课程计划
     * @param teachplanDto  课程计划信息
     * @return void
     * @author Mr.M
     * @date 2022/9/9 13:39
     */
    public void saveTeachplan(SaveTeachplanDto teachplanDto);

    /**
     * 删除课程计划
     * @param id
     */
    public void deleteTeachplan(Long id);

    /**
     * 课程计划向上或者向下移动
     * @param id
     * @param flag true为向上移动，false向下移动
     */
    void changeOrderBy(Long id, Boolean flag);


    /**
     * @description 教学计划绑定媒资
     * @param bindTeachplanMediaDto
     * @return com.xuecheng.content.model.po.TeachplanMedia
     */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

}
