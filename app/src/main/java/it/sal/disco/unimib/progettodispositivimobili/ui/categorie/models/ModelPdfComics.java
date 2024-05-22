package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models;

public class ModelPdfComics {

    String uid, id, titolo, descrizione, categoryId, url;
    long timestamp;

    public ModelPdfComics(){

    }

    public ModelPdfComics(String uid, String id, String titolo, String descrizione, String categoryId, String url, long timestamp) {
        this.uid = uid;
        this.id = id;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.categoryId = categoryId;
        this.url = url;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
