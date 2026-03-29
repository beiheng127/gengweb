import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/meme/:id',
    name: 'MemeDetail',
    component: () => import('@/views/MemeDetail.vue')
  },
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('@/views/Upload.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/audit',
    name: 'Audit',
    component: () => import('@/views/Audit.vue'),
    meta: { requiresAuth: true, minRole: 2 }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/Admin.vue'),
    meta: { requiresAuth: true, minRole: 3 }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫：检查登录状态和角色权限
router.beforeEach((to, _from, next) => {
  // 检查 token 是否存在
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    // 需要登录但未登录 → 跳转首页
    alert('请先登录后再访问此页面')
    next({ path: '/' })
    return
  }

  if (to.meta.minRole && token) {
    // 需要特定角色权限
    // 延迟导入 store 以避免循环依赖
    import('@/store/user').then(({ useUserStore }) => {
      const userStore = useUserStore()
      const userRole = userStore.user?.role ?? 0
      const requiredRole = to.meta.minRole as number

      if (userRole < requiredRole) {
        // 权限不足
        const roleNames: Record<number, string> = { 2: '审核员', 3: '管理员' }
        alert(`此页面需要 ${roleNames[requiredRole]} 或更高权限`)
        next({ path: '/' })
      } else {
        next()
      }
    }).catch(() => {
      next({ path: '/' })
    })
    return
  }

  next()
})

export default router
