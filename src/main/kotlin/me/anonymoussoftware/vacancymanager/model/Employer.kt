package me.anonymoussoftware.vacancymanager.model

import org.json.JSONObject

class Employer (override val id: Int,
                override val name: String,
                var isBanned: Boolean,
                val url: String) : ModelWithNameAndId {

    fun toJson(writeBannedStatus: Boolean): JSONObject {
        val result = JSONObject()
        result.put("id", this.id)
        if (writeBannedStatus) {
            result.put("isBanned", this.isBanned)
        }
        result.put("name", this.name)
        if (this.url.isNotEmpty()) {
            result.put("alternate_url", this.url)
        }
        return result
    }

    companion object : JsonDeserializer<Employer> {
        override fun fromJson(json: JSONObject): Employer = Employer(
            json.optInt("id"), //
            json.getString("name"), //
            json.optBoolean("isBanned"), //
            json.optString("alternate_url")
        )
    }

}
