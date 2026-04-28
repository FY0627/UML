import request from './request'
import axios from 'axios' // 【新增】引入纯净版 axios

// 1. 登录
export const loginApi = (data) => {
  return request({ url: '/api/user/login', method: 'post', data })
}

// 2. 注册
export const registerApi = (data) => {
  return request({ url: '/api/user/register', method: 'post', data })
}

// 3. 找回密码 (预留)
export const forgotPasswordApi = (email) => {
  return request({ url: '/api/user/forgot-password', method: 'post', data: { email } })
}

// 4. 修改密码
export const changePasswordApi = (data) => {
  return request({ url: '/api/user/change-password', method: 'post', data })
}

// ==========================================
// 🚀 终极版：一键导出数据 (绕过全局拦截器)
// ==========================================
export const downloadUserData = async () => {
  try {
    // 1. 手动从浏览器获取当前用户的 Token
    const token = localStorage.getItem('lingoflow_token')
    
    // 2. 使用原生 axios 发送请求，完美绕过 request.js 的业务拦截器
    const response = await axios.get('/api/data/export', {
      headers: {
        'Authorization': `Bearer ${token}` // 手动塞入大门钥匙
      },
      responseType: 'blob' // 极其关键：告诉 axios 这是一个文件流，不要当成普通 JSON 解析！
    })
    
    // 3. 将二进制文件流转换为浏览器可识别的下载链接
    const blob = new Blob([response.data], { type: 'application/json;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    
    // 4. 模拟隐形点击，触发原生的文件下载框
    const link = document.createElement('a')
    link.href = url
    
    // 动态生成带日期的文件名
    const dateStr = new Date().toISOString().split('T')[0]
    link.download = `LingoFlow_MyData_${dateStr}.json`
    
    document.body.appendChild(link)
    link.click() // 触发下载
    
    // 5. 过河拆桥，释放内存
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    return true
  } catch (error) {
    console.error('一键导出数据失败:', error)
    alert('导出失败：请检查后端导出接口是否正常！')
    return false
  }
}

export const importDataApi = (formData) => {
  const token = localStorage.getItem('lingoflow_token')
  return axios.post('/api/data/import', formData, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'multipart/form-data'
    }
  })
}