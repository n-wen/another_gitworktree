package io.github.nwen.another_gitworktree

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class OpenWorktreeTabAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        thisLogger().info("OpenWorktreeTabAction triggered for project: ${project.name}")
        
        val toolWindowManager = ToolWindowManager.getInstance(project)
        
        // 尝试多个可能的 Git tool window ID
        val gitIds = listOf("Version Control", "Git", "VCS")
        var toolWindow: ToolWindow? = null
        
        for (id in gitIds) {
            toolWindow = toolWindowManager.getToolWindow(id)
            if (toolWindow != null) {
                thisLogger().info("Found Git tool window: $id")
                break
            }
        }
        
        if (toolWindow == null) {
            thisLogger().warn("Git tool window not found")
            return
        }
        
        // 检查是否已经存在 Worktree tab
        val existingContent = toolWindow.contentManager.findContent("Worktree")
        if (existingContent != null) {
            thisLogger().info("Worktree tab already exists, selecting it")
            toolWindow.contentManager.setSelectedContent(existingContent)
            toolWindow.show()
            return
        }
        
        // 创建新的 Worktree tab
        try {
            thisLogger().info("Creating new Worktree tab")
            val contentFactory = ContentFactory.getInstance()
            val worktreePanel = WorktreePanel(project)
            WorktreeTabInstaller.setWorktreePanel(project, worktreePanel)
            val content = contentFactory.createContent(worktreePanel, "Worktree", false)
            
            toolWindow.contentManager.addContent(content)
            toolWindow.contentManager.setSelectedContent(content)
            toolWindow.show()
            thisLogger().info("Worktree tab created and selected successfully")
        } catch (ex: Exception) {
            thisLogger().error("Failed to create Worktree tab", ex)
        }
    }
    
    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabled = project != null
    }
}

