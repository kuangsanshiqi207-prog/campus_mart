<script setup>
import { onMounted, ref } from 'vue'
import UserBar from '../components/UserBar.vue'

const KEY = 'campus-wants'
const title = ref('')
const note = ref('')
const wants = ref([])

function load() {
  wants.value = JSON.parse(localStorage.getItem(KEY) || '[]')
}

function add() {
  if (!title.value.trim()) return
  wants.value = [{ title: title.value.trim(), note: note.value.trim(), time: '刚刚' }, ...wants.value]
  localStorage.setItem(KEY, JSON.stringify(wants.value))
  title.value = ''
  note.value = ''
}

onMounted(load)
</script>

<template>
  <div class="shell">
    <UserBar />
    <main class="shelf page-block">
      <div class="shelf-head">
        <h2>求购专区 <span>说说你在找什么</span></h2>
      </div>
      <form class="panel want-form" @submit.prevent="add">
        <label class="field"><span>想买什么</span><input v-model="title" placeholder="例如：高数下册、二手台灯" /></label>
        <label class="field"><span>补充说明</span><input v-model="note" placeholder="预算、成色、希望面交的地点" /></label>
        <button class="btn" type="submit">发布求购</button>
      </form>
      <p v-if="wants.length === 0" class="empty">还没有求购，做第一个发布的人</p>
      <article v-for="(item, index) in wants" :key="index" class="row">
        <div>
          <strong>{{ item.title }}</strong>
          <p class="hint">{{ item.note || '暂无补充' }} · {{ item.time }}</p>
        </div>
      </article>
    </main>
  </div>
</template>
