package me.anonymoussoftware.vacancymanager.model;

import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Snippet {

	private final String requirement;
	private final String responsibility;
	
	public static Snippet fromJson(JSONObject json) {
		if (json == null) {
			return new Snippet("", "");
		}
		return new Snippet(replaceUnknownHtmlTags(json.optString("requirement")), replaceUnknownHtmlTags(json.optString("responsibility")));
	}
	
	private static String replaceUnknownHtmlTags(String text) {
		return replaceUnknownTag(text, "highlighttext", "u");// text.replace("<highlighttext>", "<u>").replace("</highlighttext>", "</u>");
	}
	
	private static String replaceUnknownTag(String text, String tagName, String newTagName) {
		return text.replace("<" + tagName + ">", "<" + newTagName + ">").replace("</" + tagName + ">", "</" + newTagName + ">");
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.requirement != null && !this.requirement.trim().isEmpty()) {
			sb.append("<b>Requirement:</b>");
			sb.append("<br/>");
			sb.append(this.requirement);
			sb.append("<br/>");
			sb.append("<br/>");
		}
		if (this.responsibility != null && !this.responsibility.trim().isEmpty()) {
			sb.append("<b>Responsibility:</b>");
			sb.append("<br/>");
			sb.append(this.responsibility);
		}
		return sb.toString();
	}

	public JSONObject toJson() {
		JSONObject result = new JSONObject();
		if (this.requirement != null && !this.requirement.trim().isEmpty()) {
			result.put("requirement", this.requirement);			
		}
		if (this.responsibility != null && !this.responsibility.trim().isEmpty()) {
			result.put("responsibility", this.responsibility);			
		}
		return result;
	}
}
