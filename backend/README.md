# Vibe Random Notes 后端

一个后端程序提供账号、日记、音乐、互动和管理功能，使用同一个数据库和端口。

## 运行

推荐在项目根目录使用 `start.cmd`，配置与首次使用见 [项目 README](../README.md)。脚本自动读取根目录 `.env`。

IDEA 导入本目录 `pom.xml`，运行 `com.silver.VibeApplication`。直接从 IDEA 或 Java 启动时，需要在运行配置中自行设置环境变量；不会自动读取 `.env`。上传目录建议使用绝对路径，以避免不同工作目录带来的差异。

手动构建与运行（需已设置环境变量）：

```bash
mvn clean verify
java -jar music/target/vibe-backend.jar
```

固定管理员为 `ADMIN`，`ADMIN_PASSWORD` 仅在首次创建账号时使用。应用启动不会清空业务数据或覆盖已有密码。未配置 SMTP 时，音乐、日记及普通登录注册仍可使用。

## 验证

在本目录运行：

```bash
mvn test
mvn spotless:check
```

`mvn spotless:apply` 统一 Java 格式。两个模块继承同一个 Maven 父配置。测试使用 H2 内存数据库及模拟外部服务，不需要本机数据库密码，不删除真实数据、不发送邮件。

本机数据库和 MinIO 的连通性、凭据与权限需另外验证。接口见 [API.md](API.md)。

## 本地数据重建

仅在明确需要删除本机开发数据时使用：

```bash
python scripts/reset_database.py --database music_diary
python scripts/reset_database.py --database music_diary --apply
```

第一条预览范围；第二条会清空并重建已知项目表。库名必须与 `.env` 中 `DB_URL` 一致；工具拒绝非本机地址和未知表。上传文件和 MinIO 对象不会随之清理。日常启动不执行此工具。
