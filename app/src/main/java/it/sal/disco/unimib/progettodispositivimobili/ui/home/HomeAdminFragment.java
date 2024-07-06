package it.sal.disco.unimib.progettodispositivimobili.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import java.util.List;
import java.util.Map;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentAdminHomeBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ApiClient;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ComicsApi;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.ComicsAdminFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.ComicsPdfDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelCategory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeAdminFragment extends Fragment {
    private ComicsApi comicsApi;
    private ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;
    private FragmentAdminHomeBinding binding;

    private static final String TAG = "HomeAdminFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        comicsApi = ApiClient.getClient().create(ComicsApi.class);

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        return root;
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, getActivity());

        categoryArrayList = new ArrayList<>();

        // Carica le collezioni tramite API
        loadCollections(viewPager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();

                // Codice esistente per aggiungere categorie predefinite
                ModelCategory modelAll = new ModelCategory("01", "All", "", 1);
                ModelCategory modelMostViewed = new ModelCategory("02", "Most Viewed", "", 1);
                ModelCategory modelMostDownloaded = new ModelCategory("03", "Most Downloaded", "", 1);

                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                categoryArrayList.add(modelMostDownloaded);

                viewPagerAdapter.addFragment(ComicsAdminFragment.newInstance(modelAll.getId(), modelAll.getCategory(), modelAll.getUid()), modelAll.getCategory());
                viewPagerAdapter.addFragment(ComicsAdminFragment.newInstance(modelMostViewed.getId(), modelMostViewed.getCategory(), modelMostViewed.getUid()), modelMostViewed.getCategory());
                viewPagerAdapter.addFragment(ComicsAdminFragment.newInstance(modelMostDownloaded.getId(), modelMostDownloaded.getCategory(), modelMostDownloaded.getUid()), modelMostDownloaded.getCategory());

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    if (model != null) {
                        categoryArrayList.add(model);
                        viewPagerAdapter.addFragment(ComicsAdminFragment.newInstance(model.getId(), model.getCategory(), model.getUid()), model.getCategory());
                    }
                }

                viewPager.setAdapter(viewPagerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
        binding.tabLayout.setupWithViewPager(viewPager);
    }

    private void loadCollections(ViewPager viewPager) {
        comicsApi.getComicsByCollection(0, 10).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject collections = response.body();
                    for (Map.Entry<String, JsonElement> entry : collections.entrySet()) {
                        String collectionName = entry.getKey();
                        loadComicsForCollection(collectionName, viewPager);
                    }
                } else {
                    Log.e(TAG, "API response unsuccessful for collections. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "API error for collections. Message: " + t.getMessage());
            }
        });
    }

    private void loadComicsForCollection(String collection, ViewPager viewPager) {
        comicsApi.getComicsByCollection(0, 10, collection).enqueue(new Callback<List<Comic>>() {
            @Override
            public void onResponse(Call<List<Comic>> call, Response<List<Comic>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comic> comics = response.body();
                    ModelCategory modelCategory = new ModelCategory(collection, collection, "", 1);
                    categoryArrayList.add(modelCategory);
                    ComicsAdminFragment fragment = ComicsAdminFragment.newInstance(modelCategory.getId(), modelCategory.getCategory(), modelCategory.getUid());
                    fragment.setComicsList(comics);

                    viewPagerAdapter.addFragment(fragment, modelCategory.getCategory());
                    viewPagerAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "API response unsuccessful for collection: " + collection + ", Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Comic>> call, Throwable t) {
                Log.e(TAG, "API error for collection: " + collection + ", Message: " + t.getMessage());
            }
        });
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<ComicsAdminFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(ComicsAdminFragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void openComicsPdfDetailFragment(String comicsId) {
        ComicsPdfDetailFragment comicsPdfDetailFragment = new ComicsPdfDetailFragment();
        Bundle args = new Bundle();
        args.putString("comicsId", comicsId);
        comicsPdfDetailFragment.setArguments(args);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, comicsPdfDetailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
