package it.sal.disco.unimib.progettodispositivimobili.ui.ricerca.user;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.sal.disco.unimib.progettodispositivimobili.ChatActivity;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.databinding.ActivityMainBinding;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentDetailUserProfileBinding;

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

    String profileUserId;
    String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDetailUserProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        profileUserId = getArguments().getString("userId", null);

        if (profileUserId == null) {
            Toast.makeText(getContext(), "User ID is missing.", Toast.LENGTH_SHORT).show();
            return binding.getRoot();
        }

        showUserProfile(profileUserId);
        Log.d("DetailUserProfileFragment", "Received User ID: " + profileUserId);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

      //  profileUserId = currentUser.getUid();


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

     /*   profileChatButton.setOnClickListener(v -> {
            if (getActivity() != null && profileUserId != null && !profileUserId.isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString("otherUserId", profileUserId);
                ChatActivity chatActivity = new ChatActivity();
                chatActivity.setArguments(bundle);
                openFragment(chatActivity);
            } else {
                Toast.makeText(getContext(), "Dettagli utente non disponibili o ID utente mancante", Toast.LENGTH_SHORT).show();
            }
        });*/

        btnBack.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new SearchUserFragment());
            }
        });

        return root;
    }

    private void showUserProfile(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Utenti registrati").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ReadWriteUserDetails userDetails = dataSnapshot.getValue(ReadWriteUserDetails.class);
                if (userDetails != null) {
                    usernameEditText.setText(userDetails.getUsername());
                    emailEditText.setText(userDetails.getEmail());
                    dobEditText.setText(userDetails.getDob());
                    genderEditText.setText(userDetails.getGender());

                    String dataImage = userDetails.getDataImage();
                   /*if (dataImage != null && !dataImage.isEmpty()) {
                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(dataImage);
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Picasso.get().load(uri).into(profileImageView);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Errore nel caricamento dell'immagine del profilo", Toast.LENGTH_SHORT).show();
                        });
                    }*/
                } else {
                    Toast.makeText(getContext(), "Dettagli utente non trovati.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Errore nel caricamento dei dettagli utente.", Toast.LENGTH_SHORT).show();
            }
        });
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
