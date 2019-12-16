package me.anonymoussoftware.vacancymanager.model

import org.json.JSONObject

interface JsonDeserializer<T: Model> {
    fun fromJson(json: JSONObject): T
}