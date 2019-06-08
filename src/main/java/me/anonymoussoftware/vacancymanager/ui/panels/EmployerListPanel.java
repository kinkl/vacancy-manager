package me.anonymoussoftware.vacancymanager.ui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.List;

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
import me.anonymoussoftware.vacancymanager.VacancyManager.EmployerListChangeListener;
import me.anonymoussoftware.vacancymanager.model.Employer;

@SuppressWarnings("serial")
public class EmployerListPanel extends JPanel implements EmployerListChangeListener {

    private final DefaultListModel<Employer> employerListModel;

    private final VacancyManager vacancyManager = App.getBean(VacancyManager.class);

    private final JLabel label;

    private final JList<Employer> employerList;

    public EmployerListPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.label = new JLabel("Companies:");
        add(this.label);

        this.employerListModel = new DefaultListModel<>();
        this.employerList = new JList<>(this.employerListModel);
        this.employerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.employerList.setCellRenderer(new EmployerCellRenderer());
        JScrollPane vacancyListScrollPane = new JScrollPane(this.employerList);
        add(vacancyListScrollPane);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.vacancyManager.addEmployerListChangeListener(this);
    }

    @Override
    public void removeNotify() {
        this.vacancyManager.removeEmployerListChangeListener(this);
        super.removeNotify();
    }

    @Override
    public void onEmployerListChange() {
        EventQueue.invokeLater(() -> {
            List<Employer> employers = this.vacancyManager.getEmployers();
            int selectedIndex = this.employerList.getSelectedIndex();
            this.employerListModel.clear();
            employers.stream().forEach(this.employerListModel::addElement);
            if (selectedIndex >= 0) {
                int employerListSize = this.employerList.getModel().getSize();
                this.employerList.setSelectedIndex(Math.min(selectedIndex, Math.max(employerListSize - 1, 0)));
            }
            this.label.setText("Companies: " + employers.size());
        });
    }

    private class EmployerCellRenderer extends JLabel implements ListCellRenderer<Employer> {

        @Override
        public Component getListCellRendererComponent(JList<? extends Employer> list, Employer value, int index,
                boolean isSelected, boolean cellHasFocus) {
            Employer selectedValue = list.getSelectedValue();
            Color backgroundColor;
            Color foregroundColor = list.getForeground();
            if (selectedValue != null && selectedValue.getId() == value.getId()) {
                backgroundColor = list.getSelectionBackground();
                foregroundColor = list.getSelectionForeground();
            } else if (value.isBanned()) {
                backgroundColor = Color.DARK_GRAY;
                foregroundColor = Color.WHITE;
            } else {
                backgroundColor = list.getBackground();
            }
            setBackground(backgroundColor);
            setForeground(foregroundColor);
            setOpaque(true);
            setFont(list.getFont());
            setText(value.getName());
            return this;
        }

    }

}
