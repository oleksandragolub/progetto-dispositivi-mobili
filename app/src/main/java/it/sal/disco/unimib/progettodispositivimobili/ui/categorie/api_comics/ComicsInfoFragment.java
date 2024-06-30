package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

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
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterApiComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.Comic;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.archieve.ApiClient;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComicsInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        txtSearch = root.findViewById(R.id.txtSearch);
        progress = root.findViewById(R.id.progress);
        recyclerViewComics = root.findViewById(R.id.recyclerViewComics);

        Button button = root.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtSearch.getText().toString().isEmpty()) {
                    progress.setVisibility(View.VISIBLE);
                    searchComics(txtSearch.getText().toString());
                } else {
                    showAlert(getString(R.string.empty));
                }
            }
        });

        recyclerViewComics.setLayoutManager(new LinearLayoutManager(getContext()));
        comicsList = new ArrayList<>();
        comicsAdapter = new AdapterComics(comicsList, getActivity());
        recyclerViewComics.setAdapter(comicsAdapter);
    }

    private void searchComics(String query) {
        comicsList.clear();
        comicsAdapter.notifyDataSetChanged();
        searchApiComics(query);
        searchManualComics(query);
    }

    private void searchApiComics(String query) {
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        Call<List<Comic>> call = apiService.getComics(query, 10);

        call.enqueue(new Callback<List<Comic>>() {
            @Override
            public void onResponse(Call<List<Comic>> call, Response<List<Comic>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "API Comics found: " + response.body().size());
                    for (Comic comic : response.body()) {
                        ModelPdfComics model = new ModelPdfComics();
                        model.setId(comic.getId());
                        model.setTitolo(comic.getTitle());
                        model.setDescrizione(comic.getDescription());
                        model.setUrl(comic.getThumbnail());
                        model.setFromApi(true);
                        comicsList.add(model);
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

    private void searchManualComics(String query) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.orderByChild("titolo").startAt(query).endAt(query + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "Manual Comics found: " + snapshot.getChildrenCount());
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ModelPdfComics model = ds.getValue(ModelPdfComics.class);
                        model.setFromApi(false);
                        comicsList.add(model);
                    }
                    comicsAdapter.notifyDataSetChanged();
                    updateRecyclerViewVisibility();
                } else {
                    Log.d(TAG, "No Manual Comics found for query: " + query);
                }
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showAlert(getString(R.string.service_error) + " " + error.getMessage());
                progress.setVisibility(View.INVISIBLE);
            }
        });
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
}