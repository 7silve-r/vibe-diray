<script setup lang="ts">
defineProps<{ page: number; total: number; size?: number }>();
const emit = defineEmits(['change']);
</script>
<template>
  <nav class="pager" aria-label="分页">
    <button :disabled="page <= 1" @click="emit('change', page - 1)">上一页</button
    ><span>{{ page }} / {{ Math.max(1, Math.ceil(total / (size || 12))) }}</span
    ><button :disabled="page * (size || 12) >= total" @click="emit('change', page + 1)">
      下一页
    </button>
  </nav>
</template>
