# JetBrains 插件市场上架指南

本指南将帮助你将 Another Git Worktree 插件上传到 JetBrains 插件市场。

## 📋 前置准备清单

### 1. 完善 plugin.xml 配置

在上架前，需要完善 `src/main/resources/META-INF/plugin.xml`：

```xml
<idea-plugin>
    <id>io.github.nwen.another_gitworktree</id>
    
    <!-- 插件名称（显示在市场） -->
    <name>Another Git Worktree</name>
    
    <!-- 供应商信息 -->
    <vendor email="your.email@example.com" url="https://github.com/yourusername">Your Name</vendor>
    
    <!-- 插件描述（支持 HTML，显示在插件详情页） -->
    <description><![CDATA[
        <h1>Another Git Worktree</h1>
        <p>A visual Git worktree management plugin for IntelliJ IDEA.</p>
        
        <h2>Features</h2>
        <ul>
            <li><b>Worktree List Display</b>: View all worktrees with path, branch, commit hash, and status</li>
            <li><b>Double-Click to Open</b>: Open worktree directories directly in IDEA</li>
            <li><b>Create Worktree</b>: Easy creation with branch selection and path suggestion</li>
            <li><b>Delete Worktree</b>: Safe deletion with confirmation dialogs</li>
            <li><b>Branch Search</b>: Real-time filtering for branch selection</li>
            <li><b>Smart Detection</b>: Automatically switches to already open projects</li>
        </ul>
        
        <h2>How to Use</h2>
        <ol>
            <li>Open Version Control tool window (Alt+9)</li>
            <li>Switch to "Worktree" tab</li>
            <li>Manage your Git worktrees visually</li>
        </ol>
        
        <p>For more information, visit the <a href="https://github.com/yourusername/another_gitworktree">GitHub repository</a>.</p>
    ]]></description>
    
    <!-- 更新说明（每次更新时填写） -->
    <change-notes><![CDATA[
        <h2>Version 1.0.0</h2>
        <ul>
            <li>Initial release</li>
            <li>Worktree list display with detailed information</li>
            <li>Create and delete worktree functionality</li>
            <li>Double-click to open worktree in IDEA</li>
            <li>Branch search and filtering</li>
            <li>Context menu support</li>
        </ul>
    ]]></change-notes>
    
    <depends>com.intellij.modules.platform</depends>
    <depends>Git4Idea</depends>
    
    <!-- 其他配置... -->
</idea-plugin>
```

### 2. 准备插件图标（可选但推荐）

图标要求：
- **40x40 像素** - pluginIcon.svg (已有)
- **80x80 像素** - pluginIcon@2x.svg (高分辨率)

位置：`src/main/resources/META-INF/`

### 3. 准备宣传材料

#### 必需材料：
- **插件描述**：清晰说明功能和用途
- **截图**（至少 2 张，建议 3-5 张）：
  - 尺寸：最小 1280x800，推荐 1920x1080
  - 格式：PNG 或 JPG
  - 内容建议：
    - Worktree 列表展示
    - 创建 Worktree 对话框
    - 分支选择对话框
    - 右键菜单功能

#### 推荐材料：
- **演示视频**（YouTube 或 Vimeo 链接）
- **详细的使用文档**链接
- **GitHub 仓库**链接

### 4. 设置正确的版本号

在 `build.gradle.kts` 中：

```kotlin
version = "1.0.0"  // 改为正式版本号，去掉 -SNAPSHOT
```

### 5. 构建最终版本

```bash
# 清理之前的构建
./gradlew clean

# 构建插件
./gradlew buildPlugin

# 生成的文件位置：
# build/distributions/another_gitworktree-1.0.0.zip
```

## 🚀 上架步骤

### 第一步：注册 JetBrains 账号

1. 访问 [JetBrains Marketplace](https://plugins.jetbrains.com/)
2. 点击右上角 **Login**
3. 使用以下方式之一注册/登录：
   - JetBrains Account
   - GitHub
   - Google
   - 其他 OAuth 提供商

### 第二步：创建插件页面

1. 登录后，点击右上角头像，选择 **Upload plugin**
2. 或直接访问：https://plugins.jetbrains.com/plugin/add

### 第三步：上传插件文件

1. **Upload Plugin**：
   - 点击 **Choose file** 按钮
   - 选择 `build/distributions/another_gitworktree-1.0.0.zip`
   - 等待上传和验证完成

2. **验证结果**：
   - ✅ 如果验证通过，会显示插件信息
   - ❌ 如果有错误，按照提示修复（通常是 plugin.xml 配置问题）

### 第四步：填写插件信息

#### Basic Information（基本信息）

- **Plugin Name**: Another Git Worktree
- **Plugin ID**: io.github.nwen.another_gitworktree（自动填充）
- **Category**: 选择 **Version Control Systems** 或 **Tools**
- **License**: 选择 **MIT** 或 **Apache 2.0**

#### Description（描述）

插件描述会从 plugin.xml 中读取，但你可以在这里编辑：

```
A visual Git worktree management plugin for IntelliJ IDEA that helps you manage multiple working trees efficiently.

Features:
• Visual worktree list with detailed information
• Create worktrees with branch selection and search
• Delete worktrees safely with confirmation
• Open worktree directories directly in IDEA
• Smart project detection to avoid duplicates
• Intuitive context menu support

Perfect for developers who work with multiple branches simultaneously!
```

#### Documentation（文档）

- **Website**: `https://github.com/yourusername/another_gitworktree`
- **Source Code**: `https://github.com/yourusername/another_gitworktree`
- **Issue Tracker**: `https://github.com/yourusername/another_gitworktree/issues`

#### Screenshots（截图）

1. 点击 **Add Screenshot**
2. 上传准备好的截图（建议 3-5 张）
3. 为每张截图添加标题和描述
4. 设置主要截图（第一张）

建议截图顺序：
1. **主功能展示** - Worktree 列表
2. **创建功能** - 创建对话框和分支选择
3. **操作演示** - 右键菜单和其他功能

#### Tags（标签）

添加相关标签，帮助用户找到你的插件：
- `git`
- `worktree`
- `version control`
- `productivity`
- `workflow`

#### Vendor Information（供应商信息）

- **Vendor Name**: 你的名字或组织名
- **Email**: 联系邮箱
- **Website**: 个人网站或 GitHub 主页

### 第五步：设置兼容性

#### Compatible IDE Versions（兼容的 IDE 版本）

这个通常从 build.gradle.kts 自动读取：

```kotlin
intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"  // 2025.1
            untilBuild = "253.*"  // 2025.3.*
        }
    }
}
```

确保设置合理的版本范围：
- **Since Build**: 最低支持的 IDEA 版本
- **Until Build**: 最高支持的版本（使用 `*` 允许小版本更新）

支持的 IDE：
- IntelliJ IDEA (Community & Ultimate)
- Android Studio
- PyCharm
- WebStorm
- 其他 JetBrains IDEs（自动兼容）

### 第六步：预览和提交

1. **Preview**: 点击 **Preview** 查看插件页面预览
2. **Review**: 仔细检查所有信息
3. **Submit**: 点击 **Submit for Review** 提交审核

## ⏱️ 审核流程

### 审核时间
- **初次提交**: 通常 1-3 个工作日
- **更新版本**: 通常 1-2 个工作日

### 审核内容
JetBrains 会检查：
- 插件是否正常工作
- 是否符合质量标准
- 是否违反政策
- 安全性检查

### 可能的结果

#### ✅ 审核通过
- 收到邮件通知
- 插件自动发布到市场
- 用户可以搜索和下载

#### ❌ 审核拒绝
- 收到拒绝原因邮件
- 根据反馈修改
- 重新提交

#### ⏸️ 需要更多信息
- 回复 JetBrains 的问题
- 提供额外信息或截图

## 📊 发布后管理

### 查看统计
1. 访问 [Plugin Dashboard](https://plugins.jetbrains.com/author/me)
2. 查看：
   - 下载量
   - 活跃用户
   - 评分和评论
   - 版本分布

### 更新插件

#### 1. 修改代码和版本号
```kotlin
// build.gradle.kts
version = "1.1.0"
```

#### 2. 更新 change-notes
```xml
<!-- plugin.xml -->
<change-notes><![CDATA[
    <h2>Version 1.1.0</h2>
    <ul>
        <li>New: Added feature X</li>
        <li>Fixed: Bug Y</li>
        <li>Improved: Performance optimization</li>
    </ul>
]]></change-notes>
```

#### 3. 构建并上传
```bash
./gradlew clean buildPlugin
```

在插件管理页面：
1. 点击 **Upload Update**
2. 选择新的 zip 文件
3. 提交审核

### 回复用户反馈
- 及时回复评论和问题
- 在 GitHub Issues 中跟踪 bug
- 收集功能建议

## 🎯 提高插件曝光度

### 1. 优化描述
- 使用清晰的标题
- 突出核心功能
- 添加使用场景
- 包含关键词

### 2. 添加演示视频
- 录制 1-2 分钟演示
- 上传到 YouTube
- 在插件页面添加链接

### 3. 社交媒体推广
- 在 Twitter、Reddit 分享
- 写博客介绍
- 在相关社区发布

### 4. 持续更新
- 定期发布新功能
- 快速修复 bug
- 响应用户需求

### 5. 收集评价
- 鼓励用户评分
- 回复每条评论
- 展示用户案例

## 📝 最佳实践

### DO ✅
- ✅ 提供清晰的截图和描述
- ✅ 保持插件定期更新
- ✅ 及时回复用户问题
- ✅ 编写详细的文档
- ✅ 测试多个 IDEA 版本
- ✅ 遵循 JetBrains 设计规范

### DON'T ❌
- ❌ 不要抄袭其他插件
- ❌ 不要包含恶意代码
- ❌ 不要在描述中夸大功能
- ❌ 不要长期不维护
- ❌ 不要忽视用户反馈
- ❌ 不要频繁推送无意义更新

## 🔗 有用的链接

### 官方文档
- [JetBrains Marketplace 文档](https://plugins.jetbrains.com/docs/marketplace/)
- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [插件上传指南](https://plugins.jetbrains.com/docs/marketplace/uploading-a-new-plugin.html)

### 工具和资源
- [Plugin Verifier](https://github.com/JetBrains/intellij-plugin-verifier) - 本地验证插件
- [Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template) - 官方模板

### 社区
- [JetBrains Platform Slack](https://plugins.jetbrains.com/slack)
- [Plugin Developers Forum](https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979-IntelliJ-IDEA-Open-API-and-Plugin-Development)

## 💡 常见问题

### Q: 审核被拒绝了怎么办？
A: 仔细阅读拒绝原因，修复问题后重新提交。常见问题：描述不清晰、功能不完整、存在 bug。

### Q: 可以先发布测试版�吗？
A: 可以，在版本号中使用 `1.0.0-beta` 或 `1.0.0-rc1`，用户会看到是预览版本。

### Q: 如何定价插件？
A: 首次发布建议免费。如果后续想收费，可以改为 Freemium 模式或完全付费。

### Q: 更新后多久生效？
A: 审核通过后立即生效。用户会在 IDE 中收到更新通知。

### Q: 支持哪些支付方式？
A: 如果插件收费，JetBrains 支持信用卡、PayPal 等。收入按月结算。

## 🎉 恭喜！

完成这些步骤后，你的插件就会出现在 JetBrains Marketplace 上，全球数百万开发者都可以使用你的插件了！

祝你的插件获得成功！🚀

---

**需要帮助？**
- 查看 [官方文档](https://plugins.jetbrains.com/docs/marketplace/)
- 加入 [Slack 社区](https://plugins.jetbrains.com/slack)
- 提交 [GitHub Issue](https://github.com/yourusername/another_gitworktree/issues)

