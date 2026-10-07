<script setup>
import UserBar from '../components/UserBar.vue'
import MiniIcon from '../components/MiniIcon.vue'

const items = [
  { q: '怎么确认对方是同学？', a: '卖家卡片会显示昵称和信用分。建议约在图书馆、宿舍楼下等校内地点见面，当面核对商品。' },
  { q: '怎么交易更安全？', a: '先在商品页私信约定时间和地点，当面看货后再付款。订单需要卖家接受，买家确认完成后才结束。' },
  { q: '怎么发布闲置？', a: '登录后进入「发布闲置」，写清标题、价格、成色和面交地点，并上传一张实拍图。' },
  { q: '不想卖了怎么办？', a: '在发布页可以把商品下架。订单还在待确认或待交易时，买卖双方都可以取消。' }
]
</script>

<template>
  <div class="shell">
    <UserBar />
    <main class="stage">
      <header class="stage-head">
        <i class="spot-icon"><MiniIcon name="help" /></i>
        <div>
          <p class="kicker">先看明白</p>
          <h1>帮助中心</h1>
          <p>发布、私信、见面和取消，按这四步走就不会乱。</p>
        </div>
      </header>
      <section class="help-grid">
        <article v-for="(item, index) in items" :key="item.q" class="deal help-card">
          <em>0{{ index + 1 }}</em>
          <h3>{{ item.q }}</h3>
          <p>{{ item.a }}</p>
        </article>
      </section>
    </main>
  </div>
</template>
