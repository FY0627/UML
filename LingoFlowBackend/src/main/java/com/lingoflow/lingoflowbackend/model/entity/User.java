package com.lingoflow.lingoflowbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lingo_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String passwordHash;

    private String targetLanguage;

    private String currentLevel;
    
    private String userRole;

    /**
     * 是否逻辑删除：1是，0否
     * @TableLogic 注解告诉 MyBatis-Plus 这是一个逻辑删除字段
     * 调用 deleteById 时，底层会自动转为 update is_deleted = 1
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}