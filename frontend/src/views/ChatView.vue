<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import UserBar from '../components/UserBar.vue'
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
    <main class="page split">
      <section class="panel">
        <h2>会话</h2>
        <p v-if="error" class="form-error">{{ error }}</p>
        <p v-else-if="conversations.length === 0" class="empty">还没有私信</p>
        <button
          v-for="item in conversations"
          :key="item.id"
          class="row button-row"
          type="button"
          @click="openConversation(item)"
        >
          <strong>{{ item.targetNickname || '同学' }}</strong>
          <span class="hint">{{ item.lastMessage }}</span>
        </button>
      </section>
      <section class="panel" v-if="current">
        <h2>{{ current.targetNickname || '会话' }}</h2>
        <div class="thread">
          <p v-for="item in messages" :key="item.id" :class="{ mine: me && String(item.fromUserId) === String(me.userId) }">
            {{ item.content }}
          </p>
        </div>
        <form class="search" @submit.prevent="send">
          <input v-model="draft" placeholder="写一条消息" />
          <button class="btn" type="submit">发送</button>
        </form>
      </section>
    </main>
  </div>
</template>
