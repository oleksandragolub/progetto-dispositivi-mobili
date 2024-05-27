package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsPdfAddBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments.CategoryAddAdminFragment;

public class ComicsPdfAddFragment extends Fragment {

    private FragmentComicsPdfAddBinding binding;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    //private ArrayList<ModelCategory> categoryArrayList;

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    private Uri pdfUri;

    private static final String TAG = "ADD_PDF_TAG";

    private static final int PDF_PICK_CODE = 1000;

    private String title, descrizione;
    //private String title, descrizione, category;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentComicsPdfAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Per favore aspetta un secondo...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new CategoryAddAdminFragment());
            }
        });

        binding.attachFileBtn.setOnClickListener(v -> {
            if(getActivity() != null) {
                pdfPickIntent();
            }
        });

        binding.textViewComicsCategory.setOnClickListener(v -> {
            if(getActivity() != null) {
                categoryPickDialog();
            }
        });

        binding.submitBtn.setOnClickListener(v -> {
            if(getActivity() != null) {
                validateData();
            }
        });

        return root;
    }

    private void validateData() {
        Log.d(TAG, "validateData: validating data...");
        title = binding.textViewComicsTitle.getText().toString().trim();
        descrizione = binding.textViewComicsDescription.getText().toString().trim();
        //category = binding.textViewComicsCategory.getText().toString().trim();

        if(TextUtils.isEmpty(title)){
            Toast.makeText(getActivity(), "Inserisci il titolo...", Toast.LENGTH_SHORT).show();
            binding.textViewComicsTitle.setError("Titolo richiesto");
            binding.textViewComicsTitle.requestFocus();
        } else if(TextUtils.isEmpty(descrizione)){
            Toast.makeText(getActivity(), "Inserisci la descrizione...", Toast.LENGTH_SHORT).show();
            binding.textViewComicsDescription.setError("Descrizione richiesta");
            binding.textViewComicsDescription.requestFocus();
        } /*else if(TextUtils.isEmpty(category)){
            Toast.makeText(getActivity(), "Seleziona la categoria...", Toast.LENGTH_SHORT).show();
            binding.textViewComicsCategory.setError("Categoria richiesta");
            binding.textViewComicsCategory.requestFocus();
        } */ else if(TextUtils.isEmpty(selectedCategoryTitle)){
            Toast.makeText(getActivity(), "Seleziona la categoria...", Toast.LENGTH_SHORT).show();
            binding.textViewComicsCategory.setError("Categoria richiesta");
            binding.textViewComicsCategory.requestFocus();
        } else if(pdfUri == null){
            Toast.makeText(getActivity(), "Seleziona il pdf...", Toast.LENGTH_SHORT).show();
        } else {
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
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
                String uploadedPdfUri = ""+uriTask.getResult();

                uploadPdfInfoToDb(uploadedPdfUri, timestamp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: PDF upload failed due to "+ e.getMessage());
                Toast.makeText(getActivity(), "PDF upload failed due to "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadPdfInfoToDb(String uploadedPdfUri, long timestamp) {
        Log.d(TAG, "uploadPdfToStorage: uploading Pdf info to firebase db...");

        progressDialog.setMessage("Uploading pdf info...");

        String uid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("titolo", ""+title);
        hashMap.put("descrizione", ""+descrizione);
        //hashMap.put("categoria", ""+category);
        hashMap.put("categoryId", ""+selectedCategoryId);
        hashMap.put("url", ""+uploadedPdfUri);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child(""+timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Log.d(TAG, "onSuccess: Successfully uploaded...");
                Toast.makeText(getActivity(), "Successfully uploaded...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: Failed to upload to db due to..."+e.getMessage());
                Toast.makeText(getActivity(), "Failed to upload to db due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                   /* ModelCategory model = ds.getValue(ModelCategory.class);
                    categoryTitleArrayList.add(model);

                    Log.d(TAG, "onDataChange: " + model.getCategory());*/

                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedCategoryId, selectedCategoryTitle;

    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");

        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for(int i = 0; i< categoryTitleArrayList.size(); i++){
            //categoriesArray[i] = categoryTitleArrayList.get(i).getCategory();
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Category").setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               /* String category = categoriesArray[which];
                binding.textViewComicsCategory.setText(category);

                Log.d(TAG, "onClick: Selected Category: "+category);*/

                selectedCategoryTitle = categoryTitleArrayList.get(which);
                selectedCategoryId = categoryIdArrayList.get(which);
                binding.textViewComicsCategory.setText(selectedCategoryTitle);

                Log.d(TAG, "onClick: Selected Category: "+selectedCategoryId+" "+selectedCategoryTitle);
            }
        }).show();
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

        if(resultCode == RESULT_OK){
            if(requestCode == PDF_PICK_CODE){
                Log.d(TAG, "onActivityResult: PDF Picked");

                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: URI: " + pdfUri);
            }
        }else{
            Log.d(TAG, "onActivityResult: cancelled picking pdf");
            Toast.makeText(getActivity(), "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
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
