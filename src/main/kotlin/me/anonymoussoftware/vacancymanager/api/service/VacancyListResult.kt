package me.anonymoussoftware.vacancymanager.api.service

import me.anonymoussoftware.vacancymanager.model.Vacancy

class VacancyListResult (val total : Int, val vacancies : List<Vacancy>)