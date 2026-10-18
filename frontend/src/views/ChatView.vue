<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import UserBar from '../components/UserBar.vue'
import MiniIcon from '../components/MiniIcon.vue'
import { getUser, request } from '../api'

const route = useRoute()
const conversations = ref([])
const messages = ref([])
const current = ref(null)
const draft = ref('')
const error = ref('')
const me = getUser()

async function loadConversations() {
  conversations.value = await request('/user/chat/conversations')
}

async function openConversation(item) {
  error.value = ''
  current.value = item
  messages.value = await request(`/user/chat/conversations/${item.id}/messages?limit=50`)
  await request(`/user/chat/conversations/${item.id}/read`, { method: 'PUT' })
}

async function send() {
  if (!current.value || !draft.value.trim()) return
  error.value = ''
  try {
    const message = await request(`/user/chat/conversations/${current.value.id}/messages`, {
      method: 'POST',
      body: JSON.stringify({ content: draft.value.trim(), type: 'text' })
    })
    messages.value = [...messages.value, message]
    draft.value = ''
    await loadConversations()
  } catch (err) {
    error.value = err.message
  }
}

onMounted(async () => {
  try {
    await loadConversations()
    const found = route.query.id
      ? conversations.value.find((item) => item.id === route.query.id)
      : conversations.value[0]
    if (found) await openConversation(found)
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
        <i class="spot-icon"><MiniIcon name="chat" /></i>
        <div>
          <p class="kicker">同学之间</p>
          <h1>私信</h1>
          <p>问成色、约时间和见面地点，都在这里说清楚。</p>
        </div>
      </header>
      <p v-if="error" class="form-error">{{ error }}</p>
      <section class="split chat-shell">
        <aside class="panel">
          <h2>会话</h2>
          <p v-if="conversations.length === 0" class="blank">还没有私信。打开一件商品，点「私信」即可开始。</p>
          <div v-else class="chat-list">
            <button
              v-for="item in conversations"
              :key="item.id"
              class="row button-row"
              :class="{ active: current && current.id === item.id }"
              type="button"
              @click="openConversation(item)"
            >
              <span class="chat-person">
                <i class="avatar lg">{{ (item.targetNickname || '同').slice(0, 1) }}</i>
                <span>
                  <b>{{ item.targetNickname || '同学' }}</b>
                  <span>{{ item.lastMessage || '还没有消息' }}</span>
                </span>
              </span>
            </button>
          </div>
        </aside>
        <section class="panel" v-if="current">
          <h2>{{ current.targetNickname || '同学' }}</h2>
          <div class="thread">
            <p v-for="item in messages" :key="item.id" :class="{ mine: me && String(item.fromUserId) === String(me.userId) }">
              {{ item.content }}
            </p>
          </div>
          <form class="composer" @submit.prevent="send">
            <input v-model="draft" placeholder="写一条消息，比如明天下午图书馆门口" />
            <button class="btn" type="submit">发送</button>
          </form>
        </section>
        <section v-else class="panel">
          <p class="blank">选左边的一位同学，开始聊天。</p>
        </section>
      </section>
    </main>
  </div>
</template>
