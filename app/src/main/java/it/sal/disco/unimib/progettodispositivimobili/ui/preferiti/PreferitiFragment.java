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
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentPreferitiBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterPdfComicsFavorite;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsPdfDetailUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsMarvelDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.own.ProfileFragment;

public class PreferitiFragment extends Fragment {

    private FragmentPreferitiBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelPdfComics> pdfArrayList;
    private AdapterPdfComicsFavorite adapterPdfFavorite;
    private static final String TAG = "PreferitiFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPreferitiBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        pdfArrayList = new ArrayList<>();
        adapterPdfFavorite = new AdapterPdfComicsFavorite(getActivity(), pdfArrayList);
        binding.comicsFavoriteRv.setAdapter(adapterPdfFavorite);

        loadFavoriteComics();

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
            if (getActivity() != null) {
                openFragment(new ProfileFragment());
            }
        });

        // Setup item click listener
        adapterPdfFavorite.setOnItemClickListener(model -> {
            if (model.isFromApi()) {
                openComicsMarvelDetailFragment(model);
            } else {
                openComicsPdfDetailUserFragment(model);
            }
        });

        return root;
    }

    private void loadFavoriteComics() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Utenti registrati");
        ref.child(firebaseAuth.getUid()).child("Favorites").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String comicsId = ds.child("comicsId").getValue(String.class);
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
                loadFavoriteApiComics();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gestione degli errori
            }
        });
    }


    private void loadFavoriteApiComics() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Utenti registrati")
                .child(firebaseAuth.getUid())
                .child("Favorites");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) {
                    return; // Exit if the binding is null
                }
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String comicsId = ds.child("comicsId").getValue(String.class);
                    if (comicsId != null) {
                        String titolo = ds.child("titolo").getValue(String.class);
                        String descrizione = ds.child("descrizione").getValue(String.class);
                        String url = ds.child("url").getValue(String.class);

                        ModelPdfComics modelPdf = new ModelPdfComics();
                        modelPdf.setId(comicsId);
                        modelPdf.setTitolo(titolo != null ? titolo : "Unknown Title");
                        modelPdf.setDescrizione(descrizione != null ? descrizione : "No Description Available");
                        modelPdf.setUrl(url != null ? url : "No URL Available");
                        modelPdf.setFromApi(true); // Imposta questo flag per distinguere i dati API

                        if (!pdfArrayList.contains(modelPdf)) {
                            pdfArrayList.add(modelPdf);
                        }
                    }
                }

                adapterPdfFavorite.notifyDataSetChanged();
                binding.subTitleTv.setText("" + pdfArrayList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openComicsPdfDetailUserFragment(String comicsId) {
        ComicsPdfDetailUserFragment comicsPdfDetailUserFragment = new ComicsPdfDetailUserFragment();
        Bundle args = new Bundle();
        args.putString("comicsId", comicsId);
        comicsPdfDetailUserFragment.setArguments(args);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, comicsPdfDetailUserFragment);
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
