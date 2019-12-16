package me.anonymoussoftware.vacancymanager.model

import org.json.JSONObject

class Area (override val id: Int,
            override val name: String) : ModelWithNameAndId {

    fun toJson(): JSONObject {
        val result = JSONObject()
        result.put("id", this.id)
        result.put("name", this.name)
        return result
    }

    companion object : JsonDeserializer<Area> {
        override fun fromJson(json: JSONObject) : Area =
            Area(json.getInt("id"), json.getString("name"))
    }
}
