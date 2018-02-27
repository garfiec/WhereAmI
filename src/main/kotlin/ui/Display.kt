package ui

import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.WindowConstants

class Display : JFrame("Where Am I") {
    init {
        contentPane.layout = BorderLayout()

        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setSize(700, 500)
        isVisible = true
    }
}