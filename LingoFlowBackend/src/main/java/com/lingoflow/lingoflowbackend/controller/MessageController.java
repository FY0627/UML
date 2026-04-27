package com.lingoflow.lingoflowbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingoflow.lingoflowbackend.common.Result;
import com.lingoflow.lingoflowbackend.model.vo.MessageVO;
import com.lingoflow.lingoflowbackend.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 分页查询消息列表
     */
    @GetMapping("/list")
    public Result<Page<MessageVO>> getMessageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<MessageVO> messageVOPage = messageService.getMessageList(userId, page, size);
        return Result.success(messageVOPage);
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/read/{id}")
    public Result<Boolean> markAsRead(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = messageService.markAsRead(userId, id);
        return Result.success(success);
    }
}
