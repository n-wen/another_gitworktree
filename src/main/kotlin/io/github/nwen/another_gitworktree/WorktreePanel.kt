package io.github.nwen.another_gitworktree

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.BufferedReader
import java.io.File
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
    val isLocked: Boolean = false,
    val isMain: Boolean = false  // Main worktree cannot be deleted
)

// Custom TableModel to disable editing
private class NonEditableTableModel : DefaultTableModel() {
    override fun isCellEditable(row: Int, column: Int): Boolean {
        return false
    }
}

class WorktreePanel(private val project: Project) : JBPanel<WorktreePanel>(BorderLayout()) {
    private val tableModel = NonEditableTableModel()
    private val table: JTable
    private val worktreeList = mutableListOf<WorktreeInfo>()
    private lateinit var deleteButton: JButton
    
    // Throttling to prevent excessive operations
    private var lastRefreshTime = 0L
    private var lastCreateTime = 0L
    private var lastDeleteTime = 0L
    private val OPERATION_THROTTLE_MS = 1000L // 1 second throttle
    
    // Background refresh mechanism
    private val backgroundRefreshDelay = 5000L // 5 seconds
    private var backgroundRefreshScheduled = false

    init {
        // Create table
        tableModel.addColumn("Path")
        tableModel.addColumn("Branch")
        tableModel.addColumn("Commit")
        tableModel.addColumn("Status")
        
        table = JBTable(tableModel).apply {
            setShowGrid(true)
            autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
            
            // Add double-click event listener
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        val selectedRow = rowAtPoint(e.point)
                        if (selectedRow >= 0 && selectedRow < worktreeList.size) {
                            openWorktreeDirectory(worktreeList[selectedRow])
                        }
                    } else if (e.isPopupTrigger || (e.button == MouseEvent.BUTTON3 && e.clickCount == 1)) {
                        // Right-click context menu
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
            
            // Add selection listener to update delete button state
            selectionModel.addListSelectionListener {
                updateDeleteButtonState()
            }
        }
        
        // Add refresh and create buttons
        val topPanel = JBPanel<JBPanel<*>>(BorderLayout())
        val titleLabel = JBLabel("Git Worktrees")
        
        val buttonPanel = JBPanel<JBPanel<*>>(BorderLayout())
        val createButton = JButton("Create").apply {
            addActionListener(object : ActionListener {
                override fun actionPerformed(e: ActionEvent?) {
                    if (isOperationThrottled("create", lastCreateTime)) {
                        Messages.showMessageDialog(project, "Please wait a moment before creating another worktree.", "Operation Throttled", Messages.getInformationIcon())
                        return
                    }
                    lastCreateTime = System.currentTimeMillis()
                    createWorktree()
                }
            })
        }
        deleteButton = JButton("Delete").apply {
            addActionListener(object : ActionListener {
                override fun actionPerformed(e: ActionEvent?) {
                    if (isOperationThrottled("delete", lastDeleteTime)) {
                        Messages.showMessageDialog(project, "Please wait a moment before deleting another worktree.", "Operation Throttled", Messages.getInformationIcon())
                        return
                    }
                    lastDeleteTime = System.currentTimeMillis()
                    deleteSelectedWorktree()
                }
            })
        }
        val refreshButton = JButton("Refresh").apply {
            addActionListener(object : ActionListener {
                override fun actionPerformed(e: ActionEvent?) {
                    if (isOperationThrottled("refresh", lastRefreshTime)) {
                        return // Silent throttle
                    }
                    lastRefreshTime = System.currentTimeMillis()
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
        
        // Initial load - show loading and wait for repository to be ready
        showLoading()
        
        // Use invokeLater with multiple retries to ensure Git repository is ready
        scheduleRefresh(0)
        
        // Schedule background refresh every 5 seconds if needed
        scheduleBackgroundRefresh()
    }
    
    private fun showLoading() {
        tableModel.rowCount = 0
        tableModel.addRow(arrayOf("", "Loading worktrees...", "", ""))
    }
    
    private fun scheduleRefresh(attemptCount: Int) {
        ApplicationManager.getApplication().invokeLater {
            val repositoryManager = GitRepositoryManager.getInstance(project)
            val repositories = repositoryManager.repositories
            
            if (repositories.isEmpty() && attemptCount < 5) {
                // Repository not ready yet, retry after delay - limited retries
                ApplicationManager.getApplication().executeOnPooledThread {
                    Thread.sleep(300) // Wait 300ms
                    scheduleRefresh(attemptCount + 1)
                }
            } else {
                // Repository ready or max attempts reached, refresh now
                refreshWorktrees()
            }
        }
    }
    
    private fun updateDeleteButtonState() {
        val selectedRow = table.selectedRow
        if (selectedRow >= 0 && selectedRow < worktreeList.size) {
            val selectedWorktree = worktreeList[selectedRow]
            deleteButton.isEnabled = !selectedWorktree.isMain
        } else {
            deleteButton.isEnabled = false
        }
    }
    
    /**
     * Check if an operation should be throttled based on the last execution time
     */
    private fun isOperationThrottled(operationType: String, lastExecutionTime: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastExecution = currentTime - lastExecutionTime
        return timeSinceLastExecution < OPERATION_THROTTLE_MS
    }
    
    /**
     * Schedule background refresh with delay to prevent excessive operations
     */
    private fun scheduleBackgroundRefresh() {
        if (backgroundRefreshScheduled) return
        
        backgroundRefreshScheduled = true
        ApplicationManager.getApplication().executeOnPooledThread {
            Thread.sleep(backgroundRefreshDelay)
            
            ApplicationManager.getApplication().invokeLater {
                backgroundRefreshScheduled = false
                // Only refresh if needed (e.g., after operations)
                refreshWorktrees()
            }
        }
    }
    
    /**
     * Refresh the worktrees list from Git repository
     * @param isBackgroundRefresh Whether this is a background refresh (silent)
     */
    private fun showContextMenu(e: MouseEvent) {
        val selectedRow = table.rowAtPoint(e.point)
        if (selectedRow < 0 || selectedRow >= worktreeList.size) {
            return
        }
        
        val worktree = worktreeList[selectedRow]
        val popupMenu = JPopupMenu()
        
        val openItem = JMenuItem("Open Directory")
        openItem.addActionListener {
            openWorktreeDirectory(worktree)
        }
        popupMenu.add(openItem)
        
        popupMenu.addSeparator()
        
        val deleteItem = JMenuItem("Delete Worktree")
        deleteItem.isEnabled = !worktree.isMain  // Disable delete for main worktree
        deleteItem.addActionListener {
            deleteWorktree(worktree)
        }
        popupMenu.add(deleteItem)
        
        popupMenu.show(table, e.x, e.y)
    }
    
    fun refreshWorktrees() {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val repositoryManager = GitRepositoryManager.getInstance(project)
                val repositories = repositoryManager.repositories
                
                if (repositories.isEmpty()) {
                    ApplicationManager.getApplication().invokeLater {
                        tableModel.rowCount = 0
                        tableModel.addRow(arrayOf("", "Git repository not found", "", ""))
                    }
                    return@executeOnPooledThread
                }
                
                // Use first repository (usually project has only one main repository)
                val repository = repositories.first()
                val worktrees = getWorktrees(repository)
                
                ApplicationManager.getApplication().invokeLater {
                    tableModel.rowCount = 0
                    worktreeList.clear()
                    
                    if (worktrees.isEmpty()) {
                        tableModel.addRow(arrayOf("", "No worktrees found", "", ""))
                    } else {
                        worktreeList.addAll(worktrees)
                        worktrees.forEach { worktree ->
                            val branchDisplay = worktree.branch ?: "(detached HEAD)"
                            val statusDisplay = if (worktree.isLocked) "Locked" else "Normal"
                            tableModel.addRow(arrayOf(
                                worktree.path,
                                branchDisplay,
                                worktree.commitHash.take(8),
                                statusDisplay
                            ))
                        }
                    }
                }
            } catch (e: Exception) {
                thisLogger().error("Error refreshing worktrees", e)
                ApplicationManager.getApplication().invokeLater {
                    tableModel.rowCount = 0
                    tableModel.addRow(arrayOf("", "Error loading worktrees", "", ""))
                }
            }
        }
    }
    
    private fun deleteSelectedWorktree() {
        val selectedRow = table.selectedRow
        if (selectedRow < 0 || selectedRow >= worktreeList.size) {
            Messages.showWarningDialog(
                project,
                "Please select a worktree first",
                "Delete Worktree"
            )
            return
        }
        
        val worktree = worktreeList[selectedRow]
        
        // Check if it's the main worktree
        if (worktree.isMain) {
            Messages.showWarningDialog(
                project,
                "Cannot delete the main worktree.\n\nThe main worktree is the root of your Git repository and cannot be removed.",
                "Delete Worktree"
            )
            return
        }
        
        deleteWorktree(worktree)
    }
    
    private fun deleteWorktree(worktree: WorktreeInfo) {
        val worktreePath = worktree.path
        
        // Check if the worktree is currently open as a project
        val openProjects = ProjectManager.getInstance().openProjects
        var targetProject: Project? = null
        
        try {
            val worktreeFile = File(worktreePath)
            if (worktreeFile.exists()) {
                val worktreeCanonicalPath = worktreeFile.canonicalPath
                
                for (openProject in openProjects) {
                    val projectFile = File(openProject.basePath ?: continue)
                    if (projectFile.exists() && projectFile.canonicalPath == worktreeCanonicalPath) {
                        targetProject = openProject
                        break
                    }
                }
            }
        } catch (e: Exception) {
            thisLogger().error("Failed to check if worktree is open", e)
        }
        
        // Prepare confirmation message
        val message = if (targetProject != null) {
            "The worktree at:\n$worktreePath\n\nis currently open in IDEA.\n\nTo delete it, the project will be closed first.\nDo you want to continue?"
        } else {
            "Are you sure you want to delete this worktree?\n\nPath: $worktreePath\nBranch: ${worktree.branch ?: "(detached HEAD)"}"
        }
        
        // Confirm deletion
        val result = Messages.showYesNoDialog(
            project,
            message,
            "Delete Worktree",
            Messages.getQuestionIcon()
        )
        
        if (result != Messages.YES) {
            return
        }
        
        // If the worktree is open, close it first
        if (targetProject != null) {
            val projectToClose = targetProject
            ApplicationManager.getApplication().invokeLater {
                ProjectManager.getInstance().closeAndDispose(projectToClose)
                // Wait a bit for the project to close, then delete
                ApplicationManager.getApplication().executeOnPooledThread {
                    Thread.sleep(500) // Wait 500ms for project to fully close
                    executeDeleteWorktree(worktree)
                }
            }
        } else {
            executeDeleteWorktree(worktree)
        }
    }
    
    private fun executeDeleteWorktree(worktree: WorktreeInfo) {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val repositoryManager = GitRepositoryManager.getInstance(project)
                val repositories = repositoryManager.repositories
                
                if (repositories.isEmpty()) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(project, "Git repository not found", "Delete Worktree Failed")
                    }
                    return@executeOnPooledThread
                }
                
                val repository = repositories.first()
                val root = repository.root
                val rootFile = File(root.path)
                val worktreePath = worktree.path
                
                // Execute git worktree remove command
                // Use --force option in case worktree has uncommitted changes or unpushed commits
                val processBuilder = ProcessBuilder("git", "worktree", "remove", worktreePath, "--force")
                processBuilder.directory(rootFile)
                val process = processBuilder.start()
                
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                val errorLines = errorReader.readLines()
                val exitCode = process.waitFor()
                
                ApplicationManager.getApplication().invokeLater {
                    if (exitCode != 0) {
                        val errorMessage = errorLines.joinToString("\n")
                        
                        // Check if directory still exists (might be locked)
                        val worktreeDir = File(worktreePath)
                        if (worktreeDir.exists()) {
                            Messages.showWarningDialog(
                                project,
                                "Git failed to fully remove the worktree:\n$errorMessage\n\nThe directory may be in use. Please close all programs using this directory and try again.",
                                "Delete Worktree Failed"
                            )
                        } else {
                            Messages.showErrorDialog(
                                project,
                                "Failed to delete worktree:\n$errorMessage",
                                "Delete Worktree Failed"
                            )
                        }
                        return@invokeLater
                    }
                    
                    // Check if directory still exists after git worktree remove
                    val worktreeDir = File(worktreePath)
                    if (worktreeDir.exists()) {
                        // Git removed it from tracking but directory still exists
                        // Try to delete the directory manually
                        try {
                            worktreeDir.deleteRecursively()
                            Messages.showInfoMessage(
                                project,
                                "Worktree deleted successfully.\n\nNote: The directory was manually removed as it was still present after Git operation.",
                                "Delete Worktree Successful"
                            )
                        } catch (e: Exception) {
                            Messages.showWarningDialog(
                                project,
                                "Worktree removed from Git tracking, but the directory could not be deleted:\n$worktreePath\n\nThe directory may be in use by another program. Please close all programs using this directory and delete it manually.\n\nError: ${e.message}",
                                "Delete Worktree Partially Successful"
                            )
                        }
                    } else {
                        // Full success
                        Messages.showInfoMessage(
                            project,
                            "Worktree deleted successfully: $worktreePath",
                            "Delete Worktree Successful"
                        )
                    }
                    
                    // Refresh list
                    refreshWorktrees()
                }
                
            } catch (e: Exception) {
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(
                        project,
                        "Error deleting worktree: ${e.message}",
                        "Delete Worktree Failed"
                    )
                }
                e.printStackTrace()
            }
        }
    }
    
    private fun openWorktreeDirectory(worktree: WorktreeInfo) {
        val worktreePath = worktree.path
        
        // Execute file system operations on background thread, then UI operations on EDT
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val worktreeFile = java.io.File(worktreePath)
                
                if (!worktreeFile.exists()) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(
                            project,
                            "Directory does not exist: $worktreePath",
                            "Open Worktree Failed"
                        )
                    }
                    return@executeOnPooledThread
                }
                
                if (!worktreeFile.isDirectory) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(
                            project,
                            "Path is not a directory: $worktreePath",
                            "Open Worktree Failed"
                        )
                    }
                    return@executeOnPooledThread
                }
                
                // Normalize path to ensure accurate path comparison (on background thread)
                val normalizedPath = worktreeFile.canonicalPath ?: worktreePath

                // Check if project is already open on background thread (avoid file system operations on EDT)
                val existingProject = try {
                    val projectManager = ProjectManager.getInstance()
                    val openProjects = projectManager.openProjects
                    
                    // Find if a project is already open in the same directory
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
                
                // UI operations must be executed on EDT
                ApplicationManager.getApplication().invokeLater {
                    try {
                        if (existingProject != null) {
                            // Project already open, switch to its window
                            val windowManager = WindowManager.getInstance()
                            val frame = windowManager.getFrame(existingProject)
                            if (frame != null) {
                                frame.toFront()
                                frame.isVisible = true
                                // Request window focus
                                frame.requestFocus()
                            }
                        } else {
                            // Project not open, use ProjectUtil.openOrImport directly
                            // This method handles file system operations
                            ProjectUtil.openOrImport(worktreePath, null, true)
                        }
                    } catch (e: Exception) {
                        Messages.showErrorDialog(
                            project,
                            "Error opening directory: ${e.message}",
                            "Open Worktree Failed"
                        )
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(
                        project,
                        "Error opening directory: ${e.message}",
                        "Open Worktree Failed"
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
            Messages.showErrorDialog(project, "Git repository not found", "Create Worktree Failed")
            return
        }
        
        val repository = repositories.first()
        val defaultBranch = repository.currentBranchName ?: "main"
        
        // Show dialog for user to input or select branch
        val branch = showBranchInputDialog(repository, defaultBranch)
        if (branch == null || branch.isBlank()) {
            return
        }
        
        // Show dialog for user to input worktree path
        val worktreePath = showCreateWorktreeDialog(branch)
        if (worktreePath == null || worktreePath.isBlank()) {
            return
        }
        
        // Create worktree
        executeCreateWorktree(repository, branch, worktreePath)
    }
    
    private fun showBranchInputDialog(repository: GitRepository, defaultBranch: String?): String? {
        // Get all branches list
        val branches = getAllBranches(repository)
        
        if (branches.isEmpty()) {
            // If no branches, use simple input dialog
            return Messages.showInputDialog(
                project,
                "Enter branch name:",
                "Select Branch",
                Messages.getQuestionIcon(),
                defaultBranch ?: "",
                null
            )
        }
        
        // Use custom dialog to select branch
        val dialog = BranchSelectionDialog(project, branches, defaultBranch)
        if (dialog.showAndGet()) {
            var selectedBranch = dialog.selectedBranch
            if (selectedBranch != null) {
                // Remove any leading whitespace
                selectedBranch = selectedBranch.trim()
                
                // Check if branch starts with "remotes/"
                if (selectedBranch.startsWith("remotes/")) {
                    // Extract remote branch name (remove "remotes/" prefix)
                    val remoteBranch = selectedBranch
                    // Extract just the branch name without remote prefix
                    // Format: remotes/origin/feature/branch1 -> feature/branch1
                    val branchName = remoteBranch.substringAfter("remotes/").substringAfter("/")
                    
                    // Check if local branch exists
                    val localBranchExists = branches.any { it.trim().equals(branchName, ignoreCase = true) }
                    
                    if (localBranchExists) {
                        // Return local branch name
                        return branchName
                    } else {
                        // Ask user if they want to create local branch
                        val result = Messages.showYesNoDialog(
                            project,
                            "Local branch '$branchName' does not exist. Do you want to create it?",
                            "Create Local Branch",
                            Messages.getQuestionIcon()
                        )
                        
                        if (result == Messages.YES) {
                            // Create local branch tracking the remote branch
                            try {
                                val root = repository.root
                                val rootFile = java.io.File(root.path)
                                
                                // Execute git branch command to create local branch tracking remote
                                val processBuilder = ProcessBuilder(
                                    "git", "branch", branchName, remoteBranch
                                )
                                processBuilder.directory(rootFile)
                                val process = processBuilder.start()
                                val exitCode = process.waitFor()
                                
                                if (exitCode != 0) {
                                    // Read error output
                                    val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                                    val errorLines = errorReader.readLines()
                                    val errorMessage = errorLines.joinToString("\n")
                                    
                                    Messages.showErrorDialog(
                                        project,
                                        "Failed to create local branch: $errorMessage",
                                        "Create Branch Failed"
                                    )
                                    return null
                                }
                            } catch (e: Exception) {
                                Messages.showErrorDialog(
                                    project,
                                    "Error creating local branch: ${e.message}",
                                    "Create Branch Failed"
                                )
                                return null
                            }
                            
                            // Return the created branch name
                            return branchName
                        } else {
                            // User cancelled, return null
                            return null
                        }
                    }
                }
            }
            return selectedBranch
        }
        return null
    }
    
    private fun getAllBranches(repository: GitRepository): List<String> {
        val branches = mutableListOf<String>()
        try {
            val root = repository.root
            val rootFile = java.io.File(root.path)
            val processBuilder = ProcessBuilder("git", "branch", "-a")
            processBuilder.directory(rootFile)
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val lines = reader.readLines()
            process.waitFor()
            
            branches.addAll(lines.filter { it.isNotBlank() && !it.equals("HEAD", ignoreCase = true) }
                .map { line ->
                    // Remove special characters like * (current branch) or + from the beginning
                    line.trim()
                        .replaceFirst(Regex("^[*+]\\s*"), "")
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return branches
    }
    
    
    private fun showCreateWorktreeDialog(branch: String): String? {
        val defaultPath = suggestWorktreePath(branch)
        val message = "Create new worktree for branch '$branch'\n\nEnter worktree directory path:"
        
        return Messages.showInputDialog(
            project,
            message,
            "Create Worktree",
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
            
            // Check if path already exists
            val worktreeDir = java.io.File(worktreePath)
            if (worktreeDir.exists()) {
                val result = Messages.showYesNoDialog(
                    project,
                    "Directory already exists: $worktreePath\n\nDo you want to delete existing directory and create new worktree?",
                    "Create Worktree",
                    Messages.getWarningIcon()
                )
                if (result != Messages.YES) {
                    return
                }
                worktreeDir.deleteRecursively()
            }
            
            // branch is already a local branch, no need to handle remote branches
            val branchToUse = branch
            
            // Execute git worktree add command
            val processBuilder = ProcessBuilder("git", "worktree", "add", worktreePath, branchToUse)
            processBuilder.directory(rootFile)
            val process = processBuilder.start()
            
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val errorLines = errorReader.readLines()
            val exitCode = process.waitFor()
            
            if (exitCode != 0) {
                val errorMessage = errorLines.joinToString("\n")
                Messages.showErrorDialog(
                    project,
                    "Failed to create worktree:\n$errorMessage",
                    "Create Worktree Failed"
                )
                return
            }
            
            // Creation successful, refresh list
            Messages.showInfoMessage(
                project,
                "Worktree created successfully: $worktreePath",
                "Create Worktree Successful"
            )
            
            // Refresh list
            refreshWorktrees()
            
        } catch (e: Exception) {
            Messages.showErrorDialog(
                project,
                "Error creating worktree: ${e.message}",
                "Create Worktree Failed"
            )
            e.printStackTrace()
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
            var isFirstWorktree = true  // First worktree in list is the main one
            
            for (line in lines) {
                when {
                    line.startsWith("worktree ") -> {
                        // Save previous worktree
                        if (currentPath != null && currentCommit != null) {
                            worktrees.add(WorktreeInfo(
                                path = currentPath,
                                commitHash = currentCommit,
                                branch = currentBranch,
                                isLocked = isLocked,
                                isMain = isFirstWorktree
                            ))
                            isFirstWorktree = false
                        }
                        // Start new worktree
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
                        // Remove refs/heads/ prefix
                        if (currentBranch.startsWith("refs/heads/")) {
                            currentBranch = currentBranch.substring(11)
                        }
                    }
                    line.startsWith("locked") -> {
                        isLocked = true
                    }
                }
            }
            
            // Add last worktree
            if (currentPath != null && currentCommit != null) {
                worktrees.add(WorktreeInfo(
                    path = currentPath,
                    commitHash = currentCommit,
                    branch = currentBranch,
                    isLocked = isLocked,
                    isMain = isFirstWorktree
                ))
            }
            
            // If no worktree found (porcelain format may fail), try simple format
            if (worktrees.isEmpty()) {
                val simpleProcessBuilder = ProcessBuilder("git", "worktree", "list")
                simpleProcessBuilder.directory(rootFile)
                val simpleProcess = simpleProcessBuilder.start()
                val simpleReader = BufferedReader(InputStreamReader(simpleProcess.inputStream))
                val simpleLines = simpleReader.readLines()
                simpleProcess.waitFor()
                
                var isFirstWorktree = true
                for (line in simpleLines) {
                    if (line.isNotBlank()) {
                        // Parse format: /path/to/worktree [branch] commit-hash
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
                                isLocked = false,
                                isMain = isFirstWorktree
                            ))
                            isFirstWorktree = false
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            // If all failed, at least show main repository
            try {
                worktrees.add(WorktreeInfo(
                    path = repository.root.path,
                    commitHash = repository.currentRevision ?: "unknown",
                    branch = repository.currentBranchName,
                    isLocked = false,
                    isMain = true
                ))
            } catch (e2: Exception) {
                // Final error handling
                e2.printStackTrace()
            }
        }
        
        return worktrees
    }
}

// Branch selection dialog
private class BranchSelectionDialog(
    project: Project,
    private val allBranches: List<String>,
    defaultBranch: String?
) : DialogWrapper(project) {
    
    private val searchField = JBTextField()
    private val branchList = JList<String>()
    private val listModel = DefaultListModel<String>()
    private val filteredBranches = mutableListOf<String>()
    var selectedBranch: String? = null
        private set
    
    init {
        title = "Select Branch"
        branchList.model = listModel
        branchList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        filteredBranches.addAll(allBranches)
        updateListModel()
        
        // Set default selected item
        if (defaultBranch != null && allBranches.contains(defaultBranch)) {
            val index = filteredBranches.indexOf(defaultBranch)
            if (index >= 0) {
                branchList.selectedIndex = index
                branchList.ensureIndexIsVisible(index)
            }
        } else if (filteredBranches.isNotEmpty()) {
            branchList.selectedIndex = 0
        }
        
        // Search field listener for real-time filtering
        searchField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                filterBranches()
            }
            
            override fun removeUpdate(e: DocumentEvent) {
                filterBranches()
            }
            
            override fun changedUpdate(e: DocumentEvent) {
                filterBranches()
            }
        })
        
        // Double-click list item to confirm selection
        branchList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    doOKAction()
                }
            }
        })
        
        // Enter key to confirm selection
        searchField.addActionListener {
            if (branchList.selectedIndex >= 0) {
                doOKAction()
            } else if (searchField.text.isNotBlank()) {
                // If text is entered but no item selected, use the input text
                selectedBranch = searchField.text.trim()
                close(OK_EXIT_CODE)
            }
        }
        
        init()
    }
    
    private fun updateListModel() {
        listModel.clear()
        filteredBranches.forEach { listModel.addElement(it) }
    }
    
    private fun filterBranches() {
        val searchText = searchField.text.trim().lowercase()
        filteredBranches.clear()
        
        if (searchText.isEmpty()) {
            filteredBranches.addAll(allBranches)
        } else {
            filteredBranches.addAll(
                allBranches.filter { it.lowercase().contains(searchText) }
            )
        }
        
        updateListModel()
        
        // If there are filtered results, select first item
        if (filteredBranches.isNotEmpty()) {
            branchList.selectedIndex = 0
        } else {
            branchList.selectedIndex = -1
        }
    }
    
    override fun createCenterPanel(): JComponent {
        val panel = JBPanel<JBPanel<*>>(BorderLayout())
        
        // Search field
        val searchPanel = JBPanel<JBPanel<*>>(BorderLayout())
        searchPanel.add(JBLabel("Search Branch:"), BorderLayout.WEST)
        searchPanel.add(searchField, BorderLayout.CENTER)
        
        // Branch list
        val scrollPane = JBScrollPane(branchList)
        scrollPane.preferredSize = java.awt.Dimension(400, 300)
        
        panel.add(searchPanel, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)
        
        return panel
    }
    
    override fun doOKAction() {
        val selectedIndex = branchList.selectedIndex
        if (selectedIndex >= 0 && selectedIndex < filteredBranches.size) {
            selectedBranch = filteredBranches[selectedIndex]
        } else if (searchField.text.isNotBlank()) {
            // If text is entered but no item selected, use the input text
            selectedBranch = searchField.text.trim()
        }
        
        if (selectedBranch != null) {
            super.doOKAction()
        }
    }
    
    override fun getPreferredFocusedComponent(): JComponent {
        return searchField
    }
}

