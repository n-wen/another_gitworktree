# 🚀 插件上传指南

恭喜！你的插件已经准备好上传到 JetBrains Marketplace 了！

## ✅ 已完成的准备工作

- [x] ✅ 更新个人信息（wenning, n-wen@outlook.com, n-wen）
- [x] ✅ 准备了 3 张高质量截图
- [x] ✅ 设置正式版本号（1.0.0）
- [x] ✅ 构建插件成功

## 📦 插件文件位置

```
build/distributions/another_gitworktree-1.0.0.zip
```

文件大小约：~200KB

## 🌐 开始上传

### 步骤 1：访问 JetBrains Marketplace

打开浏览器，访问：
```
https://plugins.jetbrains.com/plugin/add
```

或者：
1. 访问 https://plugins.jetbrains.com/
2. 点击右上角 **Login**
3. 登录后，点击头像 → **Upload plugin**

### 步骤 2：登录账号

使用以下方式之一登录：
- ✅ **GitHub**（推荐，因为你的代码在 GitHub）
- Google
- JetBrains Account

### 步骤 3：上传插件 ZIP

1. 点击 **Choose file** 按钮
2. 选择文件：`build/distributions/another_gitworktree-1.0.0.zip`
3. 等待上传和验证（约 30 秒）
4. 如果验证通过，继续下一步

### 步骤 4：填写插件信息

#### Basic Information（基本信息）

| 字段 | 填写内容 |
|------|---------|
| **Plugin Name** | Another Git Worktree（自动填充） |
| **Plugin ID** | io.github.nwen.another_gitworktree（自动填充） |
| **Category** | 选择 **Version Control Systems** |
| **License** | 选择 **MIT License** |

#### Description（描述）

描述会从 plugin.xml 自动读取，包含：
- ✅ 完整的功能列表（7 大特性）
- ✅ 使用说明
- ✅ 适用场景
- ✅ GitHub 链接

**检查一下，确认显示正确即可。**

#### Documentation（文档链接）

| 字段 | 填写内容 |
|------|---------|
| **Website** | https://github.com/n-wen/another_gitworktree |
| **Source Code** | https://github.com/n-wen/another_gitworktree |
| **Issue Tracker** | https://github.com/n-wen/another_gitworktree/issues |

#### Screenshots（截图上传）

上传你准备好的 3 张截图：

1. **第一张（主截图）**：
   - 文件：`docs/screenshots/worktree-list.png`
   - 标题：`Worktree List Display`
   - 描述：`View all worktrees with detailed information in a clean table interface`

2. **第二张**：
   - 文件：`docs/screenshots/create-worktree.png`
   - 标题：`Create Worktree Dialog`
   - 描述：`Easy worktree creation with path suggestions`

3. **第三张**：
   - 文件：`docs/screenshots/branch-selection.png`
   - 标题：`Branch Selection with Search`
   - 描述：`Real-time branch filtering for quick selection`

#### Tags（标签）

添加以下标签（帮助用户找到你的插件）：
```
git
worktree
version control
productivity
workflow
```

#### Vendor Information（供应商信息）

会从 plugin.xml 自动读取：
- **Name**: wenning
- **Email**: n-wen@outlook.com
- **Website**: https://github.com/n-wen

### 步骤 5：Compatible IDE Versions（兼容性）

这个会从 build.gradle.kts 自动读取：
- **Since Build**: 251（IDEA 2025.1+）
- **Until Build**: 留空（支持后续版本）

支持的 IDE：
- ✅ IntelliJ IDEA Community
- ✅ IntelliJ IDEA Ultimate
- ✅ Android Studio
- ✅ 其他 JetBrains IDEs（自动兼容）

### 步骤 6：预览和提交

1. 点击 **Preview** 查看插件页面预览
2. 仔细检查所有信息：
   - [ ] 描述清晰完整
   - [ ] 截图显示正确
   - [ ] 链接都可以访问
   - [ ] 标签准确
3. 确认无误后，点击 **Submit for Review**

## ⏱️ 等待审核

### 审核时间
- **预计时间**：1-3 个工作日
- **通知方式**：邮件通知（n-wen@outlook.com）

### 审核中可能遇到的情况

#### ✅ 审核通过
- 收到通过邮件
- 插件自动发布到市场
- 用户可以搜索和安装
- 你可以在 Dashboard 查看统计

#### ❌ 审核拒绝
常见拒绝原因：
1. 描述不够清晰
2. 截图质量不高
3. 功能存在 bug
4. 违反市场政策

**如果被拒绝**：
1. 仔细阅读拒绝原因
2. 修复问题
3. 重新构建插件
4. 再次提交

#### ⏸️ 需要更多信息
- JetBrains 可能会要求补充说明
- 及时回复邮件
- 提供所需信息

## 📊 发布后操作

### 1. 查看插件页面

发布后，你的插件会有一个唯一链接：
```
https://plugins.jetbrains.com/plugin/[ID]/another-git-worktree
```

### 2. 更新 README

在 README.md 中添加 Marketplace 徽章：

```markdown
[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/[插件ID].svg)](https://plugins.jetbrains.com/plugin/[插件ID]/another-git-worktree)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/[插件ID].svg)](https://plugins.jetbrains.com/plugin/[插件ID])
```

### 3. 推送代码到 GitHub

```bash
git push origin main
```

### 4. 创建 GitHub Release

```bash
# 创建 tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# 在 GitHub 上创建 Release
# - 访问 https://github.com/n-wen/another_gitworktree/releases/new
# - 选择 tag v1.0.0
# - 填写 Release notes（从 plugin.xml 的 change-notes 复制）
# - 上传 another_gitworktree-1.0.0.zip
# - 发布
```

### 5. 宣传插件

- [ ] 在 Twitter 分享
- [ ] 在 Reddit /r/IntelliJIDEA 发布
- [ ] 在相关技术社区分享
- [ ] 写一篇博客介绍
- [ ] 在 V2EX 或掘金分享（中文社区）

### 6. 监控和维护

访问 Plugin Dashboard：
```
https://plugins.jetbrains.com/author/me
```

查看：
- 📈 下载量
- 👥 活跃用户数
- ⭐ 评分和评论
- 📊 版本分布

## 💡 常见问题

### Q: 上传时验证失败怎么办？
A: 常见原因：
- plugin.xml 配置有误
- 版本号不合法
- 缺少必要信息

查看错误提示，修复后重新构建上传。

### Q: 可以修改已提交但未审核的插件吗？
A: 可以。在审核期间可以撤回并重新提交。

### Q: 审核被拒绝后可以立即重新提交吗？
A: 可以，修复问题后随时可以重新提交。

### Q: 插件发布后可以更新吗？
A: 可以。更新版本号，重新构建，然后上传新版本即可。

## 📞 需要帮助？

- **详细指南**：[MARKETPLACE_GUIDE.md](MARKETPLACE_GUIDE.md)
- **官方文档**：https://plugins.jetbrains.com/docs/marketplace/
- **社区支持**：https://plugins.jetbrains.com/slack
- **GitHub Issues**：https://github.com/n-wen/another_gitworktree/issues

## 🎉 准备好了！

所有准备工作已完成，现在就可以上传你的插件了！

**上传地址**：https://plugins.jetbrains.com/plugin/add

祝你上架成功！🚀

---

**提示**：上传过程中遇到任何问题，请查看 MARKETPLACE_GUIDE.md 获取更详细的帮助。

