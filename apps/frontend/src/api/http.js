import axios from 'axios'

// 创建独立实例：避免污染全局 axios 默认配置
// baseURL 写成 /api，Vite 会把 /api 代理到后端，浏览器看到的仍是同源地址
const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 响应拦截器：把后端统一响应体 R 拆开
// 后端返回 { code, message, data, timestamp }；
// 业务代码只关心 data，错误统一转成 rejected Promise，页面只写一处 catch
http.interceptors.response.use(
  (response) => {
    const res = response.data

    // 兼容「返回的不是 R 结构」的情况，避免静默失败
    if (res == null || typeof res.code === 'undefined') {
      return Promise.reject(new Error('后端返回格式不是约定的 R 结构'))
    }
    if (res.code !== 0) {
      return Promise.reject(new Error(res.message || '业务请求失败'))
    }
    return res.data
  },
  (error) => {
    // HTTP 层错误：404 / 500 / 网络断开 / 超时
    let message = '网络异常，请检查后端是否已启动'
    if (error.response) {
      message = `服务返回 ${error.response.status}，请查看后端日志`
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    }
    return Promise.reject(new Error(message))
  }
)

export default http
