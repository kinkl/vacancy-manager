package me.anonymoussoftware.vacancymanager.ui.frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import me.anonymoussoftware.vacancymanager.App;
import me.anonymoussoftware.vacancymanager.VacancyManager;
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancyDescriptionRequestListener;
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancySearchListener;
import me.anonymoussoftware.vacancymanager.ui.panels.EmployerListPanel;
import me.anonymoussoftware.vacancymanager.ui.panels.SearchFormPanel;
import me.anonymoussoftware.vacancymanager.ui.panels.VacancyDescriptionPanel;
import me.anonymoussoftware.vacancymanager.ui.panels.VacancyListPanel;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements VacancySearchListener, VacancyDescriptionRequestListener {

    private static final int FRAME_WIDTH = 900;

    private static final int FRAME_HEIGHT = 700;

    private final VacancyListPanel vacancyListPanel;

    private final EmployerListPanel bannedEmployerListPanel;

    private final JLabel statusBarLabel;

    private final JProgressBar progressBar;

    private final VacancyManager vacancyManager = App.getBean(VacancyManager.class);

    public MainFrame() {
        setTitle("Vacancy manager");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - FRAME_WIDTH) / 2, (screenSize.height - FRAME_HEIGHT) / 2);
        setLayout(new BorderLayout());

        add(new SearchFormPanel(), BorderLayout.NORTH);

        this.vacancyListPanel = new VacancyListPanel();
        this.bannedEmployerListPanel = new EmployerListPanel();
        JSplitPane innerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.vacancyListPanel,
                this.bannedEmployerListPanel);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, innerSplitPane,
                new VacancyDescriptionPanel());
        add(splitPane, BorderLayout.CENTER);

        JPanel statusBarPanel = new JPanel();
        statusBarPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBarPanel.setPreferredSize(new Dimension(getWidth(), 20));
        statusBarPanel.setLayout(new BoxLayout(statusBarPanel, BoxLayout.X_AXIS));
        this.statusBarLabel = new JLabel("Status");
        this.statusBarLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusBarPanel.add(this.statusBarLabel);
        this.progressBar = new JProgressBar(0, 100);
        this.progressBar.setStringPainted(true);
        this.progressBar.setVisible(false);
        statusBarPanel.add(this.progressBar);
        add(statusBarPanel, BorderLayout.SOUTH);

        JMenuBar menubar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenu vacanciesMenu = new JMenu("Vacancies");
        JMenuItem importVacanciesMenuItem = new JMenuItem("Export...");
        importVacanciesMenuItem.addActionListener(this::onImportVacanciesMenuItemAction);
        vacanciesMenu.add(importVacanciesMenuItem);

        JMenuItem exportVacanciesMenuItem = new JMenuItem("Import...");
        exportVacanciesMenuItem.addActionListener(this::onExportVacanciesMenuItemAction);
        vacanciesMenu.add(exportVacanciesMenuItem);
        fileMenu.add(vacanciesMenu);

        JMenu employersMenu = new JMenu("Employers");
        JMenuItem importEmployersMenuItem = new JMenuItem("Export...");
        importEmployersMenuItem.addActionListener(this::onImportEmployersMenuItemAction);
        employersMenu.add(importEmployersMenuItem);

        JMenuItem exportEmployersMenuItem = new JMenuItem("Import...");
        exportEmployersMenuItem.addActionListener(this::onExportEmployersMenuItemAction);
        employersMenu.add(exportEmployersMenuItem);
        fileMenu.add(employersMenu);

        menubar.add(fileMenu);
        setJMenuBar(menubar);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.vacancyManager.addVacancySearchListener(this);
        this.vacancyManager.addVacancyDescriptionRequestListener(this);
    }

    @Override
    public void removeNotify() {
        this.vacancyManager.removeVacancyDescriptionRequestListener(this);
        this.vacancyManager.removeVacancySearchListener(this);
        super.removeNotify();
    }

    private JFileChooser createFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
        chooser.setAcceptAllFileFilterUsed(false);
        return chooser;
    }

    private void onImportVacanciesMenuItemAction(ActionEvent e) {
        JFileChooser chooser = createFileChooser();
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            String selectedFilePath = selectedFile.getPath();
            if (!selectedFilePath.toString().endsWith(".json")) {
                selectedFile = new File(selectedFilePath + ".json");
            }
            this.vacancyManager.importVacancies(selectedFile, //
                    this::showSuccessfullyLoadedVacanciesMessageDialog, //
                    this::showUnableToSaveVacanciesToFile);
        }
    }

    private void onExportVacanciesMenuItemAction(ActionEvent e) {
        JFileChooser chooser = createFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            this.vacancyManager.exportVacancies(chooser.getSelectedFile(), //
                    this::showUnableToLoadVacanciesFromFileMessageDialog);
        }
    }

    private void onImportEmployersMenuItemAction(ActionEvent e) {
        JFileChooser chooser = createFileChooser();
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            String selectedFilePath = selectedFile.getPath();
            if (!selectedFilePath.toString().endsWith(".json")) {
                selectedFile = new File(selectedFilePath + ".json");
            }
            this.vacancyManager.importEmployers(selectedFile, //
                    this::showSuccessfullyLoadedEmployersMessageDialog, //
                    this::showUnableToSaveEmployersToFile);
        }
    }

    private void onExportEmployersMenuItemAction(ActionEvent e) {
        JFileChooser chooser = createFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            this.vacancyManager.exportEmployers(chooser.getSelectedFile(), //
                    this::showUnableToLoadEmployersFromFileMessageDialog);
        }
    }

    private void showUnableToLoadVacanciesFromFileMessageDialog() {
        JOptionPane.showMessageDialog(this, "Unable to load vacancies from file", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessfullyLoadedVacanciesMessageDialog() {
        JOptionPane.showMessageDialog(this, "Vacancies are saved successfully", "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showUnableToSaveVacanciesToFile() {
        JOptionPane.showMessageDialog(this, "Unable to save employers to file", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showUnableToLoadEmployersFromFileMessageDialog() {
        JOptionPane.showMessageDialog(this, "Unable to load employers from file", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessfullyLoadedEmployersMessageDialog() {
        JOptionPane.showMessageDialog(this, "Employers are saved successfully", "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showUnableToSaveEmployersToFile() {
        JOptionPane.showMessageDialog(this, "Unable to save vacancies to file", "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onVacancyPageSuccessfulLoad(int percentage) {
        EventQueue.invokeLater(() -> {
            this.progressBar.setValue(percentage);
            this.progressBar.setString("Progress: " + percentage + "%");
            boolean isSearchCompleted = percentage == 100;
            if (isSearchCompleted) {
                this.statusBarLabel.setText("Vacancies are downloaded successfully");
            }
            this.statusBarLabel.setVisible(isSearchCompleted);
            this.progressBar.setVisible(!isSearchCompleted);
        });
    }

    @Override
    public void onStartVacancyPageLoad(String url) {
        EventQueue.invokeLater(
                () -> this.progressBar.setString(this.progressBar.getString() + " [Requesting " + url + "]"));
    }

    @Override
    public void onSearchFinish(int loaded, int total) {
        if (loaded == total) {
            JOptionPane.showMessageDialog(this, "Vacancy search is successfully done. Loaded: " + loaded, "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Vacancy search is done. Loaded " + loaded + " of " + total + " due to site restriction", "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }

    }

    @Override
    public void onVacancyDescriptionRequestStart(String url) {
        EventQueue.invokeLater(() -> {
            this.progressBar.setString(this.progressBar.getString() + " [Requesting " + url + "]");
            this.statusBarLabel.setVisible(false);
            this.progressBar.setVisible(true);
        });
    }

    @Override
    public void onVacancyDescriptionRequestFinish() {
        EventQueue.invokeLater(() -> {
            this.statusBarLabel.setText("Vacancy description is downloaded successfully");
            this.statusBarLabel.setVisible(true);
            this.progressBar.setVisible(false);
        });
    }
}
