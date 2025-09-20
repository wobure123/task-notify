# CheckInMaster

签到任务管理 Android 应用，集中管理和提醒各类 App 日常签到任务。

## 🎯 功能概述
- ✨ 任务创建 / 编辑 / 删除 / 列表展示
- 🎨 优先级标记（低/中/高）
- 🔗 Deep Link 或包名一键跳转目标应用
- 🕒 每日 00:05 自动重置完成状态（WorkManager 周期任务）
- 🔔 指定时间智能提醒（未完成才通知）
- 📱 Android 13+ 通知权限适配
- 📊 任务完成进度统计与可视化

## 🛠️ 技术栈
- **语言**: Kotlin + Coroutines + Flow
- **UI**: Jetpack Compose (Material3)
- **架构**: MVVM + Repository 模式
- **数据库**: Room 持久化
- **依赖注入**: Hilt + Hilt Work
- **后台任务**: WorkManager
- **导航**: Navigation Compose
- **CI/CD**: GitHub Actions 自动构建

## 📱 应用图标
应用采用苹果风格的蓝色渐变图标，展现清爽的签到管理主题：
- 🎨 主色调：iOS 蓝 (#007AFF) 到紫色 (#5856D6) 渐变
- ✅ 图标元素：剪贴板 + 复选列表 + 进度指示
- 🔄 支持 Android 自适应图标（API 26+）

## 🚀 GitHub Actions 自动构建指南

### 第一步：推送代码到 GitHub
```bash
# 创建 GitHub 仓库后，推送代码
git init
git add .
git commit -m "签到任务管理app完整实现"
git branch -M main
git remote add origin https://github.com/你的用户名/task-notify.git
git push -u origin main
```

### 第二步：自动构建
推送代码后，GitHub Actions 会自动：
1. 设置 JDK 17 环境
2. 缓存 Gradle 依赖
3. 构建 Debug 和 Release APK
4. 上传构建产物

### 第三步：下载 APK
1. 进入仓库的 **Actions** 页面
2. 点击最新的构建记录
3. 在 **Artifacts** 部分下载：
   - `debug-apk`: 未签名调试版本
   - `release-apk`: 发布版本（未签名）

👉 想要直接得到“已签名”的 Release APK？请阅读 `SIGNING_QUICK_START.md`，内置两种方式：
- 无需配置 Secrets，CI 自动生成测试签名并签出可安装 APK（最简单）
- 配置你自己的固定签名（正式分发/内测推荐）

### 可选：配置签名发布
如需发布签名版本，在仓库 Settings → Secrets 中添加：
- `KEYSTORE_BASE64`: 签名文件的 Base64 编码
- `KEYSTORE_PASSWORD`: Keystore 密码  
- `KEY_ALIAS`: 密钥别名
- `KEY_PASSWORD`: 密钥密码

## 🏗️ 项目结构
- 📘 **[构建指南](BUILD_GUIDE.md)** - GitHub Actions 自动构建和本地构建详细说明
- 📗 **[测试指南](TEST_GUIDE.md)** - 功能测试、性能测试和兼容性测试
- 📙 **[图标资源](app/src/main/res/ICON_README.md)** - 应用图标生成和使用说明

## 🏗️ 项目结构
```
app/src/main/java/com/example/checkinmaster/
├── data/                    # 数据层
│   ├── model/              # 数据实体 (Task.kt)
│   ├── local/              # 本地数据源
│   │   ├── dao/           # Room DAO
│   │   └── database/      # 数据库定义
│   └── repository/        # 数据仓库
├── di/                     # Hilt 依赖注入模块
├── navigation/             # 导航图定义
├── ui/                     # UI 层
│   ├── theme/             # 主题、颜色、字体
│   ├── components/        # 可复用组件
│   └── screens/           # 页面
├── worker/                 # WorkManager 后台任务
├── MainActivity.kt         # 主 Activity
└── MainApplication.kt      # Hilt 应用入口
```

## 🔐 GitHub Actions 构建设置

### 1. 生成签名密钥
```bash
keytool -genkeypair -v -keystore checkinmaster.jks \
  -alias checkinmaster -keyalg RSA -keysize 2048 -validity 3650
```

### 2. 配置 GitHub Secrets
在仓库设置中添加以下 Secrets：
- `KEYSTORE_BASE64`: 密钥库文件的 Base64 编码
- `KEYSTORE_PASSWORD`: 密钥库密码  
- `KEY_ALIAS`: 密钥别名
- `KEY_PASSWORD`: 密钥密码

### 3. 触发构建
推送到 `main` 或 `develop` 分支将自动触发构建，生成签名的 Release APK。

详细步骤请参考 **[构建指南](BUILD_GUIDE.md)**。

## 🧪 测试
运行功能测试：
```bash
# 功能测试（需要设备/模拟器）
./gradlew connectedAndroidTest

# 单元测试
./gradlew test
```

完整测试流程请参考 **[测试指南](TEST_GUIDE.md)**。

## 🔮 后续优化规划
- 🎨 UI 样式细节优化与动效
- ⏰ 更丰富的提醒策略（重复提醒、工作日过滤）
- 🔍 任务分类与搜索功能
- ☁️ 云端同步（Firebase / 自建后端）
- 📤 任务配置导出与导入
- 🌍 多语言国际化支持
- 🧪 单元测试与 UI 测试覆盖

## 📄 许可证
本项目采用 [MIT License](LICENSE) 开源协议。

---

⭐ 如果这个项目对你有帮助，请给个 Star 支持一下！
