import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/Login/LoginView.vue'
import AdminLoginView from '@/views/Login/AdminLoginView.vue'
import RegisterView from '@/views/Register/RegisterView.vue'
import AdminLayout from '@/views/Admin/AdminLayout.vue'
import TenantLayout from '@/views/Tenant/TenantLayout.vue'
import LandlordLayout from '@/views/Landlord/LandlordLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      component: LoginView
    },
    {
      path: '/admin/login',
      component: AdminLoginView
    },
    {
      path: '/register',
      component: RegisterView
    },
    {
      path: '/tenant',
      component: TenantLayout,
      redirect: '/tenant/home',
      meta: { requiresAuth: true, role: 'tenant' },
      children: [
        {
          path: 'home',
          name: 'TenantHome',
          component: () => import('@/views/Tenant/TenantHome.vue')
        },
        {
          path: 'search',
          name: 'TenantSearch',
          component: () => import('@/views/Tenant/TenantSearch.vue')
        },
        {
          path: 'house/:id',
          name: 'TenantHouseDetail',
          component: () => import('@/views/Tenant/TenantHouseDetail.vue')
        },
        {
          path: 'orders',
          name: 'TenantOrders',
          component: () => import('@/views/Tenant/TenantOrders.vue')
        },
        {
          path: 'favorites',
          name: 'TenantFavorites',
          component: () => import('@/views/Tenant/TenantFavorites.vue')
        },
        {
          path: 'appointments',
          name: 'TenantAppointments',
          component: () => import('@/views/Tenant/TenantAppointments.vue')
        },
        {
          path: 'chat',
          name: 'TenantChat',
          component: () => import('@/views/Tenant/TenantChat.vue')
        },
        {
          path: 'notifications',
          name: 'TenantNotifications',
          component: () => import('@/views/Tenant/TenantNotifications.vue')
        },
        {
          path: 'reviews',
          name: 'TenantReviews',
          component: () => import('@/views/Tenant/TenantReviews.vue')
        },
        {
          path: 'profile',
          name: 'TenantProfile',
          component: () => import('@/views/Tenant/TenantProfile.vue')
        },
        {
          path: 'user/:id',
          name: 'TenantUserProfile',
          component: () => import('@/views/Common/UserPublicProfile.vue')
        }
      ]
    },
    {
      path: '/landlord',
      component: LandlordLayout,
      redirect: '/landlord/dashboard',
      meta: { requiresAuth: true, role: 'landlord' },
      children: [
        {
          path: 'dashboard',
          name: 'LandlordDashboard',
          component: () => import('@/views/Landlord/LandlordDashboard.vue')
        },
        {
          path: 'houses',
          name: 'LandlordHouses',
          component: () => import('@/views/Landlord/LandlordHouses.vue')
        },
        {
          path: 'house/:id',
          name: 'LandlordHouseDetail',
          component: () => import('@/views/Landlord/LandlordHouseDetail.vue')
        },
        {
          path: 'orders',
          name: 'LandlordOrders',
          component: () => import('@/views/Landlord/LandlordOrders.vue')
        },
        {
          path: 'payments',
          name: 'LandlordPayments',
          component: () => import('@/views/Landlord/LandlordPayments.vue')
        },
        {
          path: 'appointments',
          name: 'LandlordAppointments',
          component: () => import('@/views/Landlord/LandlordAppointments.vue')
        },
        {
          path: 'chat',
          name: 'LandlordChat',
          component: () => import('@/views/Landlord/LandlordChat.vue')
        },
        {
          path: 'notifications',
          name: 'LandlordNotifications',
          component: () => import('@/views/Landlord/LandlordNotifications.vue')
        },
        {
          path: 'reviews',
          name: 'LandlordReviews',
          component: () => import('@/views/Landlord/LandlordReviews.vue')
        },
        {
          path: 'profile',
          name: 'LandlordProfile',
          component: () => import('@/views/Landlord/LandlordProfile.vue')
        },
        {
          path: 'user/:id',
          name: 'LandlordUserProfile',
          component: () => import('@/views/Common/UserPublicProfile.vue')
        }
      ]
    },
    {
      path: '/admin',
      component: AdminLayout,
      redirect: '/admin/dashboard',
      meta: { requiresAuth: true, role: 'admin' },
      children: [
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/Admin/AdminDashboard.vue')
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/Admin/AdminUsers.vue')
        },
        {
          path: 'houses',
          name: 'AdminHouses',
          component: () => import('@/views/Admin/AdminHouses.vue')
        },
        {
          path: 'verify',
          name: 'AdminVerify',
          component: () => import('@/views/Admin/AdminVerify.vue')
        },
        {
          path: 'announcements',
          name: 'AdminAnnouncements',
          component: () => import('@/views/Admin/AdminAnnouncements.vue')
        }
      ]
    }
  ]
})

// 路由守卫
const publicPages = ['/login', '/admin/login', '/register']

router.beforeEach((to, from, next) => {
  // 公开页面直接放行
  if (publicPages.includes(to.path)) {
    return next()
  }

  const requiredRole = to.matched.find(r => r.meta.role)?.meta.role

  if (requiredRole === 'admin') {
    // 管理员页面：检查 adminInfo
    const adminInfo = JSON.parse(localStorage.getItem('adminInfo') || '{}')
    if (!adminInfo.id) {
      return next('/admin/login')
    }
  } else if (requiredRole === 'tenant' || requiredRole === 'landlord') {
    // 普通用户页面：检查 userInfo
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!userInfo.id) {
      return next('/login')
    }
  }

  next()
})

export default router
