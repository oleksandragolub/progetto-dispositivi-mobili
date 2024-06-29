package it.sal.disco.unimib.progettodispositivimobili.ui.characters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.DialogCommentAddBinding;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsMarvelDetailBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.Constants;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.MyApplication;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterComment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelComment;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.archieve.ApiClient;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.archieve.ComicsApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComicsMarvelDetailFragment extends Fragment {
    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    private FragmentComicsMarvelDetailBinding binding;
    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;
    private String comicsId;
    private String comment = "";
    boolean isInMyFavorites = false;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private String comicsTitle;
    private String comicsUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentComicsMarvelDetailBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getActivity());

        Bundle bundle = getArguments();
        if (bundle != null) {
            String thumbnailUrl = bundle.getString("thumbnailUrl");
            String title = bundle.getString("title");
            String description = bundle.getString("description");
            comicsId = bundle.getString("comicId");

            Glide.with(this).load(thumbnailUrl).into(binding.comicThumbnail);
            binding.comicTitle.setText(title);
            binding.comicDescription.setText(description);

            comicsTitle = title; // Save the title for download

            // Fetch the PDF URL and other details
            initializeAndFetchComicDetails(comicsId);
        }

        view.findViewById(R.id.buttonBackUser).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.readComicsBtn.setOnClickListener(v -> {
            if (comicsUrl != null && !comicsUrl.isEmpty()) {
                openPdfViewer(comicsUrl);
                // Increment view count when the comic is read
                MyApplication.incrementMarvelComicsViewCount(comicsId);
            } else {
                Toast.makeText(getActivity(), "Comic URL not available. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.downloadComicsBtn.setOnClickListener(v -> {
            if (comicsUrl == null || comicsUrl.isEmpty()) {
                Toast.makeText(getActivity(), "Invalid comics URL, please try again later.", Toast.LENGTH_SHORT).show();
                return;
            }
            MyApplication.downloadMarvelComics(getActivity(), comicsId, comicsTitle, comicsUrl);
        });

        binding.addCommentBtn.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(getActivity(), "Non sei autentificato!", Toast.LENGTH_SHORT).show();
            } else {
                addCommentDialog();
            }
        });

        binding.favoriteComicsBtn.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(getActivity(), "Non sei autentificato!", Toast.LENGTH_SHORT).show();
            } else {
                if (isInMyFavorites) {
                    removeFromFavorites();
                } else {
                    addToFavorites();
                }
            }
        });

        loadComments();
        checkIsFavorite();

        return view;
    }

    private void initializeAndFetchComicDetails(String comicId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ComicsMarvel").child(comicId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild("viewsCount")) {
                    ref.child("viewsCount").setValue(0);
                }
                if (!snapshot.hasChild("downloadsCount")) {
                    ref.child("downloadsCount").setValue(0);
                }
                if (!snapshot.hasChild("size")) {
                    ref.child("size").setValue(0);
                }
                if (!snapshot.hasChild("pages")) {
                    ref.child("pages").setValue(0);
                }
                // Now fetch the comic details
                fetchComicDetails(comicId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE_INIT", "Failed to initialize counts for comicId: " + comicId + " due to " + error.getMessage());
            }
        });
    }

    private void fetchComicDetails(String comicId) {
        ComicsApi apiService = ApiClient.getClient().create(ComicsApi.class);
        Call<JsonObject> call = apiService.getComicPdf(comicId);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonObject = response.body();
                    Log.d("API_RESPONSE", jsonObject.toString());

                    if (jsonObject.has("pdf_url")) {
                        comicsUrl = jsonObject.get("pdf_url").getAsString();
                        new DownloadFileTask().execute(comicsUrl);  // Download the PDF file to get the page count
                    } else {
                        Log.e("API_RESPONSE", "pdf_url key is missing");
                        comicsUrl = "";
                    }

                    String viewsCount = "N/A";
                    if (jsonObject.has("viewsCount")) {
                        viewsCount = jsonObject.get("viewsCount").getAsString();
                    }

                    String downloadsCount = "N/A";
                    if (jsonObject.has("downloadsCount")) {
                        downloadsCount = jsonObject.get("downloadsCount").getAsString();
                    }

                    String size = "N/A";
                    if (jsonObject.has("size")) {
                        size = jsonObject.get("size").getAsString();
                    }

                    String pages = "N/A";
                    if (jsonObject.has("pages")) {
                        pages = jsonObject.get("pages").getAsString();
                    }

                    Log.d("API_RESPONSE", "Views: " + viewsCount + ", Downloads: " + downloadsCount + ", Size: " + size + ", Pages: " + pages);

                    binding.viewsTv.setText(viewsCount);
                    binding.downloadsTv.setText(downloadsCount);
                    binding.sizeTv.setText(size);
                    binding.pagesTv.setText(pages);

                    // Fetch additional details from Firebase if necessary
                    fetchAdditionalComicDetailsFromFirebase(comicId);
                } else {
                    String errorMessage = response.code() == 500 ? "Server error occurred" : "Service error occurred, code: " + response.code();
                    showAlert(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showAlert(getString(R.string.service_error) + " " + t.getMessage());
            }
        });
    }

    private void fetchAdditionalComicDetailsFromFirebase(String comicId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ComicsMarvel").child(comicId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String viewsCount = snapshot.hasChild("viewsCount") ? snapshot.child("viewsCount").getValue().toString() : "N/A";
                    String downloadsCount = snapshot.hasChild("downloadsCount") ? snapshot.child("downloadsCount").getValue().toString() : "N/A";
                    String size = snapshot.hasChild("size") ? snapshot.child("size").getValue().toString() : "N/A";
                    String pages = snapshot.hasChild("pages") ? snapshot.child("pages").getValue().toString() : "N/A";

                    binding.viewsTv.setText(viewsCount);
                    binding.downloadsTv.setText(downloadsCount);
                    binding.sizeTv.setText(size);
                    binding.pagesTv.setText(pages);
                } else {
                    binding.viewsTv.setText("N/A");
                    binding.downloadsTv.setText("N/A");
                    binding.sizeTv.setText("N/A");
                    binding.pagesTv.setText("N/A");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showAlert(getString(R.string.service_error) + " " + error.getMessage());
            }
        });
    }

    private void showAlert(String message) {
        if (getActivity() == null) {
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setMessage(message);
        alert.setPositiveButton(R.string.close, null);
        alert.show();
    }

    private void openPdfViewer(String pdfUrl) {
        ComicsMarvelViewFragment comicsMarvelViewFragment = new ComicsMarvelViewFragment();
        Bundle args = new Bundle();
        args.putString("pdfUrl", pdfUrl);
        args.putString("title", comicsTitle);
        comicsMarvelViewFragment.setArguments(args);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, comicsMarvelViewFragment);
        transaction.addToBackStack(null);
        transaction.commit();
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

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ComicsMarvel");
        ref.child(comicsId).child("Comments").child(timestamp).setValue(hashMap).addOnSuccessListener(unused -> {
            Toast.makeText(getActivity(), "Comment Added...", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getActivity(), "Failed to add comment due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }



    // Inner class to download PDF file and get the page count and file size
    private class DownloadFileTask extends AsyncTask<String, Void, File> {
        @Override
        protected File doInBackground(String... strings) {
            String fileUrl = strings[0];
            File pdfFile = null;
            try {
                pdfFile = new File(getActivity().getCacheDir(), "downloaded.pdf");
                if (pdfFile.exists()) {
                    pdfFile.delete();
                }
                pdfFile.createNewFile();

                URL url = new URL(fileUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);

                byte[] buffer = new byte[1024];
                int bufferLength;

                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, bufferLength);
                }

                fileOutputStream.close();
                inputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return pdfFile;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (file != null) {
                int pageCount = getPdfPageCount(file);
                long fileSize = file.length();

                binding.pagesTv.setText(String.valueOf(pageCount));
                binding.sizeTv.setText(String.format("%.2f MB", fileSize / (1024.0 * 1024.0)));

                updateFirebaseWithPageCount(comicsId, String.valueOf(pageCount));
                updateFirebaseWithFileSize(comicsId, fileSize);
            } else {
                Toast.makeText(getActivity(), "Failed to download PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int getPdfPageCount(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void updateFirebaseWithPageCount(String comicId, String pages) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ComicsMarvel").child(comicId);
        ref.child("pages").setValue(pages);
    }

    private void updateFirebaseWithFileSize(String comicId, long size) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ComicsMarvel").child(comicId);
        ref.child("size").setValue(size);
    }











    private void checkIsFavorite() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Utenti registrati");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(comicsId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
            }
        });
    }

    private void addToFavorites() {
        String userId = firebaseAuth.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Utenti registrati").child(userId).child("Favorites").child(comicsId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comicsId", comicsId);
        hashMap.put("titolo", comicsTitle);
        hashMap.put("descrizione", binding.comicDescription.getText().toString());
        hashMap.put("url", comicsUrl);
        //hashMap.put("timestamp", System.currentTimeMillis());

        ref.setValue(hashMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(getActivity(), "Aggiunto ai preferiti", Toast.LENGTH_SHORT).show();
            isInMyFavorites = true;
            updateFavoriteButton();
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to add to favorites", Toast.LENGTH_SHORT).show());
    }

    private void removeFromFavorites() {
        String userId = firebaseAuth.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Utenti registrati").child(userId).child("Favorites").child(comicsId);

        ref.removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(getActivity(), "Rimosso dai preferiti", Toast.LENGTH_SHORT).show();
            isInMyFavorites = false;
            updateFavoriteButton();
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to remove from favorites", Toast.LENGTH_SHORT).show());
    }

    private void updateFavoriteButton() {
        if (isInMyFavorites) {
            binding.favoriteComicsBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.baseline_favorite_24_white, 0, 0);
            binding.favoriteComicsBtn.setText("Rimuovi");
        } else {
            binding.favoriteComicsBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.baseline_favorite_border_24_white, 0, 0);
            binding.favoriteComicsBtn.setText("Aggiungi");
        }
    }
}
