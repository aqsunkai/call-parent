import Vue from 'vue'
import Router from 'vue-router'
import Container from '@/container/Container'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      redirect: '/xiaomiErp',
      name: 'Container',
      component: Container,
      children: [
        {path: '/productSend',
          name: '产品上传',
          icon: 'el-icon-upload2',
          component: () => import('@/views/product_send')
        },
        {path: '/xiaomiErp',
          name: '小米erp',
          icon: 'el-icon-upload2',
          component: () => import('@/views/erp_product_send')
        },
        {path: '/splitFile',
          name: '拆分文件',
          icon: 'el-icon-files',
          component: () => import('@/views/split_file')
        }
      ]
    }
  ]
})
