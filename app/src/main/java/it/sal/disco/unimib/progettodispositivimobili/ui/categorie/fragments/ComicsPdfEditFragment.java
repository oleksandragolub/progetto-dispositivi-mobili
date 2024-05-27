package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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


import java.util.ArrayList;
import java.util.HashMap;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsPdfEditBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments.CategoryAddAdminFragment;

public class ComicsPdfEditFragment extends Fragment {

    private FragmentComicsPdfEditBinding binding;

    private FirebaseAuth firebaseAuth;

    private static final String TAG = "COMICS_EDIT_TAG";

    private String comicsId;

    private ProgressDialog progressDialog;

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    private String selectedCategoryId, selectedCategoryTitle;

    private String title, description;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentComicsPdfEditBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Per favore aspetta un attimo");
        progressDialog.setCanceledOnTouchOutside(false);

        // Recupera l'ID del fumetto dagli argomenti
        if (getArguments() != null) {
            comicsId = getArguments().getString("comicsId");
        }

        loadCategories();
        loadComicsInfo();

        binding.textViewComicsCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });

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

    private void validateData() {
        title = binding.textViewComicsTitle.getText().toString().trim();
        description = binding.textViewComicsDescription.getText().toString().trim();

        if(TextUtils.isEmpty(title)){
            Toast.makeText(getActivity(), "Inserisci il titolo...", Toast.LENGTH_SHORT).show();
            binding.textViewInputLayoutComicsTitle.setError("Titolo richisto");
            binding.textViewInputLayoutComicsTitle.requestFocus();
            //return;
        } else if(TextUtils.isEmpty(description)){
            Toast.makeText(getActivity(), "Inserisci la descrizione...", Toast.LENGTH_SHORT).show();
            binding.textViewInputLayoutComicsDescription.setError("Descrizione richista");
            binding.textViewInputLayoutComicsDescription.requestFocus();
            //return;
        } else if(TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(getActivity(), "Seleziona la categoria...", Toast.LENGTH_SHORT).show();
            binding.textViewInputLayoutComicsCategory.setError("Categoria richista");
            binding.textViewInputLayoutComicsCategory.requestFocus();
            //return;
        } else {
            updateComicsPdf();
        }
    }

    private void updateComicsPdf() {
        Log.d(TAG, "updateComicsPdf: Starting updatinf pdf info to db...");

        progressDialog.setMessage("Updating comics info...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("titolo", ""+title);
        hashMap.put("descrizione", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);

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
                Log.d(TAG, "onFailure: failed to update due to " + e.getMessage());
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
                selectedCategoryId = ""+snapshot.child("categoryId").getValue();
                String description = ""+snapshot.child("descrizione").getValue();
                String title = ""+snapshot.child("titolo").getValue();

                binding.textViewComicsTitle.setText(title);
                binding.textViewComicsDescription.setText(description);

                Log.d(TAG, "onDataChange: Loading Comics Category Info");
                DatabaseReference refComicsCategory = FirebaseDatabase.getInstance().getReference("Categories");
                refComicsCategory.child(selectedCategoryId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();

                        binding.textViewComicsCategory.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void categoryDialog(){
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for(int i=0; i<categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Seleziona una categoria").setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedCategoryId = categoryIdArrayList.get(which);
                selectedCategoryTitle = categoryTitleArrayList.get(which);

                binding.textViewComicsCategory.setText(selectedCategoryTitle);
            }
        }).show();
    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories...");

        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    String id = ""+ds.child("id").getValue();
                    String category = ""+ds.child("category").getValue();
                    
                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);

                    Log.d(TAG, "onDataChange: ID: " + id);
                    Log.d(TAG, "onDataChange: Category: " + category);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
