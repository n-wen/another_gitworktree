package io.github.nwen.another_gitworktree

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import javax.swing.JPanel

class WorktreePanel : JBPanel<WorktreePanel>(BorderLayout()) {
    init {
        val label = JBLabel("Worktree Tab - Coming Soon")
        add(label, BorderLayout.CENTER)
    }
}

