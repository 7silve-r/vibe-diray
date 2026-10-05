# Vibe Random Notes 前端

Vue 3、TypeScript、Pinia 和 Vite 构建的音乐与日记客户端。

## 运行

也可以直接使用项目根目录的 `start.cmd` 同时启动前后端，详见 [项目说明](../README.md)。

需要 Node.js、npm，以及已配置 MySQL 和 MinIO 的后端服务。

```bash
cd frontend
npm ci
npm run dev
```

默认开发地址为 `http://127.0.0.1:5173`，端口由根目录 `.env` 的 `WEB_PORT` 设置。Vite 将 `/backend` 请求转发到项目根目录 `.env` 中 `APP_PORT` 指定的后端端口，默认 8081。后端启动与环境变量见 `../backend/README.md`。

前端环境变量参见 `.env.example`：

- `VITE_API_BASE`：接口前缀，默认 `/backend`。
- `VITE_MAIL_ENABLED`：邮件绑定与找回密码入口，默认关闭。

`VITE_` 变量会进入客户端代码，不能包含密钥。

## 功能

- 统一账号、资料、头像和权限提示。
- 音乐搜索、歌手与歌单、收藏、评论和全局播放器。
- 日记分类、公开与私有状态、Markdown 编辑和本机草稿。
- 公开日记的点赞、评论和收藏。
- 管理员内容、账号、轮播图与反馈管理。

播放器由 Pinia 维护唯一音频实例，页面切换不重建音频。刷新页面或关闭标签页会停止播放。草稿按账号与日记 ID 保存于当前浏览器，清除浏览器数据会删除草稿。

主页支持多作者诗签轮换与本机心情留笺。留笺按账号、日期隔离，仅存于当前浏览器；游客使用独立的本机留笺，不会自动发布或写入日记。

## 验证

```bash
npm run format:check
npm test
npm run build
npm run test:e2e
```

`npm run format` 统一前端格式。单元测试使用 Vitest；浏览器测试使用 Playwright 和本机 Microsoft Edge，默认模拟接口，不修改数据库。

启动真实后端后，可在 Git Bash 执行：

```bash
VIBE_LIVE=true npm run test:e2e -- --grep "live diary"
```

真实服务测试创建独立测试账号，结束后清理该账号及关联内容。

## 部署

构建产物为 `dist`。使用同源反向代理将 `/backend/` 转发至后端，配置示例见 `nginx.example.conf`。MinIO 文件地址必须能被客户端访问。

接口文档见 `../backend/API.md`，素材与诗文出处见 `public/ASSETS.md`。
