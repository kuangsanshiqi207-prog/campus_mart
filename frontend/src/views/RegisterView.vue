<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '../api'
import CampusPanel from '../components/CampusPanel.vue'

const router = useRouter()
const form = ref({ username: '', password: '', phone: '', code: '' })
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
  <main class="gate">
    <CampusPanel
      title="先成为这里的同学。"
      lead="注册后就能发布闲置、收藏心仪的旧物，并和卖家约时间面交。验证码会直接显示在表单里。"
    />
    <section class="form-wrap">
      <form class="form" @submit.prevent="submit">
        <p class="eyebrow">新同学</p>
        <h2>注册账号</h2>
        <label class="field"><span>用户名</span><input v-model="form.username" /></label>
        <label class="field"><span>密码</span><input v-model="form.password" type="password" /></label>
        <label class="field"><span>手机号</span><input v-model="form.phone" /></label>
        <label class="field">
          <span>验证码</span>
          <input v-model="form.code" />
        </label>
        <button class="btn ghost" type="button" @click="sendCode">获取验证码</button>
        <p v-if="sentCode" class="hint">本次验证码 {{ sentCode }}</p>
        <p class="form-error">{{ error }}</p>
        <button class="btn" type="submit" :disabled="loading">{{ loading ? '提交中' : '注册并去登录' }}</button>
        <p class="hint"><a href="/login">已有账号</a></p>
      </form>
    </section>
  </main>
</template>
