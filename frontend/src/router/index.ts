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
          path: 'albums/:id',
          name: 'album-detail',
          component: () => import('@/views/album/AlbumDetailView.vue'),
          props: (route) => ({ id: Number(route.params.id) })
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
        },
        {
          path: 'persons/:id',
          name: 'person-detail',
          component: () => import('@/views/person/PersonDetailView.vue'),
          props: (route) => ({ id: Number(route.params.id) })
        },
        {
          path: 'explore',
          name: 'explore',
          component: () => import('@/views/explore/ExploreView.vue')
        },
        {
          path: 'favorites',
          name: 'favorites',
          component: () => import('@/views/favorite/FavoriteView.vue')
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('@/views/settings/SettingsView.vue')
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
