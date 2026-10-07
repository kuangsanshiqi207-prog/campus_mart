<script setup>
defineProps({
  title: {
    type: String,
    default: '让闲置，在校园里继续发光。'
  },
  lead: {
    type: String,
    default: '这里只做校园二手。教材、耳机、自行车和宿舍用品，约在图书馆或楼下当面看货、当面成交。'
  }
})

const goods = [
  { name: '高等数学教材', price: '25', place: '图书馆门口', cover: '/photos/book.jpg' },
  { name: '通勤自行车', price: '280', place: '西区车棚', cover: '/photos/bike.jpg' },
  { name: '宿舍台灯', price: '18', place: '六号楼', cover: '/photos/lamp.jpg' },
  { name: '降噪耳机', price: '89', place: '教学楼中庭', cover: '/photos/headphones.jpg' }
]
</script>

<template>
  <section class="campus">
    <div>
      <p class="mark">校园集市 · 二手交易</p>
      <h1>{{ title }}</h1>
      <p class="lede">{{ lead }}</p>
    </div>
    <div class="stall" aria-label="正在转手的旧物">
      <article v-for="item in goods" :key="item.name" class="goods">
        <img :src="item.cover" :alt="item.name" />
        <div>
          <b>{{ item.name }}</b>
          <em>¥{{ item.price }}</em>
          <span>{{ item.place }}</span>
        </div>
      </article>
    </div>
    <ul class="notes">
      <li>同学之间转手</li>
      <li>当面验货再付款</li>
      <li>用完还可以再传下去</li>
    </ul>
  </section>
</template>
