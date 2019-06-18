package me.anonymoussoftware.vacancymanager.model;

import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Salary {

    private final int from;
    private final int to;
    private final String currency;
    private final boolean gross;

    public static Salary fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        return new Salary(json.optInt("from"), json.optInt("to"), json.optString("currency"), json.optBoolean("gross"));
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        if (this.from != 0) {
            result.put("from", this.from);
        }
        if (this.to != 0) {
            result.put("to", this.to);
        }
        if (this.currency != null) {
            result.put("currency", this.currency);
        }
        result.put("gross", this.gross);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.from != 0) {
            sb.append("from ");
            sb.append(this.from);
            sb.append(" ");
        }
        if (this.to != 0) {
            sb.append("to ");
            sb.append(this.to);
            sb.append(" ");
        }
        if (this.currency != null) {
            sb.append(this.currency);
            sb.append(" ");
        }
        if (!this.gross) {
            sb.append("(net)");
        }
        return sb.toString().trim();
    }

}
