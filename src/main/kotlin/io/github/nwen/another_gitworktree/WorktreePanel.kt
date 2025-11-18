package io.github.nwen.another_gitworktree

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.swing.JButton
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

data class WorktreeInfo(
    val path: String,
    val commitHash: String,
    val branch: String?,
    val isLocked: Boolean = false
)

class WorktreePanel(private val project: Project) : JBPanel<WorktreePanel>(BorderLayout()) {
    private val tableModel = DefaultTableModel()
    private val table: JTable

    init {
        // 创建表格
        tableModel.addColumn("路径")
        tableModel.addColumn("分支")
        tableModel.addColumn("提交")
        tableModel.addColumn("状态")
        
        table = JBTable(tableModel).apply {
            setShowGrid(true)
            autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
        }
        
        // 添加刷新按钮和标签
        val topPanel = JBPanel<JBPanel<*>>(BorderLayout())
        val titleLabel = JBLabel("Git Worktrees")
        val refreshButton = JButton("刷新").apply {
            addActionListener(object : ActionListener {
                override fun actionPerformed(e: ActionEvent?) {
                    refreshWorktrees()
                }
            })
        }
        
        topPanel.add(titleLabel, BorderLayout.WEST)
        topPanel.add(refreshButton, BorderLayout.EAST)
        
        add(topPanel, BorderLayout.NORTH)
        add(JBScrollPane(table), BorderLayout.CENTER)
        
        // 初始加载
        refreshWorktrees()
    }
    
    private fun refreshWorktrees() {
        tableModel.rowCount = 0
        
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

