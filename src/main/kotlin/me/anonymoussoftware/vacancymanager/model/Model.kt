package me.anonymoussoftware.vacancymanager.model

interface Model

interface ModelWithNameAndId : Model {
    val id: Int
    val name: String
}
