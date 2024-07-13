package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsPdfViewBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.Constants;

public class ComicsPdfViewFragment extends Fragment {
    private FragmentComicsPdfViewBinding binding;

    private FirebaseAuth firebaseAuth;

    private String comicsId;

    private static final String TAG = "ComicsPdfViewFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentComicsPdfViewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            comicsId = getArguments().getString("comicsId");
        }
        Log.d(TAG, "onCreate: ComicsId: " + comicsId);

        loadComicsDetails();

        binding.buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                openFragment(new ComicsPdfDetailFragment(), comicsId);
            }
        });

        return root;
    }

    private void loadComicsDetails() {
        Log.d(TAG, "loadComicsDetails: Get Pdf URL...");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child(comicsId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pdfUrl = ""+snapshot.child("url").getValue();
                Log.d(TAG, "onDataChange: PDF URL: " + pdfUrl);
                String title = ""+snapshot.child("titolo").getValue();
                Log.d(TAG, "onDataChange: Title: " + title);

                binding.toolbarTitleTv.setText(title);

                loadComicsFromUrl(pdfUrl);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadComicsFromUrl(String pdfUrl) {
        Log.d(TAG, "loadComicsFromUrl: Get PDF from storage");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                binding.progressBar.setVisibility(View.GONE);
                binding.pdfView.fromBytes(bytes).swipeHorizontal(false).onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        int currentPage = (page + 1);
                        binding.toolbarSubtitleTv.setText(currentPage + "/" + pageCount);
                        Log.d(TAG, "onPageChanged: " + currentPage + "/" + pageCount);

                        updateFirebaseWithPageCount(comicsId, pageCount);
                    }
                }).onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, "onError: " + t.getMessage());
                        Toast.makeText(getActivity(), ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).onPageError(new OnPageErrorListener() {
                    @Override
                    public void onPageError(int page, Throwable t) {
                        Log.d(TAG, "onPageError: " + t.getMessage());
                        Toast.makeText(getActivity(), "Error on page "+ page + " " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).load();
                binding.progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateFirebaseWithPageCount(String comicId, int pages) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics").child(comicId);
        ref.child("pages").setValue(pages);
    }

    private void openFragment(Fragment fragment, String comicsId) {
        Bundle args = new Bundle();
        args.putString("comicsId", comicsId);
        fragment.setArguments(args);

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