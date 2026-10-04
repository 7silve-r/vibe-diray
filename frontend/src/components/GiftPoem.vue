<script setup lang="ts">
const lines = [
  '如此幸福的一天。',
  '雾一早就散了，我在花园里干活。',
  '蜂鸟停在忍冬花上。',
  '这世上没有一样东西我想占有。',
  '我知道没有一个人值得我羡慕。',
  '任何我曾遭受的不幸，我都已忘记。',
  '想到故我今我同为一人并不使我难为情。',
  '在我身上没有痛苦。',
  '直起腰来，我望见蓝色的大海和帆影。',
];
</script>
<template>
  <aside class="gift-poem" aria-label="礼物，切斯瓦夫·米沃什">
    <h2>礼物</h2>
    <p v-for="line in lines" :key="line">{{ line }}</p>
    <cite>切斯瓦夫·米沃什</cite>
  </aside>
</template>
