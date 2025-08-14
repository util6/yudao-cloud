# 本地开发环境 Docker 配置

这个目录包含了用于本地开发的 Docker 配置文件，可以快速启动各种服务。

## 目录结构

- `compose-files/` - 各个服务的 Docker Compose 配置文件
- `config/` - 各个服务的配置文件
- `docker-compose.yml` - 主要的 Docker Compose 文件
- `start-dev.sh` - 交互式启动脚本

## 使用方法

### 1. 启动服务

#### 方法一：使用交互式脚本（推荐）
```bash
./start-dev.sh
```

然后根据提示选择需要启动的服务。

#### 方法二：使用 docker-compose 命令
```bash
# 启动 MySQL 和 Redis（学习模块所需）
docker-compose up -d mysql redis

# 启动所有服务
docker-compose up -d

# 启动特定服务
docker-compose up -d mysql
docker-compose up -d redis
```

### 2. 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止特定服务
docker-compose stop mysql
```

## 服务配置说明

### MySQL
- 端口: 3306
- 用户名: root
- 密码: 123456
- 数据库: ruoyi-vue-pro

这个配置与 yudao-learning-extension 模块的配置一致。

### Redis
- 端口: 6379
- 密码: 无密码（与项目配置一致）

### 其他服务
- MongoDB: 27017
- Nginx: 80, 443
- RabbitMQ: 5672, 15672
- Elasticsearch: 9200, 9300
- Kibana: 5601
- Nacos: 8848, 9848
- Portainer: 9000
- TiDB: 4000, 10080

## 与 yudao-cloud 项目集成

### 学习模块启动

要启动学习模块，需要：

1. 启动 MySQL 和 Redis 服务：
```bash
docker-compose up -d mysql redis
```

2. 在 yudao-learning-extension 目录下运行：
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=learning
```

### 独立学习模式

如果不想依赖外部服务，可以使用独立学习模式，该模式使用 H2 内存数据库和模拟服务：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=learning,learning-standalone
```

## 注意事项

1. 所有服务都在 `local-dev-network` 网络中运行
2. 数据默认会持久化存储在 Docker 卷中
3. 可以通过修改 `config/` 目录下的配置文件来调整服务配置
4. 如果遇到端口冲突，请修改对应的 compose 文件中的端口映射