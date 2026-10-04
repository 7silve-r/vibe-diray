<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from 'vue';
import { media, token } from '../api';
const props = defineProps<{ src?: string; alt?: string }>();
const value = ref('');
let object = '';
let generation = 0;
watch(
  () => props.src,
  async (src) => {
    const current = ++generation;
    if (object) URL.revokeObjectURL(object);
    object = '';
    value.value = '';
    const url = media(src);
    if (!url) return;
    if (src?.startsWith('/uploads/')) {
      try {
        const r = await fetch(url, {
          headers: token() ? { Authorization: `Bearer ${token()}` } : {},
        });
        if (!r.ok) return;
        const blob = await r.blob();
        if (current !== generation) return;
        object = URL.createObjectURL(blob);
        value.value = object;
      } catch {}
    } else value.value = url;
  },
  { immediate: true },
);
onBeforeUnmount(() => {
  generation++;
  if (object) URL.revokeObjectURL(object);
});
</script>
<template>
  <img v-if="value" :src="value" :alt="alt || ''" loading="lazy" @error="value = ''" /><span
    v-else
    class="image-fallback"
    role="img"
    :aria-label="alt || '暂无图片'"
    >✧</span
  >
</template>
