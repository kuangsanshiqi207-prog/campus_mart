<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '../api'

const router = useRouter()
const phone = ref('')
const code = ref('')
const newPassword = ref('')
const sentCode = ref('')
const error = ref('')
const loading = ref(false)

async function sendCode() {
  error.value = ''
  sentCode.value = ''
  try {
    sentCode.value = await request('/user/auth/send-code', {
      method: 'POST',
      body: JSON.stringify({ phone: phone.value })
    })
    code.value = sentCode.value
  } catch (err) {
    error.value = err.message
  }
}

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await request('/user/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify({ phone: phone.value, code: code.value, newPassword: newPassword.value })
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
      <h2>重置密码</h2>
      <p class="auth-lead">用注册时的手机号收取验证码</p>
      <label class="auth-field">
        <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="8" y="3" width="8" height="18" rx="2" /><path d="M11 18h2" /></svg>
        <input v-model="phone" inputmode="numeric" placeholder="手机号" />
      </label>
      <div class="auth-code">
        <label class="auth-field">
          <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="6" width="16" height="12" rx="2" /><path d="M8 12h8" /></svg>
          <input v-model="code" placeholder="验证码" />
        </label>
        <button class="code-btn" type="button" @click="sendCode">获取验证码</button>
      </div>
      <label class="auth-field">
        <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="6" y="10" width="12" height="9" rx="2" /><path d="M9 10V8a3 3 0 0 1 6 0v2" /></svg>
        <input v-model="newPassword" type="password" autocomplete="new-password" placeholder="新密码" />
      </label>
      <p v-if="sentCode" class="auth-lead">本次验证码是 {{ sentCode }}，已经填进输入框</p>
      <p class="form-error">{{ error }}</p>
      <button class="auth-submit" type="submit" :disabled="loading">{{ loading ? '提交中' : '确认重置' }}</button>
      <p class="auth-switch">想起密码了？<router-link to="/login">返回登录</router-link></p>
    </form>
</template>
