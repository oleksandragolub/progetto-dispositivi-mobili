package it.sal.disco.unimib.progettodispositivimobili.ui.characters;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
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
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsMarvelDetailBinding;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterComment;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelComment;


public class ComicsMarvelDetailFragment extends Fragment {

    private FragmentComicsMarvelDetailBinding binding;
    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;
    private String comicsId;
    private String comment = "";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inizializza il binding
        binding = FragmentComicsMarvelDetailBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Inizializza le variabili
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getActivity());

        // Ottieni i dati dal bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            String thumbnailUrl = bundle.getString("thumbnailUrl");
            String title = bundle.getString("title");
            String description = bundle.getString("description");
            comicsId = bundle.getString("comicId");

            Glide.with(this).load(thumbnailUrl).into(binding.comicThumbnail);
            binding.comicTitle.setText(title);
            binding.comicDescription.setText(description);
        }

        view.findViewById(R.id.buttonBackUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(getActivity(), "Non sei autentificato!", Toast.LENGTH_SHORT).show();
                } else {
                    addCommentDialog();
                }
            }
        });

        // Carica i commenti
        loadComments();

        return view;
    }

    private void loadComments() {
        commentArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ComicsMarvel");
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

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ComicsMarvel");
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
}
