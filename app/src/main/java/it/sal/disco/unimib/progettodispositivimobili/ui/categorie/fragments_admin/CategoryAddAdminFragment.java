package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentCategoryAddAdminBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterCategory;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ApiClient;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ComicsApi;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelCategory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryAddAdminFragment extends Fragment {
    private static final String TAG = "CategoryAddAdminFragment";
    private FragmentCategoryAddAdminBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelCategory> categoryArrayList;
    private AdapterCategory adapterCategory;
    private int currentStartIndex = 0;
    private int limit = 30;
    private Set<String> loadedCollections;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryAddAdminBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();
        categoryArrayList = new ArrayList<>();
        loadedCollections = new HashSet<>();
        adapterCategory = new AdapterCategory(getActivity(), categoryArrayList);
        binding.RecyclerViewCategory.setAdapter(adapterCategory);

        loadCategories(currentStartIndex, limit);

        binding.searchCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterCategory.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.addCategoryBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                openFragment(new CategoryAddFragment());
            }
        });

        binding.addComicsPdfBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                openFragment(new ComicsPdfAddFragment());
            }
        });

        // binding.buttonMore.setOnClickListener(v -> loadMoreCategories());

        return root;
    }

    /*private void loadCategories(int start, int limit) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) {
                    return;
                }
                categoryArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    if (model != null && loadedCollections.add(model.getId())) {
                        categoryArrayList.add(model);
                    }
                }
                adapterCategory.notifyDataSetChanged();
                loadApiCollections(start, limit); // Load API collections after manual categories
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private void loadCategories(int start, int limit) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) {
                    return;
                }
                categoryArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    if (model != null && loadedCollections.add(model.getId())) {
                        categoryArrayList.add(model);
                    }
                }
                adapterCategory.notifyDataSetChanged();
                loadApiCollections(start, limit); // Load API collections after manual categories
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to load categories: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadApiCollections(int start, int limit) {
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);

        apiService.getComicsByCollection(start, limit).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject collections = response.body();
                    int initialSize = categoryArrayList.size();
                    boolean hasNewData = false;
                    for (Map.Entry<String, JsonElement> entry : collections.entrySet()) {
                        String collectionName = entry.getKey();
                        if (loadedCollections.add(collectionName)) {
                            ModelCategory model = new ModelCategory();
                            model.setId(collectionName);
                            model.setCategory(collectionName);
                            categoryArrayList.add(model);
                            hasNewData = true;
                        }
                    }
                    if (hasNewData) {
                        adapterCategory.notifyItemRangeInserted(initialSize, categoryArrayList.size() - initialSize);
                    }
                } else {
                    try {
                        Log.e(TAG, "Response error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getActivity(), "Failed to load collections from API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (t instanceof SocketTimeoutException) {
                    retryApiCall(call.clone());
                } else {
                    Toast.makeText(getActivity(), "Failed to load collections from API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void retryApiCall(Call<JsonObject> call) {
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject collections = response.body();
                    int initialSize = categoryArrayList.size();
                    boolean hasNewData = false;
                    for (Map.Entry<String, JsonElement> entry : collections.entrySet()) {
                        String collectionName = entry.getKey();
                        if (loadedCollections.add(collectionName)) {
                            ModelCategory model = new ModelCategory();
                            model.setId(collectionName);
                            model.setCategory(collectionName);
                            categoryArrayList.add(model);
                            hasNewData = true;
                        }
                    }
                    if (hasNewData) {
                        adapterCategory.notifyItemRangeInserted(initialSize, categoryArrayList.size() - initialSize);
                    }
                } else {
                    try {
                        Log.e(TAG, "Response error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getActivity(), "Failed to load collections from API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getActivity(), "Retry failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMoreCategories() {
        currentStartIndex += limit;
        Log.d(TAG, "Loading more categories, start index: " + currentStartIndex);
        loadApiCollections(currentStartIndex, limit);
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