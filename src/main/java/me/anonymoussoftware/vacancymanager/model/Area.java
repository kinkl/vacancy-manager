package me.anonymoussoftware.vacancymanager.model;

import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Area {

    private final int id;
    private final String name;

    public static Area fromJson(JSONObject json) {
        return new Area(json.getInt("id"), json.getString("name"));
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.put("id", this.id);
        result.put("name", this.name);
        return result;
    }
}
