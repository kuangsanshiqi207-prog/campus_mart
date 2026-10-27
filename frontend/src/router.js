import { createRouter, createWebHistory } from 'vue-router'
import { getAdminToken } from './api'
import AuthStage from './components/AuthStage.vue'
import LoginView from './views/LoginView.vue'
import RegisterView from './views/RegisterView.vue'
import ResetView from './views/ResetView.vue'
import MarketView from './views/MarketView.vue'
import ProductView from './views/ProductView.vue'
import ShopView from './views/ShopView.vue'
import FavoritesView from './views/FavoritesView.vue'
import OrdersView from './views/OrdersView.vue'
import ChatView from './views/ChatView.vue'
import AdminView from './views/AdminView.vue'
import MapView from './views/MapView.vue'
import WantView from './views/WantView.vue'
import HelpView from './views/HelpView.vue'
import MineView from './views/MineView.vue'
import AiServiceView from './views/AiServiceView.vue'
import ReportView from './views/ReportView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/market' },
    {
      path: '/',
      component: AuthStage,
      children: [
        { path: 'login', component: LoginView },
        { path: 'register', component: RegisterView },
        { path: 'reset', component: ResetView }
      ]
    },
    { path: '/market', component: MarketView },
    { path: '/products/:id', component: ProductView },
    { path: '/shop', component: ShopView },
    { path: '/favorites', component: FavoritesView },
    { path: '/orders', component: OrdersView },
    { path: '/chat', component: ChatView },
    { path: '/map', component: MapView },
    { path: '/wants', component: WantView },
    { path: '/help', component: HelpView },
    { path: '/service', component: AiServiceView },
    { path: '/reports', component: ReportView },
    { path: '/me', component: MineView },
    { path: '/admin', component: AdminView, meta: { requiresAdmin: true } }
  ]
})

router.beforeEach((to) => {
  if (to.meta.requiresAdmin && !getAdminToken()) {
    return { path: '/login' }
  }
})

export default router
