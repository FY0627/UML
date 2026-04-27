package com.lingoflow.lingoflowbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingoflow.lingoflowbackend.common.Result;
import com.lingoflow.lingoflowbackend.model.dto.CorrectionSubmitRequest;
import com.lingoflow.lingoflowbackend.model.vo.CorrectionTicketVO;
import com.lingoflow.lingoflowbackend.service.CorrectionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CorrectionController {

    @Autowired
    private CorrectionService correctionService;

    /**
     * 提交纠错反馈 (用户端)
     */
    @PostMapping("/correction/submit")
    public Result<Boolean> submitCorrection(@RequestBody CorrectionSubmitRequest request, HttpServletRequest httpServletRequest) {
        Long userId = (Long) httpServletRequest.getAttribute("userId");
        boolean success = correctionService.submitCorrection(userId, request);
        return Result.success(success);
    }

    /**
     * 获取工单列表 (管理员端)
     */
    @GetMapping("/admin/correction/list")
    public Result<Page<CorrectionTicketVO>> getCorrectionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        Page<CorrectionTicketVO> list = correctionService.getCorrectionList(page, size, status);
        return Result.success(list);
    }

    /**
     * 采纳并修正 (管理员端)
     */
    @PostMapping("/admin/correction/adopt")
    public Result<Boolean> adoptCorrection(@RequestParam Long ticketId, @RequestParam String adminRemark) {
        boolean success = correctionService.adoptCorrection(ticketId, adminRemark);
        return Result.success(success);
    }

    /**
     * 驳回工单 (管理员端)
     */
    @PostMapping("/admin/correction/reject")
    public Result<Boolean> rejectCorrection(@RequestParam Long ticketId, @RequestParam String adminRemark) {
        boolean success = correctionService.rejectCorrection(ticketId, adminRemark);
        return Result.success(success);
    }
}
