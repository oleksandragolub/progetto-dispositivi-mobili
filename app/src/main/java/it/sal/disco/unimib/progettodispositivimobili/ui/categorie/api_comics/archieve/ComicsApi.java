package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ComicsApi {
    @GET("comics")
    Call<List<Comic>> getComics(@Query("query") String query, @Query("limit") int limit);

    @GET("comic/pdf")
    Call<JsonObject> getComicPdf(@Query("identifier") String identifier);

    @GET("comics/metadata/year")
    Call<JsonObject> getComicsByYear();

    @GET("comics/metadata/language")
    Call<JsonObject> getComicsByLanguage();

    @GET("comics/metadata/collection")
    Call<JsonObject> getComicsByCollection();

    @GET("comics/metadata/subject")
    Call<JsonObject> getComicsBySubject();

    @GET
    Call<ResponseBody> downloadComicPdf(@Url String fileUrl);

    @GET("comics/metadata/collection")
    Call<JsonObject> getComicsByCollection(@Query("start") int start, @Query("limit") int limit);

    @GET("comics/collection")
    Call<List<Comic>> getComicsByCollection(@Query("start") int start, @Query("limit") int limit, @Query("collectionId") String collectionId);

    @GET("comics/advanced_search")
    Call<List<Comic>> getComicsByAdvancedSearch(
            @Query("collection") String collection,
            @Query("language") String language,
            @Query("year") String year,
            @Query("genre") String genre,
            @Query("limit") int limit
    );

    @GET("comics/subject")
    Call<List<Comic>> getTopComicsBySubject(@Query("subject") String subject, @Query("limit") int limit);
}