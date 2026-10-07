<script setup>
defineProps({
  name: { type: String, default: 'spark' }
})
</script>

<template>
  <svg class="glyph" viewBox="0 0 24 24" aria-hidden="true">
    <template v-if="name === 'spark'">
      <path d="M12 3l1.4 4.6L18 9l-4.6 1.4L12 15l-1.4-4.6L6 9l4.6-1.4z" />
      <path d="M18 14l.7 2.3L21 17l-2.3.7L18 20l-.7-2.3L15 17l2.3-.7z" />
    </template>
    <template v-else-if="name === 'home'">
      <path d="M4 11l8-7 8 7" />
      <path d="M6 10v9h12v-9" />
    </template>
    <template v-else-if="name === 'grid'">
      <rect x="4" y="4" width="6" height="6" rx="1.5" />
      <rect x="14" y="4" width="6" height="6" rx="1.5" />
      <rect x="4" y="14" width="6" height="6" rx="1.5" />
      <rect x="14" y="14" width="6" height="6" rx="1.5" />
    </template>
    <template v-else-if="name === 'pin'">
      <path d="M12 21s7-6.2 7-11a7 7 0 1 0-14 0c0 4.8 7 11 7 11z" />
      <circle cx="12" cy="10" r="2.2" />
    </template>
    <template v-else-if="name === 'want'">
      <path d="M4 7h16v11H8l-4 3z" />
      <path d="M8 11h8M8 14h5" />
    </template>
    <template v-else-if="name === 'help'">
      <circle cx="12" cy="12" r="8" />
      <path d="M9.5 9.5a2.5 2.5 0 1 1 3.4 2.3c-.7.4-1.4 1-1.4 1.9" />
      <path d="M12 17h.01" />
    </template>
    <template v-else-if="name === 'search'">
      <circle cx="11" cy="11" r="6" />
      <path d="M16 16l4 4" />
    </template>
    <template v-else-if="name === 'heart'">
      <path d="M12 19s-7-4.4-7-9a4 4 0 0 1 7-2 4 4 0 0 1 7 2c0 4.6-7 9-7 9z" />
    </template>
    <template v-else-if="name === 'chat'">
      <path d="M5 6h14v9H8l-3 3z" />
    </template>
    <template v-else-if="name === 'user'">
      <circle cx="12" cy="8" r="3" />
      <path d="M6 19c1.2-3 3.2-4.5 6-4.5S16.8 16 18 19" />
    </template>
    <template v-else-if="name === 'badge'">
      <circle cx="12" cy="9" r="3" />
      <path d="M7 20l5-3 5 3v-6a5 5 0 0 0-10 0z" />
    </template>
    <template v-else-if="name === 'meet'">
      <circle cx="8" cy="8" r="2.2" />
      <circle cx="16" cy="8" r="2.2" />
      <path d="M4 17c.8-2.2 2.2-3.2 4-3.2s3.2 1 4 3.2" />
      <path d="M12 17c.8-2.2 2.2-3.2 4-3.2s3.2 1 4 3.2" />
    </template>
    <template v-else-if="name === 'leaf'">
      <path d="M5 19C5 11 11 5 20 4 19 13 13 19 5 19z" />
      <path d="M9 15c2-2 4-4 7-6" />
    </template>
    <template v-else-if="name === 'box'">
      <path d="M4 8l8-4 8 4-8 4z" />
      <path d="M4 8v8l8 4 8-4V8" />
      <path d="M12 12v8" />
    </template>
    <template v-else-if="name === 'tag'">
      <path d="M4 12V5h7l9 9-7 7z" />
      <circle cx="8.5" cy="8.5" r="1" />
    </template>
    <template v-else-if="name === 'shield'">
      <path d="M12 3l7 3v6c0 4.5-3 7-7 9-4-2-7-4.5-7-9V6z" />
      <path d="M9 12l2 2 4-4" />
    </template>
    <template v-else-if="name === 'door'">
      <path d="M6 4h9a2 2 0 0 1 2 2v14H6z" />
      <path d="M9 12h.01" />
    </template>
    <template v-else-if="name === 'book'">
      <path d="M5 5h9a3 3 0 0 1 3 3v11H8a3 3 0 0 0-3 3z" />
      <path d="M5 5v14" />
    </template>
    <template v-else-if="name === 'lamp'">
      <path d="M9 10a3 3 0 1 1 6 0c0 2-2 2.5-2 4h-2c0-1.5-2-2-2-4z" />
      <path d="M10 17h4M11 17v2h2" />
    </template>
    <template v-else-if="name === 'building'">
      <path d="M5 20V6l7-3 7 3v14" />
      <path d="M9 20v-4h6v4M9 9h.01M12 9h.01M15 9h.01M9 13h.01M12 13h.01M15 13h.01" />
    </template>
    <template v-else-if="name === 'bike'">
      <circle cx="7" cy="16" r="3" />
      <circle cx="17" cy="16" r="3" />
      <path d="M7 16l4-7h4l2 4M11 9l-2 7h8" />
    </template>
  </svg>
</template>
