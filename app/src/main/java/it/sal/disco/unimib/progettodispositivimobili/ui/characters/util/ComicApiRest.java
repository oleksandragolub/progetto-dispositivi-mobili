package it.sal.disco.unimib.progettodispositivimobili.ui.characters.util;

import it.sal.disco.unimib.progettodispositivimobili.model.ComicsDto;
import it.sal.disco.unimib.progettodispositivimobili.model.MarvelResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

interface ComicApiRest {
    @GET("comics") Call<MarvelResponse<ComicsDto>> getComics(
            @QueryMap Map<String, Object> comicFilter);

    @GET("comics/{id}") Call<MarvelResponse<ComicsDto>> getComic(
            @Path("id") String comicId);
}