package com.lingoflow.lingoflowbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lingo_vocabulary")
public class Vocabulary {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long articleId;

    private String word;

    private String translation;

    private String contextSentence;

    /**
     * 是否已掌握：1是，0否
     */
    @TableField("is_mastered")
    private Integer mastered;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}