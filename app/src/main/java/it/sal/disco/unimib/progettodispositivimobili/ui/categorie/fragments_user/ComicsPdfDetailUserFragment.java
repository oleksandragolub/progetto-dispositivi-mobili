package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user;

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

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.DialogCommentAddBinding;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsPdfDetailUserBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.Constants;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.MyApplication;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterComment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelComment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelPdfComics;

public class ComicsPdfDetailUserFragment extends Fragment {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    private FragmentComicsPdfDetailUserBinding binding;
    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;
    private FirebaseAuth firebaseAuth;
    private String comicsId, comicsTitle, comicsUrl;
    boolean isInMyFavorites = false;
    String comment = "";
    private ProgressDialog progressDialog;
    private ModelPdfComics modelPdfComics;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG_DOWNLOAD, "Permission Granted");
                    MyApplication.downloadComics(getActivity(), "" + comicsId, "" + comicsTitle, "" + comicsUrl);
                } else {
                    Log.d(TAG_DOWNLOAD, "Permission was denied...");
                    Toast.makeText(getActivity(), "Permission was denied...", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComicsPdfDetailUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getArguments() != null && getArguments().containsKey("modelPdfComics")) {
            modelPdfComics = (ModelPdfComics) getArguments().getSerializable("modelPdfComics");
            if (modelPdfComics != null) {
                comicsId = modelPdfComics.getId();
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

        binding.buttonBackUser.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.readComicsBtn.setOnClickListener(v -> {
            ComicsPdfViewUserFragment comicsPdfViewUserFragment = new ComicsPdfViewUserFragment();
            Bundle args = new Bundle();
            args.putString("comicsId", comicsId);
            comicsPdfViewUserFragment.setArguments(args);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.nav_host_fragment, comicsPdfViewUserFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        binding.downloadComicsBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    MyApplication.downloadComics(getActivity(), "" + comicsId, "" + comicsTitle, "" + comicsUrl);
                } else {
                    requestManageExternalStoragePermission();
                }
            } else {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    MyApplication.downloadComics(getActivity(), "" + comicsId, "" + comicsTitle, "" + comicsUrl);
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

    private void loadComments() {
        commentArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child(comicsId).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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

            }
        });
    }

    private void addCommentDialog() {
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(getActivity()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment = commentAddBinding.commentEt.getText().toString().trim();

                if (TextUtils.isEmpty(comment)) {
                    Toast.makeText(getActivity(), "Inserisci il tuo commento...", Toast.LENGTH_SHORT).show();
                } else {
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });
    }

    private void addComment() {
        progressDialog.setMessage("Adding comment...");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestamp);
        hashMap.put("comicsId", "" + comicsId);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("comment", "" + comment);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child(comicsId).child("Comments").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getActivity(), "Comment Added...", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Failed to add comment due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
                    MyApplication.downloadComics(getActivity(), "" + comicsId, "" + comicsTitle, "" + comicsUrl);
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
            MyApplication.loadCategory(modelPdfComics.getCategoryId(), binding.collezioniTv);
            MyApplication.loadPdfFromUrlSinglePage(comicsUrl, comicsTitle, binding.pdfView, binding.progressBar, binding.pagesTv);
            MyApplication.loadPdfSize(comicsUrl, comicsTitle, binding.sizeTv);
            MyApplication.loadPdfPageCount(getActivity(), comicsUrl, binding.pagesTv);

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
                    comicsTitle = "" + snapshot.child("titolo").getValue();
                    String description = "" + snapshot.child("descrizione").getValue();
                    String categoryId = "" + snapshot.child("categoryId").getValue();
                    String viewsCount = "" + snapshot.child("viewsCount").getValue();
                    String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                    comicsUrl = "" + snapshot.child("url").getValue();
                    String timestamp = "" + snapshot.child("timestamp").getValue();

                    binding.downloadComicsBtn.setVisibility(View.VISIBLE);

                    String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));
                    MyApplication.loadCategory(categoryId, binding.collezioniTv);
                    MyApplication.loadPdfFromUrlSinglePage(comicsUrl, comicsTitle, binding.pdfView, binding.progressBar, binding.pagesTv);
                    MyApplication.loadPdfSize(comicsUrl, comicsTitle, binding.sizeTv);
                    MyApplication.loadPdfPageCount(getActivity(), comicsUrl, binding.pagesTv);

                    binding.titleTv.setText(comicsTitle);
                    binding.descriptionTv.setText(description);
                    binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                    binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));
                    binding.yearTv.setText(date);
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
                if (binding != null) { // Ensure binding is not null
                    isInMyFavorites = snapshot.exists();
                    if (isInMyFavorites) {
                        binding.favoriteComicsBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.baseline_favorite_24_white, 0, 0);
                        binding.favoriteComicsBtn.setText("Rimuovi");
                    } else {
                        binding.favoriteComicsBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.baseline_favorite_border_24_white, 0, 0);
                        binding.favoriteComicsBtn.setText("Aggiungi");
                    }
                } else {
                    Log.e(TAG_DOWNLOAD, "Binding is null in checkIsFavorite");
                }
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
        binding = null;
    }
}