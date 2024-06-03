package it.sal.disco.unimib.progettodispositivimobili.ui.preferiti;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentDetailUserPreferitiBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterPdfComicsFavorite;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.other.DetailUserProfileFragment;

public class DetailUserPreferitiFragment extends Fragment {

    private FragmentDetailUserPreferitiBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelPdfComics> pdfArrayList;
    private AdapterPdfComicsFavorite adapterPdfFavorite;
    private String userId;
    private static final String TAG = "PDF_LIST_TAG";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDetailUserPreferitiBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Recupera l'ID dell'utente passato come argomento
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        firebaseAuth = FirebaseAuth.getInstance();

        loadUserFavorites(userId);

        binding.searchFavoriteEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Non fare nulla
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterPdfFavorite.getFilter().filter(s);
                } catch (Exception e) {
                    Log.d(TAG, "onTextChanged: " + e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Non fare nulla
            }
        });

        binding.buttonBack.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new DetailUserProfileFragment());
            }
        });


        return root;
    }

    private void loadUserFavorites(String userId) {
        pdfArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Utenti registrati").child(userId).child("Favorites");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    String comicsId = ""+ds.child("comicsId").getValue();

                    ModelPdfComics modelPdf = new ModelPdfComics();
                    modelPdf.setId(comicsId);

                    pdfArrayList.add(modelPdf);
                }

                binding.subTitleTv.setText(""+pdfArrayList.size());

                adapterPdfFavorite = new AdapterPdfComicsFavorite(getActivity(), pdfArrayList);
                binding.comicsFavoriteRv.setAdapter(adapterPdfFavorite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gestione degli errori
            }
        });
    }


   /* private void openFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }*/

    private void openFragment(Fragment fragment){
        Bundle bundle = new Bundle();
        bundle.putString("user2", userId); // Assicurati di passare il profileUserId al nuovo fragment
        fragment.setArguments(bundle);

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
