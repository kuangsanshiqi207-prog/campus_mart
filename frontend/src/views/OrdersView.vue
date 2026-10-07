<script setup>
import { onMounted, ref, watch } from 'vue'
import UserBar from '../components/UserBar.vue'
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
    <main class="page">
      <div class="chips">
        <button class="chip" :class="{ active: type === 'buy' }" type="button" @click="type = 'buy'">我买到的</button>
        <button class="chip" :class="{ active: type === 'sell' }" type="button" @click="type = 'sell'">我卖出的</button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <p v-else-if="orders.length === 0" class="empty">没有订单</p>
      <article v-for="item in orders" :key="item.id" class="row">
        <div>
          <strong>{{ item.productTitle }}</strong>
          <p class="meta">
            <span>¥{{ item.amount }}</span>
            <span>{{ statusLabel[item.status] || item.status }}</span>
            <span>{{ type === 'buy' ? item.sellerNickname : item.buyerNickname }}</span>
          </p>
          <p v-if="item.remark" class="hint">{{ item.remark }}</p>
        </div>
        <div class="actions">
          <button v-if="type === 'sell' && item.status === 'pending'" class="btn" type="button" @click="act(item.id, 'accept')">接受</button>
          <button v-if="type === 'sell' && item.status === 'pending'" class="btn ghost" type="button" @click="act(item.id, 'reject', { reason: '暂不出售' })">拒绝</button>
          <button v-if="type === 'buy' && item.status === 'accepted'" class="btn" type="button" @click="act(item.id, 'complete')">确认完成</button>
          <button v-if="item.status === 'pending' || item.status === 'accepted'" class="btn ghost" type="button" @click="act(item.id, 'cancel', { reason: '暂时不交易' })">取消</button>
        </div>
      </article>
      <p v-if="!me" class="hint">未登录时无法查看订单。</p>
    </main>
  </div>
</template>
