package it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model;

import java.util.List;

public class Root {
    private int code;
    private String status;
    private Data data;

    public Root(List<Result> results) {
        this.data = new Data();
        this.data.setResults(results);
    }

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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
