# 快速发布清单

在上传插件到 JetBrains Marketplace 之前，请检查以下项目。

## ✅ 发布前必做

### 1. 更新个人信息
- [ ] 替换 `plugin.xml` 中的 `your.email@example.com` 为你的邮箱
- [ ] 替换 `plugin.xml` 中的 `Your Name` 为你的名字
- [ ] 替换 `plugin.xml` 中的 `yourusername` 为你的 GitHub 用户名
- [ ] 更新 README.md 和 README.zh-CN.md 中的 `yourusername`
- [ ] 更新 README.md 和 README.zh-CN.md 中的 `your.email@example.com`

### 2. 准备截图
- [ ] 创建 `docs/screenshots/` 目录
- [ ] 截取至少 3 张高质量截图（推荐 1920x1080）：
  - [ ] `worktree-list.png` - Worktree 列表界面
  - [ ] `create-worktree.png` - 创建对话框
  - [ ] `branch-selection.png` - 分支选择对话框
- [ ] （可选）录制演示视频并上传到 YouTube

### 3. 设置版本号
- [ ] 在 `build.gradle.kts` 中将版本号改为 `1.0.0`（去掉 `-SNAPSHOT`）

### 4. 构建最终版本
```bash
./gradlew clean
./gradlew buildPlugin
```
- [ ] 确认构建成功，在 `build/distributions/` 目录下生成了 zip 文件

### 5. 测试
- [ ] 在新的 IDEA 实例中安装并测试插件
- [ ] 确认所有功能正常工作
- [ ] 检查是否有错误日志

## 📦 上传步骤

### 1. 注册/登录
- [ ] 访问 https://plugins.jetbrains.com/
- [ ] 使用 GitHub/Google/JetBrains 账号登录

### 2. 上传插件
- [ ] 点击 Upload plugin 或访问 https://plugins.jetbrains.com/plugin/add
- [ ] 上传 `build/distributions/another_gitworktree-1.0.0.zip`
- [ ] 等待验证完成

### 3. 填写信息
- [ ] **Category**: Version Control Systems 或 Tools
- [ ] **License**: MIT
- [ ] **Tags**: git, worktree, version control, productivity, workflow
- [ ] **Screenshots**: 上传准备好的截图
- [ ] **Documentation**: 添加 GitHub 链接

### 4. 提交审核
- [ ] 预览插件页面
- [ ] 检查所有信息正确
- [ ] 点击 Submit for Review

## 📋 需要填写的信息速查

### Plugin Information
```
Name: Another Git Worktree
Category: Version Control Systems
License: MIT
```

### Links
```
Website: https://github.com/yourusername/another_gitworktree
Source Code: https://github.com/yourusername/another_gitworktree
Issue Tracker: https://github.com/yourusername/another_gitworktree/issues
```

### Tags
```
git, worktree, version control, productivity, workflow
```

### Short Description (用于搜索结果)
```
Visual Git worktree management for IntelliJ IDEA. Create, delete, and manage worktrees with ease.
```

## ⏱️ 预期时间线

- **准备工作**: 1-2 小时（截图、信息更新）
- **上传和填写**: 30 分钟
- **审核等待**: 1-3 个工作日
- **发布后**: 立即可见

## 🎯 审核后

### 审核通过
- [ ] 在社交媒体分享
- [ ] 在相关社区发布
- [ ] 监控下载量和反馈

### 审核拒绝
- [ ] 阅读拒绝原因
- [ ] 修复问题
- [ ] 重新提交

## 📞 需要帮助？

- 查看详细指南：[MARKETPLACE_GUIDE.md](../MARKETPLACE_GUIDE.md)
- JetBrains 官方文档：https://plugins.jetbrains.com/docs/marketplace/
- 提交 Issue：https://github.com/yourusername/another_gitworktree/issues

---

**准备好了吗？开始上传你的插件吧！** 🚀

