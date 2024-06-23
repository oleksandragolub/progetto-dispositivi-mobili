package it.sal.disco.unimib.progettodispositivimobili.ui.characters;

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
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.Comic;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.ComicDataWrapper;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.Model.MarvelComicService;

public class ComicsInfoFragment extends Fragment {
    private EditText txtSearch = null;
    private ProgressBar progress = null;
    private MarvelComicService service = null;
    private RecyclerView recyclerViewComics;
    private ComicsAdapter comicsAdapter;
    private static final String TAG = "ComicInfoFragment";
    private FragmentComicsInfoBinding binding;

    final String PUBLIC_API_KEY = "93e5146b36c6609ec6a87d8104728ed2";
    final String PRIVATE_API_KEY = "80e7b32472204a8f30779ecb3e20815e84384d7b";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComicsInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initViews(root);

        service = new MarvelComicService(PUBLIC_API_KEY, PRIVATE_API_KEY);

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
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        StringBuilder text = new StringBuilder();

        if (txtSearch.getText().toString().isEmpty()) {
            text.append(getString(R.string.empty));
            alert.setMessage(text);
            alert.setPositiveButton(R.string.close, null);
            alert.show();
        } else {
            progress.setVisibility(View.VISIBLE);
            getComicInfo(txtSearch.getText().toString());
        }
    }

    public void getComicInfo(String title) {
        Log.d(TAG, "Requesting data for: " + title);
        service.requestComicData(title, (isNetworkError, statusCode, root) -> {
            getActivity().runOnUiThread(() -> {
                progress.setVisibility(View.INVISIBLE); // Hide the ProgressBar

                if (!isNetworkError && statusCode == 200 && root != null) {
                    Log.d(TAG, "Data received successfully");
                    showComicList(root.getData().getResults());
                } else {
                    showAlert(getString(R.string.service_error) + " Code: " + statusCode);
                }
            });
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
                comicsAdapter = new ComicsAdapter(comics);
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
        progress.setVisibility(View.INVISIBLE); // Hide the ProgressBar in case of error
    }
}