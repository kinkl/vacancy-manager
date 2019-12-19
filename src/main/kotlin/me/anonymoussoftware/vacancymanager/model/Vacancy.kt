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

    fun toJson(writeEmployerBannedStatus: Boolean) = JSONObject().apply {
        put("id", id)
        put("isBanned", isBanned)
        put("name", name)
        put("employer", employer.toJson(writeEmployerBannedStatus))
        put("area", area.toJson())
        if (snippet != null) {
            put("snippet", snippet.toJson())
        }
        if (salary != null) {
            put("salary", salary.toJson())
        }
        if (url.isNotEmpty()) {
            put("alternate_url", url)
        }
        if (description != null && description!!.isNotEmpty()) {
            put("description", description)
        }
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
