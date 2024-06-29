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

import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsInfoBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterApiComics;
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
    private AdapterApiComics comicsAdapter;
    private static final String TAG = "ComicInfoFragment";
    private FragmentComicsInfoBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComicsInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        initViews(root);
        return root;
    }

    public void initViews(View root) {
        txtSearch = root.findViewById(R.id.txtSearch);
        progress = root.findViewById(R.id.progress);
        recyclerViewComics = root.findViewById(R.id.recyclerViewComics);

        Button button = root.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnGetComicInfoOnClick(v);
            }
        });

        recyclerViewComics.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void btnGetComicInfoOnClick(View view) {
        if (txtSearch.getText().toString().isEmpty()) {
            showAlert(getString(R.string.empty));
        } else {
            progress.setVisibility(View.VISIBLE);
            getComicInfo(txtSearch.getText().toString(), 10); // Puoi cambiare il limite a tuo piacere
        }
    }

    public void getComicInfo(String title, int limit) {
        Log.d(TAG, "Requesting data for: " + title);

        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        Call<List<Comic>> call = apiService.getComics(title, limit);

        call.enqueue(new Callback<List<Comic>>() {
            @Override
            public void onResponse(Call<List<Comic>> call, Response<List<Comic>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comic> comics = response.body();
                    showComicList(comics);
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

    public void showComicList(List<Comic> comics) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            if (comics.isEmpty()) {
                showAlert(getString(R.string.no_exist));
            } else {
                recyclerViewComics.setVisibility(View.VISIBLE);
                comicsAdapter = new AdapterApiComics(comics, getActivity());
                recyclerViewComics.setAdapter(comicsAdapter);
            }
        });
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