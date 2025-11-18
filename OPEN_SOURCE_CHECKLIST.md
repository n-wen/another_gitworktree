# Open Source Checklist

此文档用于跟踪项目开源所需的准备工作。

## ✅ 已完成

### 文档
- [x] 英文 README.md（主文档）
- [x] 中文 README.zh-CN.md
- [x] 双语文档互相链接
- [x] LICENSE 文件（MIT License）
- [x] CONTRIBUTING.md 贡献指南
- [x] 功能特性说明
- [x] 安装和使用说明
- [x] 开发环境设置说明
- [x] FAQ 常见问题

### GitHub 配置
- [x] Issue 模板（Bug 报告）
- [x] Issue 模板（功能请求）
- [x] Pull Request 模板
- [x] GitHub Actions CI 工作流
- [x] .gitignore 配置
- [x] .gitattributes 配置

### 代码质量
- [x] 核心功能实现完整
- [x] 错误处理完善
- [x] 线程安全（EDT 和后台线程正确使用）
- [x] 代码注释清晰
- [x] Git 提交历史清晰

## 📋 建议完成（发布前）

### 文档完善
- [ ] 添加截图到 README（创建 `docs/screenshots/` 目录）
  - [ ] Worktree 列表截图
  - [ ] 创建 Worktree 对话框截图
  - [ ] 分支选择对话框截图
  - [ ] 右键菜单截图
- [ ] 录制演示 GIF/视频
- [x] 更新 README 中的占位符：✅
  - [x] 替换 `yourusername` 为实际 GitHub 用户名 (n-wen)
  - [x] 替换 `your.email@example.com` 为实际联系邮箱 (n-wen@outlook.com)
  - [x] 替换 `YourCompany` 为实际信息 (wenning)
- [ ] 添加更多使用示例和场景说明

### 插件配置
- [ ] 更新 `plugin.xml` 中的插件描述（当前是占位符）
- [ ] 更新 vendor 信息
- [ ] 添加插件图标（如果需要）
- [ ] 考虑添加 change-notes（更新说明）

### 代码优化
- [ ] 添加单元测试
- [ ] 添加集成测试
- [ ] 代码审查和重构（如有必要）
- [ ] 性能优化（如有必要）
- [ ] 国际化支持（i18n）考虑

### 发布准备
- [ ] 确定版本号（当前为 1.0-SNAPSHOT）
- [ ] 创建 GitHub Release
- [ ] 准备 JetBrains Marketplace 发布材料
- [ ] 准备宣传文案

## 🚀 发布步骤建议

### 1. GitHub 发布
```bash
# 1. 更新版本号（在 build.gradle.kts）
# 2. 创建 release tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# 3. 在 GitHub 创建 Release
# - 使用 tag v1.0.0
# - 附加编译好的 plugin zip
# - 填写 release notes
```

### 2. JetBrains Marketplace 发布
- 访问 https://plugins.jetbrains.com/
- 注册/登录账号
- 上传插件 zip 文件
- 填写插件信息：
  - 名称、描述
  - 截图和演示
  - 分类和标签
  - 支持的 IDE 版本
- 等待审核

### 3. 推广
- [ ] 在相关社区分享（Reddit, Twitter, etc.）
- [ ] 写一篇博客介绍插件
- [ ] 在 IDEA 用户群分享

## 📊 项目统计

- **编程语言**: Kotlin
- **代码文件**: 4 个 Kotlin 文件
- **文档文件**: 3 个 Markdown 文件
- **总提交数**: 10+ commits
- **功能完整度**: ~90%（核心功能完整，待优化）

## 📝 备注

### 当前状态
项目已经具备开源的基本条件，核心功能完整，文档齐全。主要需要：
1. 添加截图使文档更生动
2. 完善 plugin.xml 的描述信息
3. 考虑是否需要单元测试

### 下一步建议
1. **短期**（发布前必做）：
   - 添加截图
   - 更新所有占位符信息
   - 完善插件描述

2. **中期**（发布后逐步完善）：
   - 添加测试
   - 根据用户反馈优化功能
   - 国际化支持

3. **长期**（功能扩展）：
   - 更多 worktree 操作（如移动、锁定等）
   - 与其他 Git 功能集成
   - 工作流自动化

## ✨ 致谢

感谢你开源此项目！

