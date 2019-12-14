package me.anonymoussoftware.vacancymanager.model

import org.json.JSONObject

class Salary (

    val from: Int = 0,
    val to: Int = 0,
    val currency: String? = null,
    val isGross: Boolean = false
){
    fun toJson(): JSONObject {
        val result = JSONObject()
        if (this.from != 0) {
            result.put("from", this.from)
        }
        if (this.to != 0) {
            result.put("to", this.to)
        }
        if (this.currency != null) {
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
        if (this.currency != null) {
            sb.append(this.currency)
            sb.append(" ")
        }
        if (!this.isGross) {
            sb.append("(net)")
        }
        return sb.toString().trim { it <= ' ' }
    }

    companion object {

        fun fromJson(json: JSONObject?): Salary? {
            return if (json == null) {
                null
            } else Salary(
                json.optInt("from"),
                json.optInt("to"),
                json.optString("currency"),
                json.optBoolean("gross")
            )
        }
    }

}
