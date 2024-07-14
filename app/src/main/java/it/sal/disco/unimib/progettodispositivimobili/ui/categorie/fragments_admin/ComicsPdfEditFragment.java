package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsPdfEditBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.MultiSelectSpinner;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.SpinnerUtils;

public class ComicsPdfEditFragment extends Fragment {
    private static final String TAG = "ComicsPdfEditFragment";
    private FragmentComicsPdfEditBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private String comicsId;
    private String title, description;
    private int pageCount;
    private MultiSelectSpinner multiSelectCollection, multiSelectGenre;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentComicsPdfEditBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        // Recupera l'ID del fumetto dagli argomenti
        if (getArguments() != null) {
            comicsId = getArguments().getString("comicsId");
        }

        // Initialize the MultiSelectSpinners
        multiSelectCollection = root.findViewById(R.id.multiSelectCollection);
        multiSelectGenre = root.findViewById(R.id.multiSelectGenre);

        loadSpinnerData();
        loadComicsInfo();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Per favore aspetta un attimo");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new CategoryAddAdminFragment());
            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        return root;
    }

    private void loadSpinnerData() {
        SpinnerUtils.loadYears(getContext(), binding.spinnerYear);
        SpinnerUtils.loadLanguages(getContext(), binding.spinnerLanguage);
        SpinnerUtils.loadCollections(getContext(), multiSelectCollection);
        SpinnerUtils.loadSubjects(getContext(), multiSelectGenre);
    }

    private void validateData() {
        Log.d(TAG, "validateData: validating data...");
        title = binding.textViewComicsTitle.getText().toString().trim();
        description = binding.textViewComicsDescription.getText().toString().trim();

        String selectedYearTitle = binding.spinnerYear.getSelectedItem().toString();
        String selectedLanguageTitle = binding.spinnerLanguage.getSelectedItem().toString();

        List<String> selectedCollections = multiSelectCollection.getSelectedStrings();
        List<String> selectedGenres = multiSelectGenre.getSelectedStrings();

        if(TextUtils.isEmpty(title)){
            Toast.makeText(getActivity(), "Inserisci il titolo...", Toast.LENGTH_SHORT).show();
            binding.textViewInputLayoutComicsTitle.setError("Titolo richiesto");
            binding.textViewInputLayoutComicsTitle.requestFocus();
        } else if(TextUtils.isEmpty(description)){
            Toast.makeText(getActivity(), "Inserisci la descrizione...", Toast.LENGTH_SHORT).show();
            binding.textViewInputLayoutComicsDescription.setError("Descrizione richiesta");
            binding.textViewInputLayoutComicsDescription.requestFocus();
        } else if (TextUtils.isEmpty(selectedYearTitle)) {
            Toast.makeText(getActivity(), "Inserisci l'anno...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedLanguageTitle)) {
            Toast.makeText(getActivity(), "Inserisci la lingua...", Toast.LENGTH_SHORT).show();
        } else if (selectedCollections.isEmpty()) {
            Toast.makeText(getActivity(), "Inserisci almeno una collezione...", Toast.LENGTH_SHORT).show();
        } else if (selectedGenres.isEmpty()) {
            Toast.makeText(getActivity(), "Inserisci almeno un genere...", Toast.LENGTH_SHORT).show();
        } else {
            updateComicsPdf(selectedCollections, selectedGenres, selectedYearTitle, selectedLanguageTitle);
        }
    }

    private void updateComicsPdf(List<String> selectedCollections, List<String> selectedGenres, String selectedYearTitle, String selectedLanguageTitle) {
        Log.d(TAG, "updateComicsPdf: Updating comic info in the database...");

        progressDialog.setMessage("Updating comics info...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("titolo", ""+title);
        hashMap.put("descrizione", ""+description);
        hashMap.put("collections", selectedCollections);
        hashMap.put("year", selectedYearTitle);
        hashMap.put("language", selectedLanguageTitle);
        hashMap.put("genres", selectedGenres);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child(comicsId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: Comics updated...");
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Comics info updated...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Failed to update due to " + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComicsInfo() {
        Log.d(TAG, "loadComicsInfo: Loading comics info");

        DatabaseReference refComics = FirebaseDatabase.getInstance().getReference("Comics");
        refComics.child(comicsId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String title = ""+snapshot.child("titolo").getValue();
                String description = ""+snapshot.child("descrizione").getValue();
                String year = ""+snapshot.child("year").getValue();
                String language = ""+snapshot.child("language").getValue();
                List<String> collections = (List<String>) snapshot.child("collections").getValue();
                List<String> genres = (List<String>) snapshot.child("genres").getValue();

                binding.textViewComicsTitle.setText(title);
                binding.textViewComicsDescription.setText(description);

                // Set the selected items in the spinners
                SpinnerUtils.setSpinnerItemByValue(binding.spinnerYear, year);
                SpinnerUtils.setSpinnerItemByValue(binding.spinnerLanguage, language);
                multiSelectCollection.setItems(collections);
                multiSelectGenre.setItems(genres);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: Failed to load comics info due to " + error.getMessage());
            }
        });
    }

    private void openFragment(Fragment fragment){
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
