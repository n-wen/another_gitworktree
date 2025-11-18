# 截图指南

此目录用于存放插件的宣传截图。

## 截图要求

### 尺寸和格式
- **推荐尺寸**: 1920x1080 或 1280x800
- **格式**: PNG（推荐）或 JPG
- **文件大小**: 每张不超过 2MB

### 内容建议

需要至少 3 张截图，建议 5 张：

#### 1. worktree-list.png （必需）
**主功能展示 - Worktree 列表**
- 展示完整的 Worktree tab 界面
- 包含表格中的数据（路径、分支、提交、状态）
- 显示顶部的工具栏按钮（刷新、创建、删除）
- 确保界面清晰，字体可读

#### 2. create-worktree.png （必需）
**创建 Worktree 功能**
- 展示创建 worktree 的对话框
- 显示路径输入框
- 可以是分支选择或路径输入的任一步骤

#### 3. branch-selection.png （必需）
**分支选择对话框**
- 展示分支选择对话框
- 显示搜索框
- 显示分支列表
- 可以添加搜索过滤的示例

#### 4. context-menu.png （可选）
**右键菜单**
- 右键点击表格行
- 显示弹出的上下文菜单
- 包含"打开目录"和"删除 Worktree"选项

#### 5. full-workflow.png （可选）
**完整工作流**
- 展示从创建到使用的完整流程
- 可以是多个窗口的组合

## 截图技巧

### 准备工作
1. 使用干净的 IDEA 主题（IntelliJ Light 或 Darcula）
2. 关闭不相关的工具窗口
3. 使用有意义的示例数据（真实的分支名和路径）
4. 确保字体大小适中（不要太小）

### 截图步骤
1. 在 IDEA 中打开一个 Git 项目
2. 打开 Version Control tool window (Alt+9)
3. 切换到 Worktree tab
4. 创建一些示例 worktree 数据
5. 使用截图工具（Windows: Win+Shift+S, Mac: Cmd+Shift+4）

### 后期处理
- 如果需要，可以裁剪多余的边缘
- 添加简单的边框或阴影（可选）
- 确保没有敏感信息（路径、用户名等）

## 文件命名规范

使用小写字母和连字符：
```
worktree-list.png
create-worktree.png
branch-selection.png
context-menu.png
full-workflow.png
```

## 上传到 GitHub

截图添加到此目录后：
```bash
git add docs/screenshots/*.png
git commit -m "docs: 添加插件截图"
git push
```

## 在 README 中引用

截图会自动在 README.md 和 README.zh-CN.md 中引用：
```markdown
![Worktree List](docs/screenshots/worktree-list.png)
```

## 示例布局

```
docs/screenshots/
├── README.md              # 本文件
├── worktree-list.png      # 主界面截图
├── create-worktree.png    # 创建对话框
├── branch-selection.png   # 分支选择
├── context-menu.png       # 右键菜单（可选）
└── full-workflow.png      # 完整流程（可选）
```

---

**提示**: 高质量的截图可以显著提高插件的下载量！

