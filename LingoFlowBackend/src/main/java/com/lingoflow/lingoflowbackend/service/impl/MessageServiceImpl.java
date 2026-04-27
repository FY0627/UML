package com.lingoflow.lingoflowbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingoflow.lingoflowbackend.mapper.MessageMapper;
import com.lingoflow.lingoflowbackend.model.entity.Message;
import com.lingoflow.lingoflowbackend.model.vo.MessageVO;
import com.lingoflow.lingoflowbackend.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0);
        return (int) this.count(queryWrapper);
    }

    @Override
    public Page<MessageVO> getMessageList(Long userId, Integer page, Integer size) {
        Page<Message> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getUserId, userId)
                .orderByDesc(Message::getCreateTime);

        Page<Message> messagePage = this.page(pageParam, queryWrapper);

        Page<MessageVO> voPage = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        List<MessageVO> voList = messagePage.getRecords().stream().map(message -> {
            MessageVO vo = new MessageVO();
            vo.setId(message.getId());
            vo.setTitle(message.getTitle());
            vo.setContent(message.getContent());
            vo.setIsRead(message.getIsRead());
            vo.setCreateTime(message.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public boolean markAsRead(Long userId, Long messageId) {
        Message message = this.getById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            throw new RuntimeException("消息不存在或无权访问");
        }
        message.setIsRead(1);
        return this.updateById(message);
    }

    @Override
    public void sendMessage(Long userId, String title, String content) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(0);
        this.save(message);
    }
}
