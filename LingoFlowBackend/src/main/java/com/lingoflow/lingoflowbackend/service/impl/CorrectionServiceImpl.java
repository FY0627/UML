package com.lingoflow.lingoflowbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingoflow.lingoflowbackend.mapper.ArticleMapper;
import com.lingoflow.lingoflowbackend.mapper.VocabularyMapper;
import com.lingoflow.lingoflowbackend.model.dto.CorrectionSubmitRequest;
import com.lingoflow.lingoflowbackend.model.entity.Article;
import com.lingoflow.lingoflowbackend.model.entity.CorrectionTicket;
import com.lingoflow.lingoflowbackend.model.entity.Vocabulary;
import com.lingoflow.lingoflowbackend.model.vo.CorrectionTicketVO;
import com.lingoflow.lingoflowbackend.service.CorrectionService;
import com.lingoflow.lingoflowbackend.service.MessageService;
import com.lingoflow.lingoflowbackend.mapper.CorrectionTicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CorrectionServiceImpl extends ServiceImpl<CorrectionTicketMapper, CorrectionTicket> implements CorrectionService {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private VocabularyMapper vocabularyMapper;

    @Override
    public boolean submitCorrection(Long userId, CorrectionSubmitRequest request) {
        CorrectionTicket ticket = new CorrectionTicket();
        ticket.setUserId(userId);
        ticket.setType(request.getType());
        ticket.setTargetId(request.getTargetId());
        ticket.setOriginalContent(request.getOriginalContent());
        ticket.setUserSuggestion(request.getUserSuggestion());
        ticket.setStatus("PENDING");
        return this.save(ticket);
    }

    @Override
    public Page<CorrectionTicketVO> getCorrectionList(Integer page, Integer size, String status) {
        Page<CorrectionTicket> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CorrectionTicket> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(CorrectionTicket::getStatus, status);
        }
        queryWrapper.orderByDesc(CorrectionTicket::getCreateTime);

        Page<CorrectionTicket> ticketPage = this.page(pageParam, queryWrapper);

        Page<CorrectionTicketVO> voPage = new Page<>(ticketPage.getCurrent(), ticketPage.getSize(), ticketPage.getTotal());
        List<CorrectionTicketVO> voList = ticketPage.getRecords().stream().map(ticket -> {
            CorrectionTicketVO vo = new CorrectionTicketVO();
            vo.setId(ticket.getId());
            vo.setUserId(ticket.getUserId());
            vo.setType(ticket.getType());
            vo.setTargetId(ticket.getTargetId());
            vo.setOriginalContent(ticket.getOriginalContent());
            vo.setUserSuggestion(ticket.getUserSuggestion());
            vo.setAdminRemark(ticket.getAdminRemark());
            vo.setStatus(ticket.getStatus());
            vo.setCreateTime(ticket.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adoptCorrection(Long ticketId, String adminRemark) {
        CorrectionTicket ticket = this.getById(ticketId);
        if (ticket == null) {
            throw new RuntimeException("工单不存在");
        }
        ticket.setStatus("ACCEPTED");
        ticket.setAdminRemark(adminRemark);
        boolean success = this.updateById(ticket);

        if (success) {
            // 1. 发送系统消息告知用户
            messageService.sendMessage(ticket.getUserId(), "纠错反馈采纳通知", 
                String.format("您的纠错申请已被采纳！备注：%s", adminRemark));
            
            // 2. 根据 type 和 targetId 真正去修改业务表
            if ("ARTICLE".equalsIgnoreCase(ticket.getType())) {
                Article article = articleMapper.selectById(ticket.getTargetId());
                if (article != null) {
                    article.setAdaptedText(ticket.getUserSuggestion());
                    articleMapper.updateById(article);
                }
            } else if ("VOCABULARY".equalsIgnoreCase(ticket.getType())) {
                Vocabulary vocabulary = vocabularyMapper.selectById(ticket.getTargetId());
                if (vocabulary != null) {
                    vocabulary.setTranslation(ticket.getUserSuggestion());
                    vocabularyMapper.updateById(vocabulary);
                }
            }
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectCorrection(Long ticketId, String adminRemark) {
        CorrectionTicket ticket = this.getById(ticketId);
        if (ticket == null) {
            throw new RuntimeException("工单不存在");
        }
        ticket.setStatus("REJECTED");
        ticket.setAdminRemark(adminRemark);
        boolean success = this.updateById(ticket);

        if (success) {
            messageService.sendMessage(ticket.getUserId(), "纠错反馈处理通知", 
                String.format("您的纠错申请未被采纳。备注：%s", adminRemark));
        }
        return success;
    }
}
