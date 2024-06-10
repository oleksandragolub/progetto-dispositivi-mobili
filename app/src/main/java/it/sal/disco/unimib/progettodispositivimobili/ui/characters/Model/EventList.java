package it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model;

import java.util.List;

public class EventList {
    private int available;
    private String collectionURI;
    private List<EventSummary> items;

    // Getters and setters
    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public String getCollectionURI() {
        return collectionURI;
    }

    public void setCollectionURI(String collectionURI) {
        this.collectionURI = collectionURI;
    }

    public List<EventSummary> getItems() {
        return items;
    }

    public void setItems(List<EventSummary> items) {
        this.items = items;
    }
}