package me.anonymoussoftware.vacancymanager.api.service

import me.anonymoussoftware.vacancymanager.model.Employer
import me.anonymoussoftware.vacancymanager.model.Vacancy
import org.apache.log4j.Logger
import org.json.JSONObject
import org.springframework.stereotype.Service
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

@Service
class FileVacancyService {

    fun importVacancies(vacancies: List<Vacancy>, file: File): Boolean {
        try {
            PrintWriter(FileOutputStream(file)).use { pw ->
                val obj = JSONObject()
                obj.put("items", vacancies.stream() //
                    .map { v -> v.toJson(false) } //
                    .collect(Collectors.toList()) as List<JSONObject>)
                pw.write(obj.toString())
            }
        } catch (e: FileNotFoundException) {
            log.error("Error saving vacancies", e)
            return false
        }

        return true
    }

    fun exportVacancies(file: File): List<Vacancy>? {
        try {
            BufferedReader(
                InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8)
            ).use { reader ->
                val str = reader.lines().collect(Collectors.joining())
                val obj = JSONObject(str)
                val jsonArray = obj.getJSONArray("items")
                val vacancies = ArrayList<Vacancy>()
                for (i in 0 until jsonArray.length()) {
                    val vacancy = Vacancy.fromJson(jsonArray.getJSONObject(i))
                    vacancies.add(vacancy)
                }
                return vacancies
            }
        } catch (e: IOException) {
            log.error("Error opening vacancies", e)
        }

        return null
    }

    fun importBannedEmployers(employers: Collection<Employer>, file: File): Boolean {
        try {
            PrintWriter(FileOutputStream(file)).use { pw ->
                val obj = JSONObject()
                obj.put("banned_employers", employers.stream() //
                    .filter { it.isBanned } //
                    .map { v -> v.toJson(true) } //
                    .collect(Collectors.toList()) as List<JSONObject>)
                pw.write(obj.toString())
            }
        } catch (e: FileNotFoundException) {
            log.error("Error saving employers", e)
            return false
        }

        return true
    }

    fun exportBannedEmployers(file: File): List<Employer>? {
        try {
            BufferedReader(
                InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8)
            ).use { reader ->
                val str = reader.lines().collect(Collectors.joining())
                val obj = JSONObject(str)
                val jsonArray = obj.getJSONArray("banned_employers")
                val employers = ArrayList<Employer>()
                for (i in 0 until jsonArray.length()) {
                    val employer = Employer.fromJson(jsonArray.getJSONObject(i))
                    employers.add(employer)
                }
                return employers
            }
        } catch (e: IOException) {
            log.error("Error opening employers", e)
        }

        return null
    }

    companion object {

        private val log = Logger.getLogger(FileVacancyService::class.java)
    }

}
