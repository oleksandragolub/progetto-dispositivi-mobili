package it.sal.disco.unimib.progettodispositivimobili.ui.profile.own;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import it.sal.disco.unimib.progettodispositivimobili.ui.start_app.LoginActivity;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentDeleteGoogleProfileBinding;

    public class DeleteGoogleProfileFragment extends Fragment {

        private static final String TAG = "DeleteGoogleProfileFragment";
        private FirebaseAuth mAuth;
        private FirebaseUser currentUser;

        private GoogleSignInClient mGoogleSignInClient;
        private FragmentDeleteGoogleProfileBinding binding;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            binding = FragmentDeleteGoogleProfileBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();

            setupGoogleSignIn();
            initializeButtons();

            return root;
        }

        private void setupGoogleSignIn() {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.web_client_id))
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        }

        private void initializeButtons() {
            binding.btnDeleteUser.setOnClickListener(v -> showAlertDialog());
            binding.txtBack.setOnClickListener(v -> openFragment(new ProfileFragment()));
        }

        private void showAlertDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Eliminazione dell'account e dei relativi dati?");
            builder.setMessage("Sei sicuro di voler eliminare il tuo account ed i relativi dati? Questa azione è irreversibile!");
            builder.setPositiveButton("Continua", (dialog, which) -> revokeAccessAndDeleteAccount());
            builder.setNegativeButton("Cancella", (dialog, which) -> openFragment(new ProfileFragment()));
            AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(dialog -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.md_theme_error)));
            alertDialog.show();
        }

        private void revokeAccessAndDeleteAccount() {
            if (currentUser != null) {
                // Assumendo che l'utente debba essere autenticato per eliminare il proprio account
                mGoogleSignInClient.silentSignIn().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        deleteUserData(currentUser, new DeleteProfileFragment.OnUserDataDeletedListener() {
                            @Override
                            public void onSuccess() {
                                // Dopo l'eliminazione dei dati, procedi con l'eliminazione dell'account
                                deleteUser(task.getResult());
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Errore nell'eliminazione dei dati utente", e);
                                Toast.makeText(getActivity(), "Errore nell'eliminazione dei dati utente: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.e(TAG, "Autenticazione fallita, necessaria nuova autenticazione per l'eliminazione!", task.getException());
                        Toast.makeText(getActivity(), "Autenticazione fallita. Effettua nuovamente l'accesso.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private void deleteUser(GoogleSignInAccount signInAccount) {
            String googleIdToken = signInAccount.getIdToken();
            AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);
            currentUser.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {
                    currentUser.delete().addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(getActivity(), "Account e' stato eliminato con successo!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), LoginActivity.class));
                            getActivity().finish();
                        } else {
                            Toast.makeText(getActivity(), "Fallimento nel tentativo di eliminare l'account: " + deleteTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Re-autenticazione fallita: " + reauthTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void deleteUserData(FirebaseUser currentUser, DeleteProfileFragment.OnUserDataDeletedListener listener) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Utenti registrati").child(currentUser.getUid());

            // Eliminazione degli eventuali immagini dallo storage
            if (currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().toString().isEmpty()) {
                try {
                    StorageReference photoRef = firebaseStorage.getReferenceFromUrl(currentUser.getPhotoUrl().toString());
                    photoRef.delete().addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Foto del profilo eliminata con successo.");
                        deleteDatabaseEntry(databaseReference, listener);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Errore nell'eliminazione della foto del profilo", e);
                        listener.onFailure(e);
                    });
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "URL non valido per Firebase Storage: " + e.getMessage());
                    deleteDatabaseEntry(databaseReference, listener); // Procedi comunque con l'eliminazione dei dati dal database
                }
            } else {
                Log.d(TAG, "Nessuna foto del profilo da eliminare.");
                deleteDatabaseEntry(databaseReference, listener);
            }
        }

        private void deleteDatabaseEntry(DatabaseReference databaseReference, DeleteProfileFragment.OnUserDataDeletedListener listener) {
            databaseReference.removeValue().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Dati utente eliminati con successo.");
                listener.onSuccess();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Errore nell'eliminazione dei dati utente.", e);
                listener.onFailure(e);
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