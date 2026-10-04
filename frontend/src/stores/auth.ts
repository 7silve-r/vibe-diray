import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { api, token } from '../api';
import { useUi } from './ui';
export const useAuth = defineStore('auth', () => {
  const user = ref<any>(null);
  const ready = ref(false);
  const admin = computed(() => user.value?.role === 'ADMIN');
  async function refresh() {
    user.value = await api('/my/userinfo');
  }
  function clear() {
    sessionStorage.removeItem('vibe-token');
    user.value = null;
  }
  async function init() {
    if (token()) {
      try {
        await refresh();
      } catch {
        clear();
      }
    }
    ready.value = true;
  }
  async function login(username: string, password: string) {
    const value = await api<string>('/api/login', 'POST', { username, password });
    sessionStorage.setItem('vibe-token', value);
    try {
      await refresh();
    } catch (e) {
      clear();
      throw e;
    }
  }
  async function logout() {
    await api('/my/logout', 'POST');
    clear();
  }
  function require() {
    if (user.value) return true;
    useUi().authOpen = true;
    return false;
  }
  return { user, ready, admin, refresh, clear, init, login, logout, require };
});
