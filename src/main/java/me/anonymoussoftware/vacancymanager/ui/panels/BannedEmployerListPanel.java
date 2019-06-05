package me.anonymoussoftware.vacancymanager.ui.panels;

import java.awt.EventQueue;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import me.anonymoussoftware.vacancymanager.App;
import me.anonymoussoftware.vacancymanager.VacancyManager;
import me.anonymoussoftware.vacancymanager.VacancyManager.BannedEmployerListChangeListener;
import me.anonymoussoftware.vacancymanager.model.Employer;

@SuppressWarnings("serial")
public class BannedEmployerListPanel extends JPanel implements BannedEmployerListChangeListener {

    private final DefaultListModel<Employer> bannedEmployerListModel;

    private final VacancyManager vacancyManager = App.getBean(VacancyManager.class);

    private final JLabel label;

    private final JList<Employer> bannedEmployerList;

    public BannedEmployerListPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.label = new JLabel("Banned companies:");
        add(this.label);

        this.bannedEmployerListModel = new DefaultListModel<>();
        this.bannedEmployerList = new JList<>(this.bannedEmployerListModel);
        this.bannedEmployerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane vacancyListScrollPane = new JScrollPane(this.bannedEmployerList);
        add(vacancyListScrollPane);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.vacancyManager.addBannedEmployerListChangeListener(this);
    }

    @Override
    public void removeNotify() {
        this.vacancyManager.removeBannedEmployerListChangeListener(this);
        super.removeNotify();
    }

    @Override
    public void onBannedEmployerListChange() {
        EventQueue.invokeLater(() -> {
            List<Employer> bannedEmployers = this.vacancyManager.getBannedEmployers();
            int selectedIndex = this.bannedEmployerList.getSelectedIndex();
            this.bannedEmployerListModel.clear();
            bannedEmployers.stream().forEach(this.bannedEmployerListModel::addElement);
            if (selectedIndex >= 0) {
                int bannedEmployerListSize = this.bannedEmployerList.getModel().getSize();
                this.bannedEmployerList
                        .setSelectedIndex(Math.min(selectedIndex, Math.max(bannedEmployerListSize - 1, 0)));
            }
            this.label.setText("Banned companies: " + bannedEmployers.size());
        });
    }

}
