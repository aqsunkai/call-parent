
/* 1.引入文件 */
import axios from 'axios' // 引入 axios库
// import qs from 'qs' // 引入 node中自带的qs模块（数据格式转换）
import { Message } from 'element-ui'
import * as env from './base_url'
const baseURL = env.baseURL

// 配置axios的属性
axios.defaults.timeout = 6000 // 请求超时时间1分钟
axios.defaults.baseURL = baseURL // 你的接口地址
axios.defaults.responseType = 'json'
axios.defaults.withCredentials = false // 是否允许带cookie这些

/* 你也可以创建一个实例，然后在实例中配置相关属性，此方法和上面的方法一样，写法不同，怎么用随个人
*喜好，我比较喜欢用这种方法，如下：
*/
const Axios = axios.create({
  baseURL: baseURL, // 后台服务地址
  timeout: 60000, // 请求超时时间1分钟
  withCredentials: false // 是否允许带cookie这些
})

/* 3.设置拦截器 */
/* 如果不是用创建实例的方式配置，那么下面的Axios都要换成axios,也就是文件开头你用import引入axios
时定义的变量 */
Axios.interceptors.request.use((config) => {
  return config
}, (error) => {
  console.log(error) // for debug
  return Promise.reject(error)
})
Axios.interceptors.response.use((response) => {
  const res = response.data
  if (!res.status) {
    // 请求异常
    Message({
      message: res.message,
      type: 'error',
      duration: 3 * 1000,
      showClose: true
    })
  }
  return res
}, (error) => {
  console.log('err' + error) // for debug
  Message({
    message: error.message,
    type: 'error',
    duration: 3 * 1000,
    showClose: true
  })
  return Promise.reject(error)
})
export default Axios
