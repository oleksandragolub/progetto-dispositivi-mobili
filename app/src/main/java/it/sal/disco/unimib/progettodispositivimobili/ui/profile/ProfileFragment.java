package it.sal.disco.unimib.progettodispositivimobili.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import it.sal.disco.unimib.progettodispositivimobili.LoginActivity;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentProfileBinding;


public class ProfileFragment extends Fragment {
    FragmentProfileBinding binding;
    ImageView profileImageView, profileImageViewCamera;
    TextInputEditText usernameEditText, emailEditText, dobEditText, genderEditText, descrizioneEditText;
    TextView deleteProfileButton;
    String username, email, dob, gender, descrizione;
    Button updateProfileButton;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Collegamento delle variabili agli elementi del layout
        profileImageView = binding.profileImageView;
        profileImageViewCamera = binding.profileImageViewCamera;
        usernameEditText = binding.textViewUsername;
        emailEditText = binding.textViewEmail;
        dobEditText = binding.textViewDoB;
        genderEditText = binding.textViewGender;
        //descrizioneEditText = binding.textViewDescrizione;
        updateProfileButton = binding.profileUpdateBtn;
        deleteProfileButton = binding.textEliminaProfile;
        progressBar = binding.profileProgressBar;

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // L'utente non è loggato, avvia LoginActivity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            // Termina l'attività ospitante
            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        } else {
            progressBar.setVisibility(View.VISIBLE);
            chekifEmailVerified(currentUser);
            showUserProfile(currentUser);
        }

        binding.profileImageViewCamera.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new UploadProfilePicFragment());
            }
        });

        updateProfileButton.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new UpdateProfileFragment());
            }
        });

        deleteProfileButton.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new DeleteProfileFragment());
            }
        });

        return root;
    }

    private void openFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void chekifEmailVerified(FirebaseUser currentUser) {
        if(!currentUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Email non verificata");
        builder.setMessage("Controlla la tua email. Non puoi entrare nel tuo account senza effettuare la verifica.");

        builder.setPositiveButton("Continua", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUserProfile(FirebaseUser currentUser) {
        String userID = currentUser.getUid();

        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Utenti registrati");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if(readUserDetails != null){
                    username = readUserDetails.getUsername();
                    email = readUserDetails.getEmail();
                    dob = readUserDetails.getDob();
                    gender = readUserDetails.getGender();

                    usernameEditText.setText(username);
                    emailEditText.setText(email);
                    dobEditText.setText(dob);
                    genderEditText.setText(gender);

                    currentUser.reload().addOnSuccessListener(aVoid -> {
                        Uri uri = currentUser.getPhotoUrl();
                        if (uri != null) {
                            Picasso.with(getActivity())
                                    .load(uri.toString()) // Usa uri.toString() per ottenere l'URL dell'immagine
                                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                    .into(profileImageView);
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Qualcosa e' andato storto!", Toast.LENGTH_SHORT).show();

                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Qualcosa e' andato storto!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}