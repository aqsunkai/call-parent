import Axios from './axios' // 导入配置好的axios文件
// 封装axios请求函数，并用export导出
export function productSend (data) {
  return Axios({
    url: '/api/product/send',
    method: 'post',
    data: data
  })
}
export function erpProductSend (data) {
  return Axios({
    url: '/api/erpProduct/send',
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
export function splitFileResult (params) {
  return Axios({
    url: `/api/file/splitResult`,
    method: 'get',
    params: params
  })
}
export function priceCalculate (data) {
  return Axios({
    url: '/api/price/calculate',
    method: 'post',
    data: data
  })
}
export function calculateResult (params) {
  return Axios({
    url: `/api/price/calculate/result`,
    method: 'get',
    params: params
  })
}
export function priceChange (data) {
  return Axios({
    url: `/api/price/change`,
    method: 'post',
    data: data
  })
}
