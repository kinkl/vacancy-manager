package me.anonymoussoftware.vacancymanager

import me.anonymoussoftware.vacancymanager.ui.frames.MainFrame
import org.springframework.context.annotation.AnnotationConfigApplicationContext

import javax.swing.*
import java.awt.*

object App {
    private val ctx = AnnotationConfigApplicationContext()

    fun <T> getBean(requiredType: Class<T>): T {
        return ctx.getBean(requiredType)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        ctx.register(AppConfig::class.java)
        ctx.refresh()
        EventQueue.invokeLater {
            val mainFrame = MainFrame()
            mainFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            mainFrame.isVisible = true
        }
    }

}
