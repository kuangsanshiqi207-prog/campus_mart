<script setup>
import { onMounted, ref } from 'vue'
import UserBar from '../components/UserBar.vue'
import MiniIcon from '../components/MiniIcon.vue'
import { request, uploadFile } from '../api'

const categories = ref([])
const products = ref([])
const error = ref('')
const message = ref('')
const form = ref({
  title: '',
  description: '',
  price: '',
  originalPrice: '',
  quality: 'good',
  categoryId: '',
  tradeType: 'face',
  tradePlace: ''
})
const file = ref(null)
const statusText = {
  on_sale: '在售',
  offline: '已下架',
  reserved: '已预订',
  sold: '已售出',
  pending: '待审核',
  approved: '已通过',
  rejected: '已拒绝'
}

async function load() {
  categories.value = await request('/user/market/categories')
  if (!form.value.categoryId && categories.value[0]) form.value.categoryId = categories.value[0].id
  const page = await request('/user/shop/products?pageNum=1&pageSize=20')
  products.value = page.records || []
}

async function publish() {
  error.value = ''
  message.value = ''
  if (!file.value) {
    error.value = '请先选一张图片'
    return
  }
  try {
    const uploaded = await uploadFile(file.value)
    await request('/user/shop/products', {
      method: 'POST',
      body: JSON.stringify({
        ...form.value,
        price: Number(form.value.price),
        originalPrice: form.value.originalPrice ? Number(form.value.originalPrice) : null,
        categoryId: form.value.categoryId,
        imageFileIds: [uploaded.fileId]
      })
    })
    message.value = '已发布，刷新集市就能看到'
    form.value.title = ''
    form.value.description = ''
    await load()
  } catch (err) {
    error.value = err.message
  }
}

async function toggle(item) {
  const action = item.status === 'offline' ? 'online' : 'offline'
  await request(`/user/shop/products/${item.id}/${action}`, { method: 'PUT' })
  await load()
}

onMounted(async () => {
  try {
    await load()
  } catch (err) {
    error.value = err.message
  }
})
</script>

<template>
  <div class="shell">
    <UserBar />
    <main class="stage">
      <header class="stage-head">
        <i class="spot-icon"><MiniIcon name="tag" /></i>
        <div>
          <p class="kicker">挂到集市</p>
          <h1>发布闲置</h1>
          <p>一张实拍图、一个清楚的价格，同学才愿意点进来。</p>
        </div>
      </header>
      <div class="split">
      <form class="panel" @submit.prevent="publish">
        <h2>填写商品</h2>
        <label class="field"><span>标题</span><input v-model="form.title" placeholder="例如：九成新台灯，宿舍自提" required /></label>
        <label class="field"><span>描述</span><input v-model="form.description" placeholder="成色、配件、为什么出" /></label>
        <div class="form-grid">
          <label class="field"><span>售价</span><input v-model="form.price" type="number" min="0.01" step="0.01" required /></label>
          <label class="field"><span>原价</span><input v-model="form.originalPrice" type="number" min="0" step="0.01" /></label>
          <label class="field">
            <span>分类</span>
            <select v-model="form.categoryId">
              <option v-for="item in categories" :key="item.id" :value="item.id">{{ item.name }}</option>
            </select>
          </label>
          <label class="field">
            <span>成色</span>
            <select v-model="form.quality">
              <option value="new">全新</option>
              <option value="almost_new">几乎全新</option>
              <option value="good">成色良好</option>
              <option value="normal">日常使用</option>
            </select>
          </label>
          <label class="field">
            <span>交易方式</span>
            <select v-model="form.tradeType">
              <option value="face">当面交易</option>
              <option value="self_pickup">自提</option>
              <option value="express">快递</option>
            </select>
          </label>
          <label class="field"><span>见面地点</span><input v-model="form.tradePlace" placeholder="图书馆门口 / 宿舍楼下" /></label>
        </div>
        <label class="field"><span>实拍图</span><input type="file" accept="image/*" @change="file = $event.target.files[0]" /></label>
        <p class="form-error">{{ error }}</p>
        <p v-if="message" class="hint">{{ message }}</p>
        <button class="btn" type="submit">发布到集市</button>
      </form>
      <section>
        <h2 class="panel-title">已经发布的</h2>
        <p v-if="products.length === 0" class="blank">还没有发布过。左边填好就能挂上。</p>
        <article v-for="item in products" :key="item.id" class="row shop-item">
          <img v-if="item.cover" :src="item.cover" :alt="item.title" />
          <span v-else class="ph"></span>
          <div>
            <strong>{{ item.title }}</strong>
            <p class="meta"><span>¥{{ item.price }}</span><span class="pill">{{ statusText[item.status] || item.status }}</span></p>
          </div>
          <button class="btn ghost" type="button" @click="toggle(item)">{{ item.status === 'offline' ? '上架' : '下架' }}</button>
        </article>
      </section>
      </div>
    </main>
  </div>
</template>
