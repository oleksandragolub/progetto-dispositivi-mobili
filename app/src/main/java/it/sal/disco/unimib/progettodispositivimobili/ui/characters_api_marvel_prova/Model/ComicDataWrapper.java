package it.sal.disco.unimib.progettodispositivimobili.ui.characters_api_marvel_prova.Model;

public class ComicDataWrapper {
    private int code;
    private String status;
    private ComicDataContainer data;

    // Getters and setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ComicDataContainer getData() {
        return data;
    }

    public void setData(ComicDataContainer data) {
        this.data = data;
    }
}