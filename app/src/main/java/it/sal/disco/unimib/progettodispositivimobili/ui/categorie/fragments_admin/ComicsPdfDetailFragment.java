package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsPdfDetailBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.Constants;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.MyApplication;

public class ComicsPdfDetailFragment extends Fragment {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    private FragmentComicsPdfDetailBinding binding;

    private FirebaseAuth firebaseAuth;
    boolean isInMyFavorites = false;

    String comicsId, comicsTitle, comicsUrl;

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentComicsPdfDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getArguments() != null) {
            comicsId = getArguments().getString("comicsId");
        }

        //binding.downloadComicsBtn.setVisibility(View.GONE);
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }

        if (comicsId != null) {
            loadComicsDetails();
            MyApplication.incrementComicsViewCoint(comicsId);
        } else {
            Toast.makeText(getActivity(), "Error: Comics ID is null", Toast.LENGTH_SHORT).show();
        }

        binding.buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                openFragment(new CategoryAddAdminFragment());
            }
        });

        binding.readComicsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComicsPdfViewFragment comicsPdfViewFragment = new ComicsPdfViewFragment();
                Bundle args = new Bundle();
                args.putString("comicsId", comicsId);
                comicsPdfViewFragment.setArguments(args);

                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.nav_host_fragment, comicsPdfViewFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
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

        binding.favoriteComicsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(getActivity(), "Non sei autentificato!", Toast.LENGTH_SHORT).show();
                } else {
                    if (isInMyFavorites){
                        MyApplication.removeFromFavorite(getActivity(), comicsId);
                    } else {
                        MyApplication.addToFavorite(getActivity(), comicsId);
                    }
                }
            }
        });


        return root;
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comics");
        ref.child(comicsId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comicsTitle = ""+snapshot.child("titolo").getValue();
                String description = ""+snapshot.child("descrizione").getValue();
                String categoryId = ""+snapshot.child("categoryId").getValue();
                String viewsCount = ""+snapshot.child("viewsCount").getValue();
                String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                comicsUrl = ""+snapshot.child("url").getValue();
                String timestamp = ""+snapshot.child("timestamp").getValue();

                binding.downloadComicsBtn.setVisibility(View.VISIBLE);

                String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));
                MyApplication.loadCategory(""+categoryId, binding.categoryTv);
                MyApplication.loadPdfFromUrlSinglePage(""+comicsUrl, ""+comicsTitle, binding.pdfView, binding.progressBar, binding.pagesTv);
                MyApplication.loadPdfSize(""+comicsUrl, ""+comicsTitle, binding.sizeTv);
                //MyApplication.loadPdfPageCount(getActivity(), ""+comicsUrl, binding.pagesTv);



                binding.titleTv.setText(comicsTitle);
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

    private void checkIsFavorite(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Utenti registrati");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(comicsId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isInMyFavorites = snapshot.exists();
                if(isInMyFavorites){
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