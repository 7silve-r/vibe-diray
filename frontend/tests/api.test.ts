import { beforeEach, it, expect, vi } from 'vitest';
import { api, media, query } from '../src/api';
beforeEach(() => {
  sessionStorage.clear();
  vi.restoreAllMocks();
});
it('auth', async () => {
  sessionStorage.setItem('vibe-token', 'test');
  const fetcher = vi
    .spyOn(window, 'fetch')
    .mockResolvedValue(new Response(JSON.stringify({ code: 200, data: 42 })));
  expect(await api('/my/userinfo')).toBe(42);
  expect(fetcher.mock.calls[0][1]?.headers).toHaveProperty('Authorization', 'Bearer test');
});
it('expired', async () => {
  const callback = vi.fn();
  window.addEventListener('vibe:unauthorized', callback, { once: true });
  vi.spyOn(window, 'fetch').mockResolvedValue(
    new Response(JSON.stringify({ code: 401, message: '请重新登录' }), { status: 401 }),
  );
  await expect(api('/my/userinfo')).rejects.toThrow('请重新登录');
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
