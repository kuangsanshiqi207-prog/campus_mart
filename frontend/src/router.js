import { createRouter, createWebHistory } from 'vue-router'
import LoginView from './views/LoginView.vue'
import RegisterView from './views/RegisterView.vue'
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

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/market' },
    { path: '/login', component: LoginView },
    { path: '/register', component: RegisterView },
    { path: '/market', component: MarketView },
    { path: '/products/:id', component: ProductView },
    { path: '/shop', component: ShopView },
    { path: '/favorites', component: FavoritesView },
    { path: '/orders', component: OrdersView },
    { path: '/chat', component: ChatView },
    { path: '/admin', component: AdminView },
    { path: '/map', component: MapView },
    { path: '/wants', component: WantView },
    { path: '/help', component: HelpView },
    { path: '/me', component: MineView }
  ]
})

export default router
