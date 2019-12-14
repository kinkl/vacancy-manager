package me.anonymoussoftware.vacancymanager.model

import org.json.JSONObject

class Snippet(

    val requirement: String? = null,
    val responsibility: String? = null
){
    override fun toString(): String {
        val sb = StringBuilder()
        if (this.requirement != null && !this.requirement.trim { it <= ' ' }.isEmpty()) {
            sb.append("<b>Requirement:</b>")
            sb.append("<br/>")
            sb.append(this.requirement)
            sb.append("<br/>")
            sb.append("<br/>")
        }
        if (this.responsibility != null && !this.responsibility.trim { it <= ' ' }.isEmpty()) {
            sb.append("<b>Responsibility:</b>")
            sb.append("<br/>")
            sb.append(this.responsibility)
        }
        return sb.toString()
    }

    fun toJson(): JSONObject {
        val result = JSONObject()
        if (this.requirement != null && !this.requirement.trim { it <= ' ' }.isEmpty()) {
            result.put("requirement", this.requirement)
        }
        if (this.responsibility != null && !this.responsibility.trim { it <= ' ' }.isEmpty()) {
            result.put("responsibility", this.responsibility)
        }
        return result
    }

    companion object {

        fun fromJson(json: JSONObject?): Snippet {
            return if (json == null) {
                Snippet("", "")
            } else Snippet(
                replaceUnknownHtmlTags(json.optString("requirement")),
                replaceUnknownHtmlTags(json.optString("responsibility"))
            )
        }

        private fun replaceUnknownHtmlTags(text: String): String {
            return replaceUnknownTag(text, "highlighttext", "u")
        }

        private fun replaceUnknownTag(text: String, tagName: String, newTagName: String): String {
            return text.replace("<$tagName>", "<$newTagName>").replace(
                "</$tagName>",
                "</$newTagName>"
            )
        }
    }
}
