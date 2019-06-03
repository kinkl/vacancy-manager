package me.anonymoussoftware.vacancymanager.model;

import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Employer {

    private final int id;
    private final String name;

    public static Employer fromJson(JSONObject json) {
        return new Employer(json.optInt("id"), json.optString("name"));
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.put("id", this.id);
        result.put("name", this.name);
        return result;
    }

}
