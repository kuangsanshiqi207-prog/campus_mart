<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import UserBar from '../components/UserBar.vue'
import CatIcon from '../components/CatIcon.vue'
import { request } from '../api'

const route = useRoute()
const categoryId = ref('')
const sort = ref('latest')
const categories = ref([])
const products = ref([])
const mosaic = ref([])
const error = ref('')
const toast = ref('')
const loading = ref(false)

const qualityLabel = {
  new: '全新',
  almost_new: '几乎全新',
  good: '成色良好',
  normal: '日常使用'
}
const sorts = [
  { id: 'latest', label: '最新发布' },
  { id: 'hot', label: '最多收藏' },
  { id: 'price_asc', label: '价格从低到高' },
  { id: 'price_desc', label: '价格从高到低' }
]
const tones = {
  全部分类: '#ff6a1a',
  数码电子: '#3b82f6',
  图书教材: '#22a06b',
  生活用品: '#f5b942',
  服饰鞋包: '#fb7185',
  运动户外: '#f97316',
  美妆个护: '#a855f7',
  家具家电: '#38bdf8',
  文具手工: '#f59e0b',
  乐器影音: '#ec4899',
  校园服务: '#16a34a'
}

function tone(name) {
  return tones[name] || '#ff6a1a'
}

function money(value) {
  const number = Number(value)
  if (Number.isNaN(number)) return value
  return number.toLocaleString('zh-CN')
}

function ago(value) {
  if (!value) return ''
  const time = new Date(String(value).replace(' ', 'T'))
  const hours = Math.round((Date.now() - time.getTime()) / 3600000)
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  return `${Math.floor(hours / 24)}天前`
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const params = new URLSearchParams({ pageNum: '1', pageSize: '12', sort: sort.value })
    const keyword = route.query.keyword
    if (keyword) params.set('keyword', keyword)
    if (categoryId.value) params.set('categoryId', categoryId.value)
    const page = await request(`/user/market/products?${params}`)
    products.value = page.records || []
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function fav(item) {
  toast.value = ''
  try {
    await request(`/user/market/favorites/${item.id}`, { method: 'POST' })
    item.favorited = true
    item.favoriteCount = Number(item.favoriteCount || 0) + 1
    toast.value = `已收藏「${item.title}」`
  } catch (err) {
    toast.value = err.message
  }
}

watch([categoryId, sort, () => route.query.keyword], load)
onMounted(async () => {
  try {
    categories.value = await request('/user/market/categories')
    const page = await request('/user/market/products?pageNum=1&pageSize=5&sort=hot')
    mosaic.value = page.records || []
  } catch (err) {
    error.value = err.message
  }
  await load()
})
</script>

<template>
  <div class="shell">
    <UserBar />
    <section class="hero-home">
      <div class="hero-copy">
        <p class="kicker">校园二手集市</p>
        <h1>让闲置，<br /><em>在校园里继续发光</em></h1>
        <p class="slogan">发现好物 · 轻松转卖 · 校园见面交易</p>
        <div class="hero-actions">
          <a class="btn" href="/shop"><span class="plus">+</span> 发布闲置 <span class="go">→</span></a>
          <a class="btn ghost" href="#goods">去逛逛 <span class="go">→</span></a>
        </div>
        <ul class="trust">
          <li><i>✓</i><div><b>真实同学</b><span>校园身份认证</span></div></li>
          <li><i>✓</i><div><b>线下见面</b><span>更安全更放心</span></div></li>
          <li><i>✓</i><div><b>循环再用</b><span>让好物继续发光</span></div></li>
        </ul>
      </div>
      <div class="mosaic-wrap">
        <p class="note-float">好物不闲置，校园更美好</p>
        <div class="mosaic">
          <a v-for="(item, i) in mosaic" :key="item.id" class="spot" :class="`spot-${i}`" :href="`/products/${item.id}`">
            <img v-if="item.cover" :src="item.cover" :alt="item.title" />
            <div class="price-tag">
              <b>¥{{ money(item.price) }}</b>
              <span>{{ item.title }}</span>
            </div>
          </a>
        </div>
      </div>
    </section>

    <nav id="categories" class="catbar">
      <button class="cat" :class="{ active: categoryId === '' }" type="button" @click="categoryId = ''">
        <i :style="{ background: tone('全部分类') }"><CatIcon name="全部分类" /></i>
        <span>全部分类</span>
      </button>
      <button
        v-for="item in categories"
        :key="item.id"
        class="cat"
        :class="{ active: categoryId === item.id }"
        type="button"
        @click="categoryId = item.id"
      >
        <i :style="{ background: tone(item.name) }"><CatIcon :name="item.name" /></i>
        <span>{{ item.name }}</span>
      </button>
    </nav>

    <section id="goods" class="shelf">
      <div class="shelf-head">
        <h2>校园热门 <span>同学们都在逛的好物</span></h2>
        <div class="sorts">
          <button v-for="item in sorts" :key="item.id" :class="{ active: sort === item.id }" type="button" @click="sort = item.id">{{ item.label }}</button>
        </div>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <p v-else-if="loading" class="empty">加载中</p>
      <p v-else-if="products.length === 0" class="empty">这个分类还没有在售商品</p>
      <section v-else class="grid">
        <article v-for="item in products" :key="item.id" class="deal">
          <a :href="`/products/${item.id}`">
            <div class="cover">
              <img v-if="item.cover" :src="item.cover" :alt="item.title" />
            </div>
            <div class="deal-body">
              <h3>{{ item.title }}</h3>
              <div class="price">¥{{ money(item.price) }}</div>
              <div class="tags">
                <span>{{ qualityLabel[item.quality] || '二手' }}</span>
                <span>{{ item.categoryName }}</span>
              </div>
              <div class="seller">
                <b><i class="avatar">{{ (item.sellerNickname || '同').slice(0, 1) }}</i>{{ item.sellerNickname || '同学' }} · {{ ago(item.createTime) }}</b>
                <button type="button" @click.prevent.stop="fav(item)">{{ item.favorited ? '已藏' : '收藏' }} {{ item.favoriteCount || 0 }}</button>
              </div>
            </div>
          </a>
        </article>
      </section>
    </section>
    <p v-if="toast" class="toast">{{ toast }}</p>
  </div>
</template>
