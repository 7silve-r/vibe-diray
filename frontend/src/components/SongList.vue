<script setup lang="ts">
import { Play, Heart } from 'lucide-vue-next';
import { usePlayer } from '../stores/player';
import { useAuth } from '../stores/auth';
import { useUi } from '../stores/ui';
import { api } from '../api';
import SmartImage from './SmartImage.vue';
defineProps<{ songs: any[] }>();
const emit = defineEmits(['refresh']);
const player = usePlayer(),
  auth = useAuth(),
  ui = useUi();
async function collect(song: any) {
  if (!auth.require()) return;
  await ui.run(
    async () => {
      await api(
        `/music/favorite/${song.likeStatus ? 'cancelCollectSong' : 'collectSong'}?songId=${song.songId}`,
        song.likeStatus ? 'DELETE' : 'POST',
      );
      emit('refresh');
    },
    song.likeStatus ? '已取消收藏' : '已收藏',
  );
}
</script>
<template>
  <div v-if="!songs.length" class="empty">
    这里还很安静。<br /><span class="tiny">歌曲上传后，就能在这里与你相遇。</span>
  </div>
  <div v-for="(song, i) in songs" :key="song.songId" class="song-row">
    <span class="index">{{ String(i + 1).padStart(2, '0') }}</span
    ><SmartImage class="cover" :src="song.coverUrl" /><RouterLink
      class="song-text truncate"
      :to="'/music/song/' + song.songId"
      ><strong>{{ song.songName }}</strong
      ><small>{{ song.artistName || '未知歌手' }} · {{ song.album || '单曲' }}</small></RouterLink
    ><button :aria-label="'播放 ' + song.songName" @click="player.play(song, songs)">
      <Play :size="17" /></button
    ><button :aria-label="song.likeStatus ? '取消收藏歌曲' : '收藏歌曲'" @click="collect(song)">
      <Heart :size="17" :fill="song.likeStatus ? 'currentColor' : 'none'" />
    </button>
  </div>
</template>
