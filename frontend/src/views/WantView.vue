<script setup>
import { onMounted, ref } from 'vue'
import UserBar from '../components/UserBar.vue'
import MiniIcon from '../components/MiniIcon.vue'

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
    <main class="stage">
      <header class="stage-head">
        <i class="spot-icon"><MiniIcon name="want" /></i>
        <div>
          <p class="kicker">集市上没有的</p>
          <h1>求购专区</h1>
          <p>把想买的东西写下来。这条记录保存在这台浏览器里，方便你自己回头看。</p>
        </div>
      </header>
      <form class="panel want-form" @submit.prevent="add">
        <div class="form-grid">
          <label class="field"><span>想买什么</span><input v-model="title" placeholder="例如：高数下册、二手台灯" /></label>
          <label class="field"><span>补充说明</span><input v-model="note" placeholder="预算、成色、希望面交的地点" /></label>
        </div>
        <button class="btn" type="submit">发布求购</button>
      </form>
      <p v-if="wants.length === 0" class="blank">还没有求购。先写下你正在找的那一件。</p>
      <article v-for="(item, index) in wants" :key="index" class="row">
        <div>
          <strong><MiniIcon name="tag" />{{ item.title }}</strong>
          <p class="hint">{{ item.note || '暂无补充' }} · {{ item.time }}</p>
        </div>
      </article>
    </main>
  </div>
</template>
