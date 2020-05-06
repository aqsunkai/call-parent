import Axios from './axios' // 导入配置好的axios文件
// 封装axios请求函数，并用export导出
export function productSend (data) {
  return Axios({
    url: '/api/product/send',
    method: 'post',
    data: data
  })
}
export function productResult (data) {
  return Axios({
    url: `/api/product/result`,
    method: 'post',
    data: data
  })
}
