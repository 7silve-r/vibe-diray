import { beforeEach, it, expect, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { usePlayer } from '../src/stores/player';
let audio: any;
class AudioMock extends EventTarget {
  src = '';
  paused = true;
  preload = '';
  volume = 1;
  currentTime = 0;
  duration = 100;
  constructor() {
    super();
    audio = this;
  }
  async play() {
    this.paused = false;
    this.dispatchEvent(new Event('play'));
  }
  pause() {
    this.paused = true;
    this.dispatchEvent(new Event('pause'));
  }
}
beforeEach(() => {
  vi.stubGlobal('Audio', AudioMock);
  setActivePinia(createPinia());
});
it('queue', async () => {
  const p = usePlayer(),
    songs = [
      { songId: 1, songName: 'A', audioUrl: 'http://a.test/a.wav' },
      { songId: 2, songName: 'B', audioUrl: 'http://a.test/b.wav' },
    ];
  await p.play(songs[0], songs);
  expect(p.playing).toBe(true);
  p.next();
  expect(p.current?.songId).toBe(2);
  p.next(-1);
  expect(p.current?.songId).toBe(1);
});
it('same', async () => {
  const p = usePlayer(),
    song = { songId: 1, songName: 'A', audioUrl: 'http://a.test/a.wav' };
  await p.play(song);
  audio.currentTime = 20;
  await p.play(song);
  expect(audio.currentTime).toBe(20);
});
it('seek', async () => {
  const p = usePlayer();
  audio.dispatchEvent(new Event('loadedmetadata'));
  p.seek(120);
  expect(audio.currentTime).toBe(100);
  p.setVolume(2);
  expect(audio.volume).toBe(1);
});
it('missing', async () => {
  const p = usePlayer();
  await p.play({ songId: 1, songName: 'A' });
  expect(p.current).toBeUndefined();
});
