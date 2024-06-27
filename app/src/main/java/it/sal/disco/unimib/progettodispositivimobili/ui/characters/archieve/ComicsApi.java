package it.sal.disco.unimib.progettodispositivimobili.ui.characters.archieve;

import com.google.gson.JsonObject;

import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.Comic;
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

    @GET
    Call<ResponseBody> downloadComicPdf(@Url String fileUrl);
}
