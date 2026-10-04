<script setup lang="ts">
import { Headphones, BookOpen, ArrowUpRight } from 'lucide-vue-next';
</script>
<template>
  <section class="home-intro">
    <span class="eyebrow">A LITTLE SPACE, JUST FOR YOU</span>
    <h1>让旋律流动，<br />让心事停留。</h1>
    <p>听见生活的温柔，记下此刻的自己。</p>
  </section>
  <div class="portals">
    <RouterLink to="/music" class="portal glass music-portal"
      ><div>
        <span class="eyebrow">01 / LISTEN</span>
        <h2>听见 · 音乐</h2>
        <p>给今天，选一首背景音乐。</p>
        <span class="portal-link">进入音乐世界 <ArrowUpRight :size="18" /></span>
      </div>
      <div class="record-art">
        <div class="record-label"><Headphones :size="32" /></div></div></RouterLink
    ><RouterLink to="/diary" class="portal glass diary-portal"
      ><div>
        <span class="eyebrow">02 / REMEMBER</span>
        <h2>随心 · 日记</h2>
        <p>那些微小而闪亮的瞬间，值得被记住。</p>
        <span class="portal-link">翻开你的故事 <ArrowUpRight :size="18" /></span>
      </div>
      <div class="notebook-art"><BookOpen :size="48" /><span>dear, today.</span></div></RouterLink
    >
  </div>
  <div class="home-footer">
    <span>音乐与文字之间，是属于你的片刻。</span><span>✦ TAKE YOUR TIME</span>
  </div>
</template>
