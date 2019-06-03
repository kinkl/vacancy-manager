package me.anonymoussoftware.vacancymanager.api.result;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.anonymoussoftware.vacancymanager.model.Vacancy;

@Getter
@AllArgsConstructor
public class VacancyListResult {

    private final int total;
    private final List<Vacancy> vacancies;

}
