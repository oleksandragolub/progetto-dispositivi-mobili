package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Comic {
    private String id;
    private String title;
    private String description;
    private String thumbnail;
    private String year;
    private String language;

    @SerializedName("collection")
    private String collection;

    @SerializedName("collections")
    private List<String> collectionList;

    @SerializedName("subject")
    private String subject;

    @SerializedName("subjects")
    private List<String> subjectList;

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
        return description;
    }

    public void setDescription(String description) {
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
}