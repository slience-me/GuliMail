import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'
import Hellovue from '../components/Hellovue.vue'
import MyTable from '../components/MyTable.vue'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'HelloWorld',
      component: HelloWorld
    },
    {
      path: '/hello',
      name: 'HelloVue',
      component: Hellovue
    },
    {
      path: '/table',
      name: 'MyTable',
      component: MyTable
    }
  ]
})
