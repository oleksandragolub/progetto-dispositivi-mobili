package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsPdfListAdminBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterApiComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterPdfComicsAdmin;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsMarvelDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ApiClient;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ComicsApi;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComicsPdfListAdminFragment extends Fragment {

    private FragmentComicsPdfListAdminBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelPdfComics> pdfArrayList;
    private AdapterPdfComicsAdmin adapterPdfComicsAdmin;
    private AdapterApiComics adapterApiComics;
    private String categoryId, categoryTitle;
    private static final String TAG = "ComicsPdfListAdminFragment";

    private int currentComicCount = 0; // Variabile per tenere traccia dei fumetti caricati
    private static final int COMICS_LOAD_LIMIT = 20; // Limite per il caricamento dei fumetti

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComicsPdfListAdminBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        if (getActivity() != null && getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            categoryTitle = getArguments().getString("categoryTitle");
            binding.subTitleTv.setText(categoryTitle);
        }

        loadComics();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterPdfComicsAdmin.getFilter().filter(s);
                } catch (Exception e) {
                    Log.d(TAG, "onTextChanged: " + e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                openFragment(new CategoryAddAdminFragment());
            }
        });

        binding.moreNow.setOnClickListener(v -> loadMoreComics());

        return root;
    }

    private void loadComics() {
        pdfArrayList = new ArrayList<>();
        loadManualComics();
    }

    private void loadManualComics() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.orderByChild("categoryId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPdfComics model = ds.getValue(ModelPdfComics.class);
                    if (model != null) {
                        pdfArrayList.add(model);
                        Log.d(TAG, "Loaded comic from Firebase: " + model.getTitolo());
                    }
                }
                setupComicsAdapter();
                loadApiComics();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    private void setupComicsAdapter() {
        adapterPdfComicsAdmin = new AdapterPdfComicsAdmin(getActivity(), pdfArrayList);
        binding.comicsRv.setAdapter(adapterPdfComicsAdmin);

        adapterPdfComicsAdmin.setOnItemClickListener(model -> openComicsMarvelDetailFragment(model));
    }

    private void loadApiComics() {
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        apiService.getComicsByCollection(currentComicCount, COMICS_LOAD_LIMIT, categoryId).enqueue(new Callback<List<Comic>>() {
            @Override
            public void onResponse(Call<List<Comic>> call, Response<List<Comic>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comic> comics = response.body();
                    Log.d(TAG, "Loaded " + comics.size() + " comics from API");
                    setupApiComicsAdapter(comics);
                    currentComicCount += comics.size(); // Aggiorna il contatore dei fumetti caricati
                } else {
                    Log.d(TAG, "onResponse: Response not successful. Code: " + response.code());
                    try {
                        Log.d(TAG, "onResponse: Response body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Comic>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void setupApiComicsAdapter(List<Comic> comics) {
        if (adapterApiComics == null) {
            adapterApiComics = new AdapterApiComics(comics, getActivity());
            binding.comicsRv.setAdapter(adapterApiComics);
        } else {
            adapterApiComics.addComics(comics);
            adapterApiComics.notifyItemRangeInserted(currentComicCount, comics.size());
        }

        adapterApiComics.setOnItemClickListener(comic -> openComicsMarvelDetailFragment(comic));
        Log.d(TAG, "API comics adapter setup complete.");
    }

    private void loadMoreComics() {
        loadApiComics();
    }

    private void openComicsMarvelDetailFragment(ModelPdfComics model) {
        ComicsMarvelDetailFragment comicsMarvelDetailFragment = new ComicsMarvelDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("modelPdfComics", model);
        comicsMarvelDetailFragment.setArguments(args);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, comicsMarvelDetailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openComicsMarvelDetailFragment(Comic comic) {
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
        openComicsMarvelDetailFragment(model);
    }

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