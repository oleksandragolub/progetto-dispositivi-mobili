package it.sal.disco.unimib.progettodispositivimobili.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
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

import it.sal.disco.unimib.progettodispositivimobili.LoginActivity;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentDeleteGoogleProfileBinding;

    public class DeleteGoogleProfileFragment extends Fragment {

        private static final String TAG = "DeleteGoogleProfileFragment";
        FragmentDeleteGoogleProfileBinding binding;
        FirebaseAuth mAuth;
        FirebaseUser currentUser;
        GoogleSignInClient googleSignInClient;
        TextView btnBack;
        Button buttonDeleteUser;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {

            binding = FragmentDeleteGoogleProfileBinding.inflate(inflater, container, false);
            View root = binding.getRoot();
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();

            btnBack = binding.txtBack;
            buttonDeleteUser = binding.btnDeleteUser;
            buttonDeleteUser.setEnabled(true);

            // Configura il client Google Sign-In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.web_client_id))
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

            // Gestione del click sul pulsante di eliminazione
            buttonDeleteUser.setOnClickListener(v -> {
                showAlertDialog();
                // Revoca l'accesso a Google prima di eliminare l'account Firebase
                revokeAccessAndDeleteAccount();
            });

            btnBack.setOnClickListener(v -> {
                if(getActivity() != null) {
                    openFragment(new ProfileFragment());
                }
            });

            return root;
        }

        private void revokeAccessAndDeleteAccount() {
            // Revoca l'accesso a Google
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // L'utente è stato disconnesso con successo da Google
                    // Ora puoi procedere con l'eliminazione dell'account Firebase
                    deleteUser(currentUser);
                } else {
                    // Gestisci eventuali errori nella revoca dell'accesso a Google
                    Exception e = task.getException();
                    Log.e(TAG, "Errore nella revoca dell'accesso a Google", e);
                    Toast.makeText(getActivity(), "Errore nella revoca dell'accesso a Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        private void showAlertDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Eliminazione dell'account e dei relativi dati?");
            builder.setMessage("Sei sicuro di voler eliminare il tuo account ed i relativi dati? Questa azione è irreversibile!");

            builder.setPositiveButton("Continua", (dialog, which) -> {
                deleteUser(currentUser);
            });

            builder.setNegativeButton("Cancella", (dialog, which) -> {
                if(getActivity() != null){
                    openFragment(new ProfileFragment());
                }
            });

            AlertDialog alertDialog = builder.create();

            alertDialog.setOnShowListener(dialog -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red)));

            alertDialog.show();
        }

        private void deleteUser(FirebaseUser currentUser) {
            Log.d(TAG, "Provider di autenticazione: " + currentUser.getProviderData().get(0).getProviderId());
            // Verifica se l'utente è autenticato tramite Google
            if (currentUser.getProviderData().get(0).getProviderId().equals("google.com")) {
                currentUser.getIdToken(true)  // Ottenere il token di accesso da Firebase
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Ottieni il token di accesso
                                String googleIdToken = task.getResult().getToken();

                                // Crea un AuthCredential utilizzando il token di accesso
                                AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);

                                // Reautentica l'utente con l'AuthCredential
                                currentUser.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                                    if (reauthTask.isSuccessful()) {
                                        // Elimina l'account Firebase
                                        currentUser.delete().addOnCompleteListener(deleteTask -> {
                                            if (deleteTask.isSuccessful()) {
                                                // Dopo l'eliminazione dell'account, esegui il logout
                                                FirebaseAuth.getInstance().signOut();
                                                // Passa alla LoginActivity
                                                Toast.makeText(getActivity(), "L'utente è stato eliminato!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                startActivity(intent);
                                                getActivity().finish();
                                            } else {
                                                // Gestisci eventuali errori nell'eliminazione dell'account Firebase
                                                Exception e = deleteTask.getException();
                                                Log.e(TAG, "Errore nell'eliminazione dell'account Firebase", e);
                                                Toast.makeText(getActivity(), "Errore nell'eliminazione dell'account Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        // Gestisci eventuali errori nella reautenticazione dell'utente
                                        Exception e = reauthTask.getException();
                                        Log.e(TAG, "Errore nella reautenticazione dell'utente", e);
                                        Toast.makeText(getActivity(), "Errore nella reautenticazione dell'utente: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                // Gestisci eventuali errori nell'ottenere il token di accesso
                                Exception e = task.getException();
                                Log.e(TAG, "Errore nell'ottenere il token di accesso", e);
                                Toast.makeText(getActivity(), "Errore nell'ottenere il token di accesso: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // L'utente non è autenticato o non è autenticato tramite Google, mostra un messaggio di errore
                Toast.makeText(getActivity(), "Devi essere autenticato tramite Google per eliminare l'account Google.", Toast.LENGTH_SHORT).show();
            }
        }

        private void deleteUserData(FirebaseUser currentUser, it.sal.disco.unimib.progettodispositivimobili.ui.profile.DeleteProfileFragment.OnUserDataDeletedListener listener) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Utenti registrati").child(currentUser.getUid());

            // Eliminazione degli eventuali immagini dallo storage
            if (currentUser.getPhotoUrl() != null) {
                StorageReference photoRef = firebaseStorage.getReferenceFromUrl(currentUser.getPhotoUrl().toString());
                photoRef.delete().addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Foto del profilo eliminata con successo.");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Errore nell'eliminazione della foto del profilo", e);
                });
            } else {
                Log.d(TAG, "Nessuna foto del profilo da eliminare.");
            }

            // Eliminazione dei dati dal Realtime Database
            databaseReference.removeValue().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Dati utente eliminati con successo.");
                listener.onSuccess();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Errore nell'eliminazione dei dati utente", e);
                listener.onFailure(e);
            });
        }

        private void openFragment(Fragment fragment){
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