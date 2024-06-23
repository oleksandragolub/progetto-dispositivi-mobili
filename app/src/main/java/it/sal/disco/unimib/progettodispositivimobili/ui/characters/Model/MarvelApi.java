package it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MarvelApi {
    @GET("v1/public/characters")
    Call<Root> getCharacter(
            @Query("name") String name,
            @Query("ts") String ts,
            @Query("apikey") String apiKey,
            @Query("hash") String hash
    );

    @GET("v1/public/characters")
    Call<Root> getAllCharacters(
            @Query("ts") String ts,
            @Query("apikey") String apiKey,
            @Query("hash") String hash,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("v1/public/comics")
    Call<ComicDataWrapper> getComic(
            @Query("title") String title,
            @Query("ts") String ts,
            @Query("apikey") String apiKey,
            @Query("hash") String hash
    );

    @GET("v1/public/comics")
    Call<ComicDataWrapper> getComics(
            @Query("ts") String ts,
            @Query("apikey") String apiKey,
            @Query("hash") String hash,
            @Query("limit") int limit,
            @Query("offset") int offset,
            @Query("title") String title
    );
}