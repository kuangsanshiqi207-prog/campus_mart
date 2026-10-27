<script setup>
import { ref } from 'vue'
import UserBar from '../components/UserBar.vue'
import MiniIcon from '../components/MiniIcon.vue'
import { request } from '../api'

const SESSION_KEY = 'campus-ai-session'
const messages = ref([])
const draft = ref('')
const error = ref('')
const loading = ref(false)

function sessionId() {
  let id = localStorage.getItem(SESSION_KEY)
  if (!id) {
    id = 'web-' + Date.now() + '-' + Math.random().toString(36).slice(2, 8)
    localStorage.setItem(SESSION_KEY, id)
  }
  return id
}

function push(role, content) {
  messages.value = [...messages.value, { role, content }]
}

async function send() {
  const text = draft.value.trim()
  if (!text || loading.value) return
  error.value = ''
  draft.value = ''
  push('user', text)
  loading.value = true
  try {
    const data = await request('/api/v1/customer-service/chat', {
      method: 'POST',
      body: JSON.stringify({ session_id: sessionId(), message: text, history: [] })
    })
    push('assistant', data.content || '（没有收到回复）')
  } catch (err) {
    error.value = err.message
    push('assistant', '抱歉，客服暂时走神了，请稍后再试。')
  } finally {
    loading.value = false
  }
}

function reset() {
  localStorage.removeItem(SESSION_KEY)
  messages.value = []
  error.value = ''
}
</script>

<template>
  <div class="shell">
    <UserBar />
    <main class="stage">
      <header class="stage-head">
        <i class="spot-icon"><MiniIcon name="service" /></i>
        <div>
          <p class="kicker">智能客服</p>
          <h1>AI 客服</h1>
          <p>买卖流程、发布规则、交易安全，随时问我，秒回。</p>
        </div>
      </header>
      <p v-if="error" class="form-error">{{ error }}</p>
      <section class="panel chat-shell">
        <div class="service-head">
          <i class="avatar lg">AI</i>
          <div>
            <b>校集小助手</b>
            <span>基于平台知识库回答，仅供参考</span>
          </div>
          <button class="btn ghost" type="button" @click="reset">清空对话</button>
        </div>
        <div class="thread">
          <p v-if="messages.length === 0" class="blank">你好，我是校集小助手，有什么可以帮你？</p>
          <p v-for="(item, index) in messages" :key="index" :class="{ mine: item.role === 'user' }">
            {{ item.content }}
          </p>
        </div>
        <form class="composer" @submit.prevent="send">
          <input v-model="draft" :disabled="loading" placeholder="输入你的问题，比如：怎么发布闲置？" />
          <button class="btn" type="submit" :disabled="loading">{{ loading ? '思考中…' : '发送' }}</button>
        </form>
      </section>
    </main>
  </div>
</template>

<style scoped>
.service-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.service-head b { display: block; font-size: 16px; }
.service-head span { color: var(--muted); font-size: 13px; }
.service-head .btn { margin-left: auto; }
.thread .blank { padding: 20px; box-shadow: none; }
</style>
