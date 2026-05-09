// src/api/request.js
import axios from 'axios'

// 1. 创建 axios 实例
const request = axios.create({
  baseURL: '', // 因为我们在 vite 配置了代理，所以留空即可
  timeout: 60000 // 请求超时时间增加到 60s，因为 AI 生成较慢
})

// 2. 请求拦截器：发请求前，自动去 localStorage 找 token 带上
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('lingoflow_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 3. 响应拦截器：统一剥离外层，处理报错
request.interceptors.response.use(
  response => {
    // 这里的 response.data 就是咱们后端返回的 Result<T> 对象 (code, message, data)
    const res = response.data
    
    // 如果 code 不是 200，说明业务报错（比如密码错误）
    if (res.code !== 200) {
      alert(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    
    // 如果是 200，直接把核心的 data 剥离出来给前端业务层
    return res.data 
  },
  error => {
    alert('网络异常或服务器未启动')
    return Promise.reject(error)
  }
)

export default request