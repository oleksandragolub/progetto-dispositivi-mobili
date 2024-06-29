package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models;

import java.util.Objects;

public class ModelPdfComics {

    String uid, id, titolo, descrizione, categoryId, url;
    long timestamp, viewsCount, downloadsCount;
    boolean favorite = false;
    private boolean fromApi = false;

   /* public ModelPdfComics(){

    }*/

    public ModelPdfComics() {

    }

    public ModelPdfComics(String uid, String id, String titolo, String descrizione, String categoryId, String url, long timestamp, long viewsCount, long downloadsCount, boolean favorite) {
        this.uid = uid;
        this.id = id;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.categoryId = categoryId;
        this.url = url;
        this.timestamp = timestamp;
        this.viewsCount = viewsCount;
        this.downloadsCount = downloadsCount;
        this.favorite = favorite;
    }
    public boolean isFromApi() {
        return fromApi;
    }

    public void setFromApi(boolean fromApi) {
        this.fromApi = fromApi;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelPdfComics that = (ModelPdfComics) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }

    public long getDownloadsCount() {
        return downloadsCount;
    }

    public void setDownloadsCount(long downloadsCount) {
        this.downloadsCount = downloadsCount;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
