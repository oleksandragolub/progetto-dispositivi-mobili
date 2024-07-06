package it.sal.disco.unimib.progettodispositivimobili.ui.profile.own;

import android.app.Activity;
import android.content.Intent;
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
    private FragmentProfileBinding binding;
    private ImageView profileImageView, profileImageViewCamera;
    private TextInputEditText usernameEditText, emailEditText, dobEditText, genderEditText;
    private TextView deleteProfileButton;
    private Button updateProfileButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private ValueEventListener profileEventListener;
    private ValueEventListener emailVerifiedListener;

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
            navigateToLogin();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            checkIfEmailVerified(currentUser);
            showUserProfile(currentUser);
        }

        binding.profileImageViewCamera.setOnClickListener(v -> {
            openFragment(new UploadProfilePicFragment());
        });

        updateProfileButton.setOnClickListener(v -> {
            openFragment(new UpdateProfileFragment());
        });

        binding.favoriteBtn.setOnClickListener(v -> {
            openFragment(new PreferitiFragment());
        });

        return root;
    }

    private void navigateToLogin() {
        removeFirebaseListeners();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void checkIfEmailVerified(FirebaseUser currentUser) {
        emailVerifiedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails userDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (userDetails != null) {
                    if ("PasswordEmail".equals(userDetails.getAuthMethod())) {
                        showAlertDialog();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Qualcosa e' andato storto!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        };
        reference.addListenerForSingleValueEvent(emailVerifiedListener);
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
        profileEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding != null) {
                    if (snapshot.exists()) {
                        String email = snapshot.child("email").getValue(String.class);
                        String username = snapshot.child("username").getValue(String.class);
                        String dob = snapshot.child("dob").getValue(String.class);
                        String gender = snapshot.child("gender").getValue(String.class);
                        String authMethod = snapshot.child("authMethod").getValue(String.class);
                        String profileImage = snapshot.child("profileImage").getValue(String.class);

                        usernameEditText.setText(username);
                        emailEditText.setText(email);
                        dobEditText.setText(dob);
                        genderEditText.setText(gender);

                        if (getActivity() != null && profileImage != null) {
                            Glide.with(getActivity())
                                    .load(profileImage)
                                    .placeholder(R.drawable.profile_icone)
                                    .into(binding.profileImageView);
                        }

                        if ("Google".equals(authMethod) && (dob.isEmpty() || gender.isEmpty())) {
                            showCompletionAlert();
                        }

                        if ("PasswordEmail".equals(authMethod)) {
                            deleteProfileButton.setOnClickListener(v -> openFragment(new DeleteProfileFragment()));
                            updateProfileButton.setOnClickListener(v -> openFragment(new UpdateProfileFragment()));
                        } else if ("Google".equals(authMethod)) {
                            deleteProfileButton.setOnClickListener(v -> openFragment(new DeleteGoogleProfileFragment()));
                            updateProfileButton.setOnClickListener(v -> openFragment(new UpdateGoogleProfileFragment()));
                        }
                    } else {
                        Log.e(TAG, "Snapshot non esiste");
                    }
                    progressBar.setVisibility(View.GONE);
                } else {
                    Log.e(TAG, "Binding is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Qualcosa è andato storto!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        };
        reference.child(userID).addValueEventListener(profileEventListener);
    }

    private void showCompletionAlert() {
        if (isAdded()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Completa il tuo profilo");
            builder.setMessage("Devi completare i campi obbligatori del tuo profilo prima di procedere.");
            builder.setPositiveButton("OK", (dialog, which) -> openFragment(new UpdateGoogleProfileFragment()));
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
        removeFirebaseListeners();
        binding = null;
    }

    private void removeFirebaseListeners() {
        if (reference != null && profileEventListener != null) {
            reference.removeEventListener(profileEventListener);
            profileEventListener = null;
            Log.d(TAG, "Profile event listener removed");
        }
        if (reference != null && emailVerifiedListener != null) {
            reference.removeEventListener(emailVerifiedListener);
            emailVerifiedListener = null;
            Log.d(TAG, "Email verified listener removed");
        }
    }
}
