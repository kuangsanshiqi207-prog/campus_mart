<script setup>
import UserBar from '../components/UserBar.vue'
import MiniIcon from '../components/MiniIcon.vue'

const places = [
  { name: '图书馆门口', detail: '教材和文具常在这里面交，傍晚人最多，台阶上方便摊开检查。', icon: 'book', when: '傍晚' },
  { name: '宿舍楼下', detail: '台灯、收纳和生活用品适合约在楼下大厅，拿上楼也近。', icon: 'lamp', when: '晚饭后' },
  { name: '教学楼中庭', detail: '数码和服饰可以下课后面交，光线好，方便验货。', icon: 'building', when: '课后' },
  { name: '西区车棚', detail: '自行车等大件在车棚看车，现场试骑再决定要不要。', icon: 'bike', when: '白天' }
]
</script>

<template>
  <div class="shell">
    <UserBar />
    <main class="stage">
      <header class="stage-head">
        <i class="spot-icon"><MiniIcon name="pin" /></i>
        <div>
          <p class="kicker">校内见面</p>
          <h1>校园地图</h1>
          <p>二手交易默认当面交接。选一个你俩都顺路的地点，写进私信里。</p>
        </div>
      </header>
      <section class="map-grid">
        <article v-for="item in places" :key="item.name" class="deal place-card map-card">
          <i class="spot-icon"><MiniIcon :name="item.icon" /></i>
          <h3>{{ item.name }}</h3>
          <p>{{ item.detail }}</p>
          <span class="pill">适合{{ item.when }}</span>
        </article>
      </section>
    </main>
  </div>
</template>
