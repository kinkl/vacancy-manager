package me.anonymoussoftware.vacancymanager.model

import org.json.JSONObject

class Salary (val from: Int,
              val to: Int,
              val currency: String,
              val isGross: Boolean) : Model {
    fun toJson(): JSONObject {
        val result = JSONObject()
        if (this.from != 0) {
            result.put("from", this.from)
        }
        if (this.to != 0) {
            result.put("to", this.to)
        }
        if (this.currency.isNotBlank()) {
            result.put("currency", this.currency)
        }
        result.put("gross", this.isGross)
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder()
        if (this.from != 0) {
            sb.append("from ")
            sb.append(this.from)
            sb.append(" ")
        }
        if (this.to != 0) {
            sb.append("to ")
            sb.append(this.to)
            sb.append(" ")
        }
        if (this.currency.isNotBlank()) {
            sb.append(this.currency)
            sb.append(" ")
        }
        if (!this.isGross) {
            sb.append("(net)")
        }
        return sb.toString().trim { it <= ' ' }
    }

    companion object : JsonDeserializer<Salary> {
        override fun fromJson(json: JSONObject): Salary =
            Salary(json.optInt("from"),
                json.optInt("to"),
                json.optString("currency"),
                json.optBoolean("gross"))
    }

}
