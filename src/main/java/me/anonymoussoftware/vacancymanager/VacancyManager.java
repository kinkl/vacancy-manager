package me.anonymoussoftware.vacancymanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.anonymoussoftware.vacancymanager.api.service.FileVacancyService;
import me.anonymoussoftware.vacancymanager.api.service.HttpVacancyService;
import me.anonymoussoftware.vacancymanager.model.Employer;
import me.anonymoussoftware.vacancymanager.model.Vacancy;

@Service
public class VacancyManager implements DisposableBean {

    private static final int VACANCY_COUNT_THRESHOLD = 2000;

    @Autowired
    private FileVacancyService fileVacancyService;

    @Autowired
    private HttpVacancyService httpVacancyService;

    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    private final List<VacancyListChangeListener> vacancyChangeListeners = new CopyOnWriteArrayList<>();

    private final List<VacancySelectionListener> vacancySelectionListeners = new CopyOnWriteArrayList<>();

    private final List<VacancySearchListener> vacancySearchListeners = new CopyOnWriteArrayList<>();

    private final List<EmployerListChangeListener> employerListChangeListeners = new CopyOnWriteArrayList<>();

    private int selectedCityCode;

    private Vacancy selectedVacancy;

    private List<Vacancy> vacancies = new ArrayList<>();

    private Map<Integer, Employer> employers = new TreeMap<>();

    private final Map<String, Integer> CITIES = new LinkedHashMap<>();

    {
        CITIES.put("Saint Petersburg", 2);
        CITIES.put("Moscow", 1);
    }

    public void addVacancyListChangeListener(VacancyListChangeListener listener) {
        this.vacancyChangeListeners.add(listener);
    }

    public void removeVacancyListChangeListener(VacancyListChangeListener listener) {
        this.vacancyChangeListeners.remove(listener);
    }

    public void addVacancySelectionListener(VacancySelectionListener listener) {
        this.vacancySelectionListeners.add(listener);
    }

    public void removeVacancySelectionListener(VacancySelectionListener listener) {
        this.vacancySelectionListeners.remove(listener);
    }

    public void addVacancySearchListener(VacancySearchListener listener) {
        this.vacancySearchListeners.add(listener);
    }

    public void removeVacancySearchListener(VacancySearchListener listener) {
        this.vacancySearchListeners.remove(listener);
    }

    public void addEmployerListChangeListener(EmployerListChangeListener listener) {
        this.employerListChangeListeners.add(listener);
    }

    public void removeEmployerListChangeListener(EmployerListChangeListener listener) {
        this.employerListChangeListeners.remove(listener);
    }

    public Set<String> getAvailableCityNames() {
        return Collections.unmodifiableSet(CITIES.keySet());
    }

    public void selectCity(String cityName) {
        Integer selectedCityTmp = CITIES.get(cityName);
        if (selectedCityTmp == null) {
            throw new IllegalStateException("Unknown city \"" + cityName + "\"");
        }
        this.selectedCityCode = selectedCityTmp;
    }

    public void banSelectedVacancy() {
        this.selectedVacancy.setBanned(true);
        fireVacancyListChanged(VacancyListChangeListener.VacancyListChangeReason.VACANCY_BAN);
    }

    public void banSelectedVacancyEmployer() {
        Employer employerToBan = this.selectedVacancy.getEmployer();
        this.employers.get(employerToBan.getId()).setBanned(true);
        fireVacancyListChanged(VacancyListChangeListener.VacancyListChangeReason.VACANCY_BAN);
        fireEmployerListChanged();
    }

    public int getSelectedCityCode() {
        return this.selectedCityCode;
    }

    private void loadVacancies(List<Vacancy> vacancies) {
        this.vacancies = vacancies;
        fireVacancyListChanged(VacancyListChangeListener.VacancyListChangeReason.TOTAL_RELOAD);
        fireEmployerListChanged();
    }

    private void fireVacancyListChanged(VacancyListChangeListener.VacancyListChangeReason reason) {
        this.vacancyChangeListeners.stream().forEach(listener -> listener.onVacancyListChange(reason));
    }

    private void fireEmployerListChanged() {
        this.employerListChangeListeners.stream().forEach(EmployerListChangeListener::onEmployerListChange);
    }

    public void startVacanciesSearch(String searchText) {
        this.threadPool.submit(() -> {
            List<Vacancy> vacancies = new ArrayList<>();
            int currentPage = 0;
            int total = 0;
            int vacanciesToLoad = 0;
            fireVacancySearchProgress(0);
            Set<Integer> bannedEmployers = this.employers.values().stream() //
                    .filter(Employer::isBanned) //
                    .map(Employer::getId) //
                    .collect(Collectors.toSet());
            do {
                String url = this.httpVacancyService.getRequestVacancyUrl(searchText, getSelectedCityCode(),
                        currentPage, bannedEmployers);
                fireVacancyRequestPageStart(url);
                HttpVacancyService.VacancyListResult result = this.httpVacancyService.requestVacancies(url);
                vacancies.addAll(cacheEmployers(result.getVacancies()));
                total = result.getTotal();
                vacanciesToLoad = Math.min(total, VACANCY_COUNT_THRESHOLD);
                currentPage++;
                fireVacancySearchProgress((int) ((vacancies.size() + 0.0) / vacanciesToLoad * 100));
            } while (vacancies.size() < vacanciesToLoad);
            loadVacancies(vacancies);
            fireVacancySearchProgress(100);
            fireVacancySearchFinish(vacancies.size(), total);
        });
    }

    private void fireVacancySearchFinish(int loaded, int total) {
        for (VacancySearchListener vacancySearchListener : this.vacancySearchListeners) {
            vacancySearchListener.onSearchFinish(loaded, total);
        }
    }

    private void fireVacancySearchProgress(int percentage) {
        for (VacancySearchListener vacancySearchListener : this.vacancySearchListeners) {
            vacancySearchListener.onVacancyPageSuccessfulLoad(percentage);
        }
    }

    private void fireVacancyRequestPageStart(String url) {
        for (VacancySearchListener vacancySearchListener : this.vacancySearchListeners) {
            vacancySearchListener.onStartVacancyPageLoad(url);
        }
    }

    public List<Vacancy> getAvailableVacancies() {
        return this.vacancies.stream() //
                .sorted(Comparator.comparing(Vacancy::isBanned) //
                        .thenComparing(v -> v.getEmployer().isBanned()) //
                        .thenComparing(Vacancy::getName) //
                        .thenComparing(v -> v.getEmployer().getName()))
                .collect(Collectors.toList());
    }

    public List<Employer> getEmployers() {
        return this.employers.values().stream() //
                .sorted(Comparator.comparing((Employer e) -> !e.isBanned()) //
                        .thenComparing(Employer::getName))
                .collect(Collectors.toList());
    }

    public void setSelectedVacancy(Vacancy vacancy) {
        this.selectedVacancy = vacancy;
        this.vacancySelectionListeners.stream().forEach(e -> e.onVacancySelection());
    }

    public Vacancy getSelectedVacancy() {
        return this.selectedVacancy;
    }

    public void saveVacanciesToFile(File file, Runnable successAction, Runnable failAction) {
        boolean success = this.fileVacancyService.saveVacancies(this.vacancies, file);
        if (success) {
            successAction.run();
        } else {
            failAction.run();
        }
    }

    private List<Vacancy> cacheEmployers(List<Vacancy> rawVacancies) {
        List<Vacancy> newVacancies = new ArrayList<>();
        for (Vacancy rawVacancy : rawVacancies) {
            Employer rawEmployer = rawVacancy.getEmployer();
            Employer cachedEmployer = this.employers.get(rawEmployer.getId());
            if (cachedEmployer == null) {
                this.employers.put(rawEmployer.getId(), rawEmployer);
                cachedEmployer = rawEmployer;
            }
            newVacancies.add(new Vacancy(rawVacancy.getId(), rawVacancy.isBanned(), rawVacancy.getName(),
                    cachedEmployer, rawVacancy.getArea(), rawVacancy.getSnippet()));
        }
        return newVacancies;
    }

    public void loadVacanciesFromFile(File file, Runnable failAction) {
        List<Vacancy> rawResult = this.fileVacancyService.openRawVacancies(file);
        if (rawResult != null) {
            List<Vacancy> result = cacheEmployers(rawResult);
            loadVacancies(result);
        } else {
            failAction.run();
        }
    }

    public static interface VacancySearchListener {
        void onVacancyPageSuccessfulLoad(int percentage);

        void onStartVacancyPageLoad(String url);

        void onSearchFinish(int loaded, int total);
    }

    public static interface VacancyListChangeListener {
        public enum VacancyListChangeReason {
            TOTAL_RELOAD, VACANCY_BAN
        }

        void onVacancyListChange(VacancyListChangeReason reason);
    }

    public static interface EmployerListChangeListener {
        void onEmployerListChange();
    }

    public static interface VacancySelectionListener {
        void onVacancySelection();
    }

    @Override
    public void destroy() throws Exception {
        this.threadPool.shutdown();
    }
}
