package me.anonymoussoftware.vacancymanager.ui.panels

import me.anonymoussoftware.vacancymanager.App
import me.anonymoussoftware.vacancymanager.VacancyManager
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancyDescriptionRequestListener
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancySelectionListener
import java.awt.BorderLayout
import java.awt.EventQueue
import javax.swing.*

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
        this.banVacancyButton.addActionListener { _ -> this.vacancyManager.banSelectedVacancy() }
        buttonPanel.add(this.banVacancyButton)

        this.banEmployerButton = JButton("Ban company")
        this.banEmployerButton.addActionListener { _ -> this.vacancyManager.banSelectedVacancyEmployer() }
        this.banEmployerButton.isEnabled = false
        buttonPanel.add(banEmployerButton)

        this.requestDescriptionButton = JButton("Request vacancy description")
        this.requestDescriptionButton.addActionListener { _ -> this.vacancyManager.requestSelectedVacancyDescription() }
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
                description.append(
                    """
                    <b>${vacancy.name}</b>
                    <br/>
                    URL: ${vacancy.url}
                    <br/>
                    """
                )
                val salary = vacancy.salary
                if (salary != null) {
                    description.append("<b>$salary</b><br/>")
                }
                description.append(
                    """
                    <br/>
                    <b>${vacancy.employer.name}</b>
                    <br/>
                    URL: ${vacancy.employer.url}
                    <br/>
                    <br/>
                    """
                )
                val snippet = vacancy.snippet
                if (snippet != null) {
                    description.append("$snippet<br/><br/>")
                }
                val vacancyDescription = vacancy.description
                if (vacancyDescription != null && vacancyDescription.isNotEmpty()) {
                    description.append("<b>Detailed description: </b><br/>$vacancyDescription")
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
                aggregatedVacancy != null && !aggregatedVacancy.vacancy!!.employer.isBanned
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
