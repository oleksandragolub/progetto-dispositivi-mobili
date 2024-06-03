package it.sal.disco.unimib.progettodispositivimobili.ui.profile.own;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentUploadProfilePicBinding;

public class UploadProfilePicFragment extends Fragment {

    private ActivityResultLauncher<String> mGetContent;
    FragmentUploadProfilePicBinding binding;
    StorageReference storageReference;
    ImageView imageViewUploadPic;
    TextView btnBack;
    Button buttonUploadPicChoose, buttonUploadPic;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Uri uri, uriImage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentUploadProfilePicBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        btnBack = binding.txtBack;
        buttonUploadPicChoose = binding.btnScegliImmagine;
        buttonUploadPic = binding.btnCaricaImmagine;
        imageViewUploadPic = binding.imageViewProfileDp;

        storageReference = FirebaseStorage.getInstance().getReference("VisualizzaImmagini");
        uri = currentUser.getPhotoUrl();
        Picasso.with(getActivity()).load(uri).into(imageViewUploadPic);

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uriImage = uri;
                        imageViewUploadPic.setImageURI(uriImage);
                    }
                });

        btnBack.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new ProfileFragment());
            }
        });

        buttonUploadPicChoose.setOnClickListener(v -> openFileChooser());

        buttonUploadPic.setOnClickListener(v -> {
            uploadPic();
        });

        return root;
    }

    private void uploadPic() {
        if(uriImage != null){
            StorageReference fileReference = storageReference.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid() + "."
            + getFileExtension(uriImage));

            fileReference.putFile(uriImage).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    Uri downloadUri = uri;
                    currentUser = mAuth.getCurrentUser();

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(downloadUri).build();

                    currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Profilo è stato aggiornato con il successo!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Errore nell'aggiornamento del profilo.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

                Toast.makeText(getActivity(), "L'immagine è stata caricata con successo!", Toast.LENGTH_SHORT).show();

                if(getActivity() != null) {
                    openFragment(new ProfileFragment());
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getActivity(), "Nessun file è stato selezionato!", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));

    }

    private void openFileChooser() {
        mGetContent.launch("image/*");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
