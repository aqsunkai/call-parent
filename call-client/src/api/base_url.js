/* 2.全局默认配置 */
let baseURL
// 判断开发环境（一般用于本地代理）
if (process.env.NODE_ENV === 'development') { // 开发环境
  baseURL = '/api' // 你设置的本地代理请求（跨域代理），下文会详细介绍怎么进行跨域代理
} else { // 编译环境
  if (process.env.type === 'test') { // 测试环境
    baseURL = 'http://localhost:8080'
  } else { // 正式环境
    baseURL = 'http://localhost:8080'
  }
}

export { baseURL }
