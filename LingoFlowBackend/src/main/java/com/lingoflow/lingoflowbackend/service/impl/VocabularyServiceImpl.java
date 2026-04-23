package com.lingoflow.lingoflowbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingoflow.lingoflowbackend.mapper.VocabularyMapper;
import com.lingoflow.lingoflowbackend.model.dto.VocabularyAddRequest;
import com.lingoflow.lingoflowbackend.model.entity.Vocabulary;
import com.lingoflow.lingoflowbackend.model.vo.VocabularyVO;
import com.lingoflow.lingoflowbackend.service.VocabularyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VocabularyServiceImpl extends ServiceImpl<VocabularyMapper, Vocabulary> implements VocabularyService {

    @Override
    public Boolean addVocabulary(Long userId, VocabularyAddRequest request) {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setUserId(userId);
        vocabulary.setArticleId(request.getArticleId());
        vocabulary.setWord(request.getWord());
        vocabulary.setTranslation(request.getTranslation());
        vocabulary.setContextSentence(request.getContextSentence());
        vocabulary.setMastered(0); // 默认未掌握
        return this.save(vocabulary);
    }

    @Override
    public Page<VocabularyVO> getVocabularyList(Long userId, Integer page, Integer size, Integer mastered) {
        Page<Vocabulary> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Vocabulary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vocabulary::getUserId, userId);

        // 如果前端传了 mastered 参数，就加上这个筛选条件
        if (mastered != null) {
            queryWrapper.eq(Vocabulary::getMastered, mastered);
        }
        // 按添加时间倒序
        queryWrapper.orderByDesc(Vocabulary::getCreateTime);

        Page<Vocabulary> vocabPage = this.page(pageParam, queryWrapper);

        // 实体转 VO
        Page<VocabularyVO> voPage = new Page<>(vocabPage.getCurrent(), vocabPage.getSize(), vocabPage.getTotal());
        List<VocabularyVO> voList = vocabPage.getRecords().stream().map(v -> {
            VocabularyVO vo = new VocabularyVO();
            vo.setId(v.getId());
            vo.setArticleId(v.getArticleId());
            vo.setWord(v.getWord());
            vo.setTranslation(v.getTranslation());
            vo.setContextSentence(v.getContextSentence());
            vo.setMastered(v.getMastered());
            vo.setCreateTime(v.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Boolean markAsMastered(Long userId, Long id) {
        Vocabulary vocabulary = this.getById(id);
        // 安全校验：生词必须存在，且只能操作自己的生词
        if (vocabulary == null || !vocabulary.getUserId().equals(userId)) {
            throw new RuntimeException("生词不存在或无权操作");
        }

        vocabulary.setMastered(1); // 标记为已掌握
        return this.updateById(vocabulary);
    }
}