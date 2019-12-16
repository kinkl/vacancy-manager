package me.anonymoussoftware.vacancymanager.ui.panels

import me.anonymoussoftware.vacancymanager.App
import me.anonymoussoftware.vacancymanager.VacancyManager
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancyListChangeListener
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancySearchListener
import me.anonymoussoftware.vacancymanager.model.Employer
import me.anonymoussoftware.vacancymanager.model.aggregated.AggregatedVacancy
import java.awt.Color
import java.awt.Component
import java.awt.EventQueue
import javax.swing.*

class VacancyListPanel : JPanel(), VacancyListChangeListener, VacancySearchListener {

    private val vacancyListModel: DefaultListModel<AggregatedVacancy>

    private val vacancyManager = App.getBean(VacancyManager::class.java)

    private val label: JLabel

    private val vacanciesList: JList<AggregatedVacancy>

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        this.label = JLabel("Vacancies:")
        add(this.label)

        this.vacancyListModel = DefaultListModel()
        this.vacanciesList = JList(this.vacancyListModel)
        this.vacanciesList.cellRenderer = VacancyCellRenderer()
        this.vacanciesList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        this.vacanciesList.addListSelectionListener { _ ->
            this.vacancyManager.selectedVacancy = this.vacanciesList.selectedValue
        }
        val vacancyListScrollPane = JScrollPane(this.vacanciesList)
        add(vacancyListScrollPane)
    }

    override fun addNotify() {
        super.addNotify()
        this.vacancyManager.addVacancyListChangeListener(this)
        this.vacancyManager.addVacancySearchListener(this)
    }

    override fun removeNotify() {
        this.vacancyManager.removeVacancySearchListener(this)
        this.vacancyManager.removeVacancyListChangeListener(this)
        super.removeNotify()
    }

    override fun onVacancyListChange(reason: VacancyListChangeListener.VacancyListChangeReason) {
        EventQueue.invokeLater {
            val vacancies = this.vacancyManager.availableVacancies
            val selectedIndex = this.vacanciesList.selectedIndex
            this.vacancyListModel.clear()
            vacancies.stream().forEach { this.vacancyListModel.addElement(it) }
            if (reason === VacancyListChangeListener.VacancyListChangeReason.VACANCY_BAN && selectedIndex >= 0) {
                val vacancyListSize = this.vacanciesList.model.size
                this.vacanciesList.selectedIndex = Math.min(selectedIndex, Math.max(vacancyListSize - 1, 0))
            }
            val notBannedVacancyCount = vacancies.stream() //
                .filter { v -> !v.vacancy!!.isBanned } //
                .map<Employer> { v -> v.vacancy!!.employer } //
                .filter({ e -> !e.isBanned }) //
                .count()
            var bannedVacancyCountString = ""
            val bannedVacancyCount = vacancies.size - notBannedVacancyCount
            if (bannedVacancyCount > 0) {
                bannedVacancyCountString = " (banned $bannedVacancyCount) "
            }
            val text = String.format("Vacancies: %d%s", notBannedVacancyCount, bannedVacancyCountString)
            this.label.text = text
        }
    }

    private inner class VacancyCellRenderer : JLabel(), ListCellRenderer<AggregatedVacancy> {

        override fun getListCellRendererComponent(
            list: JList<out AggregatedVacancy>, value: AggregatedVacancy,
            index: Int, isSelected: Boolean, cellHasFocus: Boolean
        ): Component {
            val selectedValue = list.selectedValue
            val backgroundColor: Color
            var foregroundColor = list.foreground
            if (selectedValue != null && selectedValue.vacancy!!.id == value.vacancy!!.id) {
                backgroundColor = list.selectionBackground
                foregroundColor = list.selectionForeground
            } else if (value.vacancy!!.isBanned) {
                backgroundColor = Color.RED
                foregroundColor = Color.WHITE
            } else if (value.vacancy.employer.isBanned) {
                backgroundColor = Color.DARK_GRAY
                foregroundColor = Color.WHITE
            } else if (value.vacancy.salary != null) {
                backgroundColor = Color.BLACK
                foregroundColor = Color.GREEN
            } else {
                backgroundColor = list.background
            }
            background = backgroundColor
            foreground = foregroundColor
            isOpaque = true
            font = list.font
            var salaryInfo = ""
            if (value.vacancy.salary != null) {
                salaryInfo = String.format(" (%s)", value.vacancy.salary.toString())
            }
            text = String.format(
                "%s [%d] - %s%s", //
                value.vacancy.employer.name, //
                value.employerVacancies!!.size, //
                value.vacancy.name, //
                salaryInfo
            )
            return this
        }

    }

    override fun onVacancyPageSuccessfulLoad(percentage: Int) {
        if (percentage == 0) {
            EventQueue.invokeLater { this.vacancyListModel.clear() }
        }
    }

    override fun onStartVacancyPageLoad(url: String) {

    }

    override fun onSearchFinish(loaded: Int, total: Int) {

    }

}
