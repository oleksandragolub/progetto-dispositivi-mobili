package it.sal.disco.unimib.progettodispositivimobili.model;

import java.util.List;

public class ComicsDto extends MarvelCollection<ComicDto> {

    public List<ComicDto> getComics() {
        return getResults();
    }

    @Override public String toString() {
        return "CharactersDto{"
                + "offset="
                + getOffset()
                + ", limit="
                + getLimit()
                + ", total="
                + getTotal()
                + ", count="
                + getCount()
                + ", characters="
                + getComics().toString()
                + '}';
    }
}
