<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, query } from '../api';
import { useAuth } from '../stores/auth';
import { useUi } from '../stores/ui';
import { excerpt } from '../utils/markdown';
import SmartImage from '../components/SmartImage.vue';
import Pager from '../components/Pager.vue';
import Modal from '../components/Modal.vue';
const auth = useAuth(),
  ui = useUi(),
  route = useRoute(),
  router = useRouter();
const tab = ref(route.query.tab === 'mine' ? 'mine' : 'discover'),
  rows = ref<any[]>([]),
  page = ref(1),
  total = ref(0),
  state = ref(''),
  cate = ref(''),
  categories = ref<any[]>([]),
  categoryOpen = ref(false),
  name = ref(''),
  alias = ref(''),
  editId = ref<number | null>(null),
  error = ref(''),
  loading = ref(false);
async function load() {
  loading.value = true;
  error.value = '';
  try {
    if (tab.value !== 'discover' && !auth.require()) tab.value = 'discover';
    const url =
      tab.value === 'discover'
        ? '/public/articles'
        : tab.value === 'mine'
          ? '/my/article/list'
          : '/my/article/favorites';
    const d = await api(
      url +
        '?' +
        query({ pageNum: page.value, pageSize: 12, state: state.value, cateId: cate.value }),
    );
    rows.value = d.list;
    total.value = d.total;
    if (auth.user) categories.value = await api('/my/cate');
  } catch (e) {
    error.value = (e as Error).message;
  } finally {
    loading.value = false;
  }
}
function write() {
  if (auth.require()) router.push('/diary/write');
}
async function saveCategory() {
  await ui.run(async () => {
    await api('/my/cate', editId.value ? 'PUT' : 'POST', {
      id: editId.value,
      cateName: name.value,
      cateAlias: alias.value,
    });
    name.value = '';
    alias.value = '';
    editId.value = null;
    await load();
  }, '分类已保存');
}
async function deleteCategory(id: number) {
  if (!(await ui.confirm('确定删除这个分类？有日记的分类需要先移走日记。'))) return;
  await ui.run(async () => {
    await api('/my/cate?id=' + id, 'DELETE');
    await load();
  }, '分类已删除');
}
watch(
  () => auth.user?.id,
  () => {
    page.value = 1;
    load();
  },
);
load();
</script>
<template>
  <header class="section-head">
    <div>
      <span class="eyebrow">SMALL MOMENTS, SOFTLY KEPT</span>
      <h1>随心 · 日记</h1>
      <p>今天的心情，也值得拥有一页。</p>
    </div>
    <button class="primary" @click="write">＋ 写日记</button>
  </header>
  <div class="tabs">
    <button
      v-for="[v, n] in [
        ['discover', '发现'],
        ['mine', '我的日记'],
        ['favorites', '我的收藏'],
      ]"
      :key="v"
      :class="{ active: tab === v }"
      @click="
        tab = v;
        page = 1;
        load();
      "
    >
      {{ n }}</button
    ><button v-if="auth.user" @click="categoryOpen = true">管理分类</button>
  </div>
  <div v-if="tab === 'mine'" class="toolbar">
    <select
      v-model="state"
      aria-label="日记可见性"
      @change="
        page = 1;
        load();
      "
    >
      <option value="">全部状态</option>
      <option>私有</option>
      <option>公开</option></select
    ><select
      v-model="cate"
      aria-label="日记分类"
      @change="
        page = 1;
        load();
      "
    >
      <option value="">全部分类</option>
      <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.cateName }}</option>
    </select>
  </div>
  <p v-if="loading" class="empty">正在翻开日记…</p>
  <div v-else-if="error" class="empty glass">
    <p role="alert">{{ error }}</p>
    <button @click="load">重新加载</button>
  </div>
  <div v-else-if="rows.length" class="grid">
    <RouterLink
      v-for="item in rows"
      :key="item.id"
      :to="'/diary/' + item.id + (tab === 'mine' ? '?own=1' : '')"
      class="card glass"
      ><SmartImage v-if="item.coverImg" class="cover wide" :src="item.coverImg" /><span
        class="eyebrow"
        style="margin-top: 15px"
        >{{ item.createTime?.slice(0, 10) }} · {{ item.state }}</span
      >
      <h3>{{ item.title }}</h3>
      <p>{{ excerpt(item.content) }}</p>
      <div class="row tiny muted">
        <span>{{ item.authorNickname || '某位朋友' }}</span
        ><span>{{ item.cateName || '随心记录' }}</span>
      </div></RouterLink
    >
  </div>
  <div v-else class="empty glass">
    <h2>留白，也是一种开始。</h2>
    <p>
      {{
        tab === 'mine' ? '写下今天发生的一件小事吧。' : '这里还没有故事，等一阵风，也等你的分享。'
      }}
    </p>
    <button @click="write">写下第一篇</button>
  </div>
  <Pager
    :page="page"
    :total="total"
    @change="
      page = $event;
      load();
    "
  /><Modal v-if="categoryOpen" title="整理你的日记分类" @close="categoryOpen = false"
    ><div v-for="c in categories" :key="c.id" class="row comment">
      <span>{{ c.cateName }}</span>
      <div>
        <button
          @click="
            editId = c.id;
            name = c.cateName;
            alias = c.cateAlias;
          "
        >
          编辑</button
        ><button class="danger" @click="deleteCategory(c.id)">删除</button>
      </div>
    </div>
    <form class="form" @submit.prevent="saveCategory">
      <label>分类名称<input v-model="name" required maxlength="50" /></label
      ><label>分类别名<input v-model="alias" required maxlength="50" /></label
      ><button class="primary">{{ editId ? '保存分类' : '新建分类' }}</button
      ><button
        v-if="editId"
        type="button"
        @click="
          editId = null;
          name = '';
          alias = '';
        "
      >
        取消编辑
      </button>
    </form></Modal
  >
</template>
