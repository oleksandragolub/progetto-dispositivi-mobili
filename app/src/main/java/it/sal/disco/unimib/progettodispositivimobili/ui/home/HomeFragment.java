package it.sal.disco.unimib.progettodispositivimobili.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentHomeBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsMarvelDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ApiClient;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ComicsApi;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsApiUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsPdfDetailUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelCategory;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeUserFragment";
    private FragmentHomeBinding binding;
    private ComicsApi comicsApi;
    public ViewPagerUserAdapter viewPagerAdapter;
    public ArrayList<ModelCategory> categoryArrayList;
    private Set<String> loadedCategoriesSet = new HashSet<>();
    private ProgressBar progressBar; // Aggiungi questa linea

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        comicsApi = ApiClient.getClient().create(ComicsApi.class);

        progressBar = root.findViewById(R.id.progressBar); // Aggiungi questa linea

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        return root;
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        viewPagerAdapter = new HomeFragment.ViewPagerUserAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, getActivity());

        categoryArrayList = new ArrayList<>();

        // Carica le categorie manuali dal database
        loadManualCategories(viewPager);

        // Carica le collezioni tramite API
        loadCollections(viewPager, 0);
    }

    private void loadManualCategories(ViewPager viewPager) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();

                ModelCategory modelAll = new ModelCategory("01", "All", "", 1);
                ModelCategory modelMostViewed = new ModelCategory("02", "Most Viewed", "", 1);
                ModelCategory modelMostDownloaded = new ModelCategory("03", "Most Downloaded", "", 1);

                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                categoryArrayList.add(modelMostDownloaded);

                viewPagerAdapter.addFragment(ComicsUserFragment.newInstance(modelAll.getId(), modelAll.getCategory(), modelAll.getUid(), HomeFragment.this::openComicsPdfDetailUserFragment), modelAll.getCategory());
                viewPagerAdapter.addFragment(ComicsUserFragment.newInstance(modelMostViewed.getId(), modelMostViewed.getCategory(), modelMostViewed.getUid(), HomeFragment.this::openComicsPdfDetailUserFragment), modelMostViewed.getCategory());
                viewPagerAdapter.addFragment(ComicsUserFragment.newInstance(modelMostDownloaded.getId(), modelMostDownloaded.getCategory(), modelMostDownloaded.getUid(), HomeFragment.this::openComicsPdfDetailUserFragment), modelMostDownloaded.getCategory());

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    if (model != null) {
                        categoryArrayList.add(model);
                        viewPagerAdapter.addFragment(ComicsUserFragment.newInstance(model.getId(), model.getCategory(), model.getUid(), HomeFragment.this::openComicsPdfDetailUserFragment), model.getCategory());
                    }
                }

                viewPager.setAdapter(viewPagerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void loadCollections(ViewPager viewPager, int attempt) {
        Log.d(TAG, "loadCollections: Attempt " + attempt);
        progressBar.setVisibility(View.VISIBLE); // Mostra il ProgressBar
        comicsApi.getComicsByCollection(0, 10).enqueue(new Callback<JsonObject>() { // Carica solo 10 collezioni inizialmente
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    progressBar.setVisibility(View.GONE); // Nascondi il ProgressBar
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "loadCollections: API response successful");
                        JsonObject collections = response.body();
                        for (Map.Entry<String, JsonElement> entry : collections.entrySet()) {
                            String collectionName = entry.getKey();
                            Log.d(TAG, "Loading collection: " + collectionName);
                            loadComicsForCollection(collectionName, viewPager);
                        }
                    } else {
                        Log.e(TAG, "API response unsuccessful for collections. Code: " + response.code());
                    }
                } finally {
                    if (response.body() == null && response.errorBody() != null) {
                        response.errorBody().close(); // Chiudi il corpo dell'errore se presente
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE); // Nascondi il ProgressBar
                Log.e(TAG, "API error for collections. Message: " + t.getMessage());
                if (attempt < 3) {  // Retry up to 3 times
                    loadCollections(viewPager, attempt + 1);
                }
            }
        });
    }

    private void loadComicsForCollection(String collection, ViewPager viewPager) {
        comicsApi.getComicsByCollection(0, 5, collection).enqueue(new Callback<List<Comic>>() {
            @Override
            public void onResponse(Call<List<Comic>> call, Response<List<Comic>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Comic> comics = response.body();
                        Log.d(TAG, "Loaded " + comics.size() + " comics for collection: " + collection);

                        ModelCategory modelCategory = new ModelCategory(collection, collection, "", 1);
                        if (!loadedCategoriesSet.contains(modelCategory.getId())) {
                            loadedCategoriesSet.add(modelCategory.getId());
                            categoryArrayList.add(modelCategory);

                            ComicsApiUserFragment fragment = ComicsApiUserFragment.newInstance(modelCategory.getId(), modelCategory.getCategory(), modelCategory.getUid());
                            fragment.setComicsList(comics); // Imposta la lista dei fumetti

                            viewPagerAdapter.addFragment(fragment, modelCategory.getCategory());
                            viewPagerAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e(TAG, "API response unsuccessful for collection: " + collection + ", Code: " + response.code());
                    }
                } finally {
                    if (response.body() == null && response.errorBody() != null) {
                        response.errorBody().close(); // Chiudi il corpo dell'errore se presente
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Comic>> call, Throwable t) {
                Log.e(TAG, "API error for collection: " + collection + ", Message: " + t.getMessage());
            }
        });
    }

    public class ViewPagerUserAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();
        private final Context context;

        public ViewPagerUserAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            Log.d(TAG, "Opening fragment: " + fragment.getClass().getSimpleName());
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void openComicsPdfDetailUserFragment(ModelPdfComics model) {
        ComicsPdfDetailUserFragment fragment = new ComicsPdfDetailUserFragment();
        Bundle args = new Bundle();
        args.putSerializable("modelPdfComics", model);
        fragment.setArguments(args);

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