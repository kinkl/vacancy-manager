package me.anonymoussoftware.vacancymanager.ui.panels

import me.anonymoussoftware.vacancymanager.App
import me.anonymoussoftware.vacancymanager.VacancyManager
import javax.swing.*

class SearchFormPanel : JPanel(), VacancyManager.VacancySearchListener {

    private val queryTextField: JTextField

    private val searchButton: JButton

    private val cityGroup: ButtonGroup

    private val vacancyManager = App.getBean(VacancyManager::class.java)

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        this.queryTextField = JTextField("Java")
        add(queryTextField)

        this.searchButton = JButton("Run new search")
        this.searchButton.addActionListener { this.onSearchButtonAction() }
        add(this.searchButton)

        this.cityGroup = ButtonGroup()
        for (city in this.vacancyManager.availableCityNames) {
            val radioButton = JRadioButton(city)
            radioButton.addActionListener { _ -> this.vacancyManager.selectCity(radioButton.text) }
            this.cityGroup.add(radioButton)
            add(radioButton)
        }
        this.cityGroup.elements.nextElement().doClick()
    }

    override fun addNotify() {
        super.addNotify()
        this.vacancyManager.addVacancySearchListener(this)
    }

    override fun removeNotify() {
        this.vacancyManager.removeVacancySearchListener(this)
        super.removeNotify()
    }

    private fun onSearchButtonAction() {
        this.vacancyManager.startVacanciesSearch(this.queryTextField.text)
    }

    private fun setCityRadioButtonsEnabled(enabled: Boolean) {
        val buttons = this.cityGroup.elements
        while (buttons.hasMoreElements()) {
            buttons.nextElement().isEnabled = enabled
        }
    }

    override fun onVacancyPageSuccessfulLoad(percentage: Int) {
        this.searchButton.isEnabled = false
        setCityRadioButtonsEnabled(false)
    }

    override fun onStartVacancyPageLoad(url: String) {
        this.searchButton.isEnabled = false
        setCityRadioButtonsEnabled(false)
    }

    override fun onSearchFinish(loaded: Int, total: Int) {
        this.searchButton.isEnabled = true
        setCityRadioButtonsEnabled(true)
    }

    /*
     * private void performQueryJson(String text) { String queryUrl =
     * "https://api.hh.ru/vacancies?text=" + text + "&area=" +
     * App.INSTANCE.getSelectedCityCode(); System.out.print("Requesting query [" +
     * queryUrl + "]... "); try (InputStream stream = new
     * URL(queryUrl).openStream(); BufferedReader r = new BufferedReader(new
     * InputStreamReader(stream, StandardCharsets.UTF_8)); StringWriter sw = new
     * StringWriter()) { r.lines().forEach(sw::write); JSONObject jsonObject = new
     * JSONObject(sw.toString()); int found = jsonObject.getInt("found");
     * System.out.println("  found: " + found); JSONArray jsonArray =
     * jsonObject.getJSONArray("items"); App.INSTANCE.clearVacancies(); for (int i =
     * 0; i < jsonArray.length(); i++) { JSONObject obj =
     * jsonArray.getJSONObject(i); Vacancy vacancy = Vacancy.fromJson(obj);
     * App.INSTANCE.addVacancy(vacancy); System.out.println("    " +
     * vacancy.toString()); }
     * 
     * } catch (Exception e) { System.out.println("An exception has occured");
     * e.printStackTrace(); } }
     */

    /*
     * private void performQuery(String text) { String queryUrl =
     * "https://api.hh.ru/vacancies?text=" + text + "&area=" +
     * App.INSTANCE.getSelectedCityCode(); System.out.print("Requesting query [" +
     * queryUrl + "]... "); try (InputStream stream = new
     * URL(queryUrl).openStream(); BufferedReader r = new BufferedReader(new
     * InputStreamReader(stream, StandardCharsets.UTF_8)); PrintWriter pw = new
     * PrintWriter(new FileOutputStream("vacancies.json"))) {
     * 
     * r.lines().forEach(pw::println);
     * System.out.println("Request successfully completed"); } catch (Exception e) {
     * System.out.println("An exception has occured"); System.out.println(e); } }
     */

}
