# CheckInMaster 构建指南

本指南将详细说明如何通过 GitHub Actions 自动构建 CheckInMaster Android 应用的 APK 文件。

## 🚀 GitHub Actions 自动构建

### 1. 准备签名密钥

#### 生成签名密钥文件
```bash
# 在本地生成签名密钥
keytool -genkeypair -v -keystore checkinmaster.jks \
  -alias checkinmaster \
  -keyalg RSA \
  -keysize 2048 \
  -validity 3650 \
  -dname "CN=CheckInMaster,O=YourOrg,C=US" \
  -storepass your_store_password \
  -keypass your_key_password
```

#### 转换为 Base64 格式
```bash
# Linux/macOS
base64 checkinmaster.jks > keystore_base64.txt

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("checkinmaster.jks")) > keystore_base64.txt
```

### 2. 配置 GitHub Secrets

在 GitHub 仓库中配置以下 Secrets（Settings → Secrets and variables → Actions）：

| Secret 名称 | 描述 | 示例值 |
|------------|------|--------|
| `KEYSTORE_BASE64` | 密钥库文件的 Base64 编码 | `MIIKowIBAzCCCmcGCSqGSIb3DQE...` |
| `KEYSTORE_PASSWORD` | 密钥库密码 | `your_store_password` |
| `KEY_ALIAS` | 密钥别名 | `checkinmaster` |
| `KEY_PASSWORD` | 密钥密码 | `your_key_password` |

### 3. 触发构建

#### 自动触发
构建会在以下情况自动触发：
- 推送到 `main` 或 `develop` 分支
- 创建针对 `main` 或 `develop` 分支的 Pull Request

#### 手动触发
1. 进入 GitHub 仓库页面
2. 点击 "Actions" 标签
3. 选择 "Android CI Build" 工作流
4. 点击 "Run workflow"

### 4. 下载构建产物

1. 构建完成后，进入 Actions 页面
2. 点击对应的构建记录
3. 在 "Artifacts" 部分下载 `release-apk`
4. 解压后获得 `app-release.apk` 文件

## 🛠️ 本地构建

### 环境要求
- **JDK 17** 或更高版本
- **Android SDK** (API Level 34)
- **Git**

### 克隆仓库
```bash
git clone https://github.com/yourusername/task-notify.git
cd task-notify
```

### 构建 Debug 版本
```bash
# Linux/macOS
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

### 构建 Release 版本（需要签名）
```bash
# 方法1：使用环境变量
export SIGNING_STORE_FILE=/path/to/your/keystore.jks
export SIGNING_STORE_PASSWORD=your_store_password
export SIGNING_KEY_ALIAS=your_key_alias
export SIGNING_KEY_PASSWORD=your_key_password

./gradlew assembleRelease

# 方法2：生成未签名的 APK
./gradlew assembleRelease
# 然后手动签名
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore /path/to/keystore.jks \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  your_key_alias
```

### 构建产物位置
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`

## 🔧 故障排除

### 常见问题

#### 1. 构建失败：找不到 gradlew
```bash
# 如果 gradlew 文件不存在或无执行权限
chmod +x gradlew
```

#### 2. 签名配置错误
检查 GitHub Secrets 是否正确配置：
- Base64 编码是否完整
- 密钥密码是否正确
- 别名是否匹配

#### 3. 内存不足
在 `gradle.properties` 中增加内存配置：
```properties
org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC
```

#### 4. 依赖下载失败
确保网络连接正常，或配置代理：
```bash
# 设置 Gradle 代理
./gradlew assembleDebug -Dhttp.proxyHost=proxy.company.com -Dhttp.proxyPort=8080
```

### 日志调试
```bash
# 查看详细构建日志
./gradlew assembleRelease --info --stacktrace

# 查看依赖信息
./gradlew dependencies
```

## 📱 安装与测试

### 安装 APK
```bash
# 通过 ADB 安装
adb install app/build/outputs/apk/debug/app-debug.apk

# 或直接在设备上安装
# 1. 允许"未知来源"安装
# 2. 复制 APK 到设备
# 3. 点击安装
```

### 功能验证
1. **权限检查**: 确认通知权限已获取
2. **任务管理**: 创建、编辑、删除任务
3. **跳转功能**: 测试包名和 Deep Link 跳转
4. **后台任务**: 验证每日重置和提醒通知
5. **数据持久化**: 重启应用后数据保持

## 🚀 优化建议

### 构建性能优化
1. **启用 Gradle 缓存**:
   ```properties
   org.gradle.caching=true
   org.gradle.parallel=true
   ```

2. **使用 Build Cache**:
   ```bash
   ./gradlew assembleRelease --build-cache
   ```

3. **配置 CI 缓存**:
   ```yaml
   # 在 GitHub Actions 中已配置 Gradle 缓存
   - uses: actions/setup-java@v3
     with:
       cache: gradle
   ```

### 安全最佳实践
1. 不要在代码中硬编码密钥
2. 定期更新签名密钥
3. 使用不同的密钥用于 Debug 和 Release
4. 备份签名密钥文件

## 📋 清单

构建前检查：
- [ ] JDK 17+ 已安装
- [ ] Android SDK 配置正确
- [ ] 签名密钥已准备
- [ ] GitHub Secrets 已配置
- [ ] 网络连接正常

构建后验证：
- [ ] APK 文件生成成功
- [ ] 文件大小合理（通常 5-20MB）
- [ ] 安装无错误
- [ ] 基本功能正常
- [ ] 权限请求正常

---

🎉 **恭喜！** 您现在可以通过 GitHub Actions 自动构建 CheckInMaster 应用了！