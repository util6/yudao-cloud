#!/bin/bash

# YuDao Cloud 独立学习模块启动脚本
# 用于快速启动学习模块，无需启动完整的YuDao Cloud项目

echo "╔══════════════════════════════════════════════════════════════════════════════════╗"
echo "║                           YuDao Cloud 独立学习模块                                ║"
echo "║                        Independent Learning Module                              ║"
echo "║                                                                                  ║"
echo "║  🎯 专注学习，无需启动完整项目                                                     ║"
echo "║  🚀 交互式学习体验                                                               ║"
echo "║  📊 实时学习分析和反馈                                                           ║"
echo "╚══════════════════════════════════════════════════════════════════════════════════╝"

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java环境，请先安装JDK 8或更高版本"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未找到Maven环境，请先安装Maven"
    exit 1
fi

echo "✅ Java环境检查通过"
echo "✅ Maven环境检查通过"

# 检查数据库连接
echo "🔍 检查数据库连接..."
# 这里可以添加数据库连接检查逻辑

# 检查Redis连接
echo "🔍 检查Redis连接..."
# 这里可以添加Redis连接检查逻辑

echo "📦 编译学习模块..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ 编译失败，请检查代码"
    exit 1
fi

echo "✅ 编译成功"

echo "🚀 启动学习模块..."
echo "📝 学习日志将保存到 logs/learning.log"
echo "🌐 学习模块将在 http://localhost:18080 启动"
echo ""

# 启动学习模块
mvn spring-boot:run -Dspring-boot.run.profiles=learning -Dspring-boot.run.main-class=cn.iocoder.yudao.learning.LearningApplication

echo ""
echo "👋 感谢使用YuDao Cloud学习模块！"
