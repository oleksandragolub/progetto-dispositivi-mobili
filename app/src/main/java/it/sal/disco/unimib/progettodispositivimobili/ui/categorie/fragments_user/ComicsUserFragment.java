package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user;

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

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsUserBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterApiComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterPdfComicsUser;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsMarvelDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ApiClient;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ComicsApi;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ComicsUserFragment extends Fragment {

    private static final String TAG = "ComicsUserFragment";
    private String categoryId, category, uid;
    private List<Comic> comicsList;
    private ArrayList<ModelPdfComics> pdfArrayList;
    private AdapterPdfComicsUser adapterPdfUser;
    private AdapterApiComics adapterComicsApi;
    private FragmentComicsUserBinding binding;
    private PDFView pdfView;
    private FirebaseAuth firebaseAuth;
    private ComicsApi comicsApi;
    private int currentComicCount = 0;
    private static final int COMICS_LOAD_LIMIT = 20;
    private AdapterPdfComicsUser.OnItemClickListenerUser onItemClickListener;

    public static ComicsUserFragment newInstance(String categoryId, String category, String uid, AdapterPdfComicsUser.OnItemClickListenerUser onItemClickListener) {
        ComicsUserFragment fragment = new ComicsUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        fragment.setOnItemClickListener(onItemClickListener);
        return fragment;
    }

    public void setOnItemClickListener(AdapterPdfComicsUser.OnItemClickListenerUser onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
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
        binding = FragmentComicsUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        Log.d(TAG, "onCreateView: Category: " + category);

        pdfArrayList = new ArrayList<>();
        comicsList = new ArrayList<>();

        adapterComicsApi = new AdapterApiComics(comicsList, getActivity());
        adapterComicsApi.setOnItemClickListener(this::openComicDetailFragment);

        adapterPdfUser = new AdapterPdfComicsUser(getContext(), pdfArrayList);

        adapterPdfUser = new AdapterPdfComicsUser(getContext(), pdfArrayList);
        adapterPdfUser.setOnItemClickListener(model -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(model);
            }
        });

        binding.comicsRv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.comicsRv.setAdapter(adapterComicsApi);

        // Imposta l'adapter inizialmente
        if (category.equals("All")) {
            loadAllComics();
        } else if (category.equals("Most Viewed")) {
            loadMostViewedDownloadedComics("viewsCount");
        } else if (category.equals("Most Downloaded")) {
            loadMostViewedDownloadedComics("downloadsCount");
        } else {
            loadCategorizedComics();
        }

        // Carica i fumetti dall'API in ogni caso
        loadMoreComics();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (adapterPdfUser != null) {
                        adapterPdfUser.getFilter().filter(s);
                    } else if (adapterComicsApi != null) {
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

        binding.moreNow.setOnClickListener(v -> loadMoreComics());

        return root;
    }

    private void loadMoreComics() {
        Log.d(TAG, "loadMoreComics: Loading more comics...");
        comicsApi.getComicsByCollection(currentComicCount, COMICS_LOAD_LIMIT, category).enqueue(new Callback<List<Comic>>() {
            @Override
            public void onResponse(Call<List<Comic>> call, Response<List<Comic>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Comic> moreComics = response.body();
                        Log.d(TAG, "Loaded " + moreComics.size() + " more comics for category: " + category);
                        comicsList.addAll(moreComics);
                        adapterComicsApi.addComics(moreComics); // Aggiorna la lista dei fumetti
                        currentComicCount += moreComics.size();
                        Log.d(TAG, "Total comics count: " + comicsList.size());
                    } else {
                        Log.e(TAG, "API response unsuccessful. Code: " + response.code());
                    }
                } finally {
                    if (response.body() == null && response.errorBody() != null) {
                        response.errorBody().close(); // Chiudi il corpo dell'errore se presente
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Comic>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    private void loadCategorizedComics() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelPdfComics model = ds.getValue(ModelPdfComics.class);
                            pdfArrayList.add(model);
                        }
                        adapterPdfUser = new AdapterPdfComicsUser(getContext(), pdfArrayList);
                        binding.comicsRv.setAdapter(adapterPdfUser);
                        adapterPdfUser.setOnItemClickListener(model -> {
                            if (onItemClickListener != null) {
                                onItemClickListener.onItemClick(model);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "loadCategorizedComics: Database error: " + error.getMessage());
                    }
                });
    }

    private void loadMostViewedDownloadedComics(String orderBy) {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.orderByChild(orderBy).limitToLast(10)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (binding == null) return;
                        pdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelPdfComics model = ds.getValue(ModelPdfComics.class);
                            pdfArrayList.add(model);
                        }
                        adapterPdfUser = new AdapterPdfComicsUser(getContext(), pdfArrayList);
                        binding.comicsRv.setAdapter(adapterPdfUser);
                        adapterPdfUser.setOnItemClickListener(model -> {
                            if (onItemClickListener != null) {
                                onItemClickListener.onItemClick(model);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "loadMostViewedDownloadedComics: Database error: " + error.getMessage());
                    }
                });
    }

    private void loadAllComics() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                pdfArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPdfComics model = ds.getValue(ModelPdfComics.class);
                    pdfArrayList.add(model);
                }
                adapterPdfUser = new AdapterPdfComicsUser(getContext(), pdfArrayList);
                binding.comicsRv.setAdapter(adapterPdfUser);

                adapterPdfUser.setOnItemClickListener(model -> {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(model);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadAllComics: Database error: " + error.getMessage());
            }
        });
    }

    private void openComicsPdfDetailUserFragment(String comicsId) {
        ComicsPdfDetailUserFragment fragment = new ComicsPdfDetailUserFragment();
        Bundle args = new Bundle();
        args.putString("comicsId", comicsId);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment); // Assicurati che l'ID corrisponda al tuo layout
        transaction.addToBackStack(null);
        transaction.commit();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pdfView != null) {
            pdfView.recycle();  // Rilascia le risorse del PDFView
        }
        binding = null;
    }
}