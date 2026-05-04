import request from './request'

export const generateQuizApi = (articleId) => {
  return request({
    url: `/api/quiz/generate/${articleId}`,
    method: 'post'
  })
}

export const submitQuizApi = (data) => {
  return request({
    url: '/api/quiz/submit',
    method: 'post',
    data // 包含 articleId, userAnswers
  })
}