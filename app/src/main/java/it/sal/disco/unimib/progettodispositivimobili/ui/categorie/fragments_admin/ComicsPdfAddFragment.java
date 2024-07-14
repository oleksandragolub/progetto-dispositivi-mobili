package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsPdfAddBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.MultiSelectSpinner;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.SpinnerUtils;

public class ComicsPdfAddFragment extends Fragment {
    private static final String TAG = "ComicsPdfAddFragment";
    private FragmentComicsPdfAddBinding binding;
    private static final int PDF_PICK_CODE = 1000;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Uri pdfUri;
    private String title, descrizione;
    private int pageCount;
    private MultiSelectSpinner multiSelectCollection, multiSelectGenre;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentComicsPdfAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize the MultiSelectSpinners
        multiSelectCollection = root.findViewById(R.id.multiSelectCollection);
        multiSelectGenre = root.findViewById(R.id.multiSelectGenre);

        loadSpinnerData();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Per favore aspetta un secondo...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                openFragment(new CategoryAddAdminFragment());
            }
        });

        binding.attachFileBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                pdfPickIntent();
            }
        });

        binding.submitBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                validateData();
            }
        });

        return root;
    }

    private void loadSpinnerData() {
        SpinnerUtils.loadYears(getContext(), binding.spinnerYear);
        SpinnerUtils.loadLanguages(getContext(), binding.spinnerLanguage);

        // Load collections and genres into the MultiSelectSpinner
        SpinnerUtils.loadCollections(getContext(), multiSelectCollection);
        SpinnerUtils.loadSubjects(getContext(), multiSelectGenre);
    }

    private void validateData() {
        Log.d(TAG, "validateData: validating data...");
        title = binding.textViewComicsTitle.getText().toString().trim();
        descrizione = binding.textViewComicsDescription.getText().toString().trim();
        String selectedYearTitle = binding.spinnerYear.getSelectedItem().toString();
        String selectedLanguageTitle = binding.spinnerLanguage.getSelectedItem().toString();

        List<String> selectedCollections = multiSelectCollection.getSelectedStrings();
        List<String> selectedGenres = multiSelectGenre.getSelectedStrings();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(getActivity(), "Inserisci il titolo...", Toast.LENGTH_SHORT).show();
            binding.textViewComicsTitle.setError("Titolo richiesto");
            binding.textViewComicsTitle.requestFocus();
        } else if (TextUtils.isEmpty(descrizione)) {
            Toast.makeText(getActivity(), "Inserisci la descrizione...", Toast.LENGTH_SHORT).show();
            binding.textViewComicsDescription.setError("Descrizione richiesta");
            binding.textViewComicsDescription.requestFocus();
        } else if (TextUtils.isEmpty(selectedYearTitle)) {
            Toast.makeText(getActivity(), "Seleziona l'anno...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedLanguageTitle)) {
            Toast.makeText(getActivity(), "Seleziona la lingua...", Toast.LENGTH_SHORT).show();
        } else if (selectedCollections.isEmpty()) {
            Toast.makeText(getActivity(), "Seleziona almeno una collezione...", Toast.LENGTH_SHORT).show();
        } else if (selectedGenres.isEmpty()) {
            Toast.makeText(getActivity(), "Seleziona almeno un genere...", Toast.LENGTH_SHORT).show();
        } else if (pdfUri == null) {
            Toast.makeText(getActivity(), "Seleziona il pdf...", Toast.LENGTH_SHORT).show();
        } else {
            uploadPdfToStorage(selectedCollections, selectedGenres);
        }
    }

    private void uploadPdfToStorage(List<String> selectedCollections, List<String> selectedGenres) {
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...");

        progressDialog.setMessage("Caricamento del pdf...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();
        String filePathAndName = "Comics/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: PDF uploaded to storage...");
                Log.d(TAG, "onSuccess: getting pdf url...");

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedPdfUri = "" + uriTask.getResult();

                uploadPdfInfoToDb(uploadedPdfUri, timestamp, selectedCollections, selectedGenres);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: PDF upload failed due to " + e.getMessage());
                Toast.makeText(getActivity(), "PDF upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadPdfInfoToDb(String uploadedPdfUri, long timestamp, List<String> selectedCollections, List<String> selectedGenres) {
        Log.d(TAG, "uploadPdfToStorage: uploading Pdf info to firebase db...");

        progressDialog.setMessage("Uploading pdf info...");

        String uid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + uid);
        hashMap.put("id", "" + timestamp);
        hashMap.put("titolo", "" + title);
        hashMap.put("descrizione", "" + descrizione);
        hashMap.put("pages", pageCount);
        hashMap.put("collections", selectedCollections);
        hashMap.put("year", "" + binding.spinnerYear.getSelectedItem().toString());
        hashMap.put("language", "" + binding.spinnerLanguage.getSelectedItem().toString());
        hashMap.put("genres", selectedGenres);
        hashMap.put("url", "" + uploadedPdfUri);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child("" + timestamp).setValue(hashMap).addOnSuccessListener(unused -> {
            progressDialog.dismiss();
            Log.d(TAG, "onSuccess: Successfully uploaded...");
            Toast.makeText(getActivity(), "Successfully uploaded...", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Log.d(TAG, "onFailure: Failed to upload to db due to..." + e.getMessage());
            Toast.makeText(getActivity(), "Failed to upload to db due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Pdf:"), PDF_PICK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PDF_PICK_CODE) {
                Log.d(TAG, "onActivityResult: PDF Picked");

                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: URI: " + pdfUri);
            }
        } else {
            Log.d(TAG, "onActivityResult: cancelled picking pdf");
            Toast.makeText(getActivity(), "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
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
