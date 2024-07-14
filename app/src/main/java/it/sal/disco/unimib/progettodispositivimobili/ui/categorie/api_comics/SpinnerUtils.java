package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ApiClient;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ComicsApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpinnerUtils {

    private static final String TAG = "SpinnerUtils";

    // Method for loading data into a regular Spinner
    public static void loadYears(Context context, Spinner spinner) {
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        apiService.getComicsByYear().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    List<String> years = new ArrayList<>();
                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        years.add(entry.getKey());
                        Log.d(TAG, "Year loaded: " + entry.getKey());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, years);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                } else {
                    Log.e(TAG, "Failed to load years: response unsuccessful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load years: " + t.getMessage());
            }
        });
    }

    // Method for loading data into a MultiSelectSpinner
    public static void loadYears(Context context, MultiSelectSpinner spinner) {
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        apiService.getComicsByYear().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    List<String> years = new ArrayList<>();
                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        years.add(entry.getKey());
                        Log.d(TAG, "Year loaded: " + entry.getKey());
                    }
                    spinner.setItems(years);
                } else {
                    Log.e(TAG, "Failed to load years: response unsuccessful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load years: " + t.getMessage());
            }
        });
    }

    // Method for loading data into a regular Spinner
    public static void loadLanguages(Context context, Spinner spinner) {
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        apiService.getComicsByLanguage().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    List<String> languages = new ArrayList<>();
                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        languages.add(entry.getKey());
                        Log.d(TAG, "Language loaded: " + entry.getKey());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, languages);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                } else {
                    Log.e(TAG, "Failed to load languages: response unsuccessful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load languages: " + t.getMessage());
            }
        });
    }

    // Method for loading data into a MultiSelectSpinner
    public static void loadLanguages(Context context, MultiSelectSpinner spinner) {
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        apiService.getComicsByLanguage().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    List<String> languages = new ArrayList<>();
                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        languages.add(entry.getKey());
                        Log.d(TAG, "Language loaded: " + entry.getKey());
                    }
                    spinner.setItems(languages);
                } else {
                    Log.e(TAG, "Failed to load languages: response unsuccessful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load languages: " + t.getMessage());
            }
        });
    }

   /* public static void loadCollections(Context context, MultiSelectSpinner spinner) {
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        apiService.getComicsByCollection(0, 20).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    List<String> collections = new ArrayList<>();
                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        collections.add(entry.getKey());
                        Log.d(TAG, "Collection loaded: " + entry.getKey());
                    }
                    spinner.setItems(collections);
                } else {
                    Log.e(TAG, "Failed to load collections: response unsuccessful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load collections: " + t.getMessage());
            }
        });
    }*/

    public static void loadCollections(Context context, MultiSelectSpinner multiSelectSpinner) {
        List<String> collections = new ArrayList<>();

        // Load collections from API
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        apiService.getComicsByCollection(0, 20).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();
                        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                            collections.add(entry.getKey());
                        }
                    }
                } finally {
                    if (response.body() == null && response.errorBody() != null) {
                        response.errorBody().close(); // Close error body if present
                    }

                    // Load manual collections from Firebase after loading from API
                    loadManualCollections(context, collections, multiSelectSpinner);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Failed to load collections from API: " + t.getMessage());

                if (t instanceof IOException) {
                    // Load manual collections from Firebase even if API fails
                    loadManualCollections(context, collections, multiSelectSpinner);
                }
            }
        });
    }

    private static void loadManualCollections(Context context, List<String> collections, MultiSelectSpinner multiSelectSpinner) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String categoryName = ds.child("category").getValue(String.class);
                        if (categoryName != null && !collections.contains(categoryName)) {
                            collections.add(categoryName);
                        }
                    }
                    multiSelectSpinner.setItems(collections);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load manual collections: " + error.getMessage());
            }
        });
    }

    public static void loadSubjects(Context context, MultiSelectSpinner spinner) {
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        apiService.getComicsBySubject().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    List<String> subjects = new ArrayList<>();
                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        subjects.add(entry.getKey());
                        Log.d(TAG, "Subject loaded: " + entry.getKey());
                    }
                    spinner.setItems(subjects);
                } else {
                    Log.e(TAG, "Failed to load subjects: response unsuccessful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load subjects: " + t.getMessage());
            }
        });
    }

    public static void setSpinnerItemByValue(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            spinner.setSelection(position);
        }
    }
}
