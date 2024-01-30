package it.sal.disco.unimib.progettodispositivimobili.model;

import com.google.gson.annotations.SerializedName;

public class TextObject {
    @SerializedName("type") private String type;
    @SerializedName("language") private String language;
    @SerializedName("text") private String text;

    public String getType() {
        return type;
    }

    public String getLanguage() {
        return language;
    }

    public String getText() {
        return text;
    }
}