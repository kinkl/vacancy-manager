package me.anonymoussoftware.vacancymanager.api.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j;
import me.anonymoussoftware.vacancymanager.model.Vacancy;

@Log4j
@Service
public class FileVacancyService {

    public boolean importVacancies(List<Vacancy> vacancies, File file) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(file))) {
            JSONObject obj = new JSONObject();
            obj.put("items", vacancies.stream() //
                    .map(v -> v.toJson(false)) //
                    .collect(Collectors.toList()));
            pw.write(obj.toString());
        } catch (FileNotFoundException e) {
            log.error("Error saving vacancies", e);
            return false;
        }
        return true;
    }

    public List<Vacancy> exportVacancies(File file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String str = reader.lines().collect(Collectors.joining());
            JSONObject obj = new JSONObject(str);
            JSONArray jsonArray = obj.getJSONArray("items");
            List<Vacancy> vacancies = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                Vacancy vacancy = Vacancy.fromJson(jsonArray.getJSONObject(i));
                vacancies.add(vacancy);
            }
            return vacancies;
        } catch (IOException e) {
            log.error("Error opening vacancies", e);
        }
        return null;
    }

}
