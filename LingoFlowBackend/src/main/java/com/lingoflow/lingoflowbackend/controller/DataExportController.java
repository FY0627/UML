package com.lingoflow.lingoflowbackend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingoflow.lingoflowbackend.common.Result;
import com.lingoflow.lingoflowbackend.model.dto.LingoDataDTO;
import com.lingoflow.lingoflowbackend.model.entity.Article;
import com.lingoflow.lingoflowbackend.model.entity.Vocabulary;
import com.lingoflow.lingoflowbackend.service.ArticleService;
import com.lingoflow.lingoflowbackend.service.VocabularyService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/data")
public class DataExportController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private ObjectMapper objectMapper; // 注入即可，不需要在下面 new

    /**
     * 一键导出用户的全部阅读历史和生词本为 JSON 文件
     */
    @GetMapping("/export")
    public Result<LingoDataDTO> exportUserData(HttpServletRequest request) {
        // 【修复1】绝大多数项目的 JWT 拦截器在验证通过后，都会把解析出的 userId 塞进 request 域中
        // 这样可以完美绕开 JwtUtils 方法名不匹配的报错！
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            return Result.error("导出失败，请检查登录状态或Token！");
        }
        Long currentUserId = Long.valueOf(userIdObj.toString());

        // 查询该账号下的数据
        List<Article> myArticles = articleService.lambdaQuery().eq(Article::getUserId, currentUserId).list();
        List<Vocabulary> myVocabs = vocabularyService.lambdaQuery().eq(Vocabulary::getUserId, currentUserId).list();

        LingoDataDTO dataDTO = new LingoDataDTO();
        dataDTO.setArticles(myArticles);
        dataDTO.setVocabularies(myVocabs);

        return Result.success(dataDTO);
    }

    /**
     * 导入数据 (洗白并重新绑定)
     */
    @PostMapping("/import")
    public Result<Boolean> importUserData(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("上传的文件为空！");
        }

        try {
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj == null) {
                return Result.error("导入失败，请先登录！");
            }
            Long currentUserId = Long.valueOf(userIdObj.toString());

            // 1. 读取整个 JSON 文件为树状结构
            JsonNode rootNode = objectMapper.readTree(file.getInputStream());
            LingoDataDTO importData;

            // 2. 【核心修复】智能剥壳：如果文件被 Result 包裹，就提取 "data" 里面的内容
            if (rootNode.has("data")) {
                importData = objectMapper.treeToValue(rootNode.get("data"), LingoDataDTO.class);
            } else {
                // 如果没有包裹，直接解析 (兼容纯净版 JSON)
                importData = objectMapper.treeToValue(rootNode, LingoDataDTO.class);
            }

            // 防御性判断，防止 importData 依然是 null
            if (importData == null) {
                return Result.error("文件内容为空或格式不正确！");
            }

            // 3. 【核心更新】：先清空该账号的旧数据（实现覆盖逻辑），再插入新数据

            // --- 处理文章历史 ---
            // 覆盖第一步：直接抹除当前账号所有的旧文章
            articleService.lambdaUpdate().eq(Article::getUserId, currentUserId).remove();
            // 覆盖第二步：保存导入的新文章
            if (importData.getArticles() != null && !importData.getArticles().isEmpty()) {
                for (Article article : importData.getArticles()) {
                    article.setId(null); // 清空旧主键
                    article.setUserId(currentUserId); // 绑定新账号
                }
                articleService.saveBatch(importData.getArticles());
            }

            // --- 处理生词本 ---
            // 覆盖第一步：直接抹除当前账号所有的旧生词
            vocabularyService.lambdaUpdate().eq(Vocabulary::getUserId, currentUserId).remove();
            // 覆盖第二步：保存导入的新生词
            if (importData.getVocabularies() != null && !importData.getVocabularies().isEmpty()) {
                for (Vocabulary vocab : importData.getVocabularies()) {
                    vocab.setId(null);
                    vocab.setUserId(currentUserId);
                }
                vocabularyService.saveBatch(importData.getVocabularies());
            }

            // 如果有错题本 (Quiz)，也是一样的逻辑先 remove 再 saveBatch...

            return Result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入解析失败: " + e.getMessage());
        }
    }
}