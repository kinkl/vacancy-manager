package me.anonymoussoftware.vacancymanager.api.service

import me.anonymoussoftware.vacancymanager.model.Vacancy
import org.apache.log4j.Logger
import org.json.JSONObject
import org.springframework.stereotype.Service
import java.io.*
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

@Service
class HttpVacancyService {

    fun getRequestVacancyUrl(text: String, selectedCityCode: Int, page: Int, bannedEmployers: Set<Int>): String {
        var text = text
        text = text.trim { it <= ' ' } //
            .replace(" ", "%20") //
            .replace("\t", "%20") //
            .replace("\r", "%20") //
            .replace("\n", "%20")
        val sb = StringBuilder(text)
        val bannedEmployerListQueryPart = bannedEmployers.stream() //
            .map { i -> "NOT%20!COMPANY_ID:" + i!! } //
            .collect(Collectors.joining("%20and%20"))
        if (!bannedEmployerListQueryPart.isEmpty()) {
            sb.append("%20and%20") //
                .append(bannedEmployerListQueryPart)
        }
        return String.format("%s?text=%s&area=%d&page=%d&per_page=100", HOST, sb.toString(), selectedCityCode, page)
    }

    fun getRequestVacancyDescriptionUrl(vacancyId: Int): String {
        return String.format("%s/%d", HOST, vacancyId)
    }

    fun requestVacancies(queryUrl: String): VacancyListResult? {
        log.info("Requesting query [$queryUrl]")
        try {
            URL(queryUrl).openStream().use { stream ->
                BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                    StringWriter().use { stringWriter ->
                        reader.lines().forEach { stringWriter.write(it) }
                        val response = stringWriter.toString()
                        PrintWriter(FileOutputStream("last_response.json")).use { pw -> pw.append(response) }
                        val jsonObject = JSONObject(response)
                        val jsonArray = jsonObject.getJSONArray("items")
                        val result = ArrayList<Vacancy>()
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            val vacancy = Vacancy.fromJson(obj)
                            result.add(vacancy)
                        }
                        return VacancyListResult(jsonObject.getInt("found"), result)

                    }
                }
            }
        } catch (e: Exception) {
            log.fatal("An exception has occured", e)
            return null
        }

    }

    fun requestVacancyDescription(queryUrl: String): String? {
        log.info("Requesting query [$queryUrl]")
        try {
            URL(queryUrl).openStream().use { stream ->
                BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                    StringWriter().use { stringWriter ->
                        reader.lines().forEach { stringWriter.write(it) }
                        val response = stringWriter.toString()
                        PrintWriter(FileOutputStream("last_response.json")).use { pw -> pw.append(response) }
                        val jsonObject = JSONObject(response)
                        return jsonObject.getString("description")

                    }
                }
            }
        } catch (e: Exception) {
            log.fatal("An exception has occured", e)
            return null
        }

    }

    companion object {

        private val log = Logger.getLogger(HttpVacancyService::class.java)

        private val HOST = "https://api.hh.ru/vacancies"
    }

}
