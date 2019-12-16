package me.anonymoussoftware.vacancymanager.model

import org.json.JSONObject

class Vacancy(override val id: Int,
              override val name: String,
              var isBanned: Boolean,
              val employer: Employer,
              val area: Area,
              val snippet: Snippet?,
              val salary: Salary?,
              val url: String,
              var description: String?) : ModelWithNameAndId {

    override fun toString(): String {
        return this.name + " (" + this.employer.name + ")"
    }

    fun toJson(writeEmployerBannedStatus: Boolean): JSONObject {
        val result = JSONObject()
        result.put("id", this.id)
        result.put("isBanned", this.isBanned)
        result.put("name", this.name)
        result.put("employer", this.employer.toJson(writeEmployerBannedStatus))
        result.put("area", this.area.toJson())
        if (this.snippet != null) {
            result.put("snippet", this.snippet.toJson())
        }
        if (this.salary != null) {
            result.put("salary", this.salary.toJson())
        }
        if (this.url.isNotEmpty()) {
            result.put("alternate_url", this.url)
        }
        if (this.description != null && this.description!!.isNotEmpty()) {
            result.put("description", this.description)
        }
        return result
    }

    companion object : JsonDeserializer<Vacancy> {

        override fun fromJson(json: JSONObject): Vacancy {
            val salary = json.optJSONObject("salary")
            val snippet = json.optJSONObject("snippet")
            return Vacancy(
                json.getInt("id"), //
                json.getString("name"), //
                json.optBoolean("isBanned"), //
                Employer.fromJson(json.getJSONObject("employer")), //
                Area.fromJson(json.getJSONObject("area")), //
                if (snippet != null) Snippet.fromJson(snippet) else null, //
                if (salary != null) Salary.fromJson(salary) else null, //
                json.optString("alternate_url"), //
                json.optString("description")
            )
        }
    }
}
