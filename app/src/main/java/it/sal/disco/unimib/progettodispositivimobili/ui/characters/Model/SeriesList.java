package it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model;

import java.util.List;

public class SeriesList {
    private int available;
    private String collectionURI;
    private List<SeriesSummary> items;

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

    public List<SeriesSummary> getItems() {
        return items;
    }

    public void setItems(List<SeriesSummary> items) {
        this.items = items;
    }
}