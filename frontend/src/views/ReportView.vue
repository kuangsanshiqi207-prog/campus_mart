<script setup>
import { onMounted, ref } from 'vue'
import UserBar from '../components/UserBar.vue'
import MiniIcon from '../components/MiniIcon.vue'
import { request } from '../api'

const reports = ref([])
const current = ref(null)
const error = ref('')
const message = ref('')
const appealReason = ref('')

const statusText = {
  pending: '待处理',
  valid: '属实',
  invalid: '不属实'
}
const targetText = {
  product: '商品',
  user: '用户',
  order: '订单'
}

function label(map, value) {
  return map[value] || value
}

async function load() {
  const page = await request('/user/reports?pageNum=1&pageSize=50')
  reports.value = page.records || []
}

async function open(item) {
  error.value = ''
  message.value = ''
  appealReason.value = ''
  current.value = await request(`/user/reports/${item.id}`)
}

async function appeal() {
  if (!current.value || !appealReason.value.trim()) return
  error.value = ''
  message.value = ''
  try {
    await request(`/user/reports/${current.value.id}/appeal`, {
      method: 'POST',
      body: JSON.stringify({ reason: appealReason.value.trim(), imageFileIds: [] })
    })
    message.value = '申诉已提交，等待平台处理'
    appealReason.value = ''
    await load()
    current.value = await request(`/user/reports/${current.value.id}`)
  } catch (err) {
    error.value = err.message
  }
}

onMounted(async () => {
  try {
    await load()
    if (reports.value[0]) await open(reports.value[0])
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
        <i class="spot-icon"><MiniIcon name="report" /></i>
        <div>
          <p class="kicker">维护交易环境</p>
          <h1>我的举报</h1>
          <p>举报违规商品和用户，跟踪处理进度，不满意还能申诉。</p>
        </div>
      </header>
      <p v-if="error" class="form-error">{{ error }}</p>
      <section class="split chat-shell">
        <aside class="panel">
          <h2>举报记录</h2>
          <p v-if="reports.length === 0" class="blank">还没有举报过。在商品页点「举报」即可提交。</p>
          <div v-else class="chat-list">
            <button
              v-for="item in reports"
              :key="item.id"
              class="row button-row"
              :class="{ active: current && current.id === item.id }"
              type="button"
              @click="open(item)"
            >
              <span>
                <b>{{ item.reason }}</b>
                <span class="meta"><span>{{ label(targetText, item.targetType) }}</span><span class="pill">{{ label(statusText, item.status) }}</span></span>
              </span>
            </button>
          </div>
        </aside>
        <section class="panel" v-if="current">
          <h2>举报详情</h2>
          <div class="report-detail">
            <p><strong>原因</strong>{{ current.reason }}</p>
            <p v-if="current.description"><strong>描述</strong>{{ current.description }}</p>
            <p><strong>对象</strong>{{ label(targetText, current.targetType) }} #{{ current.targetId }}</p>
            <p><strong>状态</strong><span class="pill">{{ label(statusText, current.status) }}</span></p>
            <p v-if="current.handleReason"><strong>处理说明</strong>{{ current.handleReason }}</p>
            <div v-if="current.images && current.images.length" class="report-images">
              <img v-for="url in current.images" :key="url" :src="url" alt="证据" />
            </div>
          </div>
          <form v-if="!current.hasAppeal && current.status !== 'pending'" class="appeal" @submit.prevent="appeal">
            <h3>申诉</h3>
            <label class="field"><span>申诉理由</span><textarea v-model="appealReason" rows="3" required /></label>
            <p v-if="message" class="hint">{{ message }}</p>
            <button class="btn" type="submit">提交申诉</button>
          </form>
          <p v-else-if="current.hasAppeal" class="hint">已申诉，请耐心等待平台复核。</p>
        </section>
        <section v-else class="panel">
          <p class="blank">选择一条举报查看详情。</p>
        </section>
      </section>
    </main>
  </div>
</template>

<style scoped>
.report-detail p { margin: 8px 0; display: flex; gap: 10px; }
.report-detail strong { color: var(--muted); font-weight: 500; min-width: 64px; }
.report-images { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 12px; }
.report-images img { width: 80px; height: 80px; object-fit: cover; border-radius: 10px; background: #f6efe8; }
.appeal { margin-top: 18px; padding-top: 16px; border-top: 1px solid var(--line); }
.appeal h3 { margin: 0 0 12px; }
.field textarea { resize: vertical; }
</style>
