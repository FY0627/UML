import request from './request'

// 1. 生成 i+1 文章
export const generateArticleApi = (data) => {
  return request({ url: '/api/article/generate', method: 'post', data })
}

// 2. 划词翻译
export const translateWordApi = (data) => {
  return request({ url: '/api/article/translate', method: 'post', data })
}

// 3. 加入生词本
export const addVocabularyApi = (data) => {
  return request({ url: '/api/vocabulary/add', method: 'post', data })
}

// =====================================
// 【新增】阅读历史相关接口
// =====================================

// 4. 获取历史文章列表
export const getArticleListApi = (current = 1, size = 10) => {
  return request({ url: `/api/article/list?current=${current}&size=${size}`, method: 'get' })
}

// 5. 删除历史文章 (可选)
export const deleteArticleApi = (id) => request({
  url: `/api/article/delete/${id}`,
  method: 'delete'
})