package me.anonymoussoftware.vacancymanager.model

import org.json.JSONObject

class Vacancy(

    val id: Int = 0,
    var isBanned: Boolean = false,
    val name: String? = null,
    val employer: Employer? = null,
    val area: Area? = null,
    val snippet: Snippet? = null,
    val salary: Salary? = null,
    val url: String? = null,
    var description: String? = null
){

    override fun toString(): String {
        return this.name + " (" + this.employer!!.name + ")"
    }

    fun toJson(writeEmployerBannedStatus: Boolean): JSONObject {
        val result = JSONObject()
        result.put("id", this.id)
        result.put("isBanned", this.isBanned)
        result.put("name", this.name)
        if (this.employer != null) {
            result.put("employer", this.employer.toJson(writeEmployerBannedStatus))
        }
        if (this.area != null) {
            result.put("area", this.area.toJson())
        }
        if (this.snippet != null) {
            result.put("snippet", this.snippet.toJson())
        }
        if (this.salary != null) {
            result.put("salary", this.salary.toJson())
        }
        if (this.url != null && !this.url.isEmpty()) {
            result.put("alternate_url", this.url)
        }
        if (this.description != null && !this.description!!.isEmpty()) {
            result.put("description", this.description)
        }
        return result
    }

    companion object {

        fun fromJson(json: JSONObject): Vacancy {
            return Vacancy(
                json.getInt("id"), //
                json.optBoolean("isBanned"), //
                json.getString("name"), //
                Employer.fromJson(json.getJSONObject("employer")), //
                Area.fromJson(json.getJSONObject("area")), //
                Snippet.fromJson(json.optJSONObject("snippet")), //
                Salary.fromJson(json.optJSONObject("salary")), //
                json.optString("alternate_url"), //
                json.optString("description")
            )
        }
    }
}
