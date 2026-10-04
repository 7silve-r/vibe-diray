<script setup lang="ts">
import CharacterIcon from './CharacterIcon.vue';
withDefaults(defineProps<{ mood?: 'loading' | 'error' | 'empty' | 'lost'; text?: string }>(), {
  mood: 'empty',
});
</script>
<template>
  <div class="empty state-panel" :aria-busy="mood === 'loading'">
    <CharacterIcon :mood="mood" />
    <p v-if="text" :role="mood === 'error' ? 'alert' : 'status'">{{ text }}</p>
    <slot />
  </div>
</template>
