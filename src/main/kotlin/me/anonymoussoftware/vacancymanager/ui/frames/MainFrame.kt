package me.anonymoussoftware.vacancymanager.ui.frames

import me.anonymoussoftware.vacancymanager.App
import me.anonymoussoftware.vacancymanager.VacancyManager
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancyDescriptionRequestListener
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancySearchListener
import me.anonymoussoftware.vacancymanager.ui.panels.EmployerListPanel
import me.anonymoussoftware.vacancymanager.ui.panels.SearchFormPanel
import me.anonymoussoftware.vacancymanager.ui.panels.VacancyDescriptionPanel
import me.anonymoussoftware.vacancymanager.ui.panels.VacancyListPanel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Toolkit
import java.io.File
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.filechooser.FileNameExtensionFilter

class MainFrame : JFrame(), VacancySearchListener, VacancyDescriptionRequestListener {

    private val vacancyListPanel: VacancyListPanel

    private val bannedEmployerListPanel: EmployerListPanel

    private val statusBarLabel: JLabel

    private val progressBar: JProgressBar

    private val vacancyManager = App.getBean(VacancyManager::class.java)

    init {
        title = "Vacancy manager"
        setSize(FRAME_WIDTH, FRAME_HEIGHT)
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        setLocation((screenSize.width - FRAME_WIDTH) / 2, (screenSize.height - FRAME_HEIGHT) / 2)
        layout = BorderLayout()

        add(SearchFormPanel(), BorderLayout.NORTH)

        this.vacancyListPanel = VacancyListPanel()
        this.bannedEmployerListPanel = EmployerListPanel()
        val innerSplitPane = JSplitPane(
            JSplitPane.VERTICAL_SPLIT, this.vacancyListPanel,
            this.bannedEmployerListPanel
        )
        val splitPane = JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, innerSplitPane,
            VacancyDescriptionPanel()
        )
        add(splitPane, BorderLayout.CENTER)

        val statusBarPanel = JPanel()
        statusBarPanel.border = BevelBorder(BevelBorder.LOWERED)
        statusBarPanel.preferredSize = Dimension(width, 20)
        statusBarPanel.layout = BoxLayout(statusBarPanel, BoxLayout.X_AXIS)
        this.statusBarLabel = JLabel("Status")
        this.statusBarLabel.horizontalAlignment = SwingConstants.LEFT
        statusBarPanel.add(this.statusBarLabel)
        this.progressBar = JProgressBar(0, 100)
        this.progressBar.isStringPainted = true
        this.progressBar.isVisible = false
        statusBarPanel.add(this.progressBar)
        add(statusBarPanel, BorderLayout.SOUTH)

        val menubar = JMenuBar()
        val fileMenu = JMenu("File")

        val vacanciesMenu = JMenu("Vacancies")
        val importVacanciesMenuItem = JMenuItem("Export...")
        importVacanciesMenuItem.addActionListener( { this.onImportVacanciesMenuItemAction() })
        vacanciesMenu.add(importVacanciesMenuItem)

        val exportVacanciesMenuItem = JMenuItem("Import...")
        exportVacanciesMenuItem.addActionListener( { this.onExportVacanciesMenuItemAction() })
        vacanciesMenu.add(exportVacanciesMenuItem)
        fileMenu.add(vacanciesMenu)

        val employersMenu = JMenu("Employers")
        val importEmployersMenuItem = JMenuItem("Export...")
        importEmployersMenuItem.addActionListener( { this.onImportEmployersMenuItemAction() })
        employersMenu.add(importEmployersMenuItem)

        val exportEmployersMenuItem = JMenuItem("Import...")
        exportEmployersMenuItem.addActionListener { this.onExportEmployersMenuItemAction() }
        employersMenu.add(exportEmployersMenuItem)
        fileMenu.add(employersMenu)

        menubar.add(fileMenu)
        jMenuBar = menubar
    }

    override fun addNotify() {
        super.addNotify()
        this.vacancyManager.addVacancySearchListener(this)
        this.vacancyManager.addVacancyDescriptionRequestListener(this)
    }

    override fun removeNotify() {
        this.vacancyManager.removeVacancyDescriptionRequestListener(this)
        this.vacancyManager.removeVacancySearchListener(this)
        super.removeNotify()
    }

    private fun createFileChooser(): JFileChooser {
        val chooser = JFileChooser()
        chooser.currentDirectory = File(".")
        chooser.fileFilter = FileNameExtensionFilter("JSON files", "json")
        chooser.isAcceptAllFileFilterUsed = false
        return chooser
    }

    private fun onImportVacanciesMenuItemAction() {
        val chooser = createFileChooser()
        val result = chooser.showSaveDialog(this)
        if (result == JFileChooser.APPROVE_OPTION) {
            var selectedFile = chooser.selectedFile
            val selectedFilePath = selectedFile.path
            if (!selectedFilePath.toString().endsWith(".json")) {
                selectedFile = File("$selectedFilePath.json")
            }
            this.vacancyManager.importVacancies(selectedFile, //
                Runnable { this.showSuccessfullyLoadedVacanciesMessageDialog() }, //
                Runnable { this.showUnableToSaveVacanciesToFile() })
        }
    }

    private fun onExportVacanciesMenuItemAction() {
        val chooser = createFileChooser()
        val result = chooser.showOpenDialog(this)
        if (result == JFileChooser.APPROVE_OPTION) {
            this.vacancyManager.exportVacancies(chooser.selectedFile, //
                Runnable { this.showUnableToLoadVacanciesFromFileMessageDialog() })
        }
    }

    private fun onImportEmployersMenuItemAction() {
        val chooser = createFileChooser()
        val result = chooser.showSaveDialog(this)
        if (result == JFileChooser.APPROVE_OPTION) {
            var selectedFile = chooser.selectedFile
            val selectedFilePath = selectedFile.path
            if (!selectedFilePath.toString().endsWith(".json")) {
                selectedFile = File("$selectedFilePath.json")
            }
            this.vacancyManager.importEmployers(selectedFile, //
                Runnable { this.showSuccessfullyLoadedEmployersMessageDialog() }, //
                Runnable { this.showUnableToSaveEmployersToFile() })
        }
    }

    private fun onExportEmployersMenuItemAction() {
        val chooser = createFileChooser()
        val result = chooser.showOpenDialog(this)
        if (result == JFileChooser.APPROVE_OPTION) {
            this.vacancyManager.exportEmployers(chooser.selectedFile, //
                Runnable { this.showUnableToLoadEmployersFromFileMessageDialog() })
        }
    }

    private fun showUnableToLoadVacanciesFromFileMessageDialog() {
        JOptionPane.showMessageDialog(this, "Unable to load vacancies from file", "Error", JOptionPane.ERROR_MESSAGE)
    }

    private fun showSuccessfullyLoadedVacanciesMessageDialog() {
        JOptionPane.showMessageDialog(
            this, "Vacancies are saved successfully", "Success",
            JOptionPane.INFORMATION_MESSAGE
        )
    }

    private fun showUnableToSaveVacanciesToFile() {
        JOptionPane.showMessageDialog(this, "Unable to save employers to file", "Error", JOptionPane.ERROR_MESSAGE)
    }

    private fun showUnableToLoadEmployersFromFileMessageDialog() {
        JOptionPane.showMessageDialog(this, "Unable to load employers from file", "Error", JOptionPane.ERROR_MESSAGE)
    }

    private fun showSuccessfullyLoadedEmployersMessageDialog() {
        JOptionPane.showMessageDialog(
            this, "Employers are saved successfully", "Success",
            JOptionPane.INFORMATION_MESSAGE
        )
    }

    private fun showUnableToSaveEmployersToFile() {
        JOptionPane.showMessageDialog(this, "Unable to save vacancies to file", "Error", JOptionPane.ERROR_MESSAGE)
    }

    override fun onVacancyPageSuccessfulLoad(percentage: Int) {
        EventQueue.invokeLater {
            this.progressBar.value = percentage
            this.progressBar.string = "Progress: $percentage%"
            val isSearchCompleted = percentage == 100
            if (isSearchCompleted) {
                this.statusBarLabel.text = "Vacancies are downloaded successfully"
            }
            this.statusBarLabel.isVisible = isSearchCompleted
            this.progressBar.isVisible = !isSearchCompleted
        }
    }

    override fun onStartVacancyPageLoad(url: String) {
        EventQueue.invokeLater { this.progressBar.string = this.progressBar.string + " [Requesting " + url + "]" }
    }

    override fun onSearchFinish(loaded: Int, total: Int) {
        if (loaded == total) {
            JOptionPane.showMessageDialog(
                this, "Vacancy search is successfully done. Loaded: $loaded", "Success",
                JOptionPane.INFORMATION_MESSAGE
            )
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Vacancy search is done. Loaded $loaded of $total due to site restriction", "Warning",
                JOptionPane.WARNING_MESSAGE
            )
        }

    }

    override fun onVacancyDescriptionRequestStart(url: String) {
        EventQueue.invokeLater {
            this.progressBar.string = this.progressBar.string + " [Requesting " + url + "]"
            this.statusBarLabel.isVisible = false
            this.progressBar.isVisible = true
        }
    }

    override fun onVacancyDescriptionRequestFinish() {
        EventQueue.invokeLater {
            this.statusBarLabel.text = "Vacancy description is downloaded successfully"
            this.statusBarLabel.isVisible = true
            this.progressBar.isVisible = false
        }
    }

    companion object {

        private val FRAME_WIDTH = 900

        private val FRAME_HEIGHT = 700
    }
}
