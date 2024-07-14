package it.sal.disco.unimib.progettodispositivimobili.ui.characters_api_marvel_prova.resto;

public class ModelCharacter {

    String id, nome, descrizione, uid;
    long timestamp;

    public ModelCharacter(){
    }

    public ModelCharacter(String id, String nome, String descrizione, String uid, long timestamp) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
