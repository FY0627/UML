package com.lingoflow.lingoflowbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lingoflow.lingoflowbackend.model.dto.VocabularyAddRequest;
import com.lingoflow.lingoflowbackend.model.entity.Vocabulary;
import com.lingoflow.lingoflowbackend.model.vo.VocabularyVO;

public interface VocabularyService extends IService<Vocabulary> {

    // 1. 手动添加生词
    Boolean addVocabulary(Long userId, VocabularyAddRequest request);

    // 2. 分页获取生词列表（支持按是否掌握筛选）
    Page<VocabularyVO> getVocabularyList(Long userId, Integer page, Integer size, Integer mastered);

    // 3. 标记为已掌握
    Boolean markAsMastered(Long userId, Long id);
}