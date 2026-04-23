package com.lingoflow.lingoflowbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingoflow.lingoflowbackend.model.entity.Vocabulary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VocabularyMapper extends BaseMapper<Vocabulary> {
}