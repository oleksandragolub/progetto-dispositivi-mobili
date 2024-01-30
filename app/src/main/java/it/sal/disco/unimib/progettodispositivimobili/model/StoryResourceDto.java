package it.sal.disco.unimib.progettodispositivimobili.model;

import com.google.gson.annotations.SerializedName;

public class StoryResourceDto extends MarvelResourceDto {
    @SerializedName("type") private String type;

    public String getType() {
        return type;
    }

    @Override public String toString() {
        return "StoryResourceDto{"
                + "name="
                + super.getName()
                + "resourceUri="
                + super.getResourceUri()
                + "type='"
                + type
                + '\''
                +
                '}';
    }
}