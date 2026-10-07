<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser } from '../api'
const route = useRoute()
const router = useRouter()
const keyword = ref(route.query.keyword || '')
const user = getUser()

watch(() => route.query.keyword, (value) => {
  keyword.value = value || ''
})

function current(path) {
  return route.path === path
}

function search() {
  const query = keyword.value.trim() ? { keyword: keyword.value.trim() } : {}
  if (route.path === '/market') router.replace({ path: '/market', query })
  else router.push({ path: '/market', query })
}

</script>

<template>
  <header class="topbar">
    <a class="brand" href="/market">
      校集
      <small>校园里的<br />二手好物集市</small>
    </a>
    <nav class="nav">
      <a :class="{ current: current('/market') }" href="/market">首页</a>
      <a href="/market#categories">全部分类</a>
      <a :class="{ current: current('/map') }" href="/map">校园地图</a>
      <a :class="{ current: current('/wants') }" href="/wants">求购专区</a>
      <a :class="{ current: current('/help') }" href="/help">帮助中心</a>
    </nav>
    <form class="top-search" @submit.prevent="search">
      <input v-model="keyword" placeholder="搜索你想要的宝贝" />
      <button class="btn" type="submit">搜索</button>
    </form>
    <div class="tools">
      <a href="/favorites">♡ 收藏</a>
      <a :class="{ current: current('/chat') }" href="/chat">消息</a>
      <a v-if="user" class="me" href="/me">我的</a>
      <span v-else class="auth-links"><a href="/login">登录</a>/<a href="/register">注册</a></span>
    </div>
  </header>
</template>
