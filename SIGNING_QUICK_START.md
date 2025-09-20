# 签名快速上手（5分钟）

本指南教你用最少步骤完成“固定签名 + GitHub 自动构建 + 下载APK安装”。

> 已内置简化方案：即使你不配置任何密钥，工作流也会在 CI 中自动生成一个“测试 Keystore”，自动对 Release APK 进行签名，便于你立即安装测试。若你要正式发布，请改用自己的 Keystore 并配置到 GitHub Secrets。

---

## 方案A：先用内置测试签名（最简单）

- 直接推送代码或点击 Actions 手动触发构建。
- Workflow 会自动生成一个测试 keystore，并使用固定口令进行签名。
- 构建完成后，在 Actions → 最新工作流记录 → Artifacts 下载：
  - `release-apk`（已使用测试密钥签名）
  - `debug-apk`
- 可直接安装到手机进行功能测试。

注意：测试签名仅用于自测，不适合上架商店。

---

## 方案B：使用你自己的固定签名（推荐用于发布测试/内测）

### 第1步：在本地生成 JKS
```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```
- 建议牢记以下四个值：
  - Keystore 文件：`release-key.jks`
  - Keystore 密码：例如 `your-store-pass`
  - Key Alias：例如 `release`
  - Key Password：例如 `your-key-pass`

### 第2步：转换为 Base64
```bash
# 方式一（推荐，Linux/macOS）：不换行输出
base64 -w 0 release-key.jks > keystore.b64

# 方式二（通用）：删除换行
base64 -i release-key.jks | tr -d '\n' > keystore.b64
```
复制文件 `keystore.b64` 的全部内容。

### 第3步：配置 GitHub Secrets
在仓库页面：Settings → Secrets and variables → Actions → New repository secret，添加以下四个：
- `KEYSTORE_BASE64`：粘贴 `keystore.b64` 的全部内容
- `KEYSTORE_PASSWORD`：你的 Keystore 密码
- `KEY_ALIAS`：你的密钥别名
- `KEY_PASSWORD`：你的密钥密码
  - 若生成证书时在“Enter key password for <alias> (RETURN if same as keystore password):”直接回车，则与 Keystore 密码相同；此时可与 `KEYSTORE_PASSWORD` 填同一个值。

### 第4步：触发构建并下载 APK
- 推送任意代码变更或在 Actions 使用 `Run workflow` 触发
- 构建成功后，在 Artifacts 下载 `release-apk`
- 该 APK 已使用你的固定签名签名，可用于内测或分发

---

## 常见问题

- Q: 配置了 Secrets 但仍显示“未提供签名信息”？
  - A: 确认四个 Secrets 名称完全一致，且没有多余空格。重新触发构建。

- Q: 日志提示 `base64: invalid input`？
  - A: 通常是 Base64 内容中有换行或被粘贴时插入了空格/不可见字符。
  - 解决：用 `base64 -w 0 release-key.jks > keystore.b64` 重新生成，并完整拷贝内容。

- Q: 日志提示“Keystore password may be incorrect. Validation failed.”？
  - A: `KEYSTORE_PASSWORD` 可能填错，或 keystore 与密码不匹配。
  - 解决：本地用 `keytool -list -keystore release-key.jks -storepass <你的密码>` 验证；必要时用 `keytool -keypasswd` 重设密钥密码（可与 keystore 密码一致）。

- Q: 我能否换成自己的别名和密码？
  - A: 可以，确保四个 Secrets 与实际一致。

- Q: 需要在 Gradle 配置什么吗？
  - A: 已内置读取环境变量（`SIGNING_*`）逻辑。GitHub Actions 会把 Secrets 写入 `GITHUB_ENV`，Gradle 会自动使用。

---

## 参考
- 工作流文件：`.github/workflows/android-build.yml`
- Gradle 签名读取：`app/build.gradle.kts`（release buildType 内）
