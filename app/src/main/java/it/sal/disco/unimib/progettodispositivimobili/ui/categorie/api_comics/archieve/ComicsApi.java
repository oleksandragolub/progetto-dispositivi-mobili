package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve;

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

}
