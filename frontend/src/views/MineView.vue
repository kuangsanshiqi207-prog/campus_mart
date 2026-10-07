<script setup>
import { useRouter } from 'vue-router'
import UserBar from '../components/UserBar.vue'
import { clearSession, getUser } from '../api'

const router = useRouter()
const user = getUser()

function logout() {
  clearSession()
  router.push('/market')
  location.reload()
}
</script>

<template>
  <div class="shell">
    <UserBar />
    <main class="shelf page-block">
      <div class="shelf-head">
        <h2>我的 <span>{{ user ? (user.nickname || user.username) : '登录后管理订单和闲置' }}</span></h2>
      </div>
      <section class="grid mine-grid">
        <a class="deal place-card" href="/orders"><h3>我的订单</h3><p>查看买到和卖出的进度</p></a>
        <a class="deal place-card" href="/shop"><h3>发布闲置</h3><p>把用不上的东西挂到集市</p></a>
        <a class="deal place-card" href="/favorites"><h3>我的收藏</h3><p>先标记，回头再联系卖家</p></a>
        <a class="deal place-card" href="/chat"><h3>消息</h3><p>和同学约时间、问细节</p></a>
        <a class="deal place-card" href="/admin"><h3>管理后台</h3><p>审核、公告和举报</p></a>
        <button v-if="user" class="deal place-card mine-exit" type="button" @click="logout"><h3>退出登录</h3><p>换一个同学账号</p></button>
        <a v-else class="deal place-card" href="/login"><h3>去登录</h3><p>试用账号已经填好</p></a>
      </section>
    </main>
  </div>
</template>
