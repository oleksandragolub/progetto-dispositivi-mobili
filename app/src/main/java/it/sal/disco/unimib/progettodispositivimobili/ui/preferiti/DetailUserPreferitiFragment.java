package it.sal.disco.unimib.progettodispositivimobili.ui.preferiti;

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

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentDetailUserPreferitiBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterPdfComicsFavorite;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsMarvelDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsPdfDetailUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.other.DetailUserProfileFragment;

public class DetailUserPreferitiFragment extends Fragment {

    private FragmentDetailUserPreferitiBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelPdfComics> pdfArrayList;
    private AdapterPdfComicsFavorite adapterPdfFavorite;
    private String userId;
    private static final String TAG = "DetailUserPreferitiFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDetailUserPreferitiBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        // Recupera l'ID dell'utente passato come argomento
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

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
                for (DataSnapshot ds: snapshot.getChildren()){
                    String comicsId = ""+ds.child("comicsId").getValue();
                    if (comicsId != null) {
                        ModelPdfComics modelPdf = new ModelPdfComics();
                        modelPdf.setId(comicsId);

                        if (ds.hasChild("titolo") || ds.hasChild("descrizione") || ds.hasChild("url")) {
                            modelPdf.setFromApi(true);
                            modelPdf.setTitolo(ds.child("titolo").getValue(String.class));
                            modelPdf.setDescrizione(ds.child("descrizione").getValue(String.class));
                            modelPdf.setUrl(ds.child("url").getValue(String.class));
                        }
                        pdfArrayList.add(modelPdf);
                    }
                }

                binding.subTitleTv.setText(""+pdfArrayList.size());

                adapterPdfFavorite = new AdapterPdfComicsFavorite(getActivity(), pdfArrayList);
                binding.comicsFavoriteRv.setAdapter(adapterPdfFavorite);

                adapterPdfFavorite.setOnItemClickListener(model -> {
                    if (model.isFromApi()) {
                        openComicsMarvelDetailFragment(model);
                    } else {
                        openComicsPdfDetailUserFragment(model);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gestione degli errori
            }
        });
    }

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

    private void openComicsPdfDetailUserFragment(ModelPdfComics model) {
        ComicsPdfDetailUserFragment comicsPdfDetailUserFragment = new ComicsPdfDetailUserFragment();
        Bundle args = new Bundle();
        args.putSerializable("modelPdfComics", model);
        comicsPdfDetailUserFragment.setArguments(args);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, comicsPdfDetailUserFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openComicsMarvelDetailFragment(ModelPdfComics model) {
        ComicsMarvelDetailFragment comicsMarvelDetailFragment = new ComicsMarvelDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("comic", model);  // Cambia "modelPdfComics" in "comic"
        comicsMarvelDetailFragment.setArguments(args);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, comicsMarvelDetailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}