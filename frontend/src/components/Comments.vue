<script setup lang="ts">
import { ref, watch } from 'vue';
import { api, query } from '../api';
import { useAuth } from '../stores/auth';
import { useUi } from '../stores/ui';
import Pager from './Pager.vue';
const props = defineProps<{ kind: 'article' | 'song' | 'playlist'; id: number; items?: any[] }>();
const emit = defineEmits(['refresh']);
const auth = useAuth(),
  ui = useUi(),
  text = ref(''),
  rows = ref<any[]>([]),
  page = ref(1),
  total = ref(0),
  busy = ref(false);
async function load() {
  if (props.kind === 'article') {
    await ui.run(async () => {
      const data = await api(
        `/public/articles/${props.id}/comments?${query({ pageNum: page.value, pageSize: 10 })}`,
      );
      rows.value = data.list;
      total.value = data.total;
    });
  } else rows.value = props.items || [];
}
watch(
  () => [props.id, props.items],
  () => {
    page.value = 1;
    load();
  },
  { immediate: true },
);
async function send() {
  if (!auth.require() || busy.value) return;
  busy.value = true;
  await ui.run(async () => {
    if (!text.value.trim()) throw Error('先写下想说的话吧');
    await api(
      props.kind === 'article'
        ? `/my/article/${props.id}/comment`
        : `/music/comment/add${props.kind === 'song' ? 'Song' : 'Playlist'}Comment`,
      'POST',
      props.kind === 'article'
        ? { content: text.value }
        : { [props.kind + 'Id']: props.id, content: text.value },
    );
    text.value = '';
    await load();
    emit('refresh');
  }, '评论已发布');
  busy.value = false;
}
async function like(row: any, cancel = false) {
  if (!auth.require()) return;
  await ui.run(async () => {
    await api(
      props.kind === 'article'
        ? `/my/article/comments/${row.id}/like`
        : `/music/comment/${cancel ? 'cancelLikeComment' : 'likeComment'}/${row.commentId}`,
      props.kind === 'article' ? (row.liked ? 'DELETE' : 'POST') : 'PATCH',
    );
    await load();
    emit('refresh');
  });
}
async function remove(row: any) {
  if (!(await ui.confirm('确定删除这条评论？'))) return;
  await ui.run(async () => {
    await api(
      props.kind === 'article'
        ? `/my/article/comments/${row.id}`
        : `/music/comment/deleteComment/${row.commentId}`,
      'DELETE',
    );
    await load();
    emit('refresh');
  }, '评论已删除');
}
</script>
<template>
  <section class="panel glass">
    <h2>留一点回声</h2>
    <form @submit.prevent="send">
      <textarea
        v-model="text"
        maxlength="255"
        placeholder="温柔地分享你的感受…"
        aria-label="评论内容"
      ></textarea>
      <div class="actions">
        <button class="primary" :disabled="busy">
          {{ auth.user ? '发布评论' : '登录后评论' }}
        </button>
      </div>
    </form>
    <p v-if="!rows.length" class="muted">还没有评论，来留下第一句问候吧。</p>
    <article v-for="row in rows" :key="row.id || row.commentId" class="comment">
      <div class="row">
        <strong>{{ row.nickname || row.username || '某位朋友' }}</strong
        ><small>{{ row.createTime?.replace('T', ' ') }}</small>
      </div>
      <p>{{ row.content }}</p>
      <div class="actions">
        <button @click="like(row)">
          {{ row.liked ? '取消点赞' : '点赞' }} · {{ row.likeCount || 0 }}</button
        ><button v-if="kind !== 'article' && auth.user" @click="like(row, true)">取消点赞</button
        ><button
          v-if="
            auth.admin ||
            (auth.user && (row.userId === auth.user.id || row.username === auth.user.username))
          "
          class="danger"
          @click="remove(row)"
        >
          删除
        </button>
      </div>
    </article>
    <Pager
      v-if="kind === 'article' && total > 10"
      :page="page"
      :total="total"
      :size="10"
      @change="
        page = $event;
        load();
      "
    />
  </section>
</template>
