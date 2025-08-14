@echo off
chcp 65001 >nul

echo ╔══════════════════════════════════════════════════════════════════════════════════╗
echo ║                           YuDao Cloud 独立学习模块                                ║
echo ║                        Independent Learning Module                              ║
echo ║                                                                                  ║
echo ║  🎯 专注学习，无需启动完整项目                                                     ║
echo ║  🚀 交互式学习体验                                                               ║
echo ║  📊 实时学习分析和反馈                                                           ║
echo ╚══════════════════════════════════════════════════════════════════════════════════╝

REM 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未找到Java环境，请先安装JDK 8或更高版本
    pause
    exit /b 1
)

REM 检查Maven环境
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未找到Maven环境，请先安装Maven
    pause
    exit /b 1
)

echo ✅ Java环境检查通过
echo ✅ Maven环境检查通过

echo 🔍 检查数据库连接...
REM 这里可以添加数据库连接检查逻辑

echo 🔍 检查Redis连接...
REM 这里可以添加Redis连接检查逻辑

echo 📦 编译学习模块...
mvn clean compile -q

if %errorlevel% neq 0 (
    echo ❌ 编译失败，请检查代码
    pause
    exit /b 1
)

echo ✅ 编译成功

echo 🚀 启动学习模块...
echo 📝 学习日志将保存到 logs/learning.log
echo 🌐 学习模块将在 http://localhost:18080 启动
echo.

REM 启动学习模块
mvn spring-boot:run -Dspring-boot.run.profiles=learning -Dspring-boot.run.main-class=cn.iocoder.yudao.learning.LearningApplication

echo.
echo 👋 感谢使用YuDao Cloud学习模块！
pause
