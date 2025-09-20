# CheckInMaster 测试指南

本指南详细说明如何测试 CheckInMaster 应用的各项功能。

## 🧪 本地功能测试

### 环境准备
- Android 设备或模拟器（API 24+）
- 已安装调试版 APK
- 部分测试需要其他 App（如淘宝、支付宝等）

### 基础功能测试

#### 1. 应用启动与权限
```bash
# 安装测试版本
adb install app/build/outputs/apk/debug/app-debug.apk

# 启动应用
adb shell am start -n com.example.checkinmaster.debug/.MainActivity
```

**测试步骤：**
1. ✅ 应用正常启动
2. ✅ 权限弹窗显示（Android 13+）
3. ✅ 授予通知权限
4. ✅ 首页正常显示

#### 2. 任务管理功能
**创建任务：**
1. 点击 "+" 浮动按钮
2. 填写任务信息：
   - 名称：`测试签到任务`
   - 包名：`com.taobao.taobao`（如果有淘宝）
   - Deep Link：`taobao://`
   - 备注：`每日淘宝签到`
   - 优先级：选择 `2`
3. 点击"保存"
4. ✅ 返回首页并显示新任务

**编辑任务：**
1. 点击已创建的任务卡片
2. 修改任务名称
3. 点击"保存"
4. ✅ 修改成功并返回

**删除任务：**
1. 进入任务详情页
2. 点击"删除"按钮
3. ✅ 任务被删除并返回首页

#### 3. 任务跳转功能
**包名跳转测试：**
```bash
# 安装测试应用（如果没有）
adb install test_app.apk
```

1. 创建任务，填写正确的包名
2. 点击"去完成"按钮
3. ✅ 成功跳转到目标应用

**Deep Link 测试：**
1. 创建任务，填写 Deep Link：`https://www.google.com`
2. 点击"去完成"
3. ✅ 浏览器打开指定网页

**错误处理测试：**
1. 创建任务，填写不存在的包名：`com.nonexistent.app`
2. 点击"去完成"
3. ✅ 显示错误提示 Snackbar

#### 4. 提醒功能测试
**设置提醒：**
1. 编辑任务，在提醒时间输入：`2024-12-25 09:00`
2. 点击"设置提醒"
3. ✅ 显示"已设置提醒"

**清除提醒：**
1. 点击"清除提醒"
2. ✅ 输入框清空，显示"已清除提醒"

**通知测试：**
```bash
# 手动触发测试通知（需要 adb）
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
```

#### 5. 状态管理测试
**完成状态切换：**
1. 点击任务卡片上的复选框
2. ✅ 任务标记为已完成
3. ✅ 首页进度条更新

**进度统计：**
1. 创建多个任务
2. 标记部分为完成
3. ✅ 顶部显示正确的完成比例
4. ✅ 进度条正确显示

### 后台功能测试

#### 6. 每日重置测试
```bash
# 模拟时间变化（需要 root 权限）
adb shell su -c "date 122509002024.00"  # 设置到第二天
```

**或手动测试：**
1. 标记所有任务为完成
2. 等待到次日凌晨 00:05 后
3. ✅ 所有任务状态重置为未完成

#### 7. 通知点击测试
1. 设置一个即时提醒（1分钟后）
2. 等待通知出现
3. 点击通知
4. ✅ 应用打开并跳转到对应任务详情页

## 🚀 CI/CD 测试

### GitHub Actions 测试

#### 触发构建测试
```bash
# 本地提交触发 CI
git add .
git commit -m "test: trigger CI build"
git push origin main
```

**验证项目：**
1. ✅ Actions 页面显示新的构建任务
2. ✅ 构建环境正确设置（JDK 17）
3. ✅ Gradle 缓存生效
4. ✅ 依赖下载成功
5. ✅ 编译无错误
6. ✅ 签名配置正确（如果配置了）
7. ✅ APK 生成成功
8. ✅ Artifacts 上传成功

#### 下载和验证 APK
1. 从 Actions 页面下载 `release-apk`
2. 解压并安装 APK
3. ✅ 安装成功
4. ✅ 应用正常运行
5. ✅ 版本信息正确

### 构建环境测试
```bash
# 测试不同构建类型
./gradlew assembleDebug
./gradlew assembleRelease

# 清理后重新构建
./gradlew clean
./gradlew assembleDebug --info

# 测试依赖检查
./gradlew dependencies
```

## 🔧 性能测试

### 应用性能
```bash
# 启动时间测试
adb shell am start -W com.example.checkinmaster.debug/.MainActivity

# 内存使用测试  
adb shell dumpsys meminfo com.example.checkinmaster.debug
```

### 数据库性能
1. 创建大量任务（100+）
2. 测试列表滚动流畅度
3. 测试搜索响应速度
4. ✅ 操作响应时间 < 100ms

### 后台任务性能
1. 验证 WorkManager 任务不影响电池寿命
2. 检查内存泄漏
3. ✅ 后台资源使用合理

## 🛠️ 兼容性测试

### Android 版本测试
- ✅ Android 7.0 (API 24) - 最低支持版本
- ✅ Android 8.0 (API 26) - 通知渠道
- ✅ Android 10 (API 29) - 深色主题
- ✅ Android 13 (API 33) - 通知权限
- ✅ Android 14 (API 34) - 目标版本

### 设备类型测试
- ✅ 手机 (5-7 英寸)
- ✅ 平板 (8-12 英寸)  
- ✅ 折叠屏设备
- ✅ 不同分辨率 (hdpi, xhdpi, xxhdpi, xxxhdpi)

### 深色主题测试
1. 系统切换到深色主题
2. 重启应用
3. ✅ 界面适配深色主题

## 📋 测试清单

### 功能测试清单
- [ ] 应用启动正常
- [ ] 权限请求正常
- [ ] 任务 CRUD 操作正常
- [ ] 任务跳转功能正常
- [ ] 错误提示显示正常
- [ ] 提醒设置功能正常
- [ ] 通知功能正常
- [ ] 状态管理正常
- [ ] 每日重置功能正常
- [ ] 通知点击跳转正常

### 构建测试清单
- [ ] 本地 Debug 构建成功
- [ ] 本地 Release 构建成功
- [ ] CI 构建触发正常
- [ ] 依赖下载成功
- [ ] 签名配置正确
- [ ] APK 生成并上传
- [ ] 构建产物可正常安装

### 性能测试清单
- [ ] 启动时间 < 3 秒
- [ ] 内存使用合理
- [ ] 列表滚动流畅
- [ ] 数据库操作快速
- [ ] 后台任务不耗电

### 兼容性测试清单
- [ ] 最低版本兼容
- [ ] 目标版本兼容  
- [ ] 多种设备类型兼容
- [ ] 深色主题适配
- [ ] 多分辨率适配

## 🚨 问题排查

### 常见问题及解决方案

#### 应用崩溃
```bash
# 查看崩溃日志
adb logcat | grep "AndroidRuntime"
```

#### 通知不显示
1. 检查通知权限是否授予
2. 检查通知渠道是否创建
3. 验证 WorkManager 任务是否正确调度

#### 任务跳转失败
1. 验证目标应用是否已安装
2. 检查包名是否正确
3. 测试 Deep Link 是否有效

#### 构建失败
```bash
# 查看详细错误信息
./gradlew assembleDebug --stacktrace --info
```

### 调试工具
- **Android Studio Debugger**: 代码调试
- **Layout Inspector**: UI 调试  
- **Database Inspector**: Room 数据库调试
- **WorkManager Inspector**: 后台任务调试
- **adb logcat**: 运行时日志

---

🎯 **测试目标**: 确保应用在各种环境下都能稳定运行，提供良好的用户体验！