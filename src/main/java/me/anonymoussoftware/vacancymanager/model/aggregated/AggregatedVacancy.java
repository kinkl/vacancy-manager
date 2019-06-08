package me.anonymoussoftware.vacancymanager.model.aggregated;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.anonymoussoftware.vacancymanager.model.Vacancy;

@Data
@AllArgsConstructor
public class AggregatedVacancy {

    private final Vacancy vacancy;
    private final List<Vacancy> employerVacancies;

}
