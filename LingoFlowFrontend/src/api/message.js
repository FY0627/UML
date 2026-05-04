import request from './request'

// 获取未读消息数
export const getUnreadCountApi = () => {
    return request({
        url: '/api/message/unread/count',
        method: 'get'
    })
}

// 分页查询消息列表
export const getMessageListApi = (page, size) => {
    return request({
        url: '/api/message/list',
        method: 'get',
        params: { page, size }
    })
}

// 标记消息为已读
export const markAsReadApi = (id) => {
    return request({
        url: `/api/message/read/${id}`,
        method: 'put'
    })
}
