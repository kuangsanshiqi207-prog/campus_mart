<script setup>
import { onMounted, ref } from 'vue'
import UserBar from '../components/UserBar.vue'
import MiniIcon from '../components/MiniIcon.vue'
import { request } from '../api'

const products = ref([])
const error = ref('')

function money(value) {
  const number = Number(value)
  if (Number.isNaN(number)) return value
  return number.toLocaleString('zh-CN')
}

onMounted(async () => {
  try {
    const page = await request('/user/market/favorites?pageNum=1&pageSize=20')
    products.value = page.records || []
  } catch (err) {
    error.value = err.message
  }
})

async function remove(id) {
  await request(`/user/market/favorites/${id}`, { method: 'DELETE' })
  products.value = products.value.filter((item) => item.id !== id)
}
</script>

<template>
  <div class="shell">
    <UserBar />
    <main class="stage">
      <header class="stage-head">
        <i class="spot-icon"><MiniIcon name="heart" /></i>
        <div>
          <p class="kicker">先记下</p>
          <h1>我的收藏</h1>
          <p>看中的东西先放在这里，想要的时候再去私信卖家。</p>
        </div>
      </header>
      <p v-if="error" class="error">{{ error }}</p>
      <p v-else-if="products.length === 0" class="blank">还没有收藏。在集市卡片右下角点一下小心心。</p>
      <section v-else class="grid">
        <article v-for="item in products" :key="item.id" class="deal">
          <a :href="`/products/${item.id}`">
            <div class="cover">
              <img v-if="item.cover" :src="item.cover" :alt="item.title" />
            </div>
            <div class="deal-body">
              <h3>{{ item.title }}</h3>
              <div class="price">¥{{ money(item.price) }}</div>
              <div class="seller">
                <b>{{ item.sellerNickname || '同学' }}</b>
                <button type="button" @click.prevent.stop="remove(item.id)">取消收藏</button>
              </div>
            </div>
          </a>
        </article>
      </section>
    </main>
  </div>
</template>
