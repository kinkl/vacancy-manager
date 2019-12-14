package me.anonymoussoftware.vacancymanager.ui.panels

import me.anonymoussoftware.vacancymanager.App
import me.anonymoussoftware.vacancymanager.VacancyManager
import me.anonymoussoftware.vacancymanager.VacancyManager.EmployerListChangeListener
import me.anonymoussoftware.vacancymanager.model.Employer

import javax.swing.*
import java.awt.*

class EmployerListPanel : JPanel(), EmployerListChangeListener {

    private val employerListModel: DefaultListModel<Employer>

    private val vacancyManager = App.getBean(VacancyManager::class.java)

    private val label: JLabel

    private val employerList: JList<Employer>

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        this.label = JLabel("Companies:")
        add(this.label)

        this.employerListModel = DefaultListModel()
        this.employerList = JList(this.employerListModel)
        this.employerList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        this.employerList.cellRenderer = EmployerCellRenderer()
        val vacancyListScrollPane = JScrollPane(this.employerList)
        add(vacancyListScrollPane)
    }

    override fun addNotify() {
        super.addNotify()
        this.vacancyManager.addEmployerListChangeListener(this)
    }

    override fun removeNotify() {
        this.vacancyManager.removeEmployerListChangeListener(this)
        super.removeNotify()
    }

    override fun onEmployerListChange() {
        EventQueue.invokeLater {
            val employers = this.vacancyManager.getEmployers()
            val selectedIndex = this.employerList.selectedIndex
            this.employerListModel.clear()
            employers.stream().forEach{ this.employerListModel.addElement(it) }
            if (selectedIndex >= 0) {
                val employerListSize = this.employerList.model.size
                this.employerList.selectedIndex = Math.min(selectedIndex, Math.max(employerListSize - 1, 0))
            }
            this.label.text = "Companies: " + employers.size
        }
    }

    private inner class EmployerCellRenderer : JLabel(), ListCellRenderer<Employer> {

        override fun getListCellRendererComponent(
            list: JList<out Employer>, value: Employer, index: Int,
            isSelected: Boolean, cellHasFocus: Boolean
        ): Component {
            val selectedValue = list.selectedValue
            val backgroundColor: Color
            var foregroundColor = list.foreground
            if (selectedValue != null && selectedValue.id == value.id) {
                backgroundColor = list.selectionBackground
                foregroundColor = list.selectionForeground
            } else if (value.isBanned) {
                backgroundColor = Color.DARK_GRAY
                foregroundColor = Color.WHITE
            } else {
                backgroundColor = list.background
            }
            background = backgroundColor
            foreground = foregroundColor
            isOpaque = true
            font = list.font
            text = value.name
            return this
        }

    }

}
