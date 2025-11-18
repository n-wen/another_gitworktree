package io.github.nwen.another_gitworktree

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.swing.JButton
import javax.swing.JPopupMenu
import javax.swing.JMenuItem
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

data class WorktreeInfo(
    val path: String,
    val commitHash: String,
    val branch: String?,
    val isLocked: Boolean = false
)

// 自定义 TableModel，禁用编辑功能
private class NonEditableTableModel : DefaultTableModel() {
    override fun isCellEditable(row: Int, column: Int): Boolean {
        return false
    }
}

class WorktreePanel(private val project: Project) : JBPanel<WorktreePanel>(BorderLayout()) {
    private val tableModel = NonEditableTableModel()
    private val table: JTable
    private val worktreeList = mutableListOf<WorktreeInfo>()

    init {
        // 创建表格
        tableModel.addColumn("路径")
        tableModel.addColumn("分支")
        tableModel.addColumn("提交")
        tableModel.addColumn("状态")
        
        table = JBTable(tableModel).apply {
            setShowGrid(true)
            autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
            
            // 添加双击事件监听器
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        val selectedRow = rowAtPoint(e.point)
                        if (selectedRow >= 0 && selectedRow < worktreeList.size) {
                            openWorktreeDirectory(worktreeList[selectedRow])
                        }
                    } else if (e.isPopupTrigger || (e.button == MouseEvent.BUTTON3 && e.clickCount == 1)) {
                        // 右键菜单
                        showContextMenu(e)
                    }
                }
                
                override fun mousePressed(e: MouseEvent) {
                    if (e.isPopupTrigger) {
                        showContextMenu(e)
                    }
                }
                
                override fun mouseReleased(e: MouseEvent) {
                    if (e.isPopupTrigger) {
                        showContextMenu(e)
                    }
                }
            })
        }
        
        // 添加刷新按钮和创建按钮
        val topPanel = JBPanel<JBPanel<*>>(BorderLayout())
        val titleLabel = JBLabel("Git Worktrees")
        
        val buttonPanel = JBPanel<JBPanel<*>>(BorderLayout())
        val createButton = JButton("创建 Worktree").apply {
            addActionListener(object : ActionListener {
                override fun actionPerformed(e: ActionEvent?) {
                    createWorktree()
                }
            })
        }
        val deleteButton = JButton("删除 Worktree").apply {
            addActionListener(object : ActionListener {
                override fun actionPerformed(e: ActionEvent?) {
                    deleteSelectedWorktree()
                }
            })
        }
        val refreshButton = JButton("刷新").apply {
            addActionListener(object : ActionListener {
                override fun actionPerformed(e: ActionEvent?) {
                    refreshWorktrees()
                }
            })
        }
        
        buttonPanel.add(createButton, BorderLayout.WEST)
        buttonPanel.add(deleteButton, BorderLayout.CENTER)
        buttonPanel.add(refreshButton, BorderLayout.EAST)
        
        topPanel.add(titleLabel, BorderLayout.WEST)
        topPanel.add(buttonPanel, BorderLayout.EAST)
        
        add(topPanel, BorderLayout.NORTH)
        add(JBScrollPane(table), BorderLayout.CENTER)
        
        // 初始加载
        refreshWorktrees()
    }
    
    private fun showContextMenu(e: MouseEvent) {
        val selectedRow = table.rowAtPoint(e.point)
        if (selectedRow < 0 || selectedRow >= worktreeList.size) {
            return
        }
        
        val worktree = worktreeList[selectedRow]
        val popupMenu = JPopupMenu()
        
        val openItem = JMenuItem("打开目录")
        openItem.addActionListener {
            openWorktreeDirectory(worktree)
        }
        popupMenu.add(openItem)
        
        popupMenu.addSeparator()
        
        val deleteItem = JMenuItem("删除 Worktree")
        deleteItem.addActionListener {
            deleteWorktree(worktree)
        }
        popupMenu.add(deleteItem)
        
        popupMenu.show(table, e.x, e.y)
    }
    
    private fun deleteSelectedWorktree() {
        val selectedRow = table.selectedRow
        if (selectedRow < 0 || selectedRow >= worktreeList.size) {
            Messages.showWarningDialog(
                project,
                "请先选择一个 worktree",
                "删除 Worktree"
            )
            return
        }
        
        val worktree = worktreeList[selectedRow]
        deleteWorktree(worktree)
    }
    
    private fun deleteWorktree(worktree: WorktreeInfo) {
        val worktreePath = worktree.path
        
        // 确认删除
        val result = Messages.showYesNoDialog(
            project,
            "确定要删除 worktree 吗?\n\n路径: $worktreePath\n分支: ${worktree.branch ?: "(detached HEAD)"}",
            "删除 Worktree",
            Messages.getQuestionIcon()
        )
        
        if (result != Messages.YES) {
            return
        }
        
        // 执行删除
        executeDeleteWorktree(worktree)
    }
    
    private fun executeDeleteWorktree(worktree: WorktreeInfo) {
        try {
            val repositoryManager = GitRepositoryManager.getInstance(project)
            val repositories = repositoryManager.repositories
            
            if (repositories.isEmpty()) {
                Messages.showErrorDialog(project, "未找到 Git 仓库", "删除 Worktree 失败")
                return
            }
            
            val repository = repositories.first()
            val root = repository.root
            val rootFile = java.io.File(root.path)
            val worktreePath = worktree.path
            
            // 执行 git worktree remove 命令
            // 使用 --force 选项，如果 worktree 有未提交的更改或未推送的提交
            val processBuilder = ProcessBuilder("git", "worktree", "remove", worktreePath, "--force")
            processBuilder.directory(rootFile)
            val process = processBuilder.start()
            
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val errorLines = errorReader.readLines()
            val exitCode = process.waitFor()
            
            if (exitCode != 0) {
                val errorMessage = errorLines.joinToString("\n")
                Messages.showErrorDialog(
                    project,
                    "删除 worktree 失败:\n$errorMessage",
                    "删除 Worktree 失败"
                )
                return
            }
            
            // 删除成功，刷新列表
            Messages.showInfoMessage(
                project,
                "Worktree 删除成功: $worktreePath",
                "删除 Worktree 成功"
            )
            
            // 刷新列表
            refreshWorktrees()
            
        } catch (e: Exception) {
            Messages.showErrorDialog(
                project,
                "删除 worktree 时出错: ${e.message}",
                "删除 Worktree 失败"
            )
            e.printStackTrace()
        }
    }
    
    private fun openWorktreeDirectory(worktree: WorktreeInfo) {
        val worktreePath = worktree.path
        
        // 在后台线程执行文件系统操作，然后在 EDT 上执行 UI 操作
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val worktreeFile = java.io.File(worktreePath)
                
                if (!worktreeFile.exists()) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(
                            project,
                            "目录不存在: $worktreePath",
                            "打开 Worktree 失败"
                        )
                    }
                    return@executeOnPooledThread
                }
                
                if (!worktreeFile.isDirectory) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(
                            project,
                            "路径不是目录: $worktreePath",
                            "打开 Worktree 失败"
                        )
                    }
                    return@executeOnPooledThread
                }
                
                // 规范化路径，确保路径比较的准确性（在后台线程执行）
                val normalizedPath = worktreeFile.canonicalPath ?: worktreePath
                
                // 在后台线程检查项目是否已经打开（避免在 EDT 上执行文件系统操作）
                val existingProject = try {
                    val projectManager = ProjectManager.getInstance()
                    val openProjects = projectManager.openProjects
                    
                    // 查找是否已经有项目打开在相同的目录
                    openProjects.firstOrNull { openProject ->
                        val openProjectBasePath = openProject.basePath
                        if (openProjectBasePath != null) {
                            try {
                                val openProjectFile = java.io.File(openProjectBasePath)
                                val openProjectNormalizedPath = openProjectFile.canonicalPath
                                openProjectNormalizedPath == normalizedPath
                            } catch (e: Exception) {
                                false
                            }
                        } else {
                            false
                        }
                    }
                } catch (e: Exception) {
                    null
                }
                
                // UI 操作需要在 EDT 上执行
                ApplicationManager.getApplication().invokeLater {
                    try {
                        if (existingProject != null) {
                            // 项目已经打开，切换到该项目的窗口
                            val windowManager = WindowManager.getInstance()
                            val frame = windowManager.getFrame(existingProject)
                            if (frame != null) {
                                frame.toFront()
                                frame.isVisible = true
                                // 请求窗口获得焦点
                                frame.requestFocus()
                            }
                        } else {
                            // 项目未打开，直接使用 ProjectUtil.openOrImport
                            // 这个方法会处理文件系统操作
                            ProjectUtil.openOrImport(worktreePath, null, true)
                        }
                    } catch (e: Exception) {
                        Messages.showErrorDialog(
                            project,
                            "打开目录时出错: ${e.message}",
                            "打开 Worktree 失败"
                        )
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(
                        project,
                        "打开目录时出错: ${e.message}",
                        "打开 Worktree 失败"
                    )
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun createWorktree() {
        val repositoryManager = GitRepositoryManager.getInstance(project)
        val repositories = repositoryManager.repositories
        
        if (repositories.isEmpty()) {
            Messages.showErrorDialog(project, "未找到 Git 仓库", "创建 Worktree 失败")
            return
        }
        
        val repository = repositories.first()
        val defaultBranch = repository.currentBranchName ?: "main"
        
        // 显示对话框让用户输入或选择分支
        val branch = showBranchInputDialog(repository, defaultBranch)
        if (branch == null || branch.isBlank()) {
            return
        }
        
        // 验证分支是否存在
        if (!isValidBranchName(branch, repository)) {
            Messages.showErrorDialog(project, "分支 '$branch' 不存在", "创建 Worktree 失败")
            return
        }
        
        // 显示对话框让用户输入 worktree 路径
        val worktreePath = showCreateWorktreeDialog(branch)
        if (worktreePath == null || worktreePath.isBlank()) {
            return
        }
        
        // 创建 worktree
        executeCreateWorktree(repository, branch, worktreePath)
    }
    
    private fun showBranchInputDialog(repository: GitRepository, defaultBranch: String?): String? {
        // 获取所有分支列表
        val branches = getAllBranches(repository)
        val branchList = branches.joinToString("\n")
        
        val message = if (branches.isNotEmpty()) {
            "请选择或输入分支名:\n\n可用分支:\n$branchList\n\n分支名:"
        } else {
            "请输入分支名:"
        }
        
        return Messages.showInputDialog(
            project,
            message,
            "选择分支",
            Messages.getQuestionIcon(),
            defaultBranch ?: "",
            null
        )
    }
    
    private fun getAllBranches(repository: GitRepository): List<String> {
        val branches = mutableListOf<String>()
        try {
            val root = repository.root
            val rootFile = java.io.File(root.path)
            val processBuilder = ProcessBuilder("git", "branch", "--list", "--format=%(refname:short)")
            processBuilder.directory(rootFile)
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val lines = reader.readLines()
            process.waitFor()
            branches.addAll(lines.filter { it.isNotBlank() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return branches
    }
    
    private fun isValidBranchName(branchName: String, repository: GitRepository): Boolean {
        // 检查分支名是否有效（简单验证）
        if (branchName.isBlank() || branchName.contains(" ")) {
            return false
        }
        
        // 可以进一步验证分支是否存在
        try {
            val root = repository.root
            val rootFile = java.io.File(root.path)
            val processBuilder = ProcessBuilder("git", "show-ref", "--verify", "--quiet", "refs/heads/$branchName")
            processBuilder.directory(rootFile)
            val process = processBuilder.start()
            val exitCode = process.waitFor()
            return exitCode == 0
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun showCreateWorktreeDialog(branch: String): String? {
        val defaultPath = suggestWorktreePath(branch)
        val message = "为分支 '$branch' 创建新的 worktree\n\n请输入 worktree 目录路径:"
        
        return Messages.showInputDialog(
            project,
            message,
            "创建 Worktree",
            Messages.getQuestionIcon(),
            defaultPath,
            null
        )
    }
    
    private fun suggestWorktreePath(branch: String): String {
        val projectBasePath = project.basePath ?: return ""
        val parentDir = java.io.File(projectBasePath).parent ?: return ""
        val branchName = branch.replace("/", "-").replace("\\", "-")
        return "$parentDir/${java.io.File(projectBasePath).name}-$branchName"
    }
    
    private fun executeCreateWorktree(repository: GitRepository, branch: String, worktreePath: String) {
        try {
            val root = repository.root
            val rootFile = java.io.File(root.path)
            
            // 检查路径是否已存在
            val worktreeDir = java.io.File(worktreePath)
            if (worktreeDir.exists()) {
                val result = Messages.showYesNoDialog(
                    project,
                    "目录已存在: $worktreePath\n\n是否要删除现有目录并创建新的 worktree?",
                    "创建 Worktree",
                    Messages.getWarningIcon()
                )
                if (result != Messages.YES) {
                    return
                }
                worktreeDir.deleteRecursively()
            }
            
            // 执行 git worktree add 命令
            val processBuilder = ProcessBuilder("git", "worktree", "add", worktreePath, branch)
            processBuilder.directory(rootFile)
            val process = processBuilder.start()
            
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val errorLines = errorReader.readLines()
            val exitCode = process.waitFor()
            
            if (exitCode != 0) {
                val errorMessage = errorLines.joinToString("\n")
                Messages.showErrorDialog(
                    project,
                    "创建 worktree 失败:\n$errorMessage",
                    "创建 Worktree 失败"
                )
                return
            }
            
            // 创建成功，刷新列表
            Messages.showInfoMessage(
                project,
                "Worktree 创建成功: $worktreePath",
                "创建 Worktree 成功"
            )
            
            // 刷新列表
            refreshWorktrees()
            
        } catch (e: Exception) {
            Messages.showErrorDialog(
                project,
                "创建 worktree 时出错: ${e.message}",
                "创建 Worktree 失败"
            )
            e.printStackTrace()
        }
    }
    
    fun refreshWorktrees() {
        tableModel.rowCount = 0
        worktreeList.clear()
        
        val repositoryManager = GitRepositoryManager.getInstance(project)
        val repositories = repositoryManager.repositories
        
        if (repositories.isEmpty()) {
            tableModel.addRow(arrayOf("", "未找到 Git 仓库", "", ""))
            return
        }
        
        // 使用第一个仓库（通常项目只有一个主仓库）
        val repository = repositories.first()
        val worktrees = getWorktrees(repository)
        
        if (worktrees.isEmpty()) {
            tableModel.addRow(arrayOf("", "没有找到 worktree", "", ""))
        } else {
            worktreeList.addAll(worktrees)
            worktrees.forEach { worktree ->
                val branchDisplay = worktree.branch ?: "(detached HEAD)"
                val statusDisplay = if (worktree.isLocked) "已锁定" else "正常"
                tableModel.addRow(arrayOf(
                    worktree.path,
                    branchDisplay,
                    worktree.commitHash.take(8),
                    statusDisplay
                ))
            }
        }
    }
    
    private fun getWorktrees(repository: GitRepository): List<WorktreeInfo> {
        val worktrees = mutableListOf<WorktreeInfo>()
        
        try {
            val root = repository.root
            val rootFile = java.io.File(root.path)
            val processBuilder = ProcessBuilder("git", "worktree", "list", "--porcelain")
            processBuilder.directory(rootFile)
            val process = processBuilder.start()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val lines = reader.readLines()
            process.waitFor()
            
            var currentPath: String? = null
            var currentCommit: String? = null
            var currentBranch: String? = null
            var isLocked = false
            
            for (line in lines) {
                when {
                    line.startsWith("worktree ") -> {
                        // 保存上一个 worktree
                        if (currentPath != null && currentCommit != null) {
                            worktrees.add(WorktreeInfo(
                                path = currentPath,
                                commitHash = currentCommit,
                                branch = currentBranch,
                                isLocked = isLocked
                            ))
                        }
                        // 开始新的 worktree
                        currentPath = line.substring(9).trim()
                        currentCommit = null
                        currentBranch = null
                        isLocked = false
                    }
                    line.startsWith("HEAD ") -> {
                        currentCommit = line.substring(5).trim()
                    }
                    line.startsWith("branch ") -> {
                        currentBranch = line.substring(7).trim()
                        // 移除 refs/heads/ 前缀
                        if (currentBranch.startsWith("refs/heads/")) {
                            currentBranch = currentBranch.substring(11)
                        }
                    }
                    line.startsWith("locked") -> {
                        isLocked = true
                    }
                }
            }
            
            // 添加最后一个 worktree
            if (currentPath != null && currentCommit != null) {
                worktrees.add(WorktreeInfo(
                    path = currentPath,
                    commitHash = currentCommit,
                    branch = currentBranch,
                    isLocked = isLocked
                ))
            }
            
            // 如果没有找到任何 worktree（porcelain 格式可能失败），尝试简单格式
            if (worktrees.isEmpty()) {
                val simpleProcessBuilder = ProcessBuilder("git", "worktree", "list")
                simpleProcessBuilder.directory(rootFile)
                val simpleProcess = simpleProcessBuilder.start()
                val simpleReader = BufferedReader(InputStreamReader(simpleProcess.inputStream))
                val simpleLines = simpleReader.readLines()
                simpleProcess.waitFor()
                
                for (line in simpleLines) {
                    if (line.isNotBlank()) {
                        // 解析格式: /path/to/worktree [branch] commit-hash
                        val parts = line.trim().split("\\s+".toRegex())
                        if (parts.isNotEmpty()) {
                            val path = parts[0]
                            var branch: String? = null
                            var commit = ""
                            
                            for (i in 1 until parts.size) {
                                when {
                                    parts[i].startsWith("[") && parts[i].endsWith("]") -> {
                                        branch = parts[i].removeSurrounding("[", "]")
                                    }
                                    parts[i].length == 40 && parts[i].matches(Regex("[0-9a-f]+")) -> {
                                        commit = parts[i]
                                    }
                                }
                            }
                            
                            if (commit.isEmpty() && parts.size > 1) {
                                commit = parts.last()
                            }
                            
                            worktrees.add(WorktreeInfo(
                                path = path,
                                commitHash = commit.ifEmpty { "unknown" },
                                branch = branch,
                                isLocked = false
                            ))
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            // 如果都失败了，至少显示主仓库
            try {
                worktrees.add(WorktreeInfo(
                    path = repository.root.path,
                    commitHash = repository.currentRevision ?: "unknown",
                    branch = repository.currentBranchName,
                    isLocked = false
                ))
            } catch (e2: Exception) {
                // 最后的错误处理
                e2.printStackTrace()
            }
        }
        
        return worktrees
    }
}

