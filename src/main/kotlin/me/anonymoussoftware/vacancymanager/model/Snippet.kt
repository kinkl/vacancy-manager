package me.anonymoussoftware.vacancymanager.model

import org.json.JSONObject

class Snippet (val requirement: String,
               val responsibility: String) : Model {
    override fun toString(): String {
        val sb = StringBuilder()
        if (this.requirement.isNotBlank()) {
            sb.append("<b>Requirement:</b><br/>$requirement<br/><br/>")
        }
        if (this.responsibility.isNotBlank()) {
            sb.append("<b>Responsibility:</b><br/>$responsibility")
        }
        return sb.toString()
    }

    fun toJson(): JSONObject {
        val result = JSONObject()
        if (this.requirement.isNotBlank()) {
            result.put("requirement", this.requirement)
        }
        if (this.responsibility.isNotBlank()) {
            result.put("responsibility", this.responsibility)
        }
        return result
    }

    companion object : JsonDeserializer<Model> {
        override fun fromJson(json: JSONObject): Snippet =
            Snippet(
                replaceUnknownHtmlTags(json.optString("requirement")),
                replaceUnknownHtmlTags(json.optString("responsibility")))

        private fun replaceUnknownHtmlTags(text: String): String =
            replaceUnknownTag(text, "highlighttext", "u")

        private fun replaceUnknownTag(text: String, tagName: String, newTagName: String) : String =
            text.replace("<$tagName>", "<$newTagName>")
                .replace("</$tagName>", "</$newTagName>")
    }
}
