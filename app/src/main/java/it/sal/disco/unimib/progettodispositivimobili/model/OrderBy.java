package it.sal.disco.unimib.progettodispositivimobili.model;

public enum OrderBy {
    NAME("name"),
    MODIFIED("modified");

    private final String orderBy;

    private OrderBy(final String orderBy) {
        this.orderBy = orderBy;
    }

    @Override public String toString() {
        return orderBy;
    }
}