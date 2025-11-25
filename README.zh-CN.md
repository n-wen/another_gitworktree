# Another Git Worktree

[English](README.md) | 中文文档

一个用于 IntelliJ IDEA 的 Git Worktree 管理插件，提供可视化界面来管理 Git worktree。

## 功能特性

### 1. Worktree 列表展示
- 在 Version Control tool window 中添加 "Worktree" tab
- 显示所有 worktree 的详细信息：
  - 路径
  - 分支名
  - 提交 hash（前8位）
  - 状态（正常/已锁定）
- 支持手动刷新列表

### 2. 双击打开 Worktree
- 双击表格行即可在 IDEA 中打开对应的 worktree 目录
- 智能检测：如果项目已打开，自动切换到对应窗口
- 避免重复打开同一项目

### 3. 创建 Worktree
- 顶部工具栏提供"创建 Worktree"按钮
- **分支选择对话框**：
  - 显示所有可用分支列表
  - 实时搜索过滤（不区分大小写）
  - 支持键盘导航（上下箭头键）
  - 支持双击快速选择
  - 默认选中当前分支
  - 支持直接输入新分支名
- 路径输入对话框：
  - 自动建议默认路径（项目目录同级 + 分支名）
  - 支持自定义路径
- 自动刷新列表

### 4. 删除 Worktree
- **两种删除方式**：
  - 顶部工具栏"删除 Worktree"按钮
  - 右键菜单"删除 Worktree"选项
- 删除前显示确认对话框
- 显示要删除的路径和分支信息
- 使用 `--force` 选项处理未提交的更改
- 自动刷新列表

### 5. 右键菜单
- 右键点击表格行显示上下文菜单
- **菜单选项**：
  - 打开目录：在 IDEA 中打开 worktree
  - 删除 Worktree：删除选中的 worktree

## 安装

### 方式一：从 JetBrains Marketplace 安装（推荐）

1. 打开 IntelliJ IDEA
2. 进入 **File** → **Settings** → **Plugins**（macOS 上为 **IntelliJ IDEA** → **Preferences** → **Plugins**）
3. 点击 **Marketplace** 标签页
4. 搜索 "Another Git Worktree"
5. 点击 **Install** 并重启 IDEA

### 方式二：从 GitHub Releases 下载

1. 访问 [Releases 页面](https://github.com/n-wen/another_gitworktree/releases)
2. 下载最新的 `another_gitworktree-*.zip` 文件
3. 安装插件：
   - 打开 IntelliJ IDEA
   - 进入 **File** → **Settings** → **Plugins**（macOS 上为 **IntelliJ IDEA** → **Preferences** → **Plugins**）
   - 点击 ⚙️ 图标 → **Install Plugin from Disk...**
   - 选择下载的 zip 文件
   - 点击 **OK** 并重启 IDEA

### 方式三：从源码构建

1. 克隆仓库：
```bash
git clone https://github.com/n-wen/another_gitworktree.git
cd another_gitworktree
```

2. 构建插件：
```bash
./gradlew buildPlugin
```

3. 安装插件：
   - 打开 IntelliJ IDEA
   - 进入 **File** → **Settings** → **Plugins** → ⚙️ → **Install Plugin from Disk...**
   - 选择 `build/distributions/another_gitworktree-*.zip`

## 使用方法

### 查看 Worktree 列表
1. 打开 Version Control tool window（Alt+9 或 View → Tool Windows → Version Control）
2. 切换到 "Worktree" tab
3. 查看所有 worktree 的详细信息

**注意**：Worktree tab 是不可关闭的，确保你始终可以访问它

### 创建 Worktree
1. 点击顶部的"创建 Worktree"按钮
2. 在分支选择对话框中：
   - 在搜索框中输入分支名进行过滤
   - 从列表中选择分支，或直接输入新分支名
   - 双击列表项或点击确定按钮确认
3. 输入 worktree 目录路径（或使用默认路径）
4. 确认创建

### 打开 Worktree
- **双击**表格行即可在 IDEA 中打开对应的 worktree 目录
- **右键**点击行，选择"打开目录"

### 删除 Worktree
- **方式一**：选中表格行，点击顶部的"删除 Worktree"按钮
- **方式二**：右键点击表格行，选择"删除 Worktree"
- 确认删除对话框中查看信息后确认

### 刷新列表
- 点击顶部的"刷新"按钮手动刷新 worktree 列表
- 创建或删除 worktree 后会自动刷新

## 技术特性

- **线程安全**：文件系统操作在后台线程执行，UI 操作在 EDT 上执行
- **错误处理**：完善的错误处理和用户提示
- **用户体验**：
  - 禁用表格编辑，避免误操作
  - 智能路径建议
  - 确认对话框防止误删除
  - 实时搜索过滤
- **Git 集成**：使用 Git4Idea 插件 API

## 开发

### 运行开发环境
```bash
./gradlew runIde
```

### 构建插件
```bash
./gradlew buildPlugin
```

### 编译检查
```bash
./gradlew compileKotlin
```

## 系统要求

- IntelliJ IDEA 2023.3+ 或其他 IntelliJ 平台 IDE
- Git 2.15+ （支持 `git worktree` 命令）

## 依赖

- Kotlin
- IntelliJ Platform SDK
- Git4Idea 插件

## 贡献

欢迎提交 Issue 和 Pull Request！

### 开发设置

1. Fork 本仓库
2. 创建你的功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交你的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启一个 Pull Request

## 许可证

[MIT License](LICENSE)

## 更新日志

### v1.0.5 (最新版本)
- ✨ 改进远程分支选择逻辑
- 🔄 优化分支验证，移除不必要的远程分支检查
- 🧹 清理分支名称中的特殊字符（*、+）
- 📝 简化 worktree 创建过程中的分支处理
- 🎯 增强选择远程分支时的用户体验

### v1.0.2
- ✨ 扩展兼容性至 IDEA 2023.3+（无上限）
- 🛡️ 修复：主 worktree 现在无法被删除（按钮和菜单项已禁用）
- 🔄 改进：删除 worktree 前自动关闭已打开的项目
- 📁 增强：更好地处理删除时被锁定的目录
- ⚠️ 改进：更详细的 worktree 操作错误提示
- 🎯 修复：首次打开时自动加载 worktree 列表（无需手动刷新）

### v1.0.1
- ✨ 修复：将所有 UI 文本改为英文，面向国际用户
- 🌐 改进：统一整个界面的语言

### v1.0.0
- ✨ 初始版本发布
- ✨ Worktree 列表展示
- ✨ 双击打开 worktree 目录
- ✨ 创建 worktree 功能
- ✨ 删除 worktree 功能
- ✨ 右键菜单支持
- ✨ 分支搜索过滤功能
- 🐛 修复 EDT 慢操作警告
- 🎨 优化用户界面和交互体验

## 截图

### Worktree 列表
![Worktree List](docs/screenshots/worktree-list.png)

### 创建 Worktree
![Create Worktree](docs/screenshots/create-worktree.png)

### 分支选择对话框
![Branch Selection](docs/screenshots/branch-selection.png)

## 常见问题

### Q: 如何查看插件日志？
A: 在 IDEA 中打开 Help → Show Log in Explorer，查看 idea.log 文件。

### Q: 为什么看不到 Worktree tab？
A: 确保项目是一个 Git 仓库，并且 Git 插件已启用。

### Q: 删除 worktree 后文件还在吗？
A: `git worktree remove` 会删除 worktree 的 Git 关联，但文件是否删除取决于 Git 的行为。建议在删除前备份重要数据。

### Q: 可以创建基于远程分支的 worktree 吗？
A: 可以，在分支选择对话框中输入完整的远程分支名（如 `origin/feature-branch`）。

## 反馈与支持

如有问题或建议，请：
- 提交 [Issue](https://github.com/n-wen/another_gitworktree/issues)
- 发送邮件至：n-wen@outlook.com

## 致谢

感谢 JetBrains 提供强大的 IntelliJ Platform SDK！

