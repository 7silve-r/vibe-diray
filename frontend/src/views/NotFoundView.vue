<script setup lang="ts">
import StatePanel from '../components/StatePanel.vue';
</script>
<template>
  <StatePanel mood="lost" class="not-found"
    ><span class="eyebrow">404 / NOT FOUND</span>
    <h1>这个页面不存在</h1>
    <p>地址可能有误，或页面已经移走。</p>
    <RouterLink to="/" class="cta glass">返回主页</RouterLink></StatePanel
  >
</template>
