<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import UserBar from '../components/UserBar.vue'
import { request } from '../api'

const route = useRoute()
const product = ref(null)
const error = ref('')
const message = ref('')
const remark = ref('')
const imageIndex = ref(0)

const qualityLabel = {
  new: '全新',
  almost_new: '几乎全新',
  good: '成色良好',
  normal: '日常使用'
}
const tradeLabel = {
  face: '当面交易',
  express: '快递',
  self_pickup: '自提'
}

async function run(task, success) {
  error.value = ''
  message.value = ''
  try {
    await task()
    message.value = success
  } catch (err) {
    error.value = err.message
  }
}

function favorite() {
  return run(async () => {
    await request(`/user/market/favorites/${product.value.id}`, { method: 'POST' })
    product.value.favorited = true
  }, '已收藏')
}

function order() {
  return run(async () => {
    await request('/user/orders', {
      method: 'POST',
      body: JSON.stringify({
        productId: product.value.id,
        remark: remark.value,
        tradePlace: product.value.tradePlace
      })
    })
  }, '订单已提交，等卖家确认')
}

function talk() {
  return run(async () => {
    const conversation = await request('/user/chat/conversations', {
      method: 'POST',
      body: JSON.stringify({
        targetUserId: product.value.seller.id,
        productId: product.value.id
      })
    })
    location.href = `/chat?id=${conversation.id}`
  }, '')
}

onMounted(async () => {
  try {
    product.value = await request(`/user/market/products/${route.params.id}`)
  } catch (err) {
    error.value = err.message
  }
})
</script>

<template>
  <div class="shell has-buybar">
    <UserBar />
    <p v-if="error && !product" class="error">{{ error }}</p>
    <article v-else-if="product" class="detail">
      <div class="gallery">
        <div class="cover">
          <img v-if="product.images && product.images[imageIndex]" :src="product.images[imageIndex]" :alt="product.title" />
        </div>
        <div v-if="product.images && product.images.length > 1" class="thumbs">
          <button
            v-for="(url, i) in product.images"
            :key="url"
            type="button"
            :class="{ active: i === imageIndex }"
            @click="imageIndex = i"
          >
            <img :src="url" alt="" />
          </button>
        </div>
      </div>
      <div class="detail-body">
        <a class="back" href="/market">返回集市</a>
        <div class="price">¥{{ product.price }}</div>
        <h1 class="title">{{ product.title }}</h1>
        <p>{{ product.description }}</p>
        <p class="meta">
          <span>{{ qualityLabel[product.quality] || product.quality }}</span>
          <span>{{ tradeLabel[product.tradeType] || product.tradeType }} · {{ product.tradePlace }}</span>
        </p>
        <p class="meta"><span>{{ product.seller && product.seller.nickname }}</span><span v-if="product.seller">信用 {{ product.seller.creditScore }}</span></p>
        <label class="field"><span>给卖家的留言</span><input v-model="remark" placeholder="什么时候方便面交" /></label>
        <p v-if="message" class="hint">{{ message }}</p>
        <p v-if="error" class="form-error">{{ error }}</p>
      </div>
    </article>
    <div v-if="product" class="buybar">
      <div>
        <strong>¥{{ product.price }}</strong>
        <span>{{ product.title }}</span>
      </div>
      <div class="actions">
        <button class="btn ghost" type="button" @click="favorite">{{ product.favorited ? '已收藏' : '收藏' }}</button>
        <button class="btn ghost" type="button" @click="talk">私信</button>
        <button class="btn" type="button" @click="order">我想要</button>
      </div>
    </div>
  </div>
</template>
