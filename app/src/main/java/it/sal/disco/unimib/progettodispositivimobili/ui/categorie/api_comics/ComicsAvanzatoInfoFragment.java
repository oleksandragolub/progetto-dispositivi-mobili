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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsPdfDetailUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComicsAvanzatoInfoFragment extends Fragment {
    private static final String TAG = "ComicsAvanzatoInfoFragment";
    private MultiSelectSpinner multiSelectCollection, multiSelectGenre;
    private AppCompatSpinner spinnerLanguage, spinnerYear;
    private ProgressBar progress;
    private RecyclerView recyclerViewComics;
    private AdapterComics comicsAdapter;
    private List<ModelPdfComics> comicsList;
    private AppCompatImageView buttonBack;

    private static final int SEARCH_TIMEOUT = 10000; // Timeout di 10 secondi

    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_comics_avanzato_info, container, false);
        initViews(root);
        setupTimeoutHandler();

        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                openFragment(new ComicsInfoFragment());
            }
        });
        return root;
    }

    private void initViews(View root) {
        multiSelectCollection = root.findViewById(R.id.multiSelectCollection);
        multiSelectGenre = root.findViewById(R.id.multiSelectGenre);
        spinnerLanguage = root.findViewById(R.id.spinnerLanguage);
        spinnerYear = root.findViewById(R.id.spinnerYear);
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
        String toast_text = getContext().getString(R.string.toast_search_timeout);
        Toast.makeText(getContext(), toast_text, Toast.LENGTH_SHORT).show();
        progress.setVisibility(View.INVISIBLE);
        timeoutHandler.removeCallbacks(timeoutRunnable);
    }

    private void loadSpinnerData() {
        SpinnerUtils.loadYears(getContext(), spinnerYear);
        SpinnerUtils.loadLanguages(getContext(), spinnerLanguage);
        SpinnerUtils.loadCollections(getContext(), multiSelectCollection);
        SpinnerUtils.loadSubjects(getContext(), multiSelectGenre);
    }

    private void performAdvancedSearch() {
        List<String> collections = multiSelectCollection.getSelectedStrings();
        List<String> genres = multiSelectGenre.getSelectedStrings();
        String language = spinnerLanguage.getSelectedItem() != null ? spinnerLanguage.getSelectedItem().toString() : "";
        String year = spinnerYear.getSelectedItem() != null ? spinnerYear.getSelectedItem().toString() : "";

        if (collections.isEmpty() && language.isEmpty() && year.isEmpty() && genres.isEmpty()) {
            String toast_text = getContext().getString(R.string.toast_param);
            Toast.makeText(getContext(), toast_text, Toast.LENGTH_SHORT).show();
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

        Log.d(TAG, "Inizio ricerca avanzata: collections=" + collections + ", language=" + language + ", year=" + year + ", genres=" + genres);
        searchManualComics(collections, language, year, genres);
    }

    private void retrySearchApiComics(List<String> collections, String language, String year, List<String> genres) {
        searchApiComics(collections, language, year, genres, false);
    }

    private void searchApiComics(List<String> collections, String language, String year, List<String> genres, boolean showRetryDialog) {
        Log.d(TAG, "Invio richiesta API: collections=" + collections + ", language=" + language + ", year=" + year + ", genres=" + genres);
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        Call<List<Comic>> call = apiService.getComicsByAdvancedSearch(
                collections.isEmpty() ? null : String.join(",", collections),
                language.isEmpty() ? null : language,
                year.isEmpty() ? null : year,
                genres.isEmpty() ? null : String.join(",", genres),
                20
        );

        call.enqueue(new Callback<List<Comic>>() {
            @Override
            public void onResponse(Call<List<Comic>> call, Response<List<Comic>> response) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                try {
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
                        String toast_text = getContext().getString(R.string.toast_comics_unfound);
                        Toast.makeText(getContext(), toast_text, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, toast_text);
                    }
                    progress.setVisibility(View.INVISIBLE);
                } finally {
                    if (response.body() == null && response.errorBody() != null) {
                        response.errorBody().close(); // Chiudi il corpo dell'errore se presente
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Comic>> call, Throwable t) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                if (t instanceof IOException) {
                    if (showRetryDialog) {
                        showRetryDialogForSearch(collections, language, year, genres);
                    } else {
                        retrySearchApiComics(collections, language, year, genres);
                    }
                } else {
                    String toast_text = getContext().getString(R.string.toast_search_error);
                    Toast.makeText(getContext(), toast_text, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, toast_text+": " + t.getMessage());
                    progress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void searchManualComics(List<String> collections, String language, String year, List<String> genres) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        Query query = ref.orderByChild("timestamp");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ModelPdfComics model = ds.getValue(ModelPdfComics.class);
                        if (model != null && matchesFilters(model, collections, language, year, genres)) {
                            model.setFromApi(false);
                            comicsList.add(model);
                        }
                    }
                    comicsAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Comics caricati manualmente: " + comicsList.size());
                } else {
                    Log.d(TAG, "Nessun fumetto trovato nei manuali con i parametri selezionati");
                }
                // After manual comics are loaded, proceed to load API comics
                searchApiComics(collections, language, year, genres, false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showAlert(getString(R.string.service_error) + " " + error.getMessage());
                progress.setVisibility(View.INVISIBLE);
            }
        });
    }

    private boolean matchesFilters(ModelPdfComics model, List<String> collections, String language, String year, List<String> genres) {
        boolean matches = true;
        if (!language.isEmpty() && !language.equals(model.getLanguage())) {
            matches = false;
        }
        if (!year.isEmpty() && !year.equals(model.getYear())) {
            matches = false;
        }
        if (!collections.isEmpty()) {
            boolean collectionMatch = false;
            for (String collection : collections) {
                if (model.getCollections().contains(collection)) {
                    collectionMatch = true;
                    break;
                }
            }
            if (!collectionMatch) {
                matches = false;
            }
        }
        if (!genres.isEmpty()) {
            boolean genreMatch = false;
            for (String genre : genres) {
                if (model.getGenres().contains(genre)) {
                    genreMatch = true;
                    break;
                }
            }
            if (!genreMatch) {
                matches = false;
            }
        }
        return matches;
    }

    private void showAlert(String message) {
        if (getActivity() == null) {
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setMessage(message);
        alert.setPositiveButton(R.string.close, null);
        alert.show();
    }

    private void showRetryDialogForSearch(List<String> collections, String language, String year, List<String> genres) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Timeout occurred. Would you like to wait a bit longer for the data to load?")
                .setPositiveButton("Yes", (dialog, id) -> searchApiComics(collections, language, year, genres, true))
                .setNegativeButton("No", (dialog, id) -> {
                    String toast_text = getContext().getString(R.string.toast_search_cancelled);
                    Toast.makeText(getContext(), toast_text, Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.INVISIBLE);
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void openComicsMarvelDetailFragment(ModelPdfComics comic) {
        ComicsMarvelDetailFragment comicsMarvelDetailFragment = new ComicsMarvelDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("comic", comic);
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

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
