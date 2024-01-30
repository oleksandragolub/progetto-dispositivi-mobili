package it.sal.disco.unimib.progettodispositivimobili.model;

import com.google.gson.annotations.SerializedName;

public class MarvelResponse<T> {
    @SerializedName("code") private int code;
    @SerializedName("status") private String status;
    @SerializedName("copyright") private String copyright;
    @SerializedName("attributionText") private String attributionText;
    @SerializedName("attributionHTML") private String getAttributionHtml;
    @SerializedName("etag") private String etag;

    @SerializedName("data") private T response;

    public MarvelResponse() {
    }

    public MarvelResponse(MarvelResponse marvelResponse) {
        code = marvelResponse.getCode();
        status = marvelResponse.getStatus();
        copyright = marvelResponse.getCopyright();
        attributionText = marvelResponse.getAttributionText();
        getAttributionHtml = marvelResponse.getGetAttributionHtml();
        etag = marvelResponse.getEtag();
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getAttributionText() {
        return attributionText;
    }

    public String getGetAttributionHtml() {
        return getAttributionHtml;
    }

    public T getResponse() {
        return response;
    }

    public String getEtag() {
        return etag;
    }

    @Override public String toString() {
        return "MarvelResponse{"
                + "code="
                + code
                + ", status='"
                + status
                + '\''
                + ", copyright='"
                + copyright
                + '\''
                + ", attributionText='"
                + attributionText
                + '\''
                + ", getAttributionHtml='"
                + getAttributionHtml
                + '\''
                + ", etag='"
                + etag
                + '\''
                + ", response="
                + response
                + '}';
    }

    public void setResponse(T response) {
        this.response = response;
    }
}