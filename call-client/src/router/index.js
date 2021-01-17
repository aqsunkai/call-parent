import Vue from 'vue'
import Router from 'vue-router'
import Container from '@/container/Container'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      redirect: '/lonfenner',
      name: 'Container',
      component: Container,
      children: [
        {path: '/lonfenner',
          name: '洛菲纳',
          icon: 'el-icon-upload2',
          component: () => import('@/views/lonfenner_product')
        },
        // {path: '/xiaomiErp',
        //   name: '小米erp',
        //   icon: 'el-icon-upload2',
        //   component: () => import('@/views/erp_product')
        // },
        {path: '/splitFile',
          name: '拆分文件',
          icon: 'el-icon-files',
          component: () => import('@/views/split_file')
        }
      ]
    }
  ]
})
