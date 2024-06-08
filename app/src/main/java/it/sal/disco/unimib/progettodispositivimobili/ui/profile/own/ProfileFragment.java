package it.sal.disco.unimib.progettodispositivimobili.ui.profile.own;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.sal.disco.unimib.progettodispositivimobili.ui.start_app.LoginActivity;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentProfileBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.preferiti.PreferitiFragment;

public class ProfileFragment extends Fragment {

    private static final String TAG = "PROFILE_TAG";
    FragmentProfileBinding binding;
    ImageView profileImageView, profileImageViewCamera;
    TextInputEditText usernameEditText, emailEditText, dobEditText, genderEditText;
    TextView deleteProfileButton;
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

        profileImageView = binding.profileImageView;
        profileImageViewCamera = binding.profileImageViewCamera;
        usernameEditText = binding.textViewUsername;
        emailEditText = binding.textViewEmail;
        dobEditText = binding.textViewDoB;
        genderEditText = binding.textViewGender;
        updateProfileButton = binding.profileUpdateBtn;
        deleteProfileButton = binding.textEliminaProfile;
        progressBar = binding.profileProgressBar;

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        } else {
            progressBar.setVisibility(View.VISIBLE);
            checkIfEmailVerified(currentUser);
            showUserProfile(currentUser);
        }

        binding.profileImageViewCamera.setOnClickListener(v -> {
            if (getActivity() != null) {
                openFragment(new UploadProfilePicFragment());
            }
        });

        updateProfileButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                openFragment(new UpdateProfileFragment());
            }
        });

        binding.favoriteBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                openFragment(new PreferitiFragment());
            }
        });

        return root;
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void checkIfEmailVerified(FirebaseUser currentUser) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails userDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (userDetails != null) {
                    if (userDetails.getAuthMethod() != null && userDetails.getAuthMethod().equals("PasswordEmail")) {
                        showAlertDialog();
                    }
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
        reference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding != null) {
                    String email = "" + snapshot.child("email").getValue();
                    String username = "" + snapshot.child("username").getValue();
                    String dob = "" + snapshot.child("dob").getValue();
                    String gender = "" + snapshot.child("gender").getValue();
                    String authMethod = "" + snapshot.child("authMethod").getValue();
                    String profileImage = "" + snapshot.child("profileImage").getValue();

                    usernameEditText.setText(username);
                    emailEditText.setText(email);
                    dobEditText.setText(dob);
                    genderEditText.setText(gender);

                    if (getActivity() != null) {
                        Glide.with(getActivity())
                                .load(profileImage)
                                .placeholder(R.drawable.profile_icone)
                                .into(binding.profileImageView);
                    }

                    if ("Google".equals(authMethod) && (dob.isEmpty() || gender.isEmpty())) {
                        showCompletionAlert();
                    }

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
                    Log.e(TAG, "Binding is null");
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

    private void showCompletionAlert() {
        if (isAdded()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Completa il tuo profilo");
            builder.setMessage("Devi completare i campi obbligatori del tuo profilo prima di procedere.");
            builder.setPositiveButton("OK", (dialog, which) -> {
                openFragment(new UpdateGoogleProfileFragment());
            });
            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Log.e("ProfileFragment", "Tentativo di mostrare dialogo quando il Fragment non è attaccato a un'attività.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
