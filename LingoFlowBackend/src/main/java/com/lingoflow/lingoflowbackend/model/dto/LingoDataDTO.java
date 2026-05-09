package com.lingoflow.lingoflowbackend.model.dto;

import com.lingoflow.lingoflowbackend.model.entity.Article;
import com.lingoflow.lingoflowbackend.model.entity.Vocabulary;
import lombok.Data;

import java.util.List;

/**
 * 用于一键导入/导出用户个人数据的传输对象
 */
@Data
public class LingoDataDTO {

    // 用户的阅读历史记录列表
    private List<Article> articles;

    // 用户的生词本列表
    private List<Vocabulary> vocabularies;

}