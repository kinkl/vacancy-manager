package me.anonymoussoftware.vacancymanager.model

import org.json.JSONObject

class Employer (val id: Int = 0,
                var isBanned: Boolean,
                val name: String? = null,
                val url: String? = null) {

    fun toJson(writeBannedStatus: Boolean): JSONObject {
        val result = JSONObject()
        result.put("id", this.id)
        if (writeBannedStatus) {
            result.put("isBanned", this.isBanned)
        }
        result.put("name", this.name)
        if (this.url != null && this.url.isNotEmpty()) {
            result.put("alternate_url", this.url)
        }
        return result
    }

    companion object {

        fun fromJson(json: JSONObject): Employer {
            return Employer(
                json.optInt("id"), //
                json.optBoolean("isBanned"), //
                json.getString("name"), //
                json.optString("alternate_url")
            )
        }
    }

}
