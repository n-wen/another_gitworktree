package io.github.nwen.another_gitworktree

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.content.ContentFactory
import java.util.concurrent.atomic.AtomicBoolean

class WorktreeTabInstaller : ProjectManagerListener {
    companion object {
        private val worktreePanels = mutableMapOf<Project, WorktreePanel>()
        
        fun getWorktreePanel(project: Project): WorktreePanel? {
            return worktreePanels[project]
        }
        
        fun setWorktreePanel(project: Project, panel: WorktreePanel) {
            worktreePanels[project] = panel
        }
    }
    
    override fun projectOpened(project: Project) {
        thisLogger().info("WorktreeTabInstaller.projectOpened() called for project: ${project.name}")
        
        val added = AtomicBoolean(false)
        val connection = project.messageBus.connect()
        
        // 监听 tool window 注册事件
        connection.subscribe(
            ToolWindowManagerListener.TOPIC,
            object : ToolWindowManagerListener {
                override fun toolWindowRegistered(id: String) {
                    // 尝试多个可能的 Git tool window ID
                    val gitIds = listOf("Git", "VCS", "Version Control")
                    if (id in gitIds && added.compareAndSet(false, true)) {
                        thisLogger().info("Found Git-related tool window: $id, adding Worktree tab")
                        addWorktreeTab(project, id)
                    }
                }
            }
        )
        
        // 立即尝试添加（如果 tool window 已经存在）
        ApplicationManager.getApplication().invokeLater {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            
            // 尝试多个可能的 Git tool window ID
            val gitIds = listOf("Git", "VCS", "Version Control")
            for (id in gitIds) {
                val toolWindow = toolWindowManager.getToolWindow(id)
                if (toolWindow != null) {
                    thisLogger().info("Found existing tool window: $id")
                    if (added.compareAndSet(false, true)) {
                        addWorktreeTab(project, id)
                    }
                    break
                }
            }
        }
    }
    
    private fun addWorktreeTab(project: Project, toolWindowId: String) {
        ApplicationManager.getApplication().invokeLater {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            val toolWindow: ToolWindow? = toolWindowManager.getToolWindow(toolWindowId)
            
            if (toolWindow == null) {
                thisLogger().warn("Tool window $toolWindowId is null")
                return@invokeLater
            }

            thisLogger().info("Tool window $toolWindowId found, checking contents")
            
            // 列出所有现有的内容
            val existingContents = toolWindow.contentManager.contents
            val contentNames = existingContents.map { it.displayName }.joinToString()
            thisLogger().info("Existing tool window contents: $contentNames")

            // 检查是否已经添加过
            val existingContent = toolWindow.contentManager.findContent("Worktree")
            if (existingContent != null) {
                thisLogger().info("Worktree tab already exists")
                return@invokeLater
            }

            try {
                thisLogger().info("Creating Worktree tab content")
                val contentFactory = ContentFactory.getInstance()
                val worktreePanel = WorktreePanel(project)
                worktreePanels[project] = worktreePanel
                val content = contentFactory.createContent(worktreePanel, "Worktree", false)
                
                toolWindow.contentManager.addContent(content)
                thisLogger().info("Worktree tab added successfully to $toolWindowId!")
            } catch (e: Exception) {
                thisLogger().error("Failed to add Worktree tab", e)
            }
        }
    }
}

