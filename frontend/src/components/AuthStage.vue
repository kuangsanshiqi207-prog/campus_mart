<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const tab = computed(() => (route.path.startsWith('/register') ? 'register' : 'login'))
</script>

<template>
  <main class="auth-gate">
    <section class="auth-hero">
      <img src="/photos/login-campus.png" alt="飞马市集，飞马广场" width="809" height="738" />
    </section>
    <section class="auth-side">
      <div class="auth-card">
        <nav class="auth-tabs" aria-label="登录或注册">
          <router-link to="/login" :class="{ on: tab === 'login' }" :aria-current="tab === 'login' ? 'page' : null">登录</router-link>
          <router-link to="/register" :class="{ on: tab === 'register' }" :aria-current="tab === 'register' ? 'page' : null">注册</router-link>
        </nav>
        <router-view />
      </div>
    </section>
  </main>
</template>
