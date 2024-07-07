package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsAdminBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterApiComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ApiClient;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ComicsApi;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsMarvelDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComicsApiAdminFragment extends Fragment {

    private static final String TAG = "COMICS_API_ADMIN_TAG";
    private String categoryId, category, uid;
    private List<Comic> comicsList;
    private AdapterApiComics adapterComicsApi;
    private FragmentComicsAdminBinding binding;
    private FirebaseAuth firebaseAuth;
    private ComicsApi comicsApi;
    private int currentComicCount = 0;
    private static final int COMICS_LOAD_LIMIT = 20;

    public static ComicsApiAdminFragment newInstance(String categoryId, String category, String uid) {
        ComicsApiAdminFragment fragment = new ComicsApiAdminFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
        comicsApi = ApiClient.getClient().create(ComicsApi.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComicsAdminBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        Log.d(TAG, "onCreateView: Category: " + category);

        comicsList = new ArrayList<>();
        adapterComicsApi = new AdapterApiComics(comicsList, getActivity());
        adapterComicsApi.setOnItemClickListener(this::openComicDetailFragment);
        binding.comicsRv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.comicsRv.setAdapter(adapterComicsApi);

        loadMoreComics(); // Carica i primi fumetti

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (adapterComicsApi != null) {
                        adapterComicsApi.getFilter().filter(s);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onTextChanged: " + e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.moreNow.setOnClickListener(v -> {
            loadMoreComics();
        });

        return root;
    }

    public void setComicsList(List<Comic> comicsList) {
        this.comicsList = comicsList; // Salva la lista dei fumetti
        Log.d(TAG, "setComicsList: Received " + comicsList.size() + " comics for category: " + category);
        if (binding != null) {
            updateComicsList(comicsList); // Aggiorna la lista dei fumetti solo se il binding è inizializzato
        }
    }

    private void updateComicsList(List<Comic> comicsList) {
        Log.d(TAG, "updateComicsList: Updating comics list for category: " + category);
        adapterComicsApi.notifyDataSetChanged();
    }

    private void loadMoreComics() {
        Log.d(TAG, "loadMoreComics: Loading more comics...");
        comicsApi.getComicsByCollection(currentComicCount, COMICS_LOAD_LIMIT, category).enqueue(new Callback<List<Comic>>() {
            @Override
            public void onResponse(Call<List<Comic>> call, Response<List<Comic>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comic> moreComics = response.body();
                    Log.d(TAG, "Loaded " + moreComics.size() + " more comics for category: " + category);
                    comicsList.addAll(moreComics);
                    adapterComicsApi.addComics(moreComics); // Aggiorna la lista dei fumetti
                    currentComicCount += moreComics.size();
                } else {
                    Log.e(TAG, "API response unsuccessful. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Comic>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    private void openComicDetailFragment(Comic comic) {
        ComicsMarvelDetailFragment detailFragment = new ComicsMarvelDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("comic", comic);
        detailFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

  /*  private void openComicDetailFragment(Comic comic) {
        ComicsMarvelDetailFragment detailFragment = new ComicsMarvelDetailFragment();
        Bundle args = new Bundle();
        ModelPdfComics modelPdfComics = new ModelPdfComics();
        modelPdfComics.setId(comic.getId());
        modelPdfComics.setTitolo(comic.getTitle());
        modelPdfComics.setDescrizione(comic.getDescription());
        modelPdfComics.setUrl(comic.getThumbnail());
        modelPdfComics.setYear(comic.getYear());
        modelPdfComics.setLanguage(comic.getLanguage());
        modelPdfComics.setCollection(comic.getCollection());
        modelPdfComics.setSubject(comic.getSubject());
        args.putSerializable("modelPdfComics", modelPdfComics);
        detailFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, detailFragment);  // Assicurati di utilizzare l'ID corretto
        transaction.addToBackStack(null);
        transaction.commit();
    }*/

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}