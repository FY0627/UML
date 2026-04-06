package com.lingoflow.lingoflowbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lingo_article")

public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String originalText;

    private String adaptedText;

    private String targetLanguage;

    private String difficultyLevel;

    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}