<script setup lang="ts">
import { Headphones, BookOpen, ArrowUpRight } from 'lucide-vue-next';
import { useAuth } from '../stores/auth';
import { useUi } from '../stores/ui';
import CharacterIcon from '../components/CharacterIcon.vue';
const auth = useAuth(),
  ui = useUi();
</script>
<template>
  <section class="home-hero">
    <div class="hero-copy">
      <span class="eyebrow">春の音 · 随心记</span>
      <div class="hero-title glass">
        <h1>Vibe<br /><span>Random Notes</span></h1>
      </div>
      <p class="hero-verse">人闲桂花落，夜静春山空。</p>
      <a
        class="poetry-source"
        href="https://zh.wikisource.org/zh-hans/鳥鳴澗"
        target="_blank"
        rel="noopener noreferrer"
        >王维 ·《鸟鸣涧》</a
      >
      <div class="actions">
        <RouterLink to="/music" class="cta glass">进入音乐 <ArrowUpRight :size="17" /></RouterLink
        ><RouterLink to="/diary" class="text-link">浏览日记 <ArrowUpRight :size="15" /></RouterLink>
      </div>
    </div>
    <div class="hero-character">
      <div class="character-halo" aria-hidden="true"></div>
      <img
        src="/airi-cutout.png"
        alt="身穿水手服、手拿冰淇淋的爱理全身插画"
        fetchpriority="high"
      /><span class="character-spark" aria-hidden="true">✧</span>
    </div>
    <span class="hero-number" aria-hidden="true">01 — SPRING</span>
  </section>
  <section class="portal-section">
    <div class="section-kicker"><span>音 乐 与 日 记</span><span>02 / EXPLORE</span></div>
    <div class="portals">
      <RouterLink v-tilt to="/music" class="portal glass music-portal"
        ><div>
          <span class="eyebrow">LISTEN</span>
          <h2>听见 · 音乐</h2>
          <p>独坐幽篁里，弹琴复长啸。</p>
          <small>王维 ·《竹里馆》</small
          ><span class="portal-link">进入音乐世界 <ArrowUpRight :size="18" /></span>
        </div>
        <div class="record-art">
          <div class="record-label"><Headphones :size="32" /></div></div
      ></RouterLink>
      <RouterLink v-tilt to="/diary" class="portal glass diary-portal"
        ><div>
          <span class="eyebrow">REMEMBER</span>
          <h2>随心 · 日记</h2>
          <p>月出惊山鸟，时鸣春涧中。</p>
          <small>王维 ·《鸟鸣涧》</small
          ><span class="portal-link">翻开你的故事 <ArrowUpRight :size="18" /></span>
        </div>
        <div class="notebook-art"><BookOpen :size="40" /><span>日 記</span></div></RouterLink
      >
    </div>
  </section>
  <section class="home-profile glass">
    <CharacterIcon />
    <div>
      <span class="eyebrow">MY SPACE</span>
      <h2>{{ auth.user ? auth.user.nickname || auth.user.username : '我的空间' }}</h2>
      <p class="muted">个人资料 · 收藏 · 私有日记</p>
    </div>
    <RouterLink v-if="auth.user" class="cta" to="/account"
      >进入我的空间 <ArrowUpRight :size="16" /></RouterLink
    ><button v-else @click="ui.authOpen = true">登录账号 <ArrowUpRight :size="16" /></button>
  </section>
</template>
