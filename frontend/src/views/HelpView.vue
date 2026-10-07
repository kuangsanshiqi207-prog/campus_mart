<script setup>
import UserBar from '../components/UserBar.vue'

const items = [
  { q: '怎么确认对方是同学？', a: '卖家主页会显示校园认证和信用分。建议约在图书馆、宿舍楼下等校内地点见面。' },
  { q: '怎么交易更安全？', a: '先在商品页私信约定时间和地点，当面看货后再付款。订单需要卖家接受、买家确认完成。' },
  { q: '怎么发布闲置？', a: '登录后点「发布闲置」，填写标题、价格、分类，并上传一张实拍图。' },
  { q: '不想卖了怎么办？', a: '在发布页可以下架。订单还在待确认时，买卖双方都可以取消。' }
]
</script>

<template>
  <div class="shell">
    <UserBar />
    <main class="shelf page-block">
      <div class="shelf-head">
        <h2>帮助中心 <span>交易前先看这几条</span></h2>
      </div>
      <article v-for="item in items" :key="item.q" class="deal help-card">
        <h3>{{ item.q }}</h3>
        <p>{{ item.a }}</p>
      </article>
    </main>
  </div>
</template>
