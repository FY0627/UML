package com.lingoflow.lingoflowbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lingoflow.lingoflowbackend.model.dto.CorrectionSubmitRequest;
import com.lingoflow.lingoflowbackend.model.entity.CorrectionTicket;
import com.lingoflow.lingoflowbackend.model.vo.CorrectionTicketVO;

public interface CorrectionService extends IService<CorrectionTicket> {

    /**
     * 用户提交纠错反馈
     */
    boolean submitCorrection(Long userId, CorrectionSubmitRequest request);

    /**
     * 管理员获取工单列表
     */
    Page<CorrectionTicketVO> getCorrectionList(Integer page, Integer size, String status);

    /**
     * 采纳并修正
     */
    boolean adoptCorrection(Long ticketId, String adminRemark);

    /**
     * 驳回工单
     */
    boolean rejectCorrection(Long ticketId, String adminRemark);
}
