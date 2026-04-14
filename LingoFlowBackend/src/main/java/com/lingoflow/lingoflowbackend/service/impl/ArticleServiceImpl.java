package com.lingoflow.lingoflowbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingoflow.lingoflowbackend.mapper.ArticleMapper;
import com.lingoflow.lingoflowbackend.mapper.VocabularyMapper;
import com.lingoflow.lingoflowbackend.model.dto.ArticleGenerateRequest;
import com.lingoflow.lingoflowbackend.model.entity.Article;
import com.lingoflow.lingoflowbackend.model.entity.Vocabulary;
import com.lingoflow.lingoflowbackend.model.vo.ArticleVO;
import com.lingoflow.lingoflowbackend.service.ArticleService;
import com.lingoflow.lingoflowbackend.model.dto.TranslateRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private ChatClient chatClient; // 注入我们在 0.8.1 版本中跑通的大模型客户端

    @Autowired
    private VocabularyMapper vocabularyMapper; // 注入生词本 Mapper

    @Autowired
    private ObjectMapper objectMapper; // Spring Boot 自带的 JSON 处理神器

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启数据库事务：文章和生词必须同时保存成功，否则回滚
    public ArticleVO generateAndSaveArticle(Long userId, ArticleGenerateRequest request) {
        String originalText = request.getOriginalText();
        String difficultyLevel = request.getDifficultyLevel();

        String prompt = String.format(
                """
                        【SYSTEM IDENTITY】
                        You are a strict text-rewriting engine. You do NOT have conversations. You do NOT explain. You ONLY output the exact rewritten English text.

                        【TASK】
                        Rewrite the following text to English difficulty level: [%s].

                        【STRICT RULES】
                        1. NO CHINESE. Do not translate. Output must be 100%% English.
                        2. NO CONVERSATIONAL FILLERS. Do not say "Here is the rewritten text" or "Sure".
                        3. NO MARKDOWN formatting like ``` or bold text.

                        【ORIGINAL TEXT TO REWRITE】
                        %s
                        """,
                difficultyLevel, originalText);

        // 2. 调用大模型，获取生成的字符串
        String aiResponse = "";
        try {
            aiResponse = chatClient.call(prompt);
        } catch (Exception e) {
            log.error("调用大模型发生连接异常: ", e);
            throw new RuntimeException("AI 服务连接超时或异常，请稍后再试");
        }

        try {
            // 3. AI 直接返回的就是改写后的文章文本，清理可能多余的空格或换行
            String adaptedText = aiResponse.trim();

            // 构造解析后的对象供后续保存使用
            AiGenerateResult result = new AiGenerateResult();
            result.setAdaptedText(adaptedText);
            // 不再自动提取生词，用户将自己在前端划词添加

            // 4. 保存文章到数据库
            Article article = new Article();
            article.setUserId(userId);
            article.setOriginalText(originalText);
            article.setAdaptedText(result.getAdaptedText());
            article.setTargetLanguage("EN"); // 这里可以根据实际情况做成动态获取
            article.setDifficultyLevel(difficultyLevel);
            this.save(article); // 保存后，article.getId() 就会被 MyBatis-Plus 自动赋上主键值

            // 6. 封装返回值给前端
            ArticleVO articleVO = new ArticleVO();
            articleVO.setId(article.getId());
            articleVO.setOriginalText(article.getOriginalText());
            articleVO.setAdaptedText(article.getAdaptedText());
            articleVO.setTargetLanguage(article.getTargetLanguage());
            articleVO.setDifficultyLevel(article.getDifficultyLevel());
            articleVO.setCreateTime(article.getCreateTime());

            return articleVO;

        } catch (Exception e) {
            log.error("大模型返回数据解析失败，原始返回: " + aiResponse, e);
            throw new RuntimeException("AI 生成失败或返回格式错误，请重试");
        }
    }

    @Override
    public Page<ArticleVO> getArticleList(Long userId, Integer page, Integer size) {
        // 1. 构造 MyBatis-Plus 的分页参数对象
        Page<Article> pageParam = new Page<>(page, size);

        // 2. 构造查询条件：只能查当前登录用户的文章，并且按创建时间倒序排（最新的在最上面）
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getUserId, userId)
                .orderByDesc(Article::getCreateTime);

        // 3. 执行分页查询
        Page<Article> articlePage = this.page(pageParam, queryWrapper);

        // 4. 将查出来的 Page<Article> 转换为脱敏后的 Page<ArticleVO>
        Page<ArticleVO> voPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());

        List<ArticleVO> voList = articlePage.getRecords().stream().map(article -> {
            ArticleVO vo = new ArticleVO();
            vo.setId(article.getId());
            vo.setOriginalText(article.getOriginalText());
            vo.setAdaptedText(article.getAdaptedText());
            vo.setTargetLanguage(article.getTargetLanguage());
            vo.setDifficultyLevel(article.getDifficultyLevel());
            vo.setCreateTime(article.getCreateTime());
            return vo;
        }).collect(java.util.stream.Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ArticleVO getArticleDetail(Long userId, Long articleId) {
        // 1. 根据 ID 查询文章
        Article article = this.getById(articleId);

        // 2. 校验文章是否存在，且只能查看自己的文章（防止越权访问）
        if (article == null || !article.getUserId().equals(userId)) {
            throw new RuntimeException("文章不存在或无权访问");
        }

        // 3. 封装为 VO 返回
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setOriginalText(article.getOriginalText());
        vo.setAdaptedText(article.getAdaptedText());
        vo.setTargetLanguage(article.getTargetLanguage());
        vo.setDifficultyLevel(article.getDifficultyLevel());
        vo.setCreateTime(article.getCreateTime());

        return vo;
    }

    @Override
    public String translateWord(TranslateRequest request) {
        String word = request.getWord();
        String contextSentence = request.getContextSentence();

        // 构建一个非常精简轻量的 Prompt，要求大模型“闭嘴”，只输出翻译结果
        String prompt = String.format("""
                You are an expert English dictionary like Cambridge Dictionary.
                Based on the context provided, explain the given word/phrase using simpler English words, followed by its Chinese translation.
                
                【Format Requirement】
                You MUST output EXACTLY two lines without any brackets, parentheses, labels, or conversational fillers.
                Line 1: The simple English explanation
                Line 2: The Chinese translation
                
                Example:
                to use something to discover if it works or if you like it
                尝试，试用
                
                【Word/Phrase】: %s
                【Context Sentence】: %s
                """, word, contextSentence);

        try {
            String translation = chatClient.call(prompt);
            // 清理可能带有的首尾空格或多余的换行
            return translation.trim();
        } catch (Exception e) {
            log.error("划词翻译调用大模型失败", e);
            throw new RuntimeException("翻译服务繁忙，请稍后再试");
        }
    }

    // ================== 内部辅助类，专门用来接收 AI 返回的 JSON 结构 ==================
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // 忽略 JSON 中多余的未知字段，防止报错
    public static class AiGenerateResult {
        private String adaptedText;
        private List<VocabularyItem> vocabularies;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VocabularyItem {
        private String word;
        private String translation;
        private String contextSentence;
    }
}