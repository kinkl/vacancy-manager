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
    private final String url;

    public static Employer fromJson(JSONObject json) {
        return new Employer(json.optInt("id"), //
                json.optBoolean("isBanned"), //
                json.getString("name"), //
                json.optString("alternate_url"));
    }

    public JSONObject toJson(boolean writeBannedStatus) {
        JSONObject result = new JSONObject();
        result.put("id", this.id);
        if (writeBannedStatus) {
            result.put("isBanned", this.isBanned);
        }
        result.put("name", this.name);
        if (this.url != null && !this.url.isEmpty()) {
            result.put("alternate_url", this.url);
        }
        return result;
    }

}
