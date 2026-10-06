import { beforeEach, it, expect, vi } from 'vitest';
import { api, media, query, upload } from '../src/api';
beforeEach(() => {
  sessionStorage.clear();
  vi.restoreAllMocks();
});
it('auth', async () => {
  sessionStorage.setItem('vibe-token', 'test');
  const fetcher = vi
    .spyOn(window, 'fetch')
    .mockResolvedValue(new Response(JSON.stringify({ code: 200, data: 42 })));
  expect(await api('/my/profile')).toBe(42);
  expect(fetcher.mock.calls[0][1]?.headers).toHaveProperty('Authorization', 'Bearer test');
});
it('expired', async () => {
  const callback = vi.fn();
  window.addEventListener('vibe:unauthorized', callback, { once: true });
  vi.spyOn(window, 'fetch').mockResolvedValue(
    new Response(JSON.stringify({ code: 401, message: '请重新登录' }), { status: 401 }),
  );
  await expect(api('/my/profile')).rejects.toThrow('请重新登录');
  expect(callback).toHaveBeenCalledOnce();
});
it('offline', async () => {
  vi.spyOn(window, 'fetch').mockRejectedValue(new TypeError('Failed'));
  await expect(api('/public/articles')).rejects.toThrow('暂时连接不上服务');
});
it('media', () => {
  expect(media('javascript:alert(1)')).toBe('');
  expect(media('/uploads/a.png')).toBe('/backend/uploads/a.png');
  expect(query({ state: '公开', pageNum: 1, empty: '' })).toBe(
    'state=%E5%85%AC%E5%BC%80&pageNum=1',
  );
});

it('large audio', async () => {
  const file = new File(['audio'], 'large.mp3', { type: 'audio/mpeg' });
  Object.defineProperty(file, 'size', { value: 51 * 1024 * 1024 });
  const fetcher = vi.spyOn(window, 'fetch');
  await expect(upload('/music/admin/songs/1/audio', file)).rejects.toThrow('音频不能超过50MB');
  expect(fetcher).not.toHaveBeenCalled();
});
it('upload timeout', async () => {
  const timeout = vi.spyOn(AbortSignal, 'timeout');
  vi.spyOn(window, 'fetch').mockRejectedValue(new DOMException('timeout', 'TimeoutError'));
  await expect(
    upload('/music/admin/songs/1/audio', new File(['audio'], 'song.mp3')),
  ).rejects.toThrow('上传超时');
  expect(timeout).toHaveBeenCalledWith(120000);
});
