<script setup>
import { onMounted, ref, watch } from 'vue'
import UserBar from '../components/UserBar.vue'
import MiniIcon from '../components/MiniIcon.vue'
import { getUser, request } from '../api'

const type = ref('buy')
const orders = ref([])
const error = ref('')
const me = getUser()
const statusLabel = {
  pending: '待确认',
  accepted: '待交易',
  rejected: '已拒绝',
  cancelled: '已取消',
  completed: '已完成'
}

function tone(status) {
  if (status === 'completed') return 'done'
  if (status === 'rejected' || status === 'cancelled') return 'muted'
  return ''
}

async function load() {
  error.value = ''
  try {
    const page = await request(`/user/orders?type=${type.value}&pageNum=1&pageSize=20`)
    orders.value = page.records || []
  } catch (err) {
    error.value = err.message
  }
}

async function act(id, action, body) {
  await request(`/user/orders/${id}/${action}`, {
    method: 'PUT',
    body: body ? JSON.stringify(body) : '{}'
  })
  await load()
}

watch(type, load)
onMounted(load)
</script>

<template>
  <div class="shell">
    <UserBar />
    <main class="stage">
      <header class="stage-head">
        <i class="spot-icon"><MiniIcon name="box" /></i>
        <div>
          <p class="kicker">交易进度</p>
          <h1>我的订单</h1>
          <p>卖家接受之后约见面，当面看过再确认完成。</p>
        </div>
      </header>
      <div class="chips">
        <button class="chip" :class="{ active: type === 'buy' }" type="button" @click="type = 'buy'">我买到的</button>
        <button class="chip" :class="{ active: type === 'sell' }" type="button" @click="type = 'sell'">我卖出的</button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <p v-else-if="!me" class="blank">登录后才能看到订单。</p>
      <p v-else-if="orders.length === 0" class="blank">{{ type === 'buy' ? '还没有买过东西，去集市看看' : '还没有人向你下单' }}</p>
      <article v-for="item in orders" :key="item.id" class="row order-card">
        <div>
          <strong>{{ item.productTitle }}</strong>
          <span class="pill" :class="tone(item.status)">{{ statusLabel[item.status] || item.status }}</span>
          <p class="meta">
            <span>¥{{ item.amount }}</span>
            <span>{{ type === 'buy' ? `卖家 ${item.sellerNickname || '同学'}` : `买家 ${item.buyerNickname || '同学'}` }}</span>
          </p>
          <p v-if="item.remark" class="hint">留言：{{ item.remark }}</p>
        </div>
        <div class="actions">
          <button v-if="type === 'sell' && item.status === 'pending'" class="btn" type="button" @click="act(item.id, 'accept')">接受</button>
          <button v-if="type === 'sell' && item.status === 'pending'" class="btn ghost" type="button" @click="act(item.id, 'reject', { reason: '暂不出售' })">拒绝</button>
          <button v-if="type === 'buy' && item.status === 'accepted'" class="btn" type="button" @click="act(item.id, 'complete')">确认完成</button>
          <button v-if="item.status === 'pending' || item.status === 'accepted'" class="btn ghost" type="button" @click="act(item.id, 'cancel', { reason: '暂时不交易' })">取消</button>
        </div>
      </article>
    </main>
  </div>
</template>
