#!/bin/bash

# Docker Compose 管理脚本
# 用于快速管理本地开发环境容器

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印彩色信息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 显示帮助信息
show_help() {
    echo "使用方法: ./manage.sh [命令] [参数]"
    echo ""
    echo "命令:"
    echo "  start [服务名]    - 启动指定服务(不指定则启动所有服务)"
    echo "  stop [服务名]     - 停止指定服务(不指定则停止所有服务)"
    echo "  restart [服务名]  - 重启指定服务(不指定则重启所有服务)"
    echo "  status           - 查看所有服务状态"
    echo "  logs [服务名]    - 查看服务日志"
    echo "  clean            - 清理所有容器和数据卷(危险操作)"
    echo "  test             - 测试服务连接"
    echo ""
    echo "可用服务:"
    echo "  mysql, redis, mongodb, nginx, rabbitmq, elasticsearch, kibana, nacos, portainer"
    echo ""
    echo "示例:"
    echo "  ./manage.sh start mysql redis  # 启动MySQL和Redis"
    echo "  ./manage.sh logs mysql         # 查看MySQL日志"
    echo "  ./manage.sh test               # 测试所有运行中的服务"
}

# 启动服务
start_services() {
    if [ $# -eq 0 ]; then
        print_info "启动所有服务..."
        docker-compose up -d
    else
        print_info "启动服务: $@"
        docker-compose up -d "$@"
    fi
    print_info "服务启动完成！"
    sleep 2
    show_status
}

# 停止服务
stop_services() {
    if [ $# -eq 0 ]; then
        print_info "停止所有服务..."
        docker-compose stop
    else
        print_info "停止服务: $@"
        docker-compose stop "$@"
    fi
    print_info "服务停止完成！"
}

# 重启服务
restart_services() {
    if [ $# -eq 0 ]; then
        print_info "重启所有服务..."
        docker-compose restart
    else
        print_info "重启服务: $@"
        docker-compose restart "$@"
    fi
    print_info "服务重启完成！"
    sleep 2
    show_status
}

# 显示服务状态
show_status() {
    print_info "当前服务状态:"
    docker-compose ps
}

# 查看日志
show_logs() {
    if [ $# -eq 0 ]; then
        print_error "请指定服务名"
        echo "示例: ./manage.sh logs mysql"
        exit 1
    fi
    docker-compose logs -f --tail=50 "$1"
}

# 清理所有容器和数据
clean_all() {
    print_warning "⚠️  警告: 此操作将删除所有容器和数据卷!"
    echo -n "确定要继续吗? (yes/no): "
    read answer
    if [ "$answer" = "yes" ]; then
        print_info "停止并删除所有容器和数据卷..."
        docker-compose down -v
        print_info "清理完成！"
    else
        print_info "操作已取消"
    fi
}

# 测试服务连接
test_services() {
    print_info "测试服务连接..."
    
    # 测试MySQL
    if docker ps | grep -q local-mysql; then
        print_info "测试MySQL连接..."
        if docker exec local-mysql mysql -uroot -p123456 -e "SELECT 1" > /dev/null 2>&1; then
            print_info "✓ MySQL连接正常 (端口: 3306, 密码: 123456)"
        else
            print_error "✗ MySQL连接失败"
        fi
    fi
    
    # 测试Redis
    if docker ps | grep -q local-redis; then
        print_info "测试Redis连接..."
        if docker exec local-redis redis-cli ping > /dev/null 2>&1; then
            print_info "✓ Redis连接正常 (端口: 6379)"
        else
            print_error "✗ Redis连接失败"
        fi
    fi
    
    # 测试MongoDB
    if docker ps | grep -q local-mongodb; then
        print_info "测试MongoDB连接..."
        if docker exec local-mongodb mongosh --eval "db.version()" > /dev/null 2>&1; then
            print_info "✓ MongoDB连接正常 (端口: 27017)"
        else
            print_error "✗ MongoDB连接失败"
        fi
    fi
    
    # 测试RabbitMQ
    if docker ps | grep -q local-rabbitmq; then
        print_info "✓ RabbitMQ运行中 (端口: 5672, 管理界面: http://localhost:15672)"
        print_info "  用户名: guest, 密码: guest"
    fi
    
    # 测试Elasticsearch
    if docker ps | grep -q local-elasticsearch; then
        print_info "测试Elasticsearch连接..."
        if curl -s http://localhost:9200/_cluster/health > /dev/null 2>&1; then
            print_info "✓ Elasticsearch连接正常 (端口: 9200)"
        else
            print_error "✗ Elasticsearch连接失败"
        fi
    fi
    
    # 测试Nacos
    if docker ps | grep -q local-nacos; then
        print_info "✓ Nacos运行中 (端口: 8848, 管理界面: http://localhost:8848/nacos)"
        print_info "  用户名: nacos, 密码: nacos"
    fi
    
    print_info "测试完成！"
}

# 主程序
case "$1" in
    start)
        shift
        start_services "$@"
        ;;
    stop)
        shift
        stop_services "$@"
        ;;
    restart)
        shift
        restart_services "$@"
        ;;
    status)
        show_status
        ;;
    logs)
        shift
        show_logs "$@"
        ;;
    clean)
        clean_all
        ;;
    test)
        test_services
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "未知命令: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
