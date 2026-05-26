import { createRouter, createWebHistory } from 'vue-router'

import { useUserStore } from '@/store/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/auth',
      name: 'auth',
      component: () => import('@/views/auth/AuthView.vue')
    },
    {
      path: '/',
      component: () => import('@/components/AppShell.vue'),
      redirect: '/home',
      children: [
        {
          path: 'home',
          name: 'home',
          component: () => import('@/views/home/HomeView.vue')
        },
        {
          path: 'albums',
          name: 'albums',
          component: () => import('@/views/album/AlbumView.vue')
        },
        {
          path: 'photos/:id',
          name: 'photo-detail',
          component: () => import('@/views/photo/PhotoDetailView.vue'),
          props: (route) => ({ id: Number(route.params.id) })
        },
        {
          path: 'persons',
          name: 'persons',
          component: () => import('@/views/person/PersonView.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.name !== 'auth' && !userStore.isLoggedIn) return { name: 'auth' }
  if (to.name === 'auth' && userStore.isLoggedIn) return { name: 'home' }
  return true
})

export default router
