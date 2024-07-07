package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsInfoBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsPdfDetailUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ApiClient;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ComicsApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComicsInfoFragment extends Fragment {
    private EditText txtSearch;
    private ProgressBar progress;
    private RecyclerView recyclerViewComics;
    private AdapterComics comicsAdapter;
    private List<ModelPdfComics> comicsList;
    private FragmentComicsInfoBinding binding;
    private static final String TAG = "ComicInfoFragment";
    private AppCompatImageView buttonFilter;
    private static final int COMICS_INCREMENT = 50; // number of comics to load each time
    private int currentOffset = 0; // New variable to keep track of the offset
    private boolean hasMoreComics = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComicsInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        initViews(root);

        //binding.moreNow.setOnClickListener(v -> loadMoreComics());

        return root;
    }

    private void initViews(View root) {
        txtSearch = root.findViewById(R.id.txtSearch);
        progress = root.findViewById(R.id.progress);
        recyclerViewComics = root.findViewById(R.id.recyclerViewComics);
        buttonFilter = root.findViewById(R.id.buttonFilter);

        Button button = root.findViewById(R.id.button);
        button.setOnClickListener(v -> {
            if (!txtSearch.getText().toString().isEmpty()) {
                progress.setVisibility(View.VISIBLE);
                comicsList.clear(); // clear existing comics
                currentOffset = 0; // Reset the offset
                searchComics(txtSearch.getText().toString(), currentOffset);
            } else {
                showAlert(getString(R.string.empty));
            }
        });

        buttonFilter.setOnClickListener(v -> {
            if (getActivity() != null) {
                openFragment(new ComicsAvanzatoInfoFragment());
            }
        });

        recyclerViewComics.setLayoutManager(new LinearLayoutManager(getContext()));
        comicsList = new ArrayList<>();
        comicsAdapter = new AdapterComics(comicsList, getActivity());
        recyclerViewComics.setAdapter(comicsAdapter);

        comicsAdapter.setOnItemClickListener(comic -> {
            if (comic.isFromApi()) {
                openComicsMarvelDetailFragment(comic);
            } else {
                openComicsPdfDetailUserFragment(comic);
            }
        });
    }

    private void searchComics(String query, int offset) {
        progress.setVisibility(View.VISIBLE);
        searchApiComics(query, offset);
        searchManualComics(query, offset);
    }

    private void searchApiComics(String query, int offset) {
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        apiService.getComics(query, COMICS_INCREMENT, offset).enqueue(new Callback<List<Comic>>() {
            @Override
            public void onResponse(Call<List<Comic>> call, Response<List<Comic>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comic> comics = response.body();
                    for (Comic comic : comics) {
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
                    if (comics.size() < COMICS_INCREMENT) {
                        hasMoreComics = false;
                    }
                    comicsAdapter.notifyDataSetChanged();
                    updateRecyclerViewVisibility();
                } else {
                    showAlert(getString(R.string.service_error) + " Code: " + response.code());
                }
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<List<Comic>> call, Throwable t) {
                showAlert(getString(R.string.service_error) + " " + t.getMessage());
                progress.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void searchManualComics(String query, int offset) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.orderByChild("titolo")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limitToFirst(COMICS_INCREMENT + offset) // Apply offset
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (count < offset) {
                                count++;
                                continue;
                            }
                            ModelPdfComics model = ds.getValue(ModelPdfComics.class);
                            if (model != null) {
                                model.setFromApi(false);
                                comicsList.add(model);
                            }
                            if (comicsList.size() >= COMICS_INCREMENT + offset) {
                                break;
                            }
                        }
                        if (snapshot.getChildrenCount() < COMICS_INCREMENT) {
                            hasMoreComics = false;
                        }
                        comicsAdapter.notifyDataSetChanged();
                        updateRecyclerViewVisibility();
                        progress.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showAlert(getString(R.string.service_error) + " " + error.getMessage());
                        progress.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void loadMoreComics() {
        if (hasMoreComics) {
            currentOffset += COMICS_INCREMENT;
            searchComics(txtSearch.getText().toString(), currentOffset);
        } else {
            Toast.makeText(getActivity(), R.string.no_more_comics, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRecyclerViewVisibility() {
        if (comicsList.isEmpty()) {
            recyclerViewComics.setVisibility(View.GONE);
        } else {
            recyclerViewComics.setVisibility(View.VISIBLE);
        }
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

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
