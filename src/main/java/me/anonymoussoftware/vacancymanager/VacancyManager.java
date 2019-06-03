package me.anonymoussoftware.vacancymanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.anonymoussoftware.vacancymanager.api.result.VacancyListResult;
import me.anonymoussoftware.vacancymanager.api.service.FileVacancyService;
import me.anonymoussoftware.vacancymanager.api.service.HttpVacancyService;
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

    private int selectedCityCode;

    private Vacancy selectedVacancy;

    private VacancyListResult vacancies = new VacancyListResult(0, new ArrayList<>());

    private final Map<String, Integer> CITIES = new LinkedHashMap<>();

    {
        CITIES.put("Saint Petersburg", 2);
        CITIES.put("Moscow", 1);
    }

    public void addVacancyChangeListener(VacancyListChangeListener listener) {
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

    public int getSelectedCityCode() {
        return this.selectedCityCode;
    }

    private void loadVacancies(VacancyListResult vacancyListResult) {
        this.vacancies = vacancyListResult;
        fireVacancyListChanged(VacancyListChangeListener.VacancyListChangeReason.TOTAL_RELOAD);
    }

    private void fireVacancyListChanged(VacancyListChangeListener.VacancyListChangeReason reason) {
        this.vacancyChangeListeners.stream().forEach(listener -> listener.onVacancyListChange(reason));
    }

    public void startVacanciesSearch(String searchText) {
        this.threadPool.submit(() -> {
            List<Vacancy> vacancies = new ArrayList<>();
            VacancyListResult result = null;
            int currentPage = 0;
            int loaded = 0;
            int total = 0;
            int vacanciesToLoad = 0;
            fireVacancySearchProgress(0);
            do {
                String url = this.httpVacancyService.getRequestVacancyUrl(searchText, getSelectedCityCode(),
                        currentPage);
                fireVacancyRequestPageStart(url);
                result = this.httpVacancyService.requestVacancies(url);
                vacancies.addAll(result.getVacancies());
                total = result.getTotal();
                vacanciesToLoad = Math.min(total, VACANCY_COUNT_THRESHOLD);
                loaded += result.getVacancies().size();
                currentPage++;
                fireVacancySearchProgress((int) ((loaded + 0.0) / vacanciesToLoad * 100));
            } while (loaded < vacanciesToLoad);
            result = new VacancyListResult(total, vacancies);
            loadVacancies(result);
            fireVacancySearchProgress(100);
            fireVacancySearchFinish(loaded, total);
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

    public VacancyListResult getAvailableVacancies() {
        List<Vacancy> newVacancyList = this.vacancies.getVacancies().stream() //
                .sorted(Comparator.comparing(Vacancy::isBanned) //
                        .thenComparing(Vacancy::getName).thenComparing(v -> v.getEmployer().getName()))
                .collect(Collectors.toList());
        return new VacancyListResult(this.vacancies.getTotal(), newVacancyList);
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

    public void loadVacanciesFromFile(File file, Runnable failAction) {
        VacancyListResult vacanciesFromFile = this.fileVacancyService.openVacancies(file);
        if (vacanciesFromFile != null) {
            loadVacancies(vacanciesFromFile);
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

    public static interface VacancySelectionListener {
        void onVacancySelection();
    }

    @Override
    public void destroy() throws Exception {
        this.threadPool.shutdown();
    }
}
