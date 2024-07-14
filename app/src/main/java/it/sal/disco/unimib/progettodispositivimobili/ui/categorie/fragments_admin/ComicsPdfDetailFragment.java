package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.DialogCommentAddBinding;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsPdfDetailBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.Constants;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.MyApplication;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterComment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelComment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class ComicsPdfDetailFragment extends Fragment {

    private static final String TAG_DOWNLOAD = "ComicsPdfDetailFragment";
    private FragmentComicsPdfDetailBinding binding;
    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;
    private FirebaseAuth firebaseAuth;
    private String comicsId, comicsTitle, comicsUrl;
    private boolean isInMyFavorites = false;
    private String comment = "";
    private ProgressDialog progressDialog;
    private ModelPdfComics modelPdfComics;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG_DOWNLOAD, "Permission Granted");
                    MyApplication.downloadComics(getActivity(), comicsId, comicsTitle, comicsUrl);
                } else {
                    Log.d(TAG_DOWNLOAD, "Permission was denied...");
                    Toast.makeText(getActivity(), "Permission was denied...", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentComicsPdfDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getArguments() != null) {
            if (getArguments().containsKey("modelPdfComics")) {
                modelPdfComics = (ModelPdfComics) getArguments().getSerializable("modelPdfComics");
                if (modelPdfComics != null) {
                    comicsId = modelPdfComics.getId();
                    loadComicsDetailsFromModel(modelPdfComics);
                }
            } else if (getArguments().containsKey("comicsId")) {
                comicsId = getArguments().getString("comicsId");
                loadComicsDetails();
            }
        }

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Aspetta per favore");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorite();
        }

        if (comicsId != null) {
            loadComicsDetails();
            loadComments();
            MyApplication.incrementComicsViewCoint(comicsId);
        } else {
            Toast.makeText(getActivity(), "Error: Comics ID is null", Toast.LENGTH_SHORT).show();
        }

        binding.buttonBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.readComicsBtn.setOnClickListener(v -> {
            ComicsPdfViewFragment comicsPdfViewFragment = new ComicsPdfViewFragment();
            Bundle args = new Bundle();
            args.putString("comicsId", comicsId);
            comicsPdfViewFragment.setArguments(args);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.nav_host_fragment, comicsPdfViewFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        binding.downloadComicsBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    MyApplication.downloadComics(getActivity(), comicsId, comicsTitle, comicsUrl);
                } else {
                    requestManageExternalStoragePermission();
                }
            } else {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    MyApplication.downloadComics(getActivity(), comicsId, comicsTitle, comicsUrl);
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        binding.favoriteComicsBtn.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(getActivity(), "Non sei autentificato!", Toast.LENGTH_SHORT).show();
            } else {
                if (isInMyFavorites) {
                    MyApplication.removeFromFavorite(getActivity(), comicsId);
                } else {
                    MyApplication.addToFavorite(getActivity(), comicsId);
                }
            }
        });

        binding.addCommentBtn.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(getActivity(), "Non sei autentificato!", Toast.LENGTH_SHORT).show();
            } else {
                addCommentDialog();
            }
        });

        return root;
    }

    private void loadComicsDetailsFromModel(ModelPdfComics model) {
        comicsTitle = model.getTitolo();
        comicsUrl = model.getUrl();

        binding.downloadComicsBtn.setVisibility(View.VISIBLE);

        binding.titleTv.setText(comicsTitle);
        binding.descriptionTv.setText(model.getDescrizione());
        binding.viewsTv.setText(String.valueOf(model.getViewsCount()));
        binding.downloadsTv.setText(String.valueOf(model.getDownloadsCount()));
        binding.yearTv.setText(model.getYear());
        binding.linguaTv.setText(model.getLanguage());
        binding.pagesTv.setText(String.valueOf(model.getPages()));
        loadGenresAndCollections(model);
        MyApplication.loadPdfCoverFromUrl(comicsUrl, comicsTitle, binding.pdfView, binding.progressBar);
        MyApplication.loadPdfSize(comicsUrl, comicsTitle, binding.sizeTv);
    }

    private void loadComments() {
        commentArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child(comicsId).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return; // Check for null binding
                commentArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelComment model = ds.getValue(ModelComment.class);
                    commentArrayList.add(model);
                }
                adapterComment = new AdapterComment(getActivity(), commentArrayList);
                binding.commentsRv.setAdapter(adapterComment);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if necessary
            }
        });
    }

    private void addCommentDialog() {
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(getActivity()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        commentAddBinding.backBtn.setOnClickListener(v -> alertDialog.dismiss());

        commentAddBinding.submitBtn.setOnClickListener(v -> {
            comment = commentAddBinding.commentEt.getText().toString().trim();

            if (TextUtils.isEmpty(comment)) {
                Toast.makeText(getActivity(), "Inserisci il tuo commento...", Toast.LENGTH_SHORT).show();
            } else {
                alertDialog.dismiss();
                addComment();
            }
        });
    }

    private void addComment() {
        progressDialog.setMessage("Adding comment...");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", timestamp);
        hashMap.put("comicsId", comicsId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("comment", comment);
        hashMap.put("uid", firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child(comicsId).child("Comments").child(timestamp).setValue(hashMap).addOnSuccessListener(unused -> {
            Toast.makeText(getActivity(), "Comment Added...", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getActivity(), "Failed to add comment due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void requestManageExternalStoragePermission() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:" + getActivity().getPackageName()));
            startActivityForResult(intent, Constants.MANAGE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(intent, Constants.MANAGE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.MANAGE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    MyApplication.downloadComics(getActivity(), comicsId, comicsTitle, comicsUrl);
                } else {
                    Toast.makeText(getActivity(), "Permission was denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void loadComicsDetails() {
        if (modelPdfComics != null) {
            comicsTitle = modelPdfComics.getTitolo();
            comicsUrl = modelPdfComics.getUrl();

            binding.downloadComicsBtn.setVisibility(View.VISIBLE);

            String date = MyApplication.formatTimestamp(modelPdfComics.getTimestamp());
            MyApplication.loadPdfSize(comicsUrl, comicsTitle, binding.sizeTv);

            binding.titleTv.setText(comicsTitle);
            binding.descriptionTv.setText(modelPdfComics.getDescrizione());
            binding.viewsTv.setText(String.valueOf(modelPdfComics.getViewsCount()));
            binding.downloadsTv.setText(String.valueOf(modelPdfComics.getDownloadsCount()));
            binding.yearTv.setText(date);
        } else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
            ref.child(comicsId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding == null) return; // Check for null binding
                    comicsTitle = "" + snapshot.child("titolo").getValue();
                    String description = "" + snapshot.child("descrizione").getValue();
                    String viewsCount = "" + snapshot.child("viewsCount").getValue();
                    String pages = "" + snapshot.child("pages").getValue();
                    String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                    comicsUrl = "" + snapshot.child("url").getValue();
                    String timestamp = "" + snapshot.child("timestamp").getValue();
                    String year = "" + snapshot.child("year").getValue();
                    String language = "" + snapshot.child("language").getValue();
                    List<String> collections = new ArrayList<>();
                    List<String> genres = new ArrayList<>();
                    for (DataSnapshot ds : snapshot.child("collections").getChildren()) {
                        collections.add(ds.getValue(String.class));
                    }
                    for (DataSnapshot ds : snapshot.child("genres").getChildren()) {
                        genres.add(ds.getValue(String.class));
                    }

                    binding.downloadComicsBtn.setVisibility(View.VISIBLE);

                    MyApplication.loadPdfSize(comicsUrl, comicsTitle, binding.sizeTv);

                    binding.titleTv.setText(comicsTitle);
                    binding.descriptionTv.setText(description);
                    binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                    binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));
                    binding.yearTv.setText(year);
                    binding.linguaTv.setText(language);
                    binding.pagesTv.setText(pages);
                    displayGenresAndCollections(collections, genres);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG_DOWNLOAD, "loadComicsDetails: onCancelled", error.toException());
                }
            });
        }
    }

    private void checkIsFavorite() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Utenti registrati");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(comicsId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return; // Check for null binding
                isInMyFavorites = snapshot.exists();
                if (isInMyFavorites) {
                    binding.favoriteComicsBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.baseline_favorite_24_white, 0, 0);
                    binding.favoriteComicsBtn.setText("Rimuovi");
                } else {
                    binding.favoriteComicsBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.baseline_favorite_border_24_white, 0, 0);
                    binding.favoriteComicsBtn.setText("Aggiungi");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG_DOWNLOAD, "checkIsFavorite: onCancelled", error.toException());
            }
        });
    }

    private void loadGenresAndCollections(ModelPdfComics model) {
        List<String> collections = model.getCollections();
        List<String> genres = model.getGenres();

        if (collections == null) {
            collections = new ArrayList<>();
        }

        if (genres == null) {
            genres = new ArrayList<>();
        }

        displayGenresAndCollections(collections, genres);
    }

    private void displayGenresAndCollections(List<String> collections, List<String> genres) {
        StringBuilder collectionsText = new StringBuilder();
        for (String collection : collections) {
            collectionsText.append(collection).append(", ");
        }
        if (collectionsText.length() > 0) {
            collectionsText.setLength(collectionsText.length() - 2); // Remove last comma
        }
        binding.collezioniTv.setText(collectionsText.toString());

        StringBuilder genresText = new StringBuilder();
        for (String genre : genres) {
            genresText.append(genre).append(", ");
        }
        if (genresText.length() > 0) {
            genresText.setLength(genresText.length() - 2); // Remove last comma
        }
        binding.generiTv.setText(genresText.toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
