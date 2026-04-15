package com.lingoflow.lingoflowbackend.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CorrectionTicketVO {
    private Long id;
    private Long userId;
    private String type;
    private Long targetId;
    private String originalContent;
    private String userSuggestion;
    private String adminRemark;
    private String status;
    private LocalDateTime createTime;
}
