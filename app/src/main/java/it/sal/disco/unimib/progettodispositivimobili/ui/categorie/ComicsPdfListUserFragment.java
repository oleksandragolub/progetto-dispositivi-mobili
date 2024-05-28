package it.sal.disco.unimib.progettodispositivimobili.ui.categorie;

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
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsPdfListUserBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterPdfComicsUser;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class ComicsPdfListUserFragment extends Fragment {

    private FragmentComicsPdfListUserBinding binding;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelPdfComics> pdfArrayList;

    private AdapterPdfComicsUser adapterPdfUser;

    private String categoryId, categoryTitle, categoryDescription;

    private static final String TAG = "PDF_LIST_TAG";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentComicsPdfListUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        if (getActivity() != null) {
            //Intent intent = getActivity().getIntent();
            categoryId = getArguments().getString("categoryId");
            categoryTitle = getArguments().getString("categoryTitle");
            //categoryDescription = getArguments().getString("categoryTitle");
            binding.subTitleTv.setText(categoryTitle);
        }

        loadPdfList();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterPdfUser.getFilter().filter(s);
                }catch (Exception e){
                    Log.d(TAG, "onTextChanged: " + e.getMessage());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null) {
                    openFragment(new CategoryUserFragment());
                }
            }
        });

        return root;
}


    private void loadPdfList() {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.orderByChild("categoryId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) {
                    return; // Esci se il frammento è distrutto
                }

                pdfArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPdfComics model = ds.getValue(ModelPdfComics.class);
                    pdfArrayList.add(model);

                    Log.d(TAG, "onDataChanged: " + model.getId() + " " + model.getTitolo());
                }
                adapterPdfUser = new AdapterPdfComicsUser(getActivity(), pdfArrayList);
                binding.comicsRv.setAdapter(adapterPdfUser);
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
