<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '../api'

const router = useRouter()
const form = ref({ username: '', password: '', phone: '', code: '' })
const showPassword = ref(false)
const sentCode = ref('')
const error = ref('')
const loading = ref(false)

async function sendCode() {
  error.value = ''
  sentCode.value = ''
  try {
    sentCode.value = await request('/user/auth/send-code', {
      method: 'POST',
      body: JSON.stringify({ phone: form.value.phone })
    })
    form.value.code = sentCode.value
  } catch (err) {
    error.value = err.message
  }
}

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await request('/user/auth/register', {
      method: 'POST',
      body: JSON.stringify(form.value)
    })
    router.push('/login')
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
    <form class="auth-form" @submit.prevent="submit">
      <h2>创建账号</h2>
      <p class="auth-lead">注册后就可以在飞马市集发布和收藏</p>
      <label class="auth-field">
        <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="3" /><path d="M6 19c1.2-3 3.2-4.5 6-4.5S16.8 16 18 19" /></svg>
        <input v-model="form.username" autocomplete="username" placeholder="用户名" />
      </label>
      <label class="auth-field">
        <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="6" y="10" width="12" height="9" rx="2" /><path d="M9 10V8a3 3 0 0 1 6 0v2" /></svg>
        <input v-model="form.password" :type="showPassword ? 'text' : 'password'" autocomplete="new-password" placeholder="密码" />
        <button class="eye" type="button" :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword">
          <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2 12s4-6 10-6 10 6 10 6-4 6-10 6S2 12 2 12z" /><circle cx="12" cy="12" r="2.5" /></svg>
        </button>
      </label>
      <label class="auth-field">
        <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="8" y="3" width="8" height="18" rx="2" /><path d="M11 18h2" /></svg>
        <input v-model="form.phone" inputmode="numeric" placeholder="手机号" />
      </label>
      <div class="auth-code">
        <label class="auth-field">
          <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="6" width="16" height="12" rx="2" /><path d="M8 12h8" /></svg>
          <input v-model="form.code" placeholder="验证码" />
        </label>
        <button class="code-btn" type="button" @click="sendCode">获取验证码</button>
      </div>
      <p v-if="sentCode" class="auth-lead">本次验证码是 {{ sentCode }}，已经填进输入框</p>
      <p class="form-error">{{ error }}</p>
      <button class="auth-submit" type="submit" :disabled="loading">{{ loading ? '提交中' : '注册' }}</button>
      <p class="auth-switch">已有账号？<router-link to="/login">去登录</router-link></p>
    </form>
</template>
