import request from './request'

// 1. 获取纠错工单列表
export const getCorrectionListApi = (params) => {
    return request({
        url: '/api/admin/correction/list',
        method: 'get',
        params
    })
}

// 2. 采纳并修正
export const adoptCorrectionApi = (ticketId, adminRemark) => {
    return request({
        url: '/api/admin/correction/adopt',
        method: 'post',
        params: { ticketId, adminRemark }
    })
}

// 3. 驳回工单
export const rejectCorrectionApi = (ticketId, adminRemark) => {
    return request({
        url: '/api/admin/correction/reject',
        method: 'post',
        params: { ticketId, adminRemark }
    })
}
