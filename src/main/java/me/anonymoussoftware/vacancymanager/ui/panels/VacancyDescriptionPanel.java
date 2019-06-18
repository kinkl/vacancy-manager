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
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancyDescriptionRequestListener;
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancySelectionListener;
import me.anonymoussoftware.vacancymanager.model.Employer;
import me.anonymoussoftware.vacancymanager.model.Salary;
import me.anonymoussoftware.vacancymanager.model.Snippet;
import me.anonymoussoftware.vacancymanager.model.Vacancy;
import me.anonymoussoftware.vacancymanager.model.aggregated.AggregatedVacancy;

@SuppressWarnings("serial")
public class VacancyDescriptionPanel extends JPanel implements VacancySelectionListener, VacancyDescriptionRequestListener {

    private final JTextPane vacancyDescriptionTextArea;

    private final VacancyManager vacancyManager = App.getBean(VacancyManager.class);

    private final JButton banVacancyButton;

    private final JButton banEmployerButton;

    private final JButton requestDescriptionButton;

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

        this.requestDescriptionButton = new JButton("Request vacancy description");
        this.requestDescriptionButton.addActionListener(e -> this.vacancyManager.requestSelectedVacancyDescription());
        this.requestDescriptionButton.setEnabled(false);
        buttonPanel.add(requestDescriptionButton);

        add(buttonPanel, BorderLayout.EAST);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.vacancyManager.addVacancySelectionListener(this);
        this.vacancyManager.addVacancyDescriptionRequestListener(this);
    }

    @Override
    public void removeNotify() {
        this.vacancyManager.removeVacancyDescriptionRequestListener(this);
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
                description.append(String.format("<b>%s</b>", vacancy.getName()));
                description.append("<br/>");
                description.append("URL: ");
                description.append(vacancy.getUrl());
                description.append("<br/>");
                Salary salary = vacancy.getSalary();
                if (salary != null) {
                    description.append(String.format("<b>%s</b>", salary.toString()));
                    description.append("<br/>");
                }
                description.append("<br/>");
                Employer employer = vacancy.getEmployer();
                if (employer != null) {
                    description.append(String.format("<b>%s</b>", employer.getName()));
                    description.append("<br/>");
                    description.append("URL: ");
                    description.append(employer.getUrl());
                    description.append("<br/>");
                    description.append("<br/>");
                }
                Snippet snippet = vacancy.getSnippet();
                if (snippet != null) {
                    description.append(snippet.toString());
                    description.append("<br/>");
                    description.append("<br/>");
                }
                String vacancyDescription = vacancy.getDescription();
                if (vacancyDescription != null && !vacancyDescription.isEmpty()) {
                    description.append("<b>Detailed description: </b>");
                    description.append("<br/>");
                    description.append(vacancyDescription);
                }
            }
        }
        EventQueue.invokeLater(() -> {
            String vacancyDescription = description.toString().trim();
            if (vacancyDescription.isEmpty()) {
                vacancyDescription = "<br/>";
            }
            this.vacancyDescriptionTextArea.setText(vacancyDescription);
            this.banVacancyButton.setEnabled(aggregatedVacancy != null && !aggregatedVacancy.getVacancy().isBanned());
            this.banEmployerButton
                    .setEnabled(aggregatedVacancy != null && !aggregatedVacancy.getVacancy().getEmployer().isBanned());
            this.requestDescriptionButton.setEnabled(aggregatedVacancy != null //
                    && (aggregatedVacancy.getVacancy().getDescription() == null
                            || aggregatedVacancy.getVacancy().getDescription().isEmpty()));
        });
    }

    @Override
    public void onVacancyDescriptionRequestStart(String url) {
        EventQueue.invokeLater(() -> {
            this.banVacancyButton.setEnabled(false);
            this.banEmployerButton.setEnabled(false);
            this.requestDescriptionButton.setEnabled(false);
        });
    }

    @Override
    public void onVacancyDescriptionRequestFinish() {
        onVacancySelection();
    }
}
