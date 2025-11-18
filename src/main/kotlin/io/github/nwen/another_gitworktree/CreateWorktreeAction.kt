package io.github.nwen.another_gitworktree

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import java.io.BufferedReader
import java.io.InputStreamReader

class CreateWorktreeAction : AnAction("Create Worktree", "Create a new worktree for the selected branch", null) {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val repositoryManager = GitRepositoryManager.getInstance(project)
        val repositories = repositoryManager.repositories
        
        if (repositories.isEmpty()) {
            Messages.showErrorDialog(project, "未找到 Git 仓库", "创建 Worktree 失败")
            return
        }
        
        val repository = repositories.first()
        
        // 获取当前分支作为默认值
        val defaultBranch = getSelectedBranch(e, repository)
        
        // 显示对话框让用户输入或选择分支
        val branch = showBranchInputDialog(project, repository, defaultBranch)
        if (branch == null || branch.isBlank()) {
            return
        }
        
        // 验证分支是否存在
        if (!isValidBranchName(branch, repository)) {
            Messages.showErrorDialog(project, "分支 '$branch' 不存在", "创建 Worktree 失败")
            return
        }
        
        // 显示对话框让用户输入 worktree 路径
        val worktreePath = showCreateWorktreeDialog(project, branch)
        if (worktreePath == null || worktreePath.isBlank()) {
            return
        }
        
        // 创建 worktree
        createWorktree(project, repository, branch, worktreePath)
    }
    
    override fun update(e: AnActionEvent) {
        val project = e.project
        val repositoryManager = project?.let { GitRepositoryManager.getInstance(it) }
        val hasRepository = repositoryManager?.repositories?.isNotEmpty() == true
        
        e.presentation.isEnabledAndVisible = hasRepository
    }
    
    private fun getSelectedBranch(e: AnActionEvent, repository: GitRepository): String? {
        // 暂时使用当前分支
        // 后续可以改进：从 Git Log 获取选中的分支
        // 可以通过 VcsLog 的 DataProvider 获取选中的提交或分支
        val currentBranch = repository.currentBranchName
        if (currentBranch != null) {
            return currentBranch
        }
        
        // 如果没有当前分支，返回默认分支
        return "main"
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
    
    private fun showBranchInputDialog(project: Project, repository: GitRepository, defaultBranch: String?): String? {
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
    
    private fun showCreateWorktreeDialog(project: Project, branch: String): String? {
        val defaultPath = suggestWorktreePath(project, branch)
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
    
    private fun suggestWorktreePath(project: Project, branch: String): String {
        val projectBasePath = project.basePath ?: return ""
        val parentDir = java.io.File(projectBasePath).parent ?: return ""
        val branchName = branch.replace("/", "-").replace("\\", "-")
        return "$parentDir/${java.io.File(projectBasePath).name}-$branchName"
    }
    
    private fun createWorktree(project: Project, repository: GitRepository, branch: String, worktreePath: String) {
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
            
            // 创建成功，刷新 WorktreePanel
            Messages.showInfoMessage(
                project,
                "Worktree 创建成功: $worktreePath",
                "创建 Worktree 成功"
            )
            
            // 通知 WorktreePanel 刷新
            refreshWorktreePanel(project)
            
        } catch (e: Exception) {
            Messages.showErrorDialog(
                project,
                "创建 worktree 时出错: ${e.message}",
                "创建 Worktree 失败"
            )
            e.printStackTrace()
        }
    }
    
    private fun refreshWorktreePanel(project: Project) {
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
            val worktreePanel = WorktreeTabInstaller.getWorktreePanel(project)
            worktreePanel?.refreshWorktrees()
        }
    }
}

