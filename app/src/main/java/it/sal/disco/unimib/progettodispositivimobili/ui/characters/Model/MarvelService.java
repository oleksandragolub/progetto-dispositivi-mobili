package it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.MarvelApi;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.Root;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// ELENCA TUTTI PERSONAGGI
/*
public class MarvelService {
    private static final String BASE_URL = "https://gateway.marvel.com/";
    private final MarvelApi marvelApi;
    private final String publicAPIKey;
    private final String privateKey;

    public MarvelService(String publicAPIKey, String privateKey) {
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

    public void getAllCharacters(OnDataResponse delegate) {
        String ts = String.valueOf(System.currentTimeMillis());
        String hash = MarvelAuth.generateMarvelHash(ts, privateKey, publicAPIKey);
        int limit = 100; // Max limit per request
        int offset = 0;

        // Recursive function to fetch all characters
        fetchCharacters(ts, hash, limit, offset, new ArrayList<>(), delegate);
    }

    private void fetchCharacters(String ts, String hash, int limit, int offset, List<Result> allCharacters, OnDataResponse delegate) {
        Call<Root> call = marvelApi.getAllCharacters(ts, publicAPIKey, hash, limit, offset);
        call.enqueue(new Callback<Root>() {
            @Override
            public void onResponse(Call<Root> call, Response<Root> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Root root = response.body();
                    allCharacters.addAll(root.getData().getResults());

                    if (offset + limit < root.getData().getTotal()) {
                        fetchCharacters(ts, hash, limit, offset + limit, allCharacters, delegate);
                    } else {
                        delegate.onChange(false, response.code(), new Root(allCharacters));
                    }
                } else {
                    delegate.onChange(true, response.code(), null);
                    Log.d("MarvelService", "Service error with status code: " + response.code() + ", message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Root> call, Throwable t) {
                delegate.onChange(true, -1, null);
                Log.d("MarvelService", "Network error: " + t.getMessage());
            }
        });
    }

    public interface OnDataResponse {
        void onChange(boolean isNetworkError, int statusCode, Root root);
    }
}*/


public class MarvelService {
    private static final String BASE_URL = "https://gateway.marvel.com/";
    private final MarvelApi marvelApi;
    private final String publicAPIKey;
    private final String privateKey;

    public MarvelService(String publicAPIKey, String privateKey) {
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

    public void requestCharacterData(String name, OnDataResponse delegate) {
        String ts = String.valueOf(System.currentTimeMillis());
        String hash = MarvelAuth.generateMarvelHash(ts, privateKey, publicAPIKey);
        Call<Root> call = marvelApi.getCharacter(name, ts, publicAPIKey, hash);
        call.enqueue(new Callback<Root>() {
            @Override
            public void onResponse(Call<Root> call, Response<Root> response) {
                if (response.isSuccessful()) {
                    Root root = response.body();
                    delegate.onChange(false, response.code(), root);
                    Log.d("MarvelService", "OK: " + response.body().toString());
                } else {
                    delegate.onChange(true, response.code(), null);
                    Log.d("MarvelService", "Service error with status code: " + response.code() + ", message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Root> call, Throwable t) {
                delegate.onChange(true, -1, null);
                Log.d("MarvelService", "Network error: " + t.getMessage());
            }
        });
    }

    public interface OnDataResponse {
        void onChange(boolean isNetworkError, int statusCode, Root root);
    }
}

