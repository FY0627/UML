package com.lingoflow.lingoflowbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingoflow.lingoflowbackend.model.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}