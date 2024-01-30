package it.sal.disco.unimib.progettodispositivimobili.model;

import com.google.gson.annotations.SerializedName;

public class MarvelDate {
    @SerializedName("type") private String type;
    @SerializedName("date") private String date;

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }
}