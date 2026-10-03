<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { Headphones, BookOpen, House, UserRound, ShieldCheck } from 'lucide-vue-next';
import { useAuth } from './stores/auth';
import { useUi } from './stores/ui';
import AuthDialog from './components/AuthDialog.vue';
import SmartImage from './components/SmartImage.vue';
import ConfirmDialog from './components/ConfirmDialog.vue';
import PlayerDock from './components/PlayerDock.vue';
const auth = useAuth(),
  ui = useUi();
const welcome = ref(!sessionStorage.getItem('vibe-entered'));
function enter() {
  welcome.value = false;
  sessionStorage.setItem('vibe-entered', 'yes');
}
function unauthorized() {
  auth.clear();
  ui.authOpen = true;
}
onMounted(() => {
  auth.init();
  window.addEventListener('vibe:unauthorized', unauthorized);
});
onBeforeUnmount(() => window.removeEventListener('vibe:unauthorized', unauthorized));
</script>
<template>
  <div class="ambient"></div>
  <div
    v-if="welcome"
    class="welcome"
    role="button"
    tabindex="0"
    aria-label="点击任意处开始"
    @click="enter"
    @keydown.enter="enter"
    @keydown.space.prevent="enter"
  >
    <div class="welcome-copy">
      <span class="eyebrow">MUSIC · WORDS · LITTLE WONDERS</span>
      <h1>Vibe<br /><em>Random Notes</em><span class="brand-dot">✦</span></h1>
      <p class="poem">“如此幸福的一天。”</p>
      <p class="poem-author">切斯瓦夫·米沃什 ·《礼物》节选</p>
    </div>
    <span class="start-hint">点击任意处开始 <span>／ PRESS ENTER</span></span
    ><span class="welcome-corner">随心记 · 留住每一份小小的美好</span>
  </div>
  <template v-else
    ><header class="topbar glass">
      <RouterLink to="/" class="brand"
        ><span class="brand-icon">✧</span
        ><span>Vibe Random Notes<small>随 心 记</small></span></RouterLink
      >
      <nav class="main-nav" aria-label="主导航">
        <RouterLink to="/"><House :size="18" /><span>主页</span></RouterLink
        ><RouterLink to="/music"><Headphones :size="18" /><span>音乐</span></RouterLink
        ><RouterLink to="/diary"><BookOpen :size="18" /><span>日记</span></RouterLink>
      </nav>
      <RouterLink v-if="auth.user" to="/account" class="account-pill"
        ><SmartImage :src="auth.user.userPic" /><span>{{
          auth.user.nickname || auth.user.username
        }}</span
        ><span v-if="auth.admin" class="badge"><ShieldCheck :size="13" />管理员</span></RouterLink
      ><button v-else class="account-pill" @click="ui.authOpen = true">
        <UserRound :size="17" />登录 / 注册
      </button>
    </header>
    <main class="workspace">
      <RouterView v-if="auth.ready" />
      <p v-else class="empty">正在准备你的空间…</p>
    </main>
    <PlayerDock />
    <footer class="site-footer">
      VIBE RANDOM NOTES <span>日子缓缓，心有所记。</span>
    </footer></template
  ><AuthDialog v-if="ui.authOpen" /><ConfirmDialog v-if="ui.dialog" />
  <div class="toasts" aria-live="polite">
    <div
      v-for="n in ui.notices"
      :key="n.id"
      class="toast glass"
      :class="{ error: n.error }"
      role="status"
    >
      {{ n.text
      }}<button aria-label="关闭提示" @click="ui.notices = ui.notices.filter((x) => x.id !== n.id)">
        ×
      </button>
    </div>
  </div>
</template>
