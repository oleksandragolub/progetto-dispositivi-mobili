package it.sal.disco.unimib.progettodispositivimobili.ui.categorie;

import android.content.Intent;
import android.os.Bundle;
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

import it.sal.disco.unimib.progettodispositivimobili.MyApplication;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentCategoryAddBinding;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsPdfDetailBinding;

public class ComicsPdfDetailFragment extends Fragment {
    private FragmentComicsPdfDetailBinding binding;

    private FirebaseAuth firebaseAuth;

    String comicsId;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentComicsPdfDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

     /*   Intent intent = getIntent();
        comicsId = intent.getStringExtra("comicsId");*/

        if (getArguments() != null) {
            comicsId = getArguments().getString("comicsId");
        }

        loadComicsDetails();
        MyApplication.incrementComicsViewCoint(comicsId);


        binding.buttonBack.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new CategoryAddAdminFragment());
            }
        });

        return root;
    }

    private void loadComicsDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child(comicsId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String title = ""+snapshot.child("titolo").getValue();
                String description = ""+snapshot.child("descrizione").getValue();
                String categoryId = ""+snapshot.child("categoryId").getValue();
                String viewsCount = ""+snapshot.child("viewsCount").getValue();
                String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                String url = ""+snapshot.child("url").getValue();
                String timestamp = ""+snapshot.child("timestamp").getValue();

                String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));
                MyApplication.loadCategory(""+categoryId, binding.categoryTv);
                MyApplication.loadPdfFromUrlSinglePage(""+url, ""+title, binding.pdfView, binding.progressBar);
                MyApplication.loadPdfSize(""+url, ""+title, binding.sizeTv);

                binding.titleTv.setText(title);
                binding.descriptionTv.setText(description);
                binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));
                binding.dateTv.setText(date);



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
