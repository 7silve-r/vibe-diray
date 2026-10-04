<script setup lang="ts">
import StatePanel from '../components/StatePanel.vue';
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '../api';
import { useAuth } from '../stores/auth';
import { useUi } from '../stores/ui';
import { markdown } from '../utils/markdown';
import SmartImage from '../components/SmartImage.vue';
import Comments from '../components/Comments.vue';
const route = useRoute(),
  router = useRouter(),
  auth = useAuth(),
  ui = useUi(),
  article = ref<any>(null),
  error = ref('');
let ticket = 0;
async function load() {
  const t = ++ticket;
  error.value = '';
  article.value = null;
  try {
    let d = await api(
      route.query.own
        ? '/my/article/info?id=' + route.params.id
        : '/public/articles/' + route.params.id,
    );
    if (d.state === '公开' && route.query.own) d = await api('/public/articles/' + route.params.id);
    if (t === ticket) article.value = d;
  } catch (e) {
    if (t === ticket) error.value = (e as Error).message;
  }
}
async function interact(type: string) {
  if (!auth.require()) return;
  await ui.run(async () => {
    const flag = type === 'like' ? article.value.liked : article.value.collected;
    await api(`/my/article/${article.value.id}/${type}`, flag ? 'DELETE' : 'POST');
    await load();
  });
}
async function remove() {
  if (!(await ui.confirm('确定删除这篇日记？删除后无法恢复。'))) return;
  await ui.run(async () => {
    await api(
      article.value.createUser === auth.user?.id
        ? '/my/article?id=' + article.value.id
        : '/admin/diary/articles/' + article.value.id,
      'DELETE',
    );
    router.push('/diary');
  }, '日记已删除');
}
watch(() => [route.fullPath, auth.user?.id], load, { immediate: true });
</script>
<template>
  <div class="read-width">
    <RouterLink to="/diary" class="muted">← 返回日记</RouterLink>
    <StatePanel v-if="error" mood="error">
      <p role="alert">{{ error }}</p>
      <button @click="load">重试</button>
    </StatePanel>
    <article v-else-if="article" class="panel" style="margin-top: 25px">
      <span class="eyebrow"
        >{{ article.createTime?.slice(0, 10) }} / {{ article.state }} / {{ article.cateName }}</span
      >
      <h1>{{ article.title }}</h1>
      <p class="muted">{{ article.authorNickname }}</p>
      <SmartImage v-if="article.coverImg" class="cover wide" :src="article.coverImg" />
      <div class="markdown" v-html="markdown(article.content)"></div>
      <div class="actions">
        <template v-if="article.state === '公开'"
          ><button @click="interact('like')">
            {{ article.liked ? '取消点赞' : '点赞' }} · {{ article.likeCount || 0 }}</button
          ><button @click="interact('favorite')">
            {{ article.collected ? '取消收藏' : '收藏' }} · {{ article.favoriteCount || 0 }}
          </button></template
        ><RouterLink v-if="article.createUser === auth.user?.id" :to="'/diary/write/' + article.id"
          ><button>编辑日记</button></RouterLink
        ><button
          v-if="article.createUser === auth.user?.id || (auth.admin && article.state === '公开')"
          class="danger"
          @click="remove"
        >
          {{ article.createUser === auth.user?.id ? '删除' : '管理员删除' }}
        </button>
      </div>
    </article>
    <Comments v-if="article?.state === '公开'" kind="article" :id="article.id" />
  </div>
</template>
