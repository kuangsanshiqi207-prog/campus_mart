<script setup>
import { onMounted, onUnmounted, ref } from 'vue'

const showTop = ref(false)

function onScroll() {
  showTop.value = window.scrollY > 360
}

function toTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })
})

onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<template>
  <aside class="dock" aria-label="快捷操作">
    <a class="dock-btn" href="/shop">
      <span>发布</span>
      <small>上架商品</small>
    </a>
    <a class="dock-btn" href="/chat">
      <span>私信</span>
      <small>联系同学</small>
    </a>
    <a class="dock-btn" href="/orders">
      <span>订单</span>
      <small>交易进度</small>
    </a>
    <button v-show="showTop" class="dock-btn" type="button" @click="toTop">
      <span>顶部</span>
      <small>回到首屏</small>
    </button>
  </aside>
</template>
