import { test, expect, Page } from '@playwright/test';
const song = {
  songId: 1,
  songName: '午后小调',
  artistName: '测试歌手',
  album: '晴天',
  audioUrl: 'http://127.0.0.1:5173/test.wav',
  favoriteStatus: 0,
};
const user = { id: 1, username: 'ADMIN', nickname: '管理员', role: 'ADMIN' };
async function setup(page: Page, login = false) {
  await page.addInitScript(
    ({ login }) => {
      sessionStorage.setItem('vibe-entered', 'yes');
      if (login) sessionStorage.setItem('vibe-token', 'test');
      const Original = window.Audio;
      window.Audio = class extends Original {
        constructor(...args: any[]) {
          super(...args);
          (window as any).__audio = this;
        }
      } as any;
    },
    { login },
  );
  await page.route('**/backend/**', async (route) => {
    const url = new URL(route.request().url());
    const path = url.pathname.replace('/backend', '');
    let data: any = { list: [], total: 0 };
    if (path === '/api/login') data = 'test';
    else if (path === '/my/profile') data = user;
    else if (path === '/my/categories') data = [{ id: 1, cateName: '日常', cateAlias: 'daily' }];
    else if (path.endsWith('listSongs')) data = { list: [song], total: 1 };
    else if (path.includes('getSongDetail')) data = { ...song, comments: [] };
    else if (path.endsWith('listArtistNames')) data = [{ artistId: 1, artistName: '测试歌手' }];
    else if (path.endsWith('getBannerList') || path.endsWith('/styles')) data = [];
    else if (path.startsWith('/public/articles/') && !path.endsWith('/comments'))
      data = {
        id: 2,
        title: '一页阳光',
        content: '# 阳光\n<script>alert(1)</script>',
        state: '公开',
        createUser: 2,
      };
    await route.fulfill({ json: { code: 200, message: '成功', data } });
  });
}
test('welcome', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('button', { name: '点击任意处开始' })).toBeVisible();
  await expect(page.locator('.gift-poem p')).toHaveCount(9);
  await expect(page.locator('.gift-poem')).toContainText('直起腰来，我望见蓝色的大海和帆影。');
  await expect(page.locator('.enter-hint')).toContainText('Enter');
  await page.locator('.sky-scene').evaluate((img: HTMLImageElement) => img.decode());
  expect(
    await page
      .locator('.start-hint')
      .evaluate((el) => el.getBoundingClientRect().bottom <= innerHeight),
  ).toBe(true);
  await page.screenshot({ animations: 'disabled', path: 'test-results/welcome-desktop.png' });
  await page.keyboard.press('Enter');
  await expect(page.getByRole('heading', { name: '今日的一隅' })).toBeVisible();
  await page.screenshot({
    animations: 'disabled',
    path: 'test-results/home-desktop.png',
    fullPage: true,
  });
  await page.getByRole('link', { name: '日记', exact: true }).click();
  await expect(page.getByRole('heading', { name: '随心 · 日记' })).toBeVisible();
});
test('login', async ({ page }) => {
  await setup(page);
  await page.goto('/');
  await page.getByRole('button', { name: '登录 / 注册' }).click();
  await page.getByLabel('用户名').fill('ADMIN');
  await page.getByLabel('密码', { exact: true }).fill('testpassword');
  await page.getByRole('button', { name: '登录，继续旅程' }).click();
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await page.getByRole('link', { name: /管理员/ }).click();
  await page.getByRole('link', { name: /管理中心/ }).click();
  await expect(page.getByRole('heading', { name: '管理中心' })).toBeVisible();
  await page.getByRole('button', { name: '新建歌曲' }).click();
  await page.getByLabel('歌曲名称').fill('新歌曲');
  await page.getByLabel('歌手', { exact: true }).selectOption('1');
  const request = page.waitForRequest((r) => r.url().includes('/addSong'));
  await page.getByRole('button', { name: '保存', exact: true }).click();
  expect((await request).postDataJSON().artistId).toBe(1);
});
test('draft', async ({ page }) => {
  await setup(page, true);
  await page.goto('/diary/write');
  await page.getByLabel('标题', { exact: true }).fill('我的一天');
  await page.getByLabel('书写', { exact: true }).fill('# 今天\n\n**开心**');
  await page.getByLabel('选择分类').selectOption('1');
  await expect(page.locator('.markdown strong')).toHaveText('开心');
  await page.reload();
  await page.getByRole('button', { name: '恢复草稿' }).click();
  await expect(page.getByLabel('标题', { exact: true })).toHaveValue('我的一天');
  const req = page.waitForRequest((r) => r.url().endsWith('/my/article') && r.method() === 'POST');
  await page.getByRole('button', { name: '保存日记', exact: true }).click();
  expect((await req).postData()).toContain('私有');
  await expect(page).toHaveURL(/diary\?tab=mine/);
  expect(await page.evaluate(() => localStorage.getItem('vibe-draft:1:new'))).toBeNull();
});
test('playback', async ({ page }) => {
  await setup(page);
  const rate = 8000,
    seconds = 20;
  const wav = Buffer.alloc(44 + rate * seconds * 2);
  wav.write('RIFF');
  wav.writeUInt32LE(wav.length - 8, 4);
  wav.write('WAVEfmt ', 8);
  wav.writeUInt32LE(16, 16);
  wav.writeUInt16LE(1, 20);
  wav.writeUInt16LE(1, 22);
  wav.writeUInt32LE(rate, 24);
  wav.writeUInt32LE(rate * 2, 28);
  wav.writeUInt16LE(2, 32);
  wav.writeUInt16LE(16, 34);
  wav.write('data', 36);
  wav.writeUInt32LE(wav.length - 44, 40);
  await page.route('**/test.wav', (r) => r.fulfill({ contentType: 'audio/wav', body: wav }));
  await page.goto('/music');
  await page.getByRole('button', { name: '播放 午后小调' }).click();
  await expect(page.locator('.vinyl')).toHaveClass(/spinning/);
  await expect
    .poll(() => page.evaluate(() => (window as any).__audio.currentTime))
    .toBeGreaterThan(0.1);
  const before = await page.evaluate(() => (window as any).__audio.currentTime);
  await page.getByRole('link', { name: '日记', exact: true }).click();
  await expect
    .poll(() => page.evaluate(() => (window as any).__audio.currentTime))
    .toBeGreaterThan(before);
  await page.getByRole('button', { name: '展开或收起播放器' }).click();
  await page.getByRole('button', { name: '暂停', exact: true }).click();
  expect(await page.evaluate(() => (window as any).__audio.paused)).toBe(true);
  await page.getByRole('button', { name: '隐藏小窗' }).click();
  await page.getByRole('button', { name: '恢复音乐小窗' }).click();
  await expect(page.getByRole('button', { name: '播放', exact: true })).toBeVisible();
});
test('mobile', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await setup(page);
  await page.goto('/');
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  await page.screenshot({
    animations: 'disabled',
    path: 'test-results/mobile-home.png',
    fullPage: true,
  });
  await page.getByRole('button', { name: '切换侧边导航' }).click();
  await page.getByRole('link', { name: '日记', exact: true }).click();
  await page.getByRole('button', { name: '＋ 写日记' }).click();
  await expect(page.getByRole('dialog')).toBeVisible();
});
test('error', async ({ page }) => {
  await setup(page);
  await page.route('**/backend/public/articles*', (r) =>
    r.fulfill({ status: 503, json: { code: 503, message: '服务暂不可用' } }),
  );
  await page.goto('/diary');
  await expect(page.getByRole('alert')).toHaveText('服务暂不可用');
  await expect(page.getByRole('button', { name: '重新加载' })).toBeVisible();
  await page.goto('/admin');
  await expect(page.getByText('此页面仅对管理员开放。')).toBeVisible();
});

test('expired', async ({ page }) => {
  await setup(page, true);
  await page.route('**/backend/my/profile', (r) =>
    r.fulfill({ status: 401, json: { code: 401, message: '登录已失效' } }),
  );
  await page.goto('/');
  await expect(page.getByRole('dialog')).toBeVisible();
  expect(await page.evaluate(() => sessionStorage.getItem('vibe-token'))).toBeNull();
});
test('admin upload', async ({ page }) => {
  await setup(page, true);
  await page.route('**/backend/music/admin/listAdminSongs', (r) =>
    r.fulfill({ json: { code: 200, data: { list: [song], total: 1 } } }),
  );
  await page.goto('/admin');
  await page.getByRole('button', { name: '封面', exact: true }).click();
  await page
    .getByLabel('图片文件（最大 5 MB）')
    .setInputFiles({ name: 'cover.png', mimeType: 'image/png', buffer: Buffer.from('test') });
  const request = page.waitForRequest((r) => r.url().includes('/songs/1/cover'));
  await page.getByRole('button', { name: '上传', exact: true }).click();
  expect((await request).method()).toBe('PUT');
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await page.getByRole('button', { name: '删除', exact: true }).click();
  await expect(page.getByRole('dialog', { name: '请确认' })).toBeVisible();
  await page.getByRole('button', { name: '取消', exact: true }).click();
});

test('sidebar and states', async ({ page }) => {
  await setup(page);
  await page.goto('/missing-page');
  await expect(page.getByRole('heading', { name: '这个页面不存在' })).toBeVisible();
  await expect(page.locator('.mood-lost')).toBeVisible();
  await page.getByRole('link', { name: '返回主页', exact: true }).click();
  await expect(page.locator('.sky-scene')).toBeVisible();
  expect(
    await page
      .locator('.sky-scene')
      .evaluate((img: HTMLImageElement) => img.complete && img.naturalWidth > 0),
  ).toBe(true);
  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.getByRole('navigation', { name: '主导航' })).toBeHidden();
  await page.getByRole('button', { name: '切换侧边导航' }).click();
  await page.getByRole('link', { name: '日记', exact: true }).click();
  await expect(page.getByRole('navigation', { name: '主导航' })).toBeHidden();
  await expect(page.locator('.mood-empty')).toBeVisible();
  for (const width of [320, 390, 768, 1280]) {
    await page.setViewportSize({ width, height: 844 });
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(
      true,
    );
  }
  await page.screenshot({
    animations: 'disabled',
    path: 'test-results/diary-empty.png',
    fullPage: true,
  });
});

test('poem mobile and motion', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.setViewportSize({ width: 320, height: 740 });
  await page.goto('/');
  await expect(page.locator('.gift-poem p')).toHaveCount(9);
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  expect(
    await page.locator('.start-hint').evaluate((el) => getComputedStyle(el).animationName),
  ).toBe('none');
  await page.screenshot({
    animations: 'disabled',
    path: 'test-results/welcome-mobile.png',
    fullPage: true,
  });
  await page.getByRole('button', { name: '点击任意处开始' }).press('Enter');
  await expect(page.locator('.sky-scene')).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
});

test('poetry', async ({ page }) => {
  await setup(page);
  await page.goto('/');
  const authors = new Set<string>();
  for (let n = 0; n < 4; n++) {
    authors.add((await page.locator('.poem-credit').innerText()).split(' ·')[0]);
    await page.getByRole('button', { name: '换一签' }).click();
  }
  expect(authors.size).toBe(4);
  await expect(page.locator('main a[href="/music"], main a[href="/diary"]')).toHaveCount(0);
});

test('moment', async ({ page }) => {
  await setup(page);
  await page.goto('/');
  await page.getByRole('button', { name: '晴朗', exact: true }).click();
  await page.getByLabel('今日留笺', { exact: true }).fill('游客留笺');
  await page.getByRole('button', { name: '留在今天' }).click();
  await page.reload();
  await expect(page.getByLabel('今日留笺', { exact: true })).toHaveValue('游客留笺');
  await expect(page.getByRole('button', { name: '晴朗', exact: true })).toHaveAttribute(
    'aria-pressed',
    'true',
  );
  await page.getByRole('button', { name: '登录 / 注册' }).click();
  await page.getByLabel('用户名').fill('ADMIN');
  await page.getByLabel('密码', { exact: true }).fill('testpassword');
  await page.getByRole('button', { name: '登录，继续旅程' }).click();
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await expect(page.getByLabel('今日留笺', { exact: true })).toHaveValue('');
  await page.getByLabel('今日留笺', { exact: true }).fill('账号留笺');
  await page.getByRole('button', { name: '留在今天' }).click();
  await page.reload();
  await expect(page.getByLabel('今日留笺', { exact: true })).toHaveValue('账号留笺');
  await page.getByRole('button', { name: '清除今日留笺' }).click();
  await page.reload();
  await expect(page.getByLabel('今日留笺', { exact: true })).toHaveValue('');
  await page.getByRole('link', { name: /管理员/ }).click();
  await page.getByRole('button', { name: '退出登录' }).click();
  await expect(page.getByLabel('今日留笺', { exact: true })).toHaveValue('游客留笺');
});
