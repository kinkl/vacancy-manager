package me.anonymoussoftware.vacancymanager.ui.panels;

import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import me.anonymoussoftware.vacancymanager.App;
import me.anonymoussoftware.vacancymanager.VacancyManager;

@SuppressWarnings("serial")
public class SearchFormPanel extends JPanel implements VacancyManager.VacancySearchListener {
	
	private final JTextField queryTextField;
	
	private final JButton searchButton;
	
	private final ButtonGroup cityGroup;
	
	private final VacancyManager vacancyManager = App.getBean(VacancyManager.class);

	public SearchFormPanel() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.queryTextField = new JTextField("Java");
		add(queryTextField);

		this.searchButton = new JButton("Run new search");
		this.searchButton.addActionListener(this::onSearchButtonAction);
		add(this.searchButton);
		
		this.cityGroup = new ButtonGroup();
		for (String city : this.vacancyManager.getAvailableCityNames()) {
			JRadioButton radioButton = new JRadioButton(city);
			radioButton.addActionListener(e -> this.vacancyManager.selectCity(radioButton.getText()));
			this.cityGroup.add(radioButton);
			add(radioButton);
		}
		this.cityGroup.getElements().nextElement().doClick();
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
	
	private void onSearchButtonAction(ActionEvent e) {
		this.vacancyManager.startVacanciesSearch(this.queryTextField.getText());
	}
	
	private void setCityRadioButtonsEnabled(boolean enabled) {
		Enumeration<AbstractButton> buttons = this.cityGroup.getElements();
		while (buttons.hasMoreElements()) {
			buttons.nextElement().setEnabled(enabled);
		}
	}

	@Override
	public void onVacancyPageSuccessfulLoad(int percentage) {
		this.searchButton.setEnabled(false);
		setCityRadioButtonsEnabled(false);
	}

	@Override
	public void onStartVacancyPageLoad(String url) {
		this.searchButton.setEnabled(false);
		setCityRadioButtonsEnabled(false);
	}

	@Override
	public void onSearchFinish(int loaded, int total) {
		this.searchButton.setEnabled(true);
		setCityRadioButtonsEnabled(true);
	}
	
	/*private void performQueryJson(String text) {
		String queryUrl = "https://api.hh.ru/vacancies?text=" + text + "&area=" + App.INSTANCE.getSelectedCityCode();
		System.out.print("Requesting query [" + queryUrl + "]... ");
		try (InputStream stream = new URL(queryUrl).openStream();
				BufferedReader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
				StringWriter sw = new StringWriter()) {
			r.lines().forEach(sw::write);
			JSONObject jsonObject = new JSONObject(sw.toString());
			int found = jsonObject.getInt("found");
			System.out.println("  found: " + found);
			JSONArray jsonArray = jsonObject.getJSONArray("items");
			App.INSTANCE.clearVacancies();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				Vacancy vacancy = Vacancy.fromJson(obj);
				App.INSTANCE.addVacancy(vacancy);
				System.out.println("    " + vacancy.toString());
			}
					
		} catch (Exception e) {
			System.out.println("An exception has occured");
			e.printStackTrace();
		}
	}*/
	
	/*private void performQuery(String text) {
		String queryUrl = "https://api.hh.ru/vacancies?text=" + text + "&area=" + App.INSTANCE.getSelectedCityCode();
		System.out.print("Requesting query [" + queryUrl + "]... ");
		try (InputStream stream = new URL(queryUrl).openStream();
				BufferedReader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
				PrintWriter pw = new PrintWriter(new FileOutputStream("vacancies.json"))) {
			
			r.lines().forEach(pw::println);
			System.out.println("Request successfully completed");
		} catch (Exception e) {
			System.out.println("An exception has occured");
			System.out.println(e);
		}
	}*/

}
