package com.lingoflow.lingoflowbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lingoflow.lingoflowbackend.model.dto.ArticleGenerateRequest;
import com.lingoflow.lingoflowbackend.model.entity.Article;
import com.lingoflow.lingoflowbackend.model.vo.ArticleVO;
import com.lingoflow.lingoflowbackend.model.dto.TranslateRequest;

public interface ArticleService extends IService<Article> {

    /**
     * 调用大模型重写文章，提取生词，并落库保存
     * @param userId 当前登录用户的 ID
     * @param request 包含原文和目标难度
     * @return 包含重写后文章信息的视图对象
     */
    ArticleVO generateAndSaveArticle(Long userId, ArticleGenerateRequest request);

    /**
     * 分页获取当前用户的历史阅读文章列表
     */
    Page<ArticleVO> getArticleList(Long userId, Integer page, Integer size);

    /**
     * 获取文章详情
     */
    ArticleVO getArticleDetail(Long userId, Long articleId);

    /**
     * 划词即时翻译
     */
    String translateWord(TranslateRequest request);
}