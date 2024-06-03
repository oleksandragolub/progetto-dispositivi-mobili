package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsUserBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterPdfComicsUser;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;



public class ComicsUserFragment extends Fragment {

    private static final String TAG = "COMICS_USER_TAG";
    private String categoryId, category, uid;
    private ArrayList<ModelPdfComics> pdfArrayList;
    private AdapterPdfComicsUser adapterPdfUser;

    private FragmentComicsUserBinding binding;
    private PDFView pdfView;
    private FirebaseAuth firebaseAuth;

    public static ComicsUserFragment newInstance(String categoryId, String category, String uid) {
        ComicsUserFragment fragment = new ComicsUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComicsUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        Log.d(TAG, "onCreateView: Category: " + category);
        if (category.equals("All")) {
            loadAllComics();
        } else if (category.equals("Most Viewed")) {
            loadMostViewedDownloadedComics("viewsCount");
        } else if (category.equals("Most Downloaded")) {
            loadMostViewedDownloadedComics("downloadsCount");
        } else {
            loadCategorizedComics();
        }

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (adapterPdfUser != null) {
                        adapterPdfUser.getFilter().filter(s);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onTextChanged: " + e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return root;
    }

    private void loadCategorizedComics() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (binding == null) return;
                        pdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelPdfComics model = ds.getValue(ModelPdfComics.class);
                            pdfArrayList.add(model);
                        }
                        adapterPdfUser = new AdapterPdfComicsUser(getContext(), pdfArrayList);
                        binding.comicsRv.setAdapter(adapterPdfUser);
                        adapterPdfUser.setOnItemClickListener(new AdapterPdfComicsUser.OnItemClickListenerUser() {
                            @Override
                            public void onItemClick(ModelPdfComics model) {
                                openComicsPdfDetailUserFragment(model.getId());
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void openComicsPdfDetailUserFragment(String comicsId) {
        ComicsPdfDetailUserFragment comicsPdfDetailUserFragment = new ComicsPdfDetailUserFragment();
        Bundle args = new Bundle();
        args.putString("comicsId", comicsId);
        comicsPdfDetailUserFragment.setArguments(args);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, comicsPdfDetailUserFragment);
        transaction.addToBackStack(null); // Aggiungi il frammento al back stack
        transaction.commit();
    }

    private void loadMostViewedDownloadedComics(String orderBy) {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.orderByChild(orderBy).limitToLast(10)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (binding == null) return;
                        pdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelPdfComics model = ds.getValue(ModelPdfComics.class);
                            pdfArrayList.add(model);
                        }
                        adapterPdfUser = new AdapterPdfComicsUser(getContext(), pdfArrayList);
                        binding.comicsRv.setAdapter(adapterPdfUser);
                        adapterPdfUser.setOnItemClickListener(new AdapterPdfComicsUser.OnItemClickListenerUser() {
                            @Override
                            public void onItemClick(ModelPdfComics model) {
                                openComicsPdfDetailUserFragment(model.getId());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void loadAllComics() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                pdfArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPdfComics model = ds.getValue(ModelPdfComics.class);
                    pdfArrayList.add(model);
                }
                adapterPdfUser = new AdapterPdfComicsUser(getContext(), pdfArrayList);
                binding.comicsRv.setAdapter(adapterPdfUser);

                adapterPdfUser.setOnItemClickListener(new AdapterPdfComicsUser.OnItemClickListenerUser() {
                    @Override
                    public void onItemClick(ModelPdfComics model) {
                        openComicsPdfDetailUserFragment(model.getId());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pdfView != null) {
            pdfView.recycle();  // Rilascia le risorse del PDFView
        }
        binding = null;
    }
}