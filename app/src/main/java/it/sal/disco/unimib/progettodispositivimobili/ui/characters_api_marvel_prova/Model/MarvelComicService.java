package it.sal.disco.unimib.progettodispositivimobili.ui.characters_api_marvel_prova.Model;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MarvelComicService {
    private static final String BASE_URL = "https://gateway.marvel.com/";
    private final MarvelApi marvelApi;
    private final String publicAPIKey;
    private final String privateKey;

    public MarvelComicService(String publicAPIKey, String privateKey) {
        this.publicAPIKey = publicAPIKey;
        this.privateKey = privateKey;

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        marvelApi = retrofit.create(MarvelApi.class);
    }

   /* public void requestComicData(String title, OnDataResponse delegate) {
        String ts = String.valueOf(System.currentTimeMillis());
        String hash = MarvelAuth.generateMarvelHash(ts, privateKey, publicAPIKey);
        Call<ComicDataWrapper> call = marvelApi.getComic(title, ts, publicAPIKey, hash);
        call.enqueue(new Callback<ComicDataWrapper>() {
            @Override
            public void onResponse(Call<ComicDataWrapper> call, Response<ComicDataWrapper> response) {
                if (response.isSuccessful()) {
                    ComicDataWrapper root = response.body();
                    delegate.onChange(false, response.code(), root);
                    Log.d("MarvelComicService", "OK: " + response.body().toString());
                } else {
                    delegate.onChange(true, response.code(), null);
                    Log.d("MarvelComicService", "Service error with status code: " + response.code() + ", message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ComicDataWrapper> call, Throwable t) {
                delegate.onChange(true, -1, null);
                Log.d("MarvelComicService", "Network error: " + t.getMessage());
            }
        });
    }*/

    public void requestComicData(String title, OnDataResponse delegate) {
        String ts = String.valueOf(System.currentTimeMillis());
        String hash = MarvelAuth.generateMarvelHash(ts, privateKey, publicAPIKey);
        Call<ComicDataWrapper> call = marvelApi.getComic(title, ts, publicAPIKey, hash);
        call.enqueue(new Callback<ComicDataWrapper>() {
            @Override
            public void onResponse(Call<ComicDataWrapper> call, Response<ComicDataWrapper> response) {
                if (response.isSuccessful()) {
                    ComicDataWrapper root = response.body();
                    delegate.onChange(false, response.code(), root);
                    Log.d("MarvelComicService", "OK: " + response.body().toString());
                } else {
                    delegate.onChange(true, response.code(), null);
                    Log.d("MarvelComicService", "Service error with status code: " + response.code() + ", message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ComicDataWrapper> call, Throwable t) {
                delegate.onChange(true, -1, null);
                Log.d("MarvelComicService", "Network error: " + t.getMessage());
            }
        });
    }

    public void requestComicsData(String title, int limit, int offset, OnDataResponse delegate) {
        String ts = String.valueOf(System.currentTimeMillis());
        String hash = MarvelAuth.generateMarvelHash(ts, privateKey, publicAPIKey);
        Call<ComicDataWrapper> call = marvelApi.getComics(ts, publicAPIKey, hash, limit, offset, title);
        call.enqueue(new Callback<ComicDataWrapper>() {
            @Override
            public void onResponse(Call<ComicDataWrapper> call, Response<ComicDataWrapper> response) {
                if (response.isSuccessful()) {
                    ComicDataWrapper root = response.body();
                    delegate.onChange(false, response.code(), root);
                    Log.d("MarvelComicService", "OK: " + response.body().toString());
                } else {
                    delegate.onChange(true, response.code(), null);
                    Log.d("MarvelComicService", "Service error with status code: " + response.code() + ", message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ComicDataWrapper> call, Throwable t) {
                delegate.onChange(true, -1, null);
                Log.d("MarvelComicService", "Network error: " + t.getMessage());
            }
        });
    }


    public interface OnDataResponse {
        void onChange(boolean isNetworkError, int statusCode, ComicDataWrapper root);
    }
}