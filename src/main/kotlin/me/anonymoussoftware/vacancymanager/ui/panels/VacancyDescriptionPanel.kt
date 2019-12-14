package me.anonymoussoftware.vacancymanager.ui.panels

import me.anonymoussoftware.vacancymanager.App
import me.anonymoussoftware.vacancymanager.VacancyManager
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancyDescriptionRequestListener
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancySelectionListener
import me.anonymoussoftware.vacancymanager.model.Employer
import me.anonymoussoftware.vacancymanager.model.Salary
import me.anonymoussoftware.vacancymanager.model.Snippet
import me.anonymoussoftware.vacancymanager.model.Vacancy
import me.anonymoussoftware.vacancymanager.model.aggregated.AggregatedVacancy

import javax.swing.*
import java.awt.*

class VacancyDescriptionPanel : JPanel(), VacancySelectionListener, VacancyDescriptionRequestListener {

    private val vacancyDescriptionTextArea: JTextPane

    private val vacancyManager = App.getBean(VacancyManager::class.java)

    private val banVacancyButton: JButton

    private val banEmployerButton: JButton

    private val requestDescriptionButton: JButton

    init {
        layout = BorderLayout()

        this.vacancyDescriptionTextArea = JTextPane()
        this.vacancyDescriptionTextArea.contentType = "text/html"
        add(JScrollPane(this.vacancyDescriptionTextArea), BorderLayout.CENTER)

        val buttonPanel = JPanel()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.Y_AXIS)

        this.banVacancyButton = JButton("Ban vacancy")
        this.banVacancyButton.isEnabled = false
        this.banVacancyButton.addActionListener { e -> this.vacancyManager.banSelectedVacancy() }
        buttonPanel.add(this.banVacancyButton)

        this.banEmployerButton = JButton("Ban company")
        this.banEmployerButton.addActionListener { e -> this.vacancyManager.banSelectedVacancyEmployer() }
        this.banEmployerButton.isEnabled = false
        buttonPanel.add(banEmployerButton)

        this.requestDescriptionButton = JButton("Request vacancy description")
        this.requestDescriptionButton.addActionListener { e -> this.vacancyManager.requestSelectedVacancyDescription() }
        this.requestDescriptionButton.isEnabled = false
        buttonPanel.add(requestDescriptionButton)

        add(buttonPanel, BorderLayout.EAST)
    }

    override fun addNotify() {
        super.addNotify()
        this.vacancyManager.addVacancySelectionListener(this)
        this.vacancyManager.addVacancyDescriptionRequestListener(this)
    }

    override fun removeNotify() {
        this.vacancyManager.removeVacancyDescriptionRequestListener(this)
        this.vacancyManager.removeVacancySelectionListener(this)
        super.removeNotify()
    }

    override fun onVacancySelection() {
        val aggregatedVacancy = this.vacancyManager.selectedVacancy
        val description = StringBuilder()
        if (aggregatedVacancy != null) {
            val vacancy = aggregatedVacancy.vacancy
            if (vacancy != null) {
                description.append(String.format("<b>%s</b>", vacancy.name))
                description.append("<br/>")
                description.append("URL: ")
                description.append(vacancy.url)
                description.append("<br/>")
                val salary = vacancy.salary
                if (salary != null) {
                    description.append(String.format("<b>%s</b>", salary.toString()))
                    description.append("<br/>")
                }
                description.append("<br/>")
                val employer = vacancy.employer
                if (employer != null) {
                    description.append(String.format("<b>%s</b>", employer.name))
                    description.append("<br/>")
                    description.append("URL: ")
                    description.append(employer.url)
                    description.append("<br/>")
                    description.append("<br/>")
                }
                val snippet = vacancy.snippet
                if (snippet != null) {
                    description.append(snippet.toString())
                    description.append("<br/>")
                    description.append("<br/>")
                }
                val vacancyDescription = vacancy.description
                if (vacancyDescription != null && !vacancyDescription.isEmpty()) {
                    description.append("<b>Detailed description: </b>")
                    description.append("<br/>")
                    description.append(vacancyDescription)
                }
            }
        }
        EventQueue.invokeLater {
            var vacancyDescription = description.toString().trim { it <= ' ' }
            if (vacancyDescription.isEmpty()) {
                vacancyDescription = "<br/>"
            }
            this.vacancyDescriptionTextArea.text = vacancyDescription
            this.banVacancyButton.isEnabled = aggregatedVacancy != null && !aggregatedVacancy.vacancy!!.isBanned
            this.banEmployerButton.isEnabled =
                aggregatedVacancy != null && !aggregatedVacancy.vacancy!!.employer!!.isBanned
            this.requestDescriptionButton.isEnabled = (aggregatedVacancy != null //
                    && (aggregatedVacancy.vacancy!!.description == null || aggregatedVacancy.vacancy.description!!.isEmpty()))
        }
    }

    override fun onVacancyDescriptionRequestStart(url: String) {
        EventQueue.invokeLater {
            this.banVacancyButton.isEnabled = false
            this.banEmployerButton.isEnabled = false
            this.requestDescriptionButton.isEnabled = false
        }
    }

    override fun onVacancyDescriptionRequestFinish() {
        onVacancySelection()
    }
}
