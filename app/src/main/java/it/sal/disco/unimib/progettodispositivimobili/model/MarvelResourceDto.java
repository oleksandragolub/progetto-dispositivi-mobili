package it.sal.disco.unimib.progettodispositivimobili.model;

import com.google.gson.annotations.SerializedName;

public class MarvelResourceDto {
    @SerializedName("resourceURI") private String resourceUri;
    @SerializedName("name") private String name;

    public String getResourceUri() {
        return resourceUri;
    }

    public String getName() {
        return name;
    }

    @Override public String toString() {
        return "MarvelResourceDto{"
                + "resourceUri='"
                + resourceUri
                + '\''
                + ", name='"
                + name
                + '\''
                + '}';
    }
}
