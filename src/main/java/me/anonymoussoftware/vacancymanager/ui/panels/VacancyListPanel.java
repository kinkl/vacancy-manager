package me.anonymoussoftware.vacancymanager.ui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import me.anonymoussoftware.vacancymanager.App;
import me.anonymoussoftware.vacancymanager.VacancyManager;
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancyListChangeListener;
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancySearchListener;
import me.anonymoussoftware.vacancymanager.api.result.VacancyListResult;
import me.anonymoussoftware.vacancymanager.model.Vacancy;

@SuppressWarnings("serial")
public class VacancyListPanel extends JPanel
        implements VacancyListChangeListener, VacancySearchListener {

    private final DefaultListModel<Vacancy> vacancyListModel;

    private final VacancyManager vacancyManager = App.getBean(VacancyManager.class);

    private final JLabel label;

    private final JList<Vacancy> vacanciesList;

    public VacancyListPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.label = new JLabel("Vacancies:");
        add(this.label);

        this.vacancyListModel = new DefaultListModel<>();
        this.vacanciesList = new JList<>(this.vacancyListModel);
        this.vacanciesList.setCellRenderer(new VacancyCellRenderer());
        this.vacanciesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.vacanciesList.addListSelectionListener(
                e -> this.vacancyManager.setSelectedVacancy(this.vacanciesList.getSelectedValue()));
        JScrollPane vacancyListScrollPane = new JScrollPane(this.vacanciesList);
        add(vacancyListScrollPane);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.vacancyManager.addVacancyListChangeListener(this);
        this.vacancyManager.addVacancySearchListener(this);
    }

    @Override
    public void removeNotify() {
        this.vacancyManager.removeVacancySearchListener(this);
        this.vacancyManager.removeVacancyListChangeListener(this);
        super.removeNotify();
    }

    @Override
    public void onVacancyListChange(VacancyListChangeListener.VacancyListChangeReason reason) {
        EventQueue.invokeLater(() -> {
            VacancyListResult vacancies = this.vacancyManager.getAvailableVacancies();
            int selectedIndex = this.vacanciesList.getSelectedIndex();
            this.vacancyListModel.clear();
            vacancies.getVacancies().stream().forEach(this.vacancyListModel::addElement);
            if (reason == VacancyListChangeListener.VacancyListChangeReason.VACANCY_BAN && selectedIndex >= 0) {
                int vacancyListSize = this.vacanciesList.getModel().getSize();
                this.vacanciesList.setSelectedIndex(Math.min(selectedIndex, Math.max(vacancyListSize - 1, 0)));
            }
            long notBannedVacancyCount = vacancies.getVacancies().stream() //
                    .filter(v -> !v.isBanned()) //
                    .map(Vacancy::getEmployer) //
                    .filter(e -> !this.vacancyManager.isEmployerBanned(e.getId())) //
                    .count();
            String bannedVacancyCountString = "";
            long bannedVacancyCount = vacancies.getVacancies().size() - notBannedVacancyCount;
            if (bannedVacancyCount > 0) {
                bannedVacancyCountString = " (banned " + bannedVacancyCount + ") ";
            }
            String text = String.format("Vacancies: %d%s from %d", notBannedVacancyCount, bannedVacancyCountString,
                    vacancies.getTotal());
            this.label.setText(text);
        });
    }

    private class VacancyCellRenderer extends JLabel implements ListCellRenderer<Vacancy> {

        @Override
        public Component getListCellRendererComponent(JList<? extends Vacancy> list, Vacancy value, int index,
                boolean isSelected, boolean cellHasFocus) {
            Vacancy selectedValue = list.getSelectedValue();
            Color backgroundColor;
            Color foregroundColor = list.getForeground();
            if (selectedValue != null && selectedValue.getId() == value.getId()) {
                backgroundColor = list.getSelectionBackground();
                foregroundColor = list.getSelectionForeground();
            } else if (value.isBanned()) {
                backgroundColor = Color.RED;
                foregroundColor = Color.WHITE;
            } else if (VacancyListPanel.this.vacancyManager.isEmployerBanned(value.getEmployer().getId())) {
                backgroundColor = Color.DARK_GRAY;
                foregroundColor = Color.WHITE;
            } else {
                backgroundColor = list.getBackground();
            }
            setBackground(backgroundColor);
            setForeground(foregroundColor);
            setOpaque(true);
            setFont(list.getFont());
            setText(value.getName() + " (" + value.getEmployer().getName() + ")");
            return this;
        }

    }

    @Override
    public void onVacancyPageSuccessfulLoad(int percentage) {
        if (percentage == 0) {
            EventQueue.invokeLater(this.vacancyListModel::clear);
        }
    }

    @Override
    public void onStartVacancyPageLoad(String url) {

    }

    @Override
    public void onSearchFinish(int loaded, int total) {

    }

}
