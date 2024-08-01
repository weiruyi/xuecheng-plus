package com.xuecheng.ucenter.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author itcast
 */
@Data
@TableName("xc_user")
public class XcUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    private String username;

    private String password;

    private String salt;

    private String name;
    private String nickname;
    private String wxUnionid;
    private String companyId;
    /**
     * 头像
     */
    private String userpic;

    private String utype;

    private LocalDateTime birthday;

    private String sex;

    private String email;

    private String cellphone;

    private String qq;

    /**
     * 用户状态
     */
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
