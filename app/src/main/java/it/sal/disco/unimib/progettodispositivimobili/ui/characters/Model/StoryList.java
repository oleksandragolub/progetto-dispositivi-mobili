package it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model;

import java.util.List;

public class StoryList {
    private int available;
    private String collectionURI;
    private List<StorySummary> items;

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

    public List<StorySummary> getItems() {
        return items;
    }

    public void setItems(List<StorySummary> items) {
        this.items = items;
    }
}