package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models;

public class ModelComment {

    String id, comicsId, timestamp, comment, uid;

    public ModelComment(){

    }

    public ModelComment(String id, String comicsId, String timestamp, String comment, String uid) {
        this.id = id;
        this.comicsId = comicsId;
        this.timestamp = timestamp;
        this.comment = comment;
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComicsId() {
        return comicsId;
    }

    public void setComicsId(String comicsId) {
        this.comicsId = comicsId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
