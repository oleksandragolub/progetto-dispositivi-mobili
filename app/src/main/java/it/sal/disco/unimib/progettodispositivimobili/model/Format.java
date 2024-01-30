package it.sal.disco.unimib.progettodispositivimobili.model;

public enum Format {
    COMIC("comic"),
    MAGAZINE("magazine"),
    TRADE_PAPERBACK("trade paperback"),
    HARDCOVER("hardcover"),
    DIGEST("digest"),
    GRAPHIC_NOVEL("graphic novel"),
    DIGITAL_COMIC("digital comic"),
    INFINITE_COMIC("infinite comic");

    private final String format;

    private Format(final String format) {
        this.format = format;
    }

    @Override public String toString() {
        return format;
    }
}