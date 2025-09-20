# GitHub Actions 构建指南

本文档详细说明如何使用 GitHub Actions 自动构建 Android APK，适合没有本地 Android 开发环境的开发者。

## 📋 前置准备

### 1. 创建 GitHub 账号
如果还没有 GitHub 账号，请先注册：https://github.com

### 2. 创建新的 GitHub 仓库
1. 登录 GitHub，点击右上角的 "+" → "New repository"
2. 填写仓库信息：
   - Repository name: `task-notify`（或自定义名称）
   - Description: `签到任务管理 Android 应用`
   - 选择 Public 或 Private
   - **不要**勾选 "Add a README file"（项目已包含）
3. 点击 "Create repository"

## 🚀 部署步骤

### 方式一：使用自动化脚本（推荐）

1. **运行部署脚本**：
```bash
./deploy.sh [你的GitHub用户名] [仓库名称]
```

例如：
```bash
./deploy.sh myusername task-notify
```

2. **按提示操作**：
   - 脚本会自动初始化 Git、添加文件、提交更改
   - 询问确认时输入 `y` 继续推送

### 方式二：手动部署

1. **初始化 Git 仓库**：
```bash
git init
git add .
git commit -m "签到任务管理app完整实现"
```

2. **关联远程仓库**：
```bash
git branch -M main
git remote add origin https://github.com/你的用户名/task-notify.git
```

3. **推送代码**：
```bash
git push -u origin main
```

## 📦 获取构建产物

### 1. 查看构建状态
推送代码后，GitHub Actions 会自动开始构建：

1. 进入你的仓库页面
2. 点击 "Actions" 选项卡
3. 可以看到构建任务 "Android CI Build"

### 2. 构建过程说明
自动构建包含以下步骤：
- ✅ 检出代码
- ✅ 设置 JDK 17 环境
- ✅ 缓存 Gradle 依赖
- ✅ 构建 Debug APK
- ✅ 构建 Release APK
- ✅ 上传构建产物

### 3. 下载 APK 文件
构建完成后（通常需要 3-5 分钟）：

1. 点击成功的构建记录（绿色 ✅ 标记）
2. 滚动到页面底部的 "Artifacts" 部分
3. 下载文件：
   - `debug-apk`: 调试版本，可直接安装测试
   - `release-apk`: 发布版本，适合正式使用

## 🔧 高级配置：签名发布

如果需要发布到应用商店，需要配置应用签名。

### 1. 生成签名密钥（如果没有）
```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

### 2. 转换为 Base64
```bash
base64 -i release-key.jks | tr -d '\n'
```

### 3. 配置 GitHub Secrets
在仓库页面：
1. Settings → Secrets and variables → Actions
2. 点击 "New repository secret"
3. 添加以下 Secrets：

| Name | Value | 说明 |
|------|-------|------|
| `KEYSTORE_BASE64` | 上一步生成的 Base64 字符串 | 签名文件 |
| `KEYSTORE_PASSWORD` | Keystore 密码 | 创建时设置的密码 |
| `KEY_ALIAS` | release | 密钥别名 |
| `KEY_PASSWORD` | 密钥密码 | 创建时设置的密码 |

### 4. 重新构建
配置完成后，推送任何更改都会触发新的构建，生成签名的 APK。

## 🐛 常见问题

### Q: 构建失败了怎么办？
A: 点击失败的构建记录，查看详细日志找出错误原因。常见问题：
- 网络超时：重新运行构建
- 依赖冲突：检查 build.gradle 配置
- 权限问题：确保 gradlew 有执行权限

### Q: 无法推送代码到 GitHub？
A: 可能原因：
- 仓库地址错误：检查远程仓库 URL
- 权限问题：配置 SSH 密钥或使用个人访问令牌
- 网络问题：检查网络连接

### Q: APK 下载后无法安装？
A: 可能需要：
- 允许安装未知来源应用
- 检查设备兼容性（最低 Android 7.0）
- 使用 Debug APK 进行测试

### Q: 如何更新应用？
A: 修改代码后：
```bash
git add .
git commit -m "更新说明"
git push
```
GitHub Actions 会自动构建新版本。

## 📱 安装和使用

1. **下载 APK**：从 GitHub Actions 下载构建产物
2. **安装应用**：在 Android 设备上安装 APK
3. **授权权限**：首次运行时允许通知权限
4. **开始使用**：添加签到任务，设置提醒时间

## 🔄 持续集成

每次推送代码到 `main` 或 `develop` 分支，都会自动触发构建。支持：
- Pull Request 自动构建
- 多分支并行构建  
- 构建缓存加速
- 自动化测试（可扩展）

## 📞 技术支持

如果遇到问题，可以：
1. 查看项目 README.md
2. 检查 GitHub Actions 构建日志
3. 提交 Issue 到项目仓库