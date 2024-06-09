package it.sal.disco.unimib.progettodispositivimobili.ui.chat.messages;

public class Message {

    private String id, ownerId, text, date;
    private boolean isRead;

    public Message(String id, String ownerId, String text, String date) {
        this.id = id;
        this.ownerId = ownerId;
        this.text = text;
        this.date = date;
        this.isRead = false; // Valore predefinito
    }

    public Message(String id, String ownerId, String text, String date, boolean isRead) {
        this.id = id;
        this.ownerId = ownerId;
        this.text = text;
        this.date = date;
        this.isRead = isRead;
    }

    // Getter e setter per isRead
    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
