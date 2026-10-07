<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { request, saveSession } from '../api'
import CampusPanel from '../components/CampusPanel.vue'

const router = useRouter()
const username = ref('林同学')
const password = ref('123456')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const data = await request('/user/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username: username.value, password: password.value })
    })
    saveSession(data)
    router.push('/market')
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="gate">
    <CampusPanel />
    <section class="form-wrap">
      <form class="form" @submit.prevent="submit">
        <p class="eyebrow">同学登录</p>
        <h2>进入二手集市</h2>
        <p class="hint">试用账号已经填好，密码是 123456</p>
        <label class="field">
          <span>用户名</span>
          <input v-model="username" autocomplete="username" />
        </label>
        <label class="field">
          <span>密码</span>
          <input v-model="password" type="password" autocomplete="current-password" />
        </label>
        <p class="form-error">{{ error }}</p>
        <button class="btn" type="submit" :disabled="loading">{{ loading ? '登录中' : '进入集市' }}</button>
        <p class="hint"><a href="/register">注册新账号</a></p>
      </form>
    </section>
  </main>
</template>
