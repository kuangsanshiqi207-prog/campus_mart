<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { request, saveSession } from '../api'

const REMEMBER_KEY = 'campus-login-name'
const router = useRouter()
const username = ref('')
const password = ref('')
const remember = ref(false)
const showPassword = ref(false)
const error = ref('')
const loading = ref(false)
const usernameInput = ref(null)
const passwordInput = ref(null)

onMounted(() => {
  const saved = localStorage.getItem(REMEMBER_KEY)
  if (saved) {
    username.value = saved
    remember.value = true
  }
  nextTick(() => (saved ? passwordInput.value : usernameInput.value)?.focus())
})

async function submit() {
  error.value = ''
  username.value = username.value.trim()
  if (!username.value) {
    error.value = '请输入用户名'
    usernameInput.value?.focus()
    return
  }
  if (!password.value) {
    error.value = '请输入密码'
    passwordInput.value?.focus()
    return
  }
  loading.value = true
  try {
    const data = await request('/user/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username: username.value, password: password.value })
    })
    if (remember.value) localStorage.setItem(REMEMBER_KEY, username.value.trim())
    else localStorage.removeItem(REMEMBER_KEY)
    saveSession(data)
    router.push('/market')
  } catch (err) {
    error.value = err.message
    await nextTick()
    passwordInput.value?.focus()
  } finally {
    loading.value = false
  }
}
</script>

<template>
    <form class="auth-form" @submit.prevent="submit">
      <div class="auth-brandline"><span class="auth-brand-mark" aria-hidden="true">理</span><span>飞马市集</span><i></i><span class="auth-brand-note">校园闲置新去处</span></div>
      <h2>欢迎回来</h2>
      <p class="auth-lead">登录后继续逛飞马市集</p>
      <label class="auth-field">
        <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="3" /><path d="M6 19c1.2-3 3.2-4.5 6-4.5S16.8 16 18 19" /></svg>
        <input ref="usernameInput" v-model="username" autocomplete="username" placeholder="用户名" aria-label="用户名" />
      </label>
      <label class="auth-field">
        <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="6" y="10" width="12" height="9" rx="2" /><path d="M9 10V8a3 3 0 0 1 6 0v2" /></svg>
        <input ref="passwordInput" v-model="password" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" placeholder="密码" aria-label="密码" />
        <button class="eye" type="button" :aria-label="showPassword ? '隐藏密码' : '显示密码'" :aria-pressed="showPassword" @click="showPassword = !showPassword">
          <svg v-if="!showPassword" viewBox="0 0 24 24" aria-hidden="true"><path d="M2 12s4-6 10-6 10 6 10 6-4 6-10 6S2 12 2 12z" /><circle cx="12" cy="12" r="2.5" /></svg>
          <svg v-else viewBox="0 0 24 24" aria-hidden="true"><path d="M3 3l18 18" /><path d="M10 6.2A10 10 0 0 1 12 6c6 0 10 6 10 6a17 17 0 0 1-3.2 3.6" /><path d="M6.1 6.8C3.8 8.4 2 12 2 12s4 6 10 6c1.4 0 2.6-.3 3.7-.8" /></svg>
        </button>
      </label>
      <div class="auth-row">
        <label class="remember">
          <input v-model="remember" type="checkbox" />
          <i></i>
          记住登录
        </label>
        <router-link to="/reset">忘记密码？</router-link>
      </div>
      <p v-if="error" class="form-error auth-feedback" role="alert"><span aria-hidden="true">!</span>{{ error }}</p>
      <button class="auth-submit" type="submit" :disabled="loading" :aria-busy="loading"><span>{{ loading ? '正在登录' : '登录' }}</span><span class="auth-submit-arrow" aria-hidden="true">{{ loading ? '···' : '→' }}</span></button>
      <p class="auth-switch">还没有账号？<router-link to="/register">立即注册</router-link></p>
    </form>
</template>
