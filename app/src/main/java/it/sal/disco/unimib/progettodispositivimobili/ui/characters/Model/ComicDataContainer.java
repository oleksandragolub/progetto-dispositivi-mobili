package it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model;

import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;

public class ComicDataContainer {
    private int total;
    private List<Comic> results;

    // Getters and setters
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Comic> getResults() {
        return results;
    }

    public void setResults(List<Comic> results) {
        this.results = results;
    }
}