package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user;

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
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsPdfViewUserBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.Constants;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

import java.io.File;
import java.io.IOException;

public class ComicsPdfViewUserFragment extends Fragment {
    private FragmentComicsPdfViewUserBinding binding;
    private String comicsId, comicsTitle, comicsUrl;
    private static final String TAG = "ComicsPdfViewUserFragment";
    private static final int MAX_RETRY_COUNT = 3;
    private int retryCount = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComicsPdfViewUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getArguments() != null) {
            comicsId = getArguments().getString("comicsId");
            comicsTitle = getArguments().getString("comicsTitle");
            comicsUrl = getArguments().getString("comicsUrl");
            if (comicsId == null || comicsTitle == null || comicsUrl == null) {
                Log.e(TAG, "Missing arguments");
                Toast.makeText(getActivity(), "Error: Missing arguments", Toast.LENGTH_SHORT).show();
                return root;
            }
        } else {
            Log.e(TAG, "getArguments() returned null");
            Toast.makeText(getActivity(), "Error: Arguments are null", Toast.LENGTH_SHORT).show();
            return root;
        }

        Log.d(TAG, "onCreate: ComicsId: " + comicsId);
        binding.toolbarTitleTv.setText(comicsTitle);
        loadComicsFromUrl(comicsUrl);

        binding.buttonBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return root;
    }

    private void loadComicsFromUrl(String pdfUrl) {
        Log.d(TAG, "loadComicsFromUrl: Get PDF from storage");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);

        try {
            File localFile = File.createTempFile("comics", "pdf");

            reference.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.pdfView.fromFile(localFile)
                        .enableSwipe(true)
                        .swipeHorizontal(true)
                        .pageSnap(true)
                        .pageFling(true)
                        .autoSpacing(true)
                        .pageFitPolicy(FitPolicy.HEIGHT)
                        .onPageChange((page, pageCount) -> {
                            int currentPage = page + 1;
                            binding.toolbarSubtitleTv.setText(currentPage + "/" + pageCount);
                            Log.d(TAG, "onPageChanged: " + currentPage + "/" + pageCount);
                            updateFirebaseWithPageCount(comicsId, pageCount);
                        })
                        .onError(t -> {
                            Log.d(TAG, "onError: " + t.getMessage());
                            Toast.makeText(getActivity(), "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        })
                        .onPageError((page, t) -> {
                            Log.d(TAG, "onPageError: " + t.getMessage());
                            Toast.makeText(getActivity(), "Error on page " + page + " " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }).load();
                binding.progressBar.setVisibility(View.GONE);
            }).addOnFailureListener(e -> {
                Log.d(TAG, "onFailure: " + e.getMessage());
                binding.progressBar.setVisibility(View.GONE);
                handleFailure(e, pdfUrl);
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error creating temp file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFailure(Exception e, String pdfUrl) {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            Toast.makeText(getActivity(), "Retrying... (" + retryCount + ")", Toast.LENGTH_SHORT).show();
            loadComicsFromUrl(pdfUrl);
        } else {
            Toast.makeText(getActivity(), "Failed to load PDF after multiple attempts: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateFirebaseWithPageCount(String comicId, int pages) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics").child(comicId);
        ref.child("pages").setValue(pages);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

