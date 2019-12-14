package me.anonymoussoftware.vacancymanager.model

import org.json.JSONObject

class Area (val id: Int = 0,
    val name: String? = null) {

    fun toJson(): JSONObject {
        val result = JSONObject()
        result.put("id", this.id)
        result.put("name", this.name)
        return result
    }

    companion object {

        fun fromJson(json: JSONObject): Area {
            return Area(json.getInt("id"), json.getString("name"))
        }
    }
}
