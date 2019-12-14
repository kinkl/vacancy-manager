package me.anonymoussoftware.vacancymanager.model.aggregated

import me.anonymoussoftware.vacancymanager.model.Vacancy

class AggregatedVacancy (val vacancy: Vacancy? = null,
    val employerVacancies: List<Vacancy>? = null)
