# 发布到 JetBrains Marketplace - 快速指南

## 📚 文档导航

我已经为你准备了完整的上架材料：

1. **[MARKETPLACE_GUIDE.md](../MARKETPLACE_GUIDE.md)** - 完整详细指南
   - 注册账号流程
   - 上传步骤
   - 审核流程
   - 最佳实践
   - 常见问题

2. **[QUICK_PUBLISH_CHECKLIST.md](QUICK_PUBLISH_CHECKLIST.md)** - 快速清单
   - 发布前必做事项
   - 上传步骤速查
   - 需要填写的信息

3. **[docs/screenshots/README.md](screenshots/README.md)** - 截图指南
   - 截图要求
   - 截图技巧
   - 文件命名规范

## 🚀 5 分钟快速开始

### 步骤 1：更新个人信息（5 分钟）

在以下文件中替换占位符：

#### `plugin.xml`
```xml
<vendor email="你的邮箱" url="https://github.com/你的用户名">你的名字</vendor>
```

#### `README.md` 和 `README.zh-CN.md`
- 将所有 `yourusername` 替换为你的 GitHub 用户名
- 将 `your.email@example.com` 替换为你的邮箱

一键替换命令（Windows Git Bash / Linux / Mac）：
```bash
# 设置你的信息
YOUR_NAME="Your Name"
YOUR_EMAIL="your.email@example.com"
YOUR_GITHUB="yourusername"

# 批量替换（先备份！）
find . -type f -name "*.md" -o -name "*.xml" | xargs sed -i "s/yourusername/$YOUR_GITHUB/g"
find . -type f -name "*.md" -o -name "*.xml" | xargs sed -i "s/your.email@example.com/$YOUR_EMAIL/g"
find . -type f -name "*.xml" | xargs sed -i "s/Your Name/$YOUR_NAME/g"
```

### 步骤 2：准备截图（30 分钟）

在 IDEA 中：
1. 打开一个 Git 项目
2. 打开 Version Control tool window (Alt+9)
3. 切换到 Worktree tab
4. 截取以下界面：
   - Worktree 列表
   - 创建对话框
   - 分支选择对话框

保存到 `docs/screenshots/` 目录。

### 步骤 3：构建插件（2 分钟）

```bash
# 更新版本号
# 编辑 build.gradle.kts，将 version = "1.0-SNAPSHOT" 改为 version = "1.0.0"

# 清理并构建
./gradlew clean buildPlugin

# 生成的文件：
# build/distributions/another_gitworktree-1.0.0.zip
```

### 步骤 4：上传到 Marketplace（10 分钟）

1. 访问 https://plugins.jetbrains.com/
2. 登录（推荐使用 GitHub 账号）
3. 点击 **Upload plugin**
4. 上传 `build/distributions/another_gitworktree-1.0.0.zip`
5. 填写信息：
   - **Category**: Version Control Systems
   - **License**: MIT
   - **Tags**: git, worktree, version control, productivity
6. 上传截图
7. 提交审核

### 步骤 5：等待审核（1-3 天）

审核通过后：
- 插件会自动发布
- 用户可以搜索和下载
- 你会收到邮件通知

## 📋 信息速查表

### 需要填写的信息

| 字段 | 内容 |
|------|------|
| Plugin Name | Another Git Worktree |
| Category | Version Control Systems |
| License | MIT |
| Tags | git, worktree, version control, productivity, workflow |
| Website | https://github.com/你的用户名/another_gitworktree |
| Source Code | 同上 |
| Issue Tracker | https://github.com/你的用户名/another_gitworktree/issues |

### 插件描述（已在 plugin.xml 中）

plugin.xml 中已经包含了完整的描述和更新说明，上传时会自动读取。

## ✅ 发布前最终检查

- [ ] 更新了所有个人信息（邮箱、名字、GitHub 用户名）
- [ ] 准备了至少 3 张高质量截图
- [ ] 版本号设置为 1.0.0（去掉 -SNAPSHOT）
- [ ] 成功构建了插件 zip 文件
- [ ] 在本地 IDEA 中测试了插件功能
- [ ] 提交并推送了所有代码到 GitHub

## 🎯 预期结果

### 审核通过后
- ✅ 插件出现在 JetBrains Marketplace
- ✅ 用户可以在 IDEA 的 Plugins 中搜索并安装
- ✅ 获得唯一的插件页面链接
- ✅ 开始统计下载量和用户数

### Marketplace 页面示例
```
https://plugins.jetbrains.com/plugin/[ID]/another-git-worktree
```

## 📊 发布后

### 监控指标
访问 Plugin Dashboard 查看：
- 下载量
- 活跃用户数
- 评分和评论
- 版本分布

### 推广建议
1. 在 GitHub README 中添加 Marketplace 徽章
2. 在社交媒体分享（Twitter、Reddit）
3. 在相关开发社区发布
4. 写一篇博客介绍插件

### 持续维护
- 及时回复用户评论
- 快速修复 bug
- 定期发布更新
- 收集用户反馈

## 🆘 需要帮助？

- **详细指南**: 查看 [MARKETPLACE_GUIDE.md](../MARKETPLACE_GUIDE.md)
- **官方文档**: https://plugins.jetbrains.com/docs/marketplace/
- **社区支持**: https://plugins.jetbrains.com/slack
- **问题反馈**: 在 GitHub Issues 中提问

## 🎉 准备好了吗？

如果你已经完成了上述步骤，现在就可以上传你的插件了！

祝发布成功！🚀

---

**提示**: 第一次发布可能需要更长的审核时间，请耐心等待。审核通过后，后续更新会更快。

