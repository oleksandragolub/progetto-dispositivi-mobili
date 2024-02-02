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
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Utenti registrati");

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
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails userDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (userDetails != null) {
                    // Controlla se l'utente si è registrato tramite PasswordEmail o se l'email è stata verificata
                    if (userDetails.getAuthMethod() != null && userDetails.getAuthMethod().equals("PasswordEmail")) {
                        showAlertDialog();
                    } // Altrimenti l'email è verificata o l'utente è registrato tramite Google, percio' non serve mostrare l'alert
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Qualcosa e' andato storto!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
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
        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    .load(uri.toString())
                                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                    .into(profileImageView);
                        }
                    });
                    String authMethod = readUserDetails.getAuthMethod();

                    // Gestione dei pulsanti deleteProfileButton e updateProfileButton in base all'authMethod
                    if (authMethod.equals("PasswordEmail")) {
                        deleteProfileButton.setOnClickListener(v -> {
                            if (getActivity() != null) {
                                openFragment(new DeleteProfileFragment());
                            }
                        });

                        updateProfileButton.setOnClickListener(v -> {
                            if (getActivity() != null) {
                                openFragment(new UpdateProfileFragment());
                            }
                        });
                    } else if (authMethod.equals("Google")) {
                        deleteProfileButton.setOnClickListener(v -> {
                            if (getActivity() != null) {
                                openFragment(new DeleteGoogleProfileFragment());
                            }
                        });

                        updateProfileButton.setOnClickListener(v -> {
                            if (getActivity() != null) {
                                openFragment(new UpdateGoogleProfileFragment());
                            }
                        });
                    }
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