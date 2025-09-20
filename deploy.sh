#!/bin/bash

# 签到任务管理 App - GitHub 部署脚本
# 使用方法: ./deploy.sh [your-github-username] [repository-name]

set -e

GITHUB_USERNAME=${1:-"yourusername"}
REPO_NAME=${2:-"task-notify"}

echo "🚀 开始部署签到任务管理 App 到 GitHub..."
echo "📂 用户名: $GITHUB_USERNAME"
echo "📦 仓库名: $REPO_NAME"
echo ""

# 检查是否已经初始化 Git
if [ ! -d ".git" ]; then
    echo "📝 初始化 Git 仓库..."
    git init
    echo ""
fi

# 添加所有文件
echo "📄 添加项目文件..."
git add .
echo ""

# 提交更改
if git diff --staged --quiet; then
    echo "⚠️  没有检测到文件更改，跳过提交步骤"
else
    echo "💾 提交更改..."
    git commit -m "签到任务管理app完整实现

功能特性:
- ✅ 任务管理：添加、编辑、删除签到任务
- ⏰ 自动重置：每日凌晨自动重置任务状态  
- 📱 应用启动：支持通过包名启动其他应用
- 🔔 智能提醒：为未完成任务发送通知
- 📊 进度统计：显示每日任务完成进度
- 🎨 美观界面：采用 Material 3 设计语言
- 🍎 苹果风格图标：蓝色渐变设计

技术栈:
- Kotlin + Jetpack Compose
- MVVM + Repository 架构
- Room 数据库 + Hilt 依赖注入
- WorkManager 后台任务
- GitHub Actions 自动构建"
fi
echo ""

# 设置主分支
echo "🌿 设置主分支..."
git branch -M main
echo ""

# 添加远程仓库
REMOTE_URL="https://github.com/${GITHUB_USERNAME}/${REPO_NAME}.git"
echo "🔗 配置远程仓库: $REMOTE_URL"

if git remote get-url origin >/dev/null 2>&1; then
    git remote set-url origin "$REMOTE_URL"
    echo "✅ 远程仓库地址已更新"
else
    git remote add origin "$REMOTE_URL"
    echo "✅ 远程仓库已添加"
fi
echo ""

# 推送到 GitHub
echo "📤 推送代码到 GitHub..."
echo "⚠️  请确保你已经在 GitHub 创建了仓库: $REPO_NAME"
echo "⚠️  如果是私有仓库，可能需要配置 GitHub 访问凭据"
echo ""

read -p "🤔 确认继续推送？(y/N): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    git push -u origin main
    echo ""
    echo "🎉 部署完成！"
    echo ""
    echo "📋 下一步操作:"
    echo "1. 访问仓库: https://github.com/${GITHUB_USERNAME}/${REPO_NAME}"
    echo "2. 进入 Actions 页面查看自动构建进度"
    echo "3. 构建完成后在 Artifacts 中下载 APK"
    echo ""
    echo "📱 APK 下载地址:"
    echo "   https://github.com/${GITHUB_USERNAME}/${REPO_NAME}/actions"
    echo ""
    echo "🔧 如需配置签名发布，请在仓库设置中添加以下 Secrets:"
    echo "   - KEYSTORE_BASE64 (签名文件的 Base64 编码)"
    echo "   - KEYSTORE_PASSWORD (Keystore 密码)" 
    echo "   - KEY_ALIAS (密钥别名)"
    echo "   - KEY_PASSWORD (密钥密码)"
    echo ""
else
    echo "❌ 部署已取消"
    exit 1
fi