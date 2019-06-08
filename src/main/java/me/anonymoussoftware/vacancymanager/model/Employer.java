package me.anonymoussoftware.vacancymanager.model;

import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Employer {

    private final int id;
    private boolean isBanned;
    private final String name;

    public static Employer fromJson(JSONObject json) {
        return new Employer(json.optInt("id"), json.optBoolean("isBanned"), json.getString("name"));
    }

    public JSONObject toJson(boolean writeBannedStatus) {
        JSONObject result = new JSONObject();
        result.put("id", this.id);
        if (writeBannedStatus) {
            result.put("isBanned", this.isBanned);
        }
        result.put("name", this.name);
        return result;
    }

}
