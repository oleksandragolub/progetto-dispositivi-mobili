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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentComicsMarvelDetailBinding;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters.AdapterComment;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.ModelComment;

public class ComicsMarvelDetailFragment extends Fragment {

    private static final String TAG = "DETAIL_TAG";
    private FragmentComicsMarvelDetailBinding binding;
    private ImageView comicThumbnail;
    private TextView comicTitle;
    private TextView comicDescription;
    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;
    //String comicsId, comicsTitle, comicsUrl;
    boolean isInMyFavorites = false;
    String comment = "";
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comics_marvel_detail, container, false);

        comicThumbnail = view.findViewById(R.id.comicThumbnail);
        comicTitle = view.findViewById(R.id.comicTitle);
        comicDescription = view.findViewById(R.id.comicDescription);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String thumbnailUrl = bundle.getString("thumbnailUrl");
            String title = bundle.getString("title");
            String description = bundle.getString("description");

            Glide.with(this).load(thumbnailUrl).into(comicThumbnail);
            comicTitle.setText(title);
            comicDescription.setText(description);
        }


        view.findViewById(R.id.buttonBackUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

       /* binding.readComicsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComicsPdfViewUserFragment comicsPdfViewUserFragment = new ComicsPdfViewUserFragment();
                Bundle args = new Bundle();
                args.putString("comicsId", comicsId);
                comicsPdfViewUserFragment.setArguments(args);

                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.nav_host_fragment, comicsPdfViewUserFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });*/

        /*binding.downloadComicsBtn.setOnClickListener(v -> {
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
        });*/

       /* binding.favoriteComicsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(getActivity(), "Non sei autentificato!", Toast.LENGTH_SHORT).show();
                } else {
                    if (isInMyFavorites) {
                        MyApplication.removeFromFavorite(getActivity(), comicsId);
                    } else {
                        MyApplication.addToFavorite(getActivity(), comicsId);
                    }
                }
            }
        });*/

      /*  binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(getActivity(), "Non sei autentificato!", Toast.LENGTH_SHORT).show();
                } else {
                    addCommentDialog();
                }
            }
        });*/

        return view;
    }


}
