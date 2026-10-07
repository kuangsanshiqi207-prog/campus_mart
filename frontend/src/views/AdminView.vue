<script setup>
import { onMounted, ref } from 'vue'
import { clearAdminSession, getAdminToken, request, saveAdminSession } from '../api'

const tabs = [
  { id: 'overview', label: '概览' },
  { id: 'users', label: '用户' },
  { id: 'products', label: '商品审核' },
  { id: 'categories', label: '分类' },
  { id: 'reviews', label: '评价' },
  { id: 'announcements', label: '公告' },
  { id: 'reports', label: '举报' }
]
const statusText = {
  normal: '正常',
  banned: '已封禁',
  pending: '待处理',
  approved: '已通过',
  rejected: '已拒绝',
  on_sale: '在售',
  offline: '已下架',
  reserved: '已预订',
  sold: '已售出',
  valid: '属实',
  invalid: '不属实',
  published: '已发布',
  draft: '草稿',
  product: '商品',
  user: '用户',
  order: '订单'
}
function label(value) {
  return statusText[value] || value
}
const tab = ref('overview')
const username = ref('管理员')
const password = ref('123456')
const authed = ref(!!getAdminToken())
const error = ref('')
const overview = ref(null)
const users = ref([])
const products = ref([])
const categories = ref([])
const reviews = ref([])
const announcements = ref([])
const reports = ref([])
const categoryName = ref('')
const notice = ref({ title: '', content: '' })

async function login() {
  error.value = ''
  try {
    const data = await request('/admin/auth/login', {
      method: 'POST',
      admin: true,
      body: JSON.stringify({ username: username.value, password: password.value })
    })
    saveAdminSession(data)
    authed.value = true
    await load()
  } catch (err) {
    error.value = err.message
  }
}

function logout() {
  clearAdminSession()
  authed.value = false
}

async function load() {
  error.value = ''
  try {
    overview.value = await request('/admin/stats/overview', { admin: true })
    users.value = (await request('/admin/users?pageNum=1&pageSize=20', { admin: true })).records || []
    products.value = (await request('/admin/products?pageNum=1&pageSize=20', { admin: true })).records || []
    categories.value = await request('/admin/categories', { admin: true })
    reviews.value = (await request('/admin/reviews?pageNum=1&pageSize=20', { admin: true })).records || []
    announcements.value = (await request('/admin/announcements?pageNum=1&pageSize=20', { admin: true })).records || []
    reports.value = (await request('/admin/reports?pageNum=1&pageSize=20', { admin: true })).records || []
  } catch (err) {
    error.value = err.message
  }
}

async function setUserStatus(user, status) {
  await request(`/admin/users/${user.id}/status`, {
    method: 'PUT',
    admin: true,
    body: JSON.stringify({ status, reason: status === 'banned' ? '违规' : '恢复' })
  })
  await load()
}

async function audit(product, result) {
  await request(`/admin/products/${product.id}/audit`, {
    method: 'PUT',
    admin: true,
    body: JSON.stringify({ result, reason: result === 'reject' ? '信息不完整' : '' })
  })
  await load()
}

async function addCategory() {
  if (!categoryName.value.trim()) return
  await request('/admin/categories', {
    method: 'POST',
    admin: true,
    body: JSON.stringify({ name: categoryName.value.trim(), parentId: 0, sort: 0, status: 1 })
  })
  categoryName.value = ''
  await load()
}

async function publishNotice() {
  await request('/admin/announcements', {
    method: 'POST',
    admin: true,
    body: JSON.stringify({ ...notice.value, status: 'published' })
  })
  notice.value = { title: '', content: '' }
  await load()
}

async function handleReport(item, result) {
  await request(`/admin/reports/${item.id}/handle`, {
    method: 'PUT',
    admin: true,
    body: JSON.stringify({ result, action: 'none', reason: result === 'valid' ? '情况属实' : '证据不足' })
  })
  await load()
}

async function deleteReview(item) {
  await request(`/admin/reviews/${item.id}`, { method: 'DELETE', admin: true })
  await load()
}

onMounted(() => {
  if (authed.value) load()
})
</script>

<template>
  <div class="shell">
    <header class="topbar">
      <a class="brand" href="/admin">管理后台</a>
      <a class="btn ghost" href="/market">回集市</a>
      <button v-if="authed" class="btn ghost" type="button" @click="logout">退出管理</button>
    </header>
    <main class="page" v-if="!authed">
      <form class="panel narrow" @submit.prevent="login">
        <h2>管理员登录</h2>
        <p class="hint">管理员账号已经填好，密码是 123456</p>
        <label class="field"><span>用户名</span><input v-model="username" /></label>
        <label class="field"><span>密码</span><input v-model="password" type="password" /></label>
        <p class="form-error">{{ error }}</p>
        <button class="btn" type="submit">登录</button>
      </form>
    </main>
    <main class="page" v-else>
      <div class="chips">
        <button v-for="item in tabs" :key="item.id" class="chip" :class="{ active: tab === item.id }" type="button" @click="tab = item.id">{{ item.label }}</button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <section v-if="tab === 'overview' && overview" class="stats">
        <article><strong>{{ overview.userTotal }}</strong><span>用户</span></article>
        <article><strong>{{ overview.productTotal }}</strong><span>商品</span></article>
        <article><strong>{{ overview.orderTotal }}</strong><span>订单</span></article>
        <article><strong>{{ overview.auditPending }}</strong><span>待审核</span></article>
        <article><strong>{{ overview.reportPending }}</strong><span>待处理举报</span></article>
      </section>
      <section v-if="tab === 'users'">
        <article v-for="item in users" :key="item.id" class="row">
          <div><strong>{{ item.nickname || item.username }}</strong><p class="meta"><span>{{ item.username }}</span><span>{{ label(item.status) }}</span><span>信用 {{ item.creditScore }}</span></p></div>
          <button class="btn ghost" type="button" @click="setUserStatus(item, item.status === 'banned' ? 'normal' : 'banned')">{{ item.status === 'banned' ? '解封' : '封禁' }}</button>
        </article>
      </section>
      <section v-if="tab === 'products'">
        <article v-for="item in products" :key="item.id" class="row">
          <div><strong>{{ item.title }}</strong><p class="meta"><span>{{ label(item.auditStatus || item.status) }}</span><span>¥{{ item.price }}</span></p></div>
          <div class="actions">
            <button class="btn" type="button" @click="audit(item, 'pass')">通过</button>
            <button class="btn ghost" type="button" @click="audit(item, 'reject')">拒绝</button>
          </div>
        </article>
      </section>
      <section v-if="tab === 'categories'">
        <form class="search" @submit.prevent="addCategory">
          <input v-model="categoryName" placeholder="新分类名称" />
          <button class="btn" type="submit">添加</button>
        </form>
        <article v-for="item in categories" :key="item.id" class="row"><strong>{{ item.name }}</strong></article>
      </section>
      <section v-if="tab === 'reviews'">
        <p v-if="reviews.length === 0" class="empty">没有评价</p>
        <article v-for="item in reviews" :key="item.id" class="row">
          <div><strong>{{ item.score }} 分</strong><p>{{ item.content }}</p></div>
          <button class="btn ghost" type="button" @click="deleteReview(item)">删除</button>
        </article>
      </section>
      <section v-if="tab === 'announcements'">
        <form class="panel" @submit.prevent="publishNotice">
          <label class="field"><span>标题</span><input v-model="notice.title" required /></label>
          <label class="field"><span>内容</span><input v-model="notice.content" required /></label>
          <button class="btn" type="submit">发布公告</button>
        </form>
        <article v-for="item in announcements" :key="item.id" class="row">
          <div><strong>{{ item.title }}</strong><p>{{ item.content }}</p></div>
        </article>
      </section>
      <section v-if="tab === 'reports'">
        <p v-if="reports.length === 0" class="empty">没有举报</p>
        <article v-for="item in reports" :key="item.id" class="row">
          <div><strong>{{ item.reason }}</strong><p class="meta"><span>{{ label(item.status) }}</span><span>{{ label(item.targetType) }}</span></p></div>
          <div class="actions">
            <button class="btn" type="button" @click="handleReport(item, 'valid')">属实</button>
            <button class="btn ghost" type="button" @click="handleReport(item, 'invalid')">不属实</button>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>
