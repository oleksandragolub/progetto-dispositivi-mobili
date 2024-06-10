package it.sal.disco.unimib.progettodispositivimobili.ui.marvel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("comics")
    Call<ComicResponse> getComics(
            @Query("ts") long timestamp,
            @Query("apikey") String apiKey,
            @Query("hash") String hash,
            @Query("format") String format,
            @Query("formatType") String formatType,
            @Query("noVariants") boolean noVariants,
            @Query("dateDescriptor") String dateDescriptor,
            @Query("titleStartsWith") String titleStartsWith,
            @Query("limit") int limit
    );
}