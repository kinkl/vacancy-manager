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
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancySearchListener;
import me.anonymoussoftware.vacancymanager.ui.panels.BannedEmployerListPanel;
import me.anonymoussoftware.vacancymanager.ui.panels.SearchFormPanel;
import me.anonymoussoftware.vacancymanager.ui.panels.VacancyDescriptionPanel;
import me.anonymoussoftware.vacancymanager.ui.panels.VacancyListPanel;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements VacancySearchListener {

    private static final int FRAME_WIDTH = 900;

    private static final int FRAME_HEIGHT = 700;

    private final VacancyListPanel vacancyListPanel;

    private final BannedEmployerListPanel bannedEmployerListPanel;

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
        this.bannedEmployerListPanel = new BannedEmployerListPanel();
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

        JMenuItem saveFileMenuItem = new JMenuItem("Save as...");
        saveFileMenuItem.addActionListener(this::onSaveFileMenuItemAction);
        fileMenu.add(saveFileMenuItem);

        JMenuItem openFileMenuItem = new JMenuItem("Open...");
        openFileMenuItem.addActionListener(this::onOpenFileMenuItemAction);
        fileMenu.add(openFileMenuItem);

        menubar.add(fileMenu);
        setJMenuBar(menubar);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.vacancyManager.addVacancySearchListener(this);
    }

    @Override
    public void removeNotify() {
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

    private void onSaveFileMenuItemAction(ActionEvent e) {
        JFileChooser chooser = createFileChooser();
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            String selectedFilePath = selectedFile.getPath();
            if (!selectedFilePath.toString().endsWith(".json")) {
                selectedFile = new File(selectedFilePath + ".json");
            }
            this.vacancyManager.saveVacanciesToFile(selectedFile, //
                    this::showSuccessfullyLoadedVacanciesMessageDialog, //
                    this::showUnableToSaveVacanciesToFile);
        }
    }

    private void onOpenFileMenuItemAction(ActionEvent e) {
        JFileChooser chooser = createFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            this.vacancyManager.loadVacanciesFromFile(chooser.getSelectedFile(), //
                    this::showUnableToLoadVacanciesFromFileMessageDialog);
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
}
