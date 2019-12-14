package me.anonymoussoftware.vacancymanager

import me.anonymoussoftware.vacancymanager.api.service.FileVacancyService
import me.anonymoussoftware.vacancymanager.api.service.HttpVacancyService
import me.anonymoussoftware.vacancymanager.model.Employer
import me.anonymoussoftware.vacancymanager.model.Vacancy
import me.anonymoussoftware.vacancymanager.model.aggregated.AggregatedVacancy
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Collectors

@Service
class VacancyManager : DisposableBean {

    @Autowired
    private val fileVacancyService: FileVacancyService? = null

    @Autowired
    private val httpVacancyService: HttpVacancyService? = null

    private val threadPool = Executors.newSingleThreadExecutor()

    private val vacancyChangeListeners = CopyOnWriteArrayList<VacancyListChangeListener>()

    private val vacancySelectionListeners = CopyOnWriteArrayList<VacancySelectionListener>()

    private val vacancySearchListeners = CopyOnWriteArrayList<VacancySearchListener>()

    private val employerListChangeListeners = CopyOnWriteArrayList<EmployerListChangeListener>()

    private val vacancyDescriptionRequestListeners = CopyOnWriteArrayList<VacancyDescriptionRequestListener>()

    var selectedCityCode: Int = 0
        private set

    var selectedVacancy: AggregatedVacancy? = null
        set(vacancy) {
            field = vacancy
            this.vacancySelectionListeners.stream().forEach { e -> e.onVacancySelection() }
        }

    private var vacancies: List<Vacancy> = ArrayList()

    private val employers = TreeMap<Int, Employer>()

    private val CITIES = LinkedHashMap<String, Int>()

    val availableCityNames: Set<String>
        get() = Collections.unmodifiableSet(CITIES.keys)

    //
    //
    //
    //
    //
    val availableVacancies: List<AggregatedVacancy>
        get() {
            val employerToVacancies = this.vacancies.stream()
                .collect(Collectors.groupingBy { v: Vacancy -> v.employer!!.id })
            return this.vacancies.stream()
                .map { v -> AggregatedVacancy(v, employerToVacancies[v.employer!!.id]) }
                .sorted(Comparator.comparing { v: AggregatedVacancy -> v.vacancy!!.isBanned }
                    .thenComparing { v -> v.vacancy!!.employer!!.isBanned }
                    .thenComparing { v -> -v.employerVacancies!!.size }
                    .thenComparing { v -> v.vacancy!!.employer!!.name!! }
                    .thenComparing { v -> v.vacancy!!.name!! })
                .collect(Collectors.toList())
        }

    init {
        CITIES["Saint Petersburg"] = 2
        CITIES["Moscow"] = 1
        CITIES["Sochi"] = 237
        CITIES["Krasnodar"] = 53
        CITIES["Chelyabinsk"] = 104
    }

    fun addVacancyListChangeListener(listener: VacancyListChangeListener) {
        this.vacancyChangeListeners.add(listener)
    }

    fun removeVacancyListChangeListener(listener: VacancyListChangeListener) {
        this.vacancyChangeListeners.remove(listener)
    }

    fun addVacancySelectionListener(listener: VacancySelectionListener) {
        this.vacancySelectionListeners.add(listener)
    }

    fun removeVacancySelectionListener(listener: VacancySelectionListener) {
        this.vacancySelectionListeners.remove(listener)
    }

    fun addVacancySearchListener(listener: VacancySearchListener) {
        this.vacancySearchListeners.add(listener)
    }

    fun removeVacancySearchListener(listener: VacancySearchListener) {
        this.vacancySearchListeners.remove(listener)
    }

    fun addEmployerListChangeListener(listener: EmployerListChangeListener) {
        this.employerListChangeListeners.add(listener)
    }

    fun removeEmployerListChangeListener(listener: EmployerListChangeListener) {
        this.employerListChangeListeners.remove(listener)
    }

    fun addVacancyDescriptionRequestListener(listener: VacancyDescriptionRequestListener) {
        this.vacancyDescriptionRequestListeners.add(listener)
    }

    fun removeVacancyDescriptionRequestListener(listener: VacancyDescriptionRequestListener) {
        this.vacancyDescriptionRequestListeners.remove(listener)
    }

    fun selectCity(cityName: String) {
        val selectedCityTmp = CITIES[cityName] ?: throw IllegalStateException("Unknown city \"$cityName\"")
        this.selectedCityCode = selectedCityTmp
    }

    fun banSelectedVacancy() {
        this.selectedVacancy!!.vacancy!!.isBanned = true
        fireVacancyListChanged(VacancyListChangeListener.VacancyListChangeReason.VACANCY_BAN)
    }

    fun banSelectedVacancyEmployer() {
        val employerToBan = this.selectedVacancy!!.vacancy!!.employer
        this.employers[employerToBan!!.id]?.isBanned = true
        fireVacancyListChanged(VacancyListChangeListener.VacancyListChangeReason.VACANCY_BAN)
        fireEmployerListChanged()
    }

    fun requestSelectedVacancyDescription() {
        if (this.selectedVacancy == null) {
            return
        }
        this.threadPool.submit {
            val vacancy = this.selectedVacancy!!.vacancy
            val id = vacancy!!.id
            val url = this.httpVacancyService!!.getRequestVacancyDescriptionUrl(id)
            fireVacancyDescriptionRequestStart(url)
            val description = this.httpVacancyService.requestVacancyDescription(url)
            vacancy.description = description
            fireVacancyDescriptionRequestFinish()
        }
    }

    private fun loadVacancies(vacancies: List<Vacancy>) {
        this.vacancies = vacancies
        fireVacancyListChanged(VacancyListChangeListener.VacancyListChangeReason.TOTAL_RELOAD)
        fireEmployerListChanged()
    }

    private fun fireVacancyListChanged(reason: VacancyListChangeListener.VacancyListChangeReason) {
        this.vacancyChangeListeners.stream().forEach { listener -> listener.onVacancyListChange(reason) }
    }

    private fun fireVacancyDescriptionRequestStart(url: String) {
        this.vacancyDescriptionRequestListeners.stream()
            .forEach { listener -> listener.onVacancyDescriptionRequestStart(url) }
    }

    private fun fireVacancyDescriptionRequestFinish() {
        this.vacancyDescriptionRequestListeners.stream()
            .forEach { listener -> listener.onVacancyDescriptionRequestFinish() }
    }

    private fun fireEmployerListChanged() {
        this.employerListChangeListeners.stream()
            .forEach(Consumer<EmployerListChangeListener> { it.onEmployerListChange() })
    }

    fun startVacanciesSearch(searchText: String) {
        this.threadPool.submit {
            val vacancies = ArrayList<Vacancy>()
            var currentPage = 0
            var total = 0
            var vacanciesToLoad = 0
            fireVacancySearchProgress(0)
            val bannedEmployers = this.employers.values.stream() //
                .filter(Predicate<Employer> { it.isBanned }) //
                .map { it.id } //
                .collect(Collectors.toSet())
            do {
                val url = this.httpVacancyService!!.getRequestVacancyUrl(
                    searchText, selectedCityCode,
                    currentPage, bannedEmployers
                )
                fireVacancyRequestPageStart(url)
                val result = this.httpVacancyService.requestVacancies(url)
                vacancies.addAll(cacheVacancies(result!!.vacancies))
                total = result.total
                vacanciesToLoad = Math.min(total, VACANCY_COUNT_THRESHOLD)
                currentPage++
                fireVacancySearchProgress(((vacancies.size + 0.0) / vacanciesToLoad * 100).toInt())
            } while (vacancies.size < vacanciesToLoad)
            loadVacancies(vacancies)
            fireVacancySearchProgress(100)
            fireVacancySearchFinish(vacancies.size, total)
        }
    }

    private fun fireVacancySearchFinish(loaded: Int, total: Int) {
        for (vacancySearchListener in this.vacancySearchListeners) {
            vacancySearchListener.onSearchFinish(loaded, total)
        }
    }

    private fun fireVacancySearchProgress(percentage: Int) {
        for (vacancySearchListener in this.vacancySearchListeners) {
            vacancySearchListener.onVacancyPageSuccessfulLoad(percentage)
        }
    }

    private fun fireVacancyRequestPageStart(url: String) {
        for (vacancySearchListener in this.vacancySearchListeners) {
            vacancySearchListener.onStartVacancyPageLoad(url)
        }
    }

    fun getEmployers(): List<Employer> {
        return this.employers.values.stream() //
            .sorted(Comparator.comparing { e: Employer -> !e.isBanned } //
                .thenComparing { e: Employer -> e.name!! })
            .collect(Collectors.toList())
    }

    fun importVacancies(file: File, successAction: Runnable, failAction: Runnable) {
        val success = this.fileVacancyService!!.importVacancies(this.vacancies, file)
        if (success) {
            successAction.run()
        } else {
            failAction.run()
        }
    }

    private fun cacheVacancies(rawVacancies: List<Vacancy>): List<Vacancy> {
        val newVacancies = ArrayList<Vacancy>()
        for (rawVacancy in rawVacancies) {
            val rawEmployer = rawVacancy.employer
            var cachedEmployer: Employer? = this.employers[rawEmployer!!.id]
            if (cachedEmployer == null) {
                this.employers[rawEmployer!!.id] = rawEmployer
                cachedEmployer = rawEmployer
            }
            newVacancies.add(
                Vacancy(
                    rawVacancy.id, //
                    rawVacancy.isBanned, //
                    rawVacancy.name, //
                    cachedEmployer, //
                    rawVacancy.area, //
                    rawVacancy.snippet, //
                    rawVacancy.salary, //
                    rawVacancy.url, //
                    rawVacancy.description
                )
            )
        }
        return newVacancies
    }

    private fun updateEmployersCache(bannedEmployers: List<Employer>) {
        for (bannedEmployer in bannedEmployers) {
            val cachedEmployer = this.employers[bannedEmployer.id]
            if (cachedEmployer == null) {
                this.employers[bannedEmployer.id] = bannedEmployer
            } else {
                cachedEmployer.isBanned = true
            }
        }
        fireVacancyListChanged(VacancyListChangeListener.VacancyListChangeReason.TOTAL_RELOAD)
        fireEmployerListChanged()
    }

    fun exportVacancies(file: File, failAction: Runnable) {
        val nonCachedVacancies = this.fileVacancyService!!.exportVacancies(file)
        if (nonCachedVacancies != null) {
            val cachedResult = cacheVacancies(nonCachedVacancies)
            loadVacancies(cachedResult)
        } else {
            failAction.run()
        }
    }

    fun importEmployers(file: File, successAction: Runnable, failAction: Runnable) {
        val success = this.fileVacancyService!!.importBannedEmployers(this.employers.values, file)
        if (success) {
            successAction.run()
        } else {
            failAction.run()
        }
    }

    fun exportEmployers(file: File, failAction: Runnable) {
        val nonCachedVacancies = this.fileVacancyService!!.exportBannedEmployers(file)
        if (nonCachedVacancies != null) {
            updateEmployersCache(nonCachedVacancies)
        } else {
            failAction.run()
        }
    }

    interface VacancySearchListener {
        fun onVacancyPageSuccessfulLoad(percentage: Int)

        fun onStartVacancyPageLoad(url: String)

        fun onSearchFinish(loaded: Int, total: Int)
    }

    interface VacancyListChangeListener {
        enum class VacancyListChangeReason {
            TOTAL_RELOAD, VACANCY_BAN
        }

        fun onVacancyListChange(reason: VacancyListChangeReason)
    }

    interface EmployerListChangeListener {
        fun onEmployerListChange()
    }

    interface VacancySelectionListener {
        fun onVacancySelection()
    }

    interface VacancyDescriptionRequestListener {
        fun onVacancyDescriptionRequestStart(url: String)

        fun onVacancyDescriptionRequestFinish()
    }

    @Throws(Exception::class)
    override fun destroy() {
        this.threadPool.shutdown()
    }

    companion object {

        private val VACANCY_COUNT_THRESHOLD = 2000
    }
}
