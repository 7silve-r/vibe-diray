# 日记测试

日记现为一体化程序中的 Maven 模块。生产启动类位于 music 模块的 com.silver.VibeApplication。

IDEA 导入 backend/pom.xml 后，可右键 diary/src/test/java 运行全部测试；也可在 backend 运行 mvn test，同时验证音乐模块。

DiaryTestApplication 只用于加载日记模块的测试环境，不是生产服务启动类。DiaryApplicationTests.contextLoads 使用 test profile 和 H2 内存数据库，不再依赖本机 DB_PASSWORD 等配置。

SecurityTest 检查真实过滤链、JWT、方法授权、角色、退出和账号状态；ArticleSocialTest 检查公开/私有隔离、点赞评论收藏、管理员权限和私有封面；EmailTest 使用模拟邮件服务验证邮箱流程。原有控制器、上传、异常处理和 JWT 测试继续保留。

测试方法名保持简短。异常分支可能打印预期错误日志，最终以 Maven 测试统计和退出码判断结果。完整说明见 ../README.md。
