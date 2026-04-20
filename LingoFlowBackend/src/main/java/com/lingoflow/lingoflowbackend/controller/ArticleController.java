package com.lingoflow.lingoflowbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingoflow.lingoflowbackend.common.Result;
import com.lingoflow.lingoflowbackend.model.dto.ArticleGenerateRequest;
import com.lingoflow.lingoflowbackend.model.vo.ArticleVO;
import com.lingoflow.lingoflowbackend.service.ArticleService;
import com.lingoflow.lingoflowbackend.model.dto.TranslateRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 提交原文，AI 智能改写并提取生词
     */
    @PostMapping("/generate")
    public Result<ArticleVO> generateArticle(@RequestBody ArticleGenerateRequest request, HttpServletRequest httpServletRequest) {
        // 从 JWT 拦截器中取出我们之前放进去的 userId
        Long userId = (Long) httpServletRequest.getAttribute("userId");

        // 调用核心业务逻辑
        ArticleVO articleVO = articleService.generateAndSaveArticle(userId, request);

        return Result.success(articleVO);
    }

    /**
     * 分页查询历史阅读列表
     */
    @GetMapping("/list")
    public Result<Page<ArticleVO>> getArticleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        Page<ArticleVO> articleVOPage = articleService.getArticleList(userId, page, size);
        return Result.success(articleVOPage);
    }

    /**
     * 获取单篇文章详情
     */
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ArticleVO articleVO = articleService.getArticleDetail(userId, id);
        return Result.success(articleVO);
    }

    /**
     * 划词即时翻译
     */
    @PostMapping("/translate")
    public Result<String> translateWord(@RequestBody TranslateRequest request) {
        // 这个接口比较轻量，不涉及存数据库，可以直接返回字符串
        String translation = articleService.translateWord(request);
        return Result.success(translation);
    }

    /**
     * 删除历史生成的阅读文章
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteArticle(@PathVariable("id") Long id) {
        boolean success = articleService.removeById(id);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error(500, "删除历史文章失败");
        }
    }
}