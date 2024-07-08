package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Comic implements Serializable {
    private String id;
    private String title;
    private String thumbnail;
    private String year;
    private String language;

    @SerializedName("description")
    private Object description;

    @SerializedName("collection")
    private String collection;

    @SerializedName("collections")
    private List<String> collectionList;

    @SerializedName("subject")
    private String subject;

    @SerializedName("subjects")
    private List<String> subjectList;

    // Additional field to distinguish between API and manual data
    private boolean fromApi;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        if (description instanceof String) {
            return (String) description;
        } else if (description instanceof List) {
            return String.join(", ", (List<String>) description);
        }
        return "No Description Available";
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public List<String> getCollectionList() {
        return collectionList;
    }

    public void setCollectionList(List<String> collectionList) {
        this.collectionList = collectionList;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getSubjectList() {
        return subjectList;
    }

    public void setSubjectList(List<String> subjectList) {
        this.subjectList = subjectList;
    }

    public boolean isFromApi() {
        return fromApi;
    }

    public void setFromApi(boolean fromApi) {
        this.fromApi = fromApi;
    }
}
