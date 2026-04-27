package com.lingoflow.lingoflowbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lingoflow.lingoflowbackend.model.entity.Message;
import com.lingoflow.lingoflowbackend.model.vo.MessageVO;

public interface MessageService extends IService<Message> {

    /**
     * 获取未读消息数
     */
    Integer getUnreadCount(Long userId);

    /**
     * 分页查询消息列表
     */
    Page<MessageVO> getMessageList(Long userId, Integer page, Integer size);

    /**
     * 标记消息为已读
     */
    boolean markAsRead(Long userId, Long messageId);

    /**
     * 发送系统消息
     */
    void sendMessage(Long userId, String title, String content);
}
