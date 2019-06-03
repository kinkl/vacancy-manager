package me.anonymoussoftware.vacancymanager.api.service;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j;
import me.anonymoussoftware.vacancymanager.api.result.VacancyListResult;
import me.anonymoussoftware.vacancymanager.model.Vacancy;

@Log4j
@Service
public class HttpVacancyService {

    private static final String HOST = "https://api.hh.ru/vacancies";

    public String getRequestVacancyUrl(String text, int selectedCityCode, int page) {
        return HOST + "?text=" + text + "&area=" + selectedCityCode + "&page=" + page + "&per_page=100";
    }

    public VacancyListResult requestVacancies(String queryUrl) {
        log.info("Requesting query [" + queryUrl + "]");
        try (InputStream stream = new URL(queryUrl).openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                StringWriter stringWriter = new StringWriter()) {
            reader.lines().forEach(stringWriter::write);
            String response = stringWriter.toString();
            try (PrintWriter pw = new PrintWriter(new FileOutputStream("last_response.json"))) {
                pw.append(response);
            }
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            List<Vacancy> result = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Vacancy vacancy = Vacancy.fromJson(obj);
                result.add(vacancy);
            }
            return new VacancyListResult(result.size(), jsonObject.getInt("found"), result);

        } catch (Exception e) {
            log.fatal("An exception has occured", e);
            return null;
        }
    }

}
