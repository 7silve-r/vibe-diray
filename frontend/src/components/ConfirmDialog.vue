<script setup lang="ts">
import { ref } from 'vue';
import Modal from './Modal.vue';
import { useUi } from '../stores/ui';
const ui = useUi(),
  value = ref('');
</script>
<template>
  <Modal :title="ui.dialog?.input ? '写一个名字' : '请确认'" @close="ui.answer(false)"
    ><p style="line-height: 1.9; margin-top: 20px">{{ ui.dialog?.text }}</p>
    <form class="form" @submit.prevent="ui.answer(ui.dialog?.input ? value : true)">
      <input v-if="ui.dialog?.input" v-model="value" required maxlength="50" aria-label="名称" />
      <div class="actions">
        <button type="button" @click="ui.answer(false)">取消</button
        ><button class="primary">确认</button>
      </div>
    </form></Modal
  >
</template>
