package it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model;

import java.util.List;

public class ComicList {
    private int available;
    private String collectionURI;
    private List<ComicSummary> items;

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

    public List<ComicSummary> getItems() {
        return items;
    }

    public void setItems(List<ComicSummary> items) {
        this.items = items;
    }
}