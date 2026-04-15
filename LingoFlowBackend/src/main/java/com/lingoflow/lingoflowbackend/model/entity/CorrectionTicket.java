package com.lingoflow.lingoflowbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lingo_correction_ticket")
public class CorrectionTicket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String type;

    private Long targetId;

    private String originalContent;

    private String userSuggestion;

    private String adminRemark;

    private String status;

    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
