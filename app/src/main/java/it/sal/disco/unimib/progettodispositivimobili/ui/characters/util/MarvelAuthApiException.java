package it.sal.disco.unimib.progettodispositivimobili.ui.characters.util;

public class MarvelAuthApiException extends MarvelApiException {
    public MarvelAuthApiException(int httpCode, String marvelCode, String description,
                                  Throwable cause) {
        super(httpCode, marvelCode, description, cause);
    }

}