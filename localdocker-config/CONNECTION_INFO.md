# 本地开发环境服务连接信息

## 🎉 问题已解决！

MySQL容器无限重启的问题已经修复。主要问题是配置文件路径错误，已经通过以下方式解决：
1. 修正了MySQL配置文件挂载路径
2. 简化了MySQL启动配置，使用命令行参数代替配置文件
3. 移除了平台限制，让Docker自动选择合适的镜像

## 📦 当前运行的服务

### MySQL
- **端口**: 3306
- **用户名**: root
- **密码**: 123456
- **默认数据库**: ruoyi-vue-pro
- **连接字符串**: 
  ```
  mysql -h127.0.0.1 -P3306 -uroot -p123456
  ```
- **JDBC URL**: 
  ```
  jdbc:mysql://localhost:3306/ruoyi-vue-pro?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
  ```

### Redis
- **端口**: 6379
- **密码**: 无
- **连接命令**: 
  ```
  redis-cli -h 127.0.0.1 -p 6379
  ```
- **连接字符串**: 
  ```
  redis://localhost:6379
  ```

## 🛠 快速管理命令

使用 `manage.sh` 脚本可以方便地管理所有服务：

```bash
# 查看帮助
./manage.sh help

# 启动服务
./manage.sh start mysql redis    # 启动MySQL和Redis
./manage.sh start                # 启动所有服务

# 停止服务
./manage.sh stop mysql           # 停止MySQL
./manage.sh stop                 # 停止所有服务

# 重启服务
./manage.sh restart mysql        # 重启MySQL

# 查看状态
./manage.sh status               # 查看所有服务状态

# 查看日志
./manage.sh logs mysql           # 查看MySQL日志

# 测试连接
./manage.sh test                 # 测试所有运行中的服务

# 清理环境（危险操作）
./manage.sh clean                # 删除所有容器和数据
```

## 🚀 其他可用服务

编辑 `docker-compose.yml` 文件，取消注释相应的服务即可启用：

- **MongoDB**: 文档数据库 (端口: 27017)
- **Nginx**: Web服务器 (端口: 80)
- **RabbitMQ**: 消息队列 (端口: 5672, 管理界面: 15672)
- **Elasticsearch**: 搜索引擎 (端口: 9200)
- **Kibana**: 数据可视化 (端口: 5601)
- **Nacos**: 服务注册与配置中心 (端口: 8848)
- **Portainer**: Docker管理界面 (端口: 9000)
- **TiDB**: 分布式数据库 (端口: 4000)

## 📝 注意事项

1. 所有服务都在 `local-dev-network` 网络中，容器之间可以通过服务名互相访问
2. 数据都保存在Docker卷中，删除容器不会丢失数据（除非使用 `docker-compose down -v`）
3. 首次启动服务可能需要下载镜像，请耐心等待
4. 如果遇到端口冲突，请修改对应的 `compose-files/docker-compose-*.yml` 文件中的端口映射

## 🔧 故障排查

如果服务无法启动，请按以下步骤排查：

1. 查看容器状态：
   ```bash
   docker ps -a
   ```

2. 查看容器日志：
   ```bash
   docker logs <container-name>
   ```

3. 检查端口占用：
   ```bash
   lsof -i :3306  # 检查3306端口
   ```

4. 重新创建容器：
   ```bash
   docker-compose down
   docker-compose up -d
   ```

5. 完全清理并重建（会删除数据）：
   ```bash
   ./manage.sh clean
   ./manage.sh start
   ```
