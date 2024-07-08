package it.sal.disco.unimib.progettodispositivimobili.model;

import com.google.gson.annotations.SerializedName;

public class CreatorResourceDto extends MarvelResourceDto {
    @SerializedName("role") private String role;

    public String getRole() {
        return role;
    }
}
