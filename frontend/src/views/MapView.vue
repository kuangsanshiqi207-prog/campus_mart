<script setup>
import UserBar from '../components/UserBar.vue'

const places = [
  { name: '图书馆门口', detail: '教材和文具常在这里面交，傍晚人最多。' },
  { name: '宿舍楼下', detail: '台灯、收纳和生活用品适合约在楼下大厅。' },
  { name: '教学楼中庭', detail: '数码和服饰可以下课后面交，光线好，方便验货。' },
  { name: '西区车棚', detail: '自行车等大件在车棚看车，现场试骑。' }
]
</script>

<template>
  <div class="shell">
    <UserBar />
    <main class="shelf page-block">
      <div class="shelf-head">
        <h2>校园地图 <span>约在这些地方见面交易</span></h2>
      </div>
      <section class="grid">
        <article v-for="item in places" :key="item.name" class="deal place-card">
          <h3>{{ item.name }}</h3>
          <p>{{ item.detail }}</p>
        </article>
      </section>
    </main>
  </div>
</template>
