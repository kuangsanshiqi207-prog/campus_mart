<script setup>
defineProps({
  name: { type: String, default: '全部分类' }
})
</script>

<template>
  <svg viewBox="0 0 24 24" aria-hidden="true">
    <template v-if="name === '全部分类'">
      <rect x="3" y="3" width="7" height="7" rx="1.5" />
      <rect x="14" y="3" width="7" height="7" rx="1.5" />
      <rect x="3" y="14" width="7" height="7" rx="1.5" />
      <rect x="14" y="14" width="7" height="7" rx="1.5" />
    </template>
    <template v-else-if="name === '数码电子'">
      <rect x="4" y="6" width="16" height="12" rx="2" />
      <circle cx="12" cy="12" r="3" />
    </template>
    <template v-else-if="name === '图书教材'">
      <path d="M5 5h10a3 3 0 0 1 3 3v11H8a3 3 0 0 0-3 3z" />
      <path d="M5 5v14" />
    </template>
    <template v-else-if="name === '生活用品'">
      <path d="M6 8h12l-1 12H7z" />
      <path d="M9 8V6a3 3 0 0 1 6 0v2" />
    </template>
    <template v-else-if="name === '服饰鞋包'">
      <path d="M8 6l2-2h4l2 2v14H8z" />
      <path d="M8 11h8" />
    </template>
    <template v-else-if="name === '运动户外'">
      <circle cx="12" cy="12" r="7" />
      <path d="M12 5v14M5 12h14" />
    </template>
    <template v-else-if="name === '美妆个护'">
      <rect x="8" y="3" width="8" height="5" rx="1" />
      <path d="M9 8h6l1 13H8z" />
    </template>
    <template v-else-if="name === '家具家电'">
      <path d="M4 11h16v7H4z" />
      <path d="M7 11V8h10v3" />
    </template>
    <template v-else-if="name === '文具手工'">
      <path d="M5 19l9-9 3 3-9 9H5z" />
      <path d="M13 7l3 3" />
    </template>
    <template v-else-if="name === '乐器影音'">
      <path d="M9 18V6l10-2v12" />
      <circle cx="7" cy="18" r="2" />
      <circle cx="17" cy="16" r="2" />
    </template>
    <template v-else>
      <path d="M4 10l8-6 8 6v8a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2z" />
      <path d="M10 20v-6h4v6" />
    </template>
  </svg>
</template>
