<script setup lang="ts">
withDefaults(
  defineProps<{ mood?: 'profile' | 'loading' | 'success' | 'error' | 'empty' | 'lost' }>(),
  { mood: 'profile' },
);
</script>
<template><span class="character-icon" :class="'mood-' + mood" aria-hidden="true"></span></template>
