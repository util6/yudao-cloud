#!/bin/bash

# 定义颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 输出欢迎信息
echo -e "${BLUE}====================================${NC}"
echo -e "${BLUE}    本地开发环境快速启动脚本    ${NC}"
echo -e "${BLUE}====================================${NC}"

# 创建网络（如果不存在）
echo -e "${YELLOW}创建Docker网络...${NC}"
docker network create local-dev-network 2>/dev/null || true

# 进入配置目录
cd "$(dirname "$0")"

# 显示可用的服务列表
echo -e "${GREEN}可用的服务列表:${NC}"
echo "1) mysql       - MySQL数据库"
echo "2) redis       - Redis缓存"
echo "3) mongodb     - MongoDB文档数据库"
echo "4) nginx       - Nginx Web服务器"
echo "5) rabbitmq    - RabbitMQ消息队列"
echo "6) elastic     - Elasticsearch搜索引擎"
echo "7) kibana      - Kibana可视化工具"
echo "8) nacos       - Nacos服务注册中心"
echo "9) portainer   - Portainer Docker管理工具"
echo "10) tidb       - TiDB数据库集群"
echo "11) all        - 启动所有服务"
echo "12) learning   - 启动学习模块所需服务 (mysql + redis)"
echo "0) exit        - 退出脚本"

# 定义服务映射
declare -A services
services["mysql"]="mysql"
services["redis"]="redis"
services["mongodb"]="mongodb"
services["nginx"]="nginx"
services["rabbitmq"]="rabbitmq"
services["elastic"]="elasticsearch"
services["kibana"]="elasticsearch kibana"
services["nacos"]="nacos"
services["portainer"]="portainer"
services["tidb"]="pd tikv tidb"
services["learning"]="mysql redis"

# 获取用户选择
selected_services=""

while true; do
    echo ""
    echo -e "${YELLOW}请选择要启动的服务(输入数字，多个服务用空格分隔，输入0退出):${NC}"
    read -r choices
    
    # 检查是否退出
    if [[ "$choices" == "0" ]]; then
        echo -e "${RED}已取消操作${NC}"
        exit 0
    fi
    
    # 处理"all"选项
    if [[ "$choices" == "11" ]]; then
        echo -e "${GREEN}将启动所有服务${NC}"
        selected_services="mysql redis mongodb nginx rabbitmq elasticsearch kibana nacos portainer pd tikv tidb"
        break
    fi
    
    # 处理"learning"选项
    if [[ "$choices" == "12" ]]; then
        echo -e "${GREEN}将启动学习模块所需服务 (MySQL + Redis)${NC}"
        selected_services="mysql redis"
        break
    fi
    
    # 处理选择的服务
    for choice in $choices; do
        case $choice in
            1) selected_services="$selected_services mysql" ;;
            2) selected_services="$selected_services redis" ;;
            3) selected_services="$selected_services mongodb" ;;
            4) selected_services="$selected_services nginx" ;;
            5) selected_services="$selected_services rabbitmq" ;;
            6) selected_services="$selected_services elasticsearch" ;;
            7) selected_services="$selected_services elasticsearch kibana" ;;
            8) selected_services="$selected_services nacos" ;;
            9) selected_services="$selected_services portainer" ;;
            10) selected_services="$selected_services pd tikv tidb" ;;
            *) echo -e "${RED}无效的选择: $choice${NC}" ;;
        esac
    done
    
    # 如果有有效选择，则退出循环
    if [[ -n "$selected_services" ]]; then
        break
    fi
done

# 启动选择的服务
echo -e "${GREEN}正在启动以下服务: $selected_services${NC}"
docker-compose up -d $selected_services

# 显示服务状态
echo -e "${GREEN}服务状态:${NC}"
docker-compose ps

echo -e "${BLUE}====================================${NC}"
echo -e "${GREEN}服务已启动完成!${NC}"
echo -e "${BLUE}====================================${NC}"

# 如果启动了学习模块所需服务，提供启动学习模块的提示
if [[ "$selected_services" == *"mysql"* ]] && [[ "$selected_services" == *"redis"*" ]]; then
    echo -e "${YELLOW}提示: 学习模块所需服务已启动${NC}"
    echo -e "${YELLOW}请执行以下命令启动学习模块:${NC}"
    echo -e "  cd ../yudao-learning-extension"
    echo -e "  mvn spring-boot:run -Dspring-boot.run.profiles=learning"
    echo ""
    echo -e "${YELLOW}或者使用独立模式 (无需外部依赖):${NC}"
    echo -e "  mvn spring-boot:run -Dspring-boot.run.profiles=learning,learning-standalone"
    echo ""
fi