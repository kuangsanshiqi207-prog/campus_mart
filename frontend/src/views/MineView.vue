<script setup>
import { useRouter } from 'vue-router'
import UserBar from '../components/UserBar.vue'
import MiniIcon from '../components/MiniIcon.vue'
import { clearSession, getUser } from '../api'

const router = useRouter()
const user = getUser()
const links = [
  { href: '/orders', icon: 'box', title: '我的订单', text: '买到的和卖出的都在这里跟进' },
  { href: '/shop', icon: 'tag', title: '发布闲置', text: '填好价格和照片，挂到集市' },
  { href: '/favorites', icon: 'heart', title: '我的收藏', text: '先标记，回头再约卖家' },
  { href: '/reports', icon: 'report', title: '我的举报', text: '查看举报进度，不满意可申诉' },
  { href: '/service', icon: 'service', title: 'AI 客服', text: '流程和规则问题，随时问' },
  { href: '/chat', icon: 'chat', title: '消息', text: '和同学确认时间和地点' }
]

function logout() {
  clearSession()
  router.push('/market')
  location.reload()
}
</script>

<template>
  <div class="shell">
    <UserBar />
    <main class="stage">
      <header class="stage-head">
        <i class="spot-icon"><MiniIcon name="user" /></i>
        <div>
          <p class="kicker">个人中心</p>
          <h1>{{ user ? (user.nickname || user.username) : '还没有登录' }}</h1>
          <p>{{ user ? '订单、闲置和私信都从这里进去。' : '登录后可以发布闲置、收藏和下单。' }}</p>
        </div>
      </header>
      <section class="mine-links">
        <a v-for="item in links" :key="item.href" :href="item.href">
          <i class="spot-icon"><MiniIcon :name="item.icon" /></i>
          <div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.text }}</p>
          </div>
        </a>
        <button v-if="user" type="button" @click="logout">
          <i class="spot-icon"><MiniIcon name="door" /></i>
          <div>
            <h3>退出登录</h3>
            <p>换一个同学账号继续逛</p>
          </div>
        </button>
        <a v-else href="/login">
          <i class="spot-icon"><MiniIcon name="user" /></i>
          <div>
            <h3>去登录</h3>
            <p>试用账号已经填好</p>
          </div>
        </a>
      </section>
    </main>
  </div>
</template>
