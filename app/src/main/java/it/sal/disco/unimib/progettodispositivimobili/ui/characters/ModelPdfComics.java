package it.sal.disco.unimib.progettodispositivimobili.ui.characters;

public class ModelPdfComics {
    private String id, title, description, thumbnail, url;

    public ModelPdfComics() {
        // Default constructor required for calls to DataSnapshot.getValue(ModelPdfComics.class)
    }

    public ModelPdfComics(String id, String title, String description,  String url) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.url = url;
    }

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}