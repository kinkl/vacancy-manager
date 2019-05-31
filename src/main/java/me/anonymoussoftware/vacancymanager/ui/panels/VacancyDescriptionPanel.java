package me.anonymoussoftware.vacancymanager.ui.panels;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import me.anonymoussoftware.vacancymanager.App;
import me.anonymoussoftware.vacancymanager.VacancyManager;
import me.anonymoussoftware.vacancymanager.VacancyManager.VacancySelectionListener;
import me.anonymoussoftware.vacancymanager.model.Vacancy;

@SuppressWarnings("serial")
public class VacancyDescriptionPanel extends JPanel implements VacancySelectionListener {
	
	private final JTextPane vacancyDescriptionTextArea;
	
	private final VacancyManager vacancyManager = App.getBean(VacancyManager.class);
	
	private final JButton banVacancyButton;
	
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
		
		JButton banCompanyButton = new JButton("Ban company");
		buttonPanel.add(banCompanyButton);
		
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
		Vacancy vacancy = this.vacancyManager.getSelectedVacancy();
		String text = Optional.ofNullable(vacancy) //
				.map(Vacancy::getSnippet) //
				.map(Object::toString) //
				.orElse("");
		EventQueue.invokeLater(() -> {
			this.vacancyDescriptionTextArea.setText(text);
			this.banVacancyButton.setEnabled(vacancy != null);
		});
	}
}
