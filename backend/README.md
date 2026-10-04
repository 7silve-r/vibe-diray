# 随心记后端

## 结构和启动

本项目只有一个后端程序、一个端口和一套账号。`diary` 是账号、权限和日记模块，`music` 依赖它并提供音乐业务与唯一启动类 `com.silver.music.MusicApplication`。

在 IDEA 打开 `backend/pom.xml`，作为 Maven 项目导入，等待依赖加载。不要只打开 music 的 pom。模块源码分别位于 `diary/src/main/java/com/silver/diary` 和 `music/src/main/java/com/silver/music`。

配置入口为 `music/src/main/resources/application.yml`，其中导入 `diary/src/main/resources/diary.yml`。在 MusicApplication 运行配置的 Environment variables 中设置根目录 `.env.example` 列出的变量。Spring Boot 不会自动读取 `.env`；本目录的数据重建脚本才会读取它。

必要配置：MySQL 连接信息、至少32字节的 `DIARY_JWT_SECRET`、首次启动使用的 `ADMIN_PASSWORD`、MinIO 凭据。上传目录和 URL 前缀保留末尾的 `/`。邮箱功能需要真实 SMTP 配置；未配置时正常浏览、登录和日记业务仍可使用。

启动 MinIO 并创建 `MINIO_BUCKET` 指定的桶。音乐文件需要可公开读取，桶策略只授予对象读取，不开放匿名写入或删除；日记封面不放进公开桶，而由后端按公开状态及作者身份检查后返回。

启动 MySQL，完成下方初始化后，在 IDEA 运行 MusicApplication。使用 `APP_PORT` 设置整个后端端口，默认8081，不再读取 `DIARY_PORT`。不需要第二个音乐或日记服务，也不需要 Redis。旧音乐推荐算法保留，结果从数据库计算；用户收藏状态不会跨账号缓存。

Git Bash 中构建：

```bash
cd /c/.ProgramS/Projects/vibe-diray/backend
mvn clean verify
java -jar music/target/music_backend-0.0.1-SNAPSHOT.jar
```

启动前须在同一个终端导出环境变量。无需将真实密码提交到 Git。

## 数据库初始化和旧数据清理

两个模块共用同一个数据库，默认库名为 music_diary。原项目也曾使用 vibe_diary，务必以你实际配置的 DIARY_DB_URL 为准。

新建空库时，依次执行 diary/sql/schema.sql 和 music/sql/schema.sql；两份脚本顶部默认使用 music_diary，如选择其他库需要相应修改。不要在旧表上直接执行 CREATE TABLE IF NOT EXISTS 来代替重建，它不会更新旧字段。

需要重建本机开发数据时，可将根目录 `.env.example` 复制为 `.env` 并填入本机连接信息，然后执行：

```bash
python scripts/reset_database.py --database music_diary
python scripts/reset_database.py --database music_diary --apply
```

第一条仅列出实际连接的本机库及每张表的行数。第二条清空并重建已知项目表；不会删除整个数据库或其他数据库。若出现未知表、库名不匹配或连接不是本机，工具会停止。若实际库名为 vibe_diary，请把参数换成 vibe_diary，并确保 .env 中一致。目标库必须已经存在。

重建会删除旧账号、日记、音乐元数据和互动记录。上传目录和 MinIO 旧对象不自动删除。应用每次启动不会清空数据。

固定管理员用户名为 ADMIN；首次启动时使用 ADMIN_PASSWORD 的 BCrypt 散列创建账号。以后启动不覆盖已有密码。普通注册不能注册 ADMIN，也没有公开的管理员注册或提权接口。

## 账号与权限

统一注册 POST /api/reg；统一登录 POST /api/login。两模块接受同一个 Authorization，可以传原始 JWT 或 Bearer JWT。账号角色从数据库读取；管理员同时拥有普通用户能力。退出、修改密码、重置密码或管理员改变账号状态后，旧 Token 失效。

Spring Security 的过滤链负责身份认证，`@EnableMethodSecurity` 和 `@PreAuthorize` 负责方法授权；资源所属用户和公开状态在业务层继续检查。参考 [Spring Security 方法授权文档](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)。

| 身份 | 能力 |
| --- | --- |
| 游客 | 浏览公开日记、公开评论、音乐、歌手、歌单、推荐、轮播图 |
| 普通用户 | 上述能力；管理自己的日记、分类、资料；点赞、评论和收藏；音乐互动；反馈 |
| ADMIN | 普通用户能力；音乐内容和用户管理；删除他人的公开日记及公开日记评论 |

管理员不能浏览或删除他人的私有日记，不能修改他人的正文。存在私有日记的用户不能被管理员删除，以免绕过隐私限制；可以停用账号。固定管理员不能被管理接口删除、停用或通过邮箱重置密码。

日记 state 只有“私有”和“公开”，旧“草稿/已发布”不再接受。公开转私有时，点赞、评论、收藏记录保留，但其他人无法访问，收藏列表中也会隐藏；重新公开后恢复。删除日记通过数据库外键级联清理互动记录。封面使用 no-store 响应；已经被用户下载的公开内容无法远程撤回。

日记和音乐都支持一级评论、评论点赞/取消点赞、删除自己的评论，以及管理员删除评论。日记还支持对日记本身点赞和收藏；原音乐界面的 likeStatus 表示歌曲或歌单收藏状态，不另建重复的歌曲点赞体系。重复点赞、收藏或取消都是幂等操作。

注册沿用 diary 的用户名和密码方式；注册后通过验证码绑定邮箱，再使用邮箱找回密码。验证码5分钟有效、同邮箱同用途60秒发送间隔、最多5次错误尝试、使用后失效。修改资料中的邮箱会清除已验证标记，需要重新验证。

## 测试

在 backend 目录执行 mvn test 可一次测试两个模块；IDEA 中也可运行各测试类。测试使用 test profile 和 H2 内存数据库，邮件与 MinIO 测试使用模拟对象，不读取真实密码、不清理实际数据库、不发送真实邮件。

真实 MySQL、MinIO 和 SMTP 的连通性需要通过实际环境配置另外验证；自动测试通过不表示这些外部服务已经连通。

接口路径与请求参数见 API.md。完整客户端位于 ../frontend。
