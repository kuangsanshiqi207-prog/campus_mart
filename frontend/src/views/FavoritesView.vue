<script setup>
import { onMounted, ref } from 'vue'
import UserBar from '../components/UserBar.vue'
import { request } from '../api'

const products = ref([])
const error = ref('')

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
    <main class="page">
      <h2>收藏</h2>
      <p v-if="error" class="error">{{ error }}</p>
      <p v-else-if="products.length === 0" class="empty">还没有收藏</p>
      <section v-else class="grid">
        <article v-for="item in products" :key="item.id" class="card">
          <a :href="`/products/${item.id}`">
            <div class="cover">
              <img v-if="item.cover" :src="item.cover" :alt="item.title" />
            </div>
            <div class="card-body">
              <div class="price">¥{{ item.price }}</div>
              <div class="title">{{ item.title }}</div>
            </div>
          </a>
          <button class="btn ghost" type="button" @click="remove(item.id)">取消收藏</button>
        </article>
      </section>
    </main>
  </div>
</template>
