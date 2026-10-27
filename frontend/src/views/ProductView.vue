<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import UserBar from '../components/UserBar.vue'
import MiniIcon from '../components/MiniIcon.vue'
import { request } from '../api'

const route = useRoute()
const product = ref(null)
const error = ref('')
const message = ref('')
const remark = ref('')
const imageIndex = ref(0)
const reporting = ref(false)
const reportReason = ref('信息不实')
const reportDesc = ref('')

const reportReasons = ['信息不实', '违禁商品', '卖家欺诈', '图片与实物不符', '其他']

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

function submitReport() {
  return run(async () => {
    await request('/user/reports', {
      method: 'POST',
      body: JSON.stringify({
        targetType: 'product',
        targetId: product.value.id,
        reason: reportReason.value,
        description: reportDesc.value.trim() || undefined,
        imageFileIds: []
      })
    })
    reporting.value = false
    reportDesc.value = ''
  }, '举报已提交，平台会尽快处理')
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
        <a class="back" href="/market"><MiniIcon name="home" />返回集市</a>
        <div class="price">¥{{ product.price }}</div>
        <h1 class="title">{{ product.title }}</h1>
        <p>{{ product.description || '卖家还没有写更多说明，可以私信问成色和配件。' }}</p>
        <div class="fact-row">
          <span>{{ qualityLabel[product.quality] || '二手' }}</span>
          <span>{{ tradeLabel[product.tradeType] || '当面交易' }}</span>
          <span v-if="product.tradePlace">{{ product.tradePlace }}</span>
        </div>
        <div v-if="product.seller" class="seller-card">
          <i class="avatar lg">{{ (product.seller.nickname || '同').slice(0, 1) }}</i>
          <div>
            <b>{{ product.seller.nickname || '同学' }}</b>
            <span>信用 {{ product.seller.creditScore }}</span>
          </div>
        </div>
        <label class="field"><span>给卖家的留言</span><input v-model="remark" placeholder="什么时候方便面交" /></label>
        <p v-if="message" class="hint">{{ message }}</p>
        <p v-if="error" class="form-error">{{ error }}</p>
      </div>
    </article>
    <section v-if="reporting" class="report-panel">
      <form class="panel" @submit.prevent="submitReport">
        <h2>举报这件商品</h2>
        <label class="field">
          <span>举报原因</span>
          <select v-model="reportReason">
            <option v-for="r in reportReasons" :key="r" :value="r">{{ r }}</option>
          </select>
        </label>
        <label class="field">
          <span>详细描述（选填）</span>
          <textarea v-model="reportDesc" rows="3" placeholder="补充说明问题，帮助平台更快处理" />
        </label>
        <p class="form-error">{{ error }}</p>
        <div class="actions">
          <button class="btn" type="submit">提交举报</button>
          <button class="btn ghost" type="button" @click="reporting = false">取消</button>
        </div>
      </form>
    </section>
    <div v-if="product" class="buybar">
      <div>
        <strong>¥{{ product.price }}</strong>
        <span>{{ product.title }}</span>
      </div>
      <div class="actions">
        <button class="btn ghost" type="button" @click="favorite">{{ product.favorited ? '已收藏' : '收藏' }}</button>
        <button class="btn ghost" type="button" @click="reporting = !reporting">{{ reporting ? '收起举报' : '举报' }}</button>
        <button class="btn ghost" type="button" @click="talk">私信</button>
        <button class="btn" type="button" @click="order">我想要</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.report-panel {
  width: min(1080px, calc(100% - 64px));
  margin: -8px auto 100px;
}
.report-panel .field textarea { resize: vertical; }
</style>
