package me.anonymoussoftware.vacancymanager.model;

import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Vacancy {

    private final int id;
    private boolean isBanned;
    private final String name;
    private final Employer employer;
    private final Area area;
    private final Snippet snippet;
    private final Salary salary;
    private final String url;
    private String description;

    public static Vacancy fromJson(JSONObject json) {
        return new Vacancy(json.getInt("id"), //
                json.optBoolean("isBanned"), //
                json.getString("name"), //
                Employer.fromJson(json.getJSONObject("employer")), //
                Area.fromJson(json.getJSONObject("area")), //
                Snippet.fromJson(json.optJSONObject("snippet")), //
                Salary.fromJson(json.optJSONObject("salary")), //
                json.optString("alternate_url"), //
                json.optString("description"));
    }

    @Override
    public String toString() {
        return this.name + " (" + this.employer.getName() + ")";
    }

    public JSONObject toJson(boolean writeEmployerBannedStatus) {
        JSONObject result = new JSONObject();
        result.put("id", this.id);
        result.put("isBanned", this.isBanned);
        result.put("name", this.name);
        if (this.employer != null) {
            result.put("employer", this.employer.toJson(writeEmployerBannedStatus));
        }
        if (this.area != null) {
            result.put("area", this.area.toJson());
        }
        if (this.snippet != null) {
            result.put("snippet", this.snippet.toJson());
        }
        if (this.salary != null) {
            result.put("salary", this.salary.toJson());
        }
        if (this.url != null && !this.url.isEmpty()) {
            result.put("alternate_url", this.url);
        }
        if (this.description != null && !this.description.isEmpty()) {
            result.put("description", this.description);
        }
        return result;
    }
}
