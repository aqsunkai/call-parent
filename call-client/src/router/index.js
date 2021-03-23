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
        {path: '/lonfennerVariation',
          name: '洛菲纳变体',
          icon: 'el-icon-upload2',
          component: () => import('@/views/lonfenner_product_variation')
        },
        {path: '/price',
          name: '修改价格',
          icon: 'el-icon-edit',
          component: () => import('@/views/price_calculate')
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
