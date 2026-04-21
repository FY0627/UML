import request from './request'

export const getVocabularyListApi = (current = 1, size = 10) => {
  return request({ url: `/api/vocabulary/list?current=${current}&size=${size}`, method: 'get' })
}

// 修复：确保使用标准的 restful 传参 (或者根据你的后端改为 post)
export const deleteVocabularyApi = (id) => request({ url: `/api/vocabulary/delete/${id}`, method: 'delete' })

// 修复：把状态和 ID 放在 body 里传给后端
export const updateVocabularyStatusApi = (id, status) => {
  return request({
    url: '/api/vocabulary/update',
    method: 'put',
    data: { id: id, mastered: status } // 确保这里传的是 mastered
  })
}