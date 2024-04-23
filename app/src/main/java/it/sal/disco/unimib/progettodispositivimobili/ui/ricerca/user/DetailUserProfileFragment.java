package it.sal.disco.unimib.progettodispositivimobili.ui.ricerca.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentDetailUserProfileBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.userscommunication.ChatFragment;

public class DetailUserProfileFragment extends Fragment {

    FragmentDetailUserProfileBinding binding;

    ImageView profileImageView, profileImageViewCamera;
    TextInputEditText usernameEditText, emailEditText, dobEditText, genderEditText, descrizioneEditText;
    String username, email, dob, gender, descrizione;
    Button profileChatButton;
    TextView btnBack;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference reference;

    private String profileUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDetailUserProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnBack = binding.txtBack;
        profileChatButton = binding.profileChatBtn;
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Utenti registrati");

        // Collegamento delle variabili agli elementi del layout
        profileImageView = binding.profileImageView;
        profileImageViewCamera = binding.profileImageViewCamera;
        usernameEditText = binding.textViewUsername;
        emailEditText = binding.textViewEmail;
        dobEditText = binding.textViewDoB;
        genderEditText = binding.textViewGender;
        //profileUserId = profileUserId.;


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            showUserProfile(currentUser);
        }

       /*profileChatButton.setOnClickListener(v -> {
            if(getActivity() != null) {
                Bundle bundle = new Bundle();
                bundle.putString("otherUserId", currentUser.getUid()); // Passa l'ID dell'utente con cui avviare la chat
                ChatFragment chatFragment = new ChatFragment();
                chatFragment.setArguments(bundle); // Imposta gli argumenti sul fragment
                openFragment(chatFragment); // Usa questo fragment con gli argumenti per l'apertura
            }
        });*/


        profileChatButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                Bundle bundle = new Bundle();
                bundle.putString("otherUserId", profileUserId);
                ChatFragment chatFragment = new ChatFragment();
                chatFragment.setArguments(bundle);
                openFragment(chatFragment);
            } else {
                // Consider adding a Toast here to inform why the button isn't clickable
                Toast.makeText(getContext(), "Dettagli utente non disponibili", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new SearchUserFragment());
            }
        });

        return root;
    }

    private void showUserProfile(FirebaseUser currentUser) {
        Bundle args = getArguments();
        if (args != null && args.containsKey("userDetails")) {
            ReadWriteUserDetails readUserDetails = args.getParcelable("userDetails");
            if (readUserDetails != null) {
                profileUserId = readUserDetails.getUserId();
                username = readUserDetails.getUsername();
                email = readUserDetails.getEmail();
                dob = readUserDetails.getDob();
                gender = readUserDetails.getGender();

                usernameEditText.setText(username);
                emailEditText.setText(email);
                dobEditText.setText(dob);
                genderEditText.setText(gender);

                // Carica l'immagine del profilo da Firebase Storage
                String dataImage = readUserDetails.getDataImage();
                if (dataImage != null && !dataImage.isEmpty()) {
                    // Ottieni un riferimento all'immagine in Firebase Storage
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference().child("VisualizzaImmagini");

                    // Carica l'immagine nell'ImageView
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Picasso.with(getActivity())
                                .load(uri)
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .into(profileImageView);
                    }).addOnFailureListener(e -> {
                        // Gestisci eventuali errori nel recupero dell'URL dell'immagine
                        Toast.makeText(getActivity(), "Errore nel caricamento dell'immagine del profilo", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }
    }

    private void openFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment); // Utilizza il fragment fornito come parametro
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
