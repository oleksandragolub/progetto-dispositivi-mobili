package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ApiClient;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ComicsApi;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.CategoryAddAdminFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsPdfDetailUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComicsAvanzatoInfoFragment extends Fragment {
    private AppCompatSpinner spinnerCollection, spinnerLanguage, spinnerYear, spinnerGenre;
    private ProgressBar progress;
    private RecyclerView recyclerViewComics;
    private AdapterComics comicsAdapter;
    private List<ModelPdfComics> comicsList;

    private AppCompatImageView buttonBack;

    private static final String TAG = "ComicsAvanzatoInfoFrag";
    private static final int SEARCH_TIMEOUT = 10000; // Timeout di 10 secondi

    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_comics_avanzato_info, container, false);
        initViews(root);
        setupTimeoutHandler();

        buttonBack.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new ComicsInfoFragment());
            }
        });
        return root;
    }

    private void initViews(View root) {
        spinnerCollection = root.findViewById(R.id.spinnerCollection);
        spinnerLanguage = root.findViewById(R.id.spinnerLanguage);
        spinnerYear = root.findViewById(R.id.spinnerYear);
        spinnerGenre = root.findViewById(R.id.spinnerGenre);
        progress = root.findViewById(R.id.progress);
        recyclerViewComics = root.findViewById(R.id.recyclerViewComics);
        Button searchButton = root.findViewById(R.id.searchButton);
        buttonBack = root.findViewById(R.id.buttonBack);

        recyclerViewComics.setLayoutManager(new LinearLayoutManager(getContext()));
        comicsList = new ArrayList<>();
        comicsAdapter = new AdapterComics(comicsList, getActivity());
        recyclerViewComics.setAdapter(comicsAdapter);

        searchButton.setOnClickListener(v -> performAdvancedSearch());

        comicsAdapter.setOnItemClickListener(comic -> {
            if (comic.isFromApi()) {
                openComicsMarvelDetailFragment(comic);
            } else {
                openComicsPdfDetailUserFragment(comic);
            }
        });



        loadSpinnerData();
    }

    private void setupTimeoutHandler() {
        timeoutHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == SEARCH_TIMEOUT) {
                    onSearchTimeout();
                }
            }
        };
    }

    private void onSearchTimeout() {
        Toast.makeText(getContext(), "La ricerca ha impiegato troppo tempo. Riprovare più tardi.", Toast.LENGTH_SHORT).show();
        progress.setVisibility(View.INVISIBLE);
        timeoutHandler.removeCallbacks(timeoutRunnable);
    }

    private void loadSpinnerData() {
        loadCollections(false);
        loadLanguages(false);
        loadYears(false);
        loadGenres(false);
    }

    private void loadCollections(boolean showRetryDialog) {
        Log.d(TAG, "Inizio caricamento collezioni");
        loadSpinnerData("collections", showRetryDialog, () -> {
            ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
            apiService.getComicsByCollection(0, 20).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();
                        List<String> collections = new ArrayList<>();
                        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                            collections.add(entry.getKey());
                            Log.d(TAG, "Collection loaded: " + entry.getKey());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, collections);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCollection.setAdapter(adapter);
                    } else {
                        Log.e(TAG, "Failed to load collections: response unsuccessful");
                        if (showRetryDialog) showRetryDialogForSpinner("collections");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e(TAG, "Failed to load collections: " + t.getMessage());
                    if (t instanceof IOException) {
                        if (showRetryDialog) {
                            showRetryDialogForSpinner("collections");
                        } else {
                            retryLoadCollections();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load collections", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void retryLoadCollections() {
        loadCollections(false);
    }

    private void loadLanguages(boolean showRetryDialog) {
        Log.d(TAG, "Inizio caricamento lingue");
        loadSpinnerData("languages", showRetryDialog, () -> {
            ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
            apiService.getComicsByLanguage().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();
                        List<String> languages = new ArrayList<>();
                        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                            languages.add(entry.getKey());
                            Log.d(TAG, "Language loaded: " + entry.getKey());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, languages);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerLanguage.setAdapter(adapter);
                    } else {
                        Log.e(TAG, "Failed to load languages: response unsuccessful");
                        if (showRetryDialog) showRetryDialogForSpinner("languages");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e(TAG, "Failed to load languages: " + t.getMessage());
                    if (t instanceof IOException) {
                        if (showRetryDialog) {
                            showRetryDialogForSpinner("languages");
                        } else {
                            retryLoadLanguages();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load languages", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void retryLoadLanguages() {
        loadLanguages(false);
    }

    private void loadYears(boolean showRetryDialog) {
        Log.d(TAG, "Inizio caricamento anni");
        loadSpinnerData("years", showRetryDialog, () -> {
            ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
            List<String> yearsAndDates = new ArrayList<>();

            apiService.getComicsByYear().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();
                        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                            yearsAndDates.add(entry.getKey());
                            Log.d(TAG, "Year loaded: " + entry.getKey());
                        }
                        updateSpinnerYear(yearsAndDates);
                    } else {
                        Log.e(TAG, "Failed to load years: response unsuccessful");
                        if (showRetryDialog) showRetryDialogForSpinner("years");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e(TAG, "Failed to load years: " + t.getMessage());
                    if (t instanceof IOException) {
                        if (showRetryDialog) {
                            showRetryDialogForSpinner("years");
                        } else {
                            retryLoadYears();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load years", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            /*apiService.getComicsByDate().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();
                        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                            yearsAndDates.add(entry.getKey());
                            Log.d(TAG, "Date loaded: " + entry.getKey());
                        }
                        updateSpinnerYear(yearsAndDates);
                    } else {
                        Log.e(TAG, "Failed to load dates: response unsuccessful");
                        if (showRetryDialog) showRetryDialogForSpinner("dates");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e(TAG, "Failed to load dates: " + t.getMessage());
                    if (t instanceof IOException) {
                        if (showRetryDialog) {
                            showRetryDialogForSpinner("dates");
                        } else {
                            retryLoadYears();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load dates", Toast.LENGTH_SHORT).show();
                    }
                }
            });*/
        });
    }

    private void retryLoadYears() {
        loadYears(false);
    }

    private void updateSpinnerYear(List<String> yearsAndDates) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, yearsAndDates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapter);
    }

    private void loadGenres(boolean showRetryDialog) {
        Log.d(TAG, "Inizio caricamento generi");
        loadSpinnerData("genres", showRetryDialog, () -> {
            ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
            apiService.getComicsBySubject().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();
                        List<String> genres = new ArrayList<>();
                        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                            genres.add(entry.getKey());
                            Log.d(TAG, "Genre loaded: " + entry.getKey());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, genres);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerGenre.setAdapter(adapter);
                    } else {
                        Log.e(TAG, "Failed to load genres: response unsuccessful");
                        if (showRetryDialog) showRetryDialogForSpinner("genres");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e(TAG, "Failed to load genres: " + t.getMessage());
                    if (t instanceof IOException) {
                        if (showRetryDialog) {
                            showRetryDialogForSpinner("genres");
                        } else {
                            retryLoadGenres();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load genres", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void retryLoadGenres() {
        loadGenres(false);
    }

    private void loadSpinnerData(String field, boolean showRetryDialog, Runnable loadFunction) {
        if (showRetryDialog) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Timeout occurred while loading " + field + ". Would you like to wait a bit longer for the data to load?")
                    .setPositiveButton("Yes", (dialog, id) -> loadFunction.run())
                    .setNegativeButton("No", (dialog, id) -> Toast.makeText(getContext(), "Loading " + field + " cancelled", Toast.LENGTH_SHORT).show());
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            loadFunction.run();
        }
    }

    private void performAdvancedSearch() {
        String collection = spinnerCollection.getSelectedItem() != null ? spinnerCollection.getSelectedItem().toString() : "";
        String language = spinnerLanguage.getSelectedItem() != null ? spinnerLanguage.getSelectedItem().toString() : "";
        String year = spinnerYear.getSelectedItem() != null ? spinnerYear.getSelectedItem().toString() : "";
        String genre = spinnerGenre.getSelectedItem() != null ? spinnerGenre.getSelectedItem().toString() : "";

        if (collection.isEmpty() && language.isEmpty() && year.isEmpty() && genre.isEmpty()) {
            Toast.makeText(getContext(), "Seleziona almeno un parametro di ricerca", Toast.LENGTH_SHORT).show();
            return;
        }

        comicsList.clear();
        comicsAdapter.notifyDataSetChanged();
        progress.setVisibility(View.VISIBLE);

        // Rimuovi le callback esistenti per evitare timeout multipli
        timeoutHandler.removeCallbacks(timeoutRunnable);

        // Imposta il timeout
        timeoutRunnable = () -> timeoutHandler.sendEmptyMessage(SEARCH_TIMEOUT);
        timeoutHandler.postDelayed(timeoutRunnable, SEARCH_TIMEOUT);

        Log.d(TAG, "Inizio ricerca avanzata: collection=" + collection + ", language=" + language + ", year=" + year + ", genre=" + genre);
        searchApiComics(collection, language, year, genre, false);
    }

    private void searchApiComics(String collection, String language, String year, String genre, boolean showRetryDialog) {
        Log.d(TAG, "Invio richiesta API: collection=" + collection + ", language=" + language + ", year=" + year + ", genre=" + genre);
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        Call<List<Comic>> call = apiService.getComicsByAdvancedSearch(
                collection.isEmpty() ? null : collection,
                language.isEmpty() ? null : language,
                year.isEmpty() ? null : year,
                genre.isEmpty() ? null : genre,
                20
        );

        call.enqueue(new Callback<List<Comic>>() {
            @Override
            public void onResponse(Call<List<Comic>> call, Response<List<Comic>> response) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    for (Comic comic : response.body()) {
                        ModelPdfComics model = new ModelPdfComics();
                        model.setId(comic.getId());
                        model.setTitolo(comic.getTitle());
                        model.setDescrizione(comic.getDescription());
                        model.setUrl(comic.getThumbnail());
                        model.setYear(comic.getYear());
                        model.setLanguage(comic.getLanguage());
                        model.setCollection(comic.getCollection());
                        model.setSubject(comic.getSubject());
                        model.setFromApi(true);
                        comicsList.add(model);
                    }
                    comicsAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Comics caricati dall'API: " + comicsList.size());
                } else {
                    Toast.makeText(getContext(), "Nessun fumetto trovato con i parametri selezionati", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Nessun fumetto trovato con i parametri selezionati");
                }
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<List<Comic>> call, Throwable t) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                if (t instanceof IOException) {
                    if (showRetryDialog) {
                        showRetryDialogForSearch(collection, language, year, genre);
                    } else {
                        retrySearchApiComics(collection, language, year, genre);
                    }
                } else {
                    Toast.makeText(getContext(), "Errore durante la ricerca", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Errore durante la ricerca: " + t.getMessage());
                    progress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void retrySearchApiComics(String collection, String language, String year, String genre) {
        searchApiComics(collection, language, year, genre, false);
    }

    private void showRetryDialogForSpinner(String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Timeout occurred while loading " + field + ". Would you like to wait a bit longer for the data to load?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    switch (field) {
                        case "collections":
                            loadCollections(true);
                            break;
                        case "languages":
                            loadLanguages(true);
                            break;
                        case "years":
                        //case "dates":
                            loadYears(true);
                            break;
                        case "genres":
                            loadGenres(true);
                            break;
                    }
                })
                .setNegativeButton("No", (dialog, id) -> Toast.makeText(getContext(), "Loading " + field + " cancelled", Toast.LENGTH_SHORT).show());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showRetryDialogForSearch(String collection, String language, String year, String genre) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Timeout occurred. Would you like to wait a bit longer for the data to load?")
                .setPositiveButton("Yes", (dialog, id) -> searchApiComics(collection, language, year, genre, true))
                .setNegativeButton("No", (dialog, id) -> {
                    Toast.makeText(getContext(), "Search cancelled", Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.INVISIBLE);
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void openComicsMarvelDetailFragment(ModelPdfComics comic) {
        ComicsMarvelDetailFragment comicsMarvelDetailFragment = new ComicsMarvelDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("modelPdfComics", comic);
        comicsMarvelDetailFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, comicsMarvelDetailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openComicsPdfDetailUserFragment(ModelPdfComics comic) {
        ComicsPdfDetailUserFragment comicsPdfDetailUserFragment = new ComicsPdfDetailUserFragment();
        Bundle args = new Bundle();
        args.putSerializable("modelPdfComics", comic);
        comicsPdfDetailUserFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, comicsPdfDetailUserFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}