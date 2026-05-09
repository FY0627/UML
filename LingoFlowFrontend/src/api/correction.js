import request from './request'

// 提交纠错反馈 (用户端)
export const submitCorrectionApi = (data) => {
    return request({
        url: '/api/correction/submit',
        method: 'post',
        data
    })
}
