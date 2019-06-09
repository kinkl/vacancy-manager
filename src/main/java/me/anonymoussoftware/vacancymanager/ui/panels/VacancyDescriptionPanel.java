package me.anonymoussoftware.vacancymanager.ui.panels;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import me.anonymoussoftware.vacancymanager.App;
import me.anonymoussoftware.vacancymanager.VacancyManager;
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancySelectionListener;
import me.anonymoussoftware.vacancymanager.model.Snippet;
import me.anonymoussoftware.vacancymanager.model.Vacancy;
import me.anonymoussoftware.vacancymanager.model.aggregated.AggregatedVacancy;

@SuppressWarnings("serial")
public class VacancyDescriptionPanel extends JPanel implements VacancySelectionListener {

    private final JTextPane vacancyDescriptionTextArea;

    private final VacancyManager vacancyManager = App.getBean(VacancyManager.class);

    private final JButton banVacancyButton;

    private final JButton banEmployerButton;

    public VacancyDescriptionPanel() {
        setLayout(new BorderLayout());

        this.vacancyDescriptionTextArea = new JTextPane();
        this.vacancyDescriptionTextArea.setContentType("text/html");
        add(new JScrollPane(this.vacancyDescriptionTextArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        this.banVacancyButton = new JButton("Ban vacancy");
        this.banVacancyButton.setEnabled(false);
        this.banVacancyButton.addActionListener(e -> this.vacancyManager.banSelectedVacancy());
        buttonPanel.add(this.banVacancyButton);

        this.banEmployerButton = new JButton("Ban company");
        this.banEmployerButton.addActionListener(e -> this.vacancyManager.banSelectedVacancyEmployer());
        this.banEmployerButton.setEnabled(false);
        buttonPanel.add(banEmployerButton);

        add(buttonPanel, BorderLayout.EAST);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.vacancyManager.addVacancySelectionListener(this);
    }

    @Override
    public void removeNotify() {
        this.vacancyManager.removeVacancySelectionListener(this);
        super.removeNotify();
    }

    @Override
    public void onVacancySelection() {
        AggregatedVacancy aggregatedVacancy = this.vacancyManager.getSelectedVacancy();
        StringBuilder description = new StringBuilder();
        if (aggregatedVacancy != null) {
            Vacancy vacancy = aggregatedVacancy.getVacancy();
            if (vacancy != null) {
                description.append("<b>Vacancy URL: </b>");
                description.append(aggregatedVacancy.getVacancy().getUrl());
                description.append("<br/>");
                description.append("<br/>");
                Snippet snippet = vacancy.getSnippet();
                if (snippet != null) {
                    description.append(snippet.toString());
                }
            }
        }
        EventQueue.invokeLater(() -> {
            this.vacancyDescriptionTextArea.setText(description.toString());
            this.banVacancyButton.setEnabled(aggregatedVacancy != null && !aggregatedVacancy.getVacancy().isBanned());
            this.banEmployerButton
                    .setEnabled(aggregatedVacancy != null && !aggregatedVacancy.getVacancy().getEmployer().isBanned());
        });
    }
}
