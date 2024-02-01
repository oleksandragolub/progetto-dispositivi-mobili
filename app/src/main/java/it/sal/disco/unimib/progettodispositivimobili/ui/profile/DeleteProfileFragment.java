package it.sal.disco.unimib.progettodispositivimobili.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import it.sal.disco.unimib.progettodispositivimobili.LoginActivity;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentDeleteProfileBinding;

public class DeleteProfileFragment extends Fragment {

    private static final String TAG = "DeleteProfileFragment";
    FragmentDeleteProfileBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    TextView btnBack;

    EditText editTextUserPwd;
    TextView textViewAuthenticated;
    String userPwd;
    Button buttonReAuthenticate, buttonDeleteUser;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDeleteProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        btnBack = binding.txtBack;
        editTextUserPwd = binding.password;
        textViewAuthenticated = binding.textProfileNotAuthenticated;
        buttonDeleteUser = binding.btnDeleteUser;
        buttonReAuthenticate = binding.btnAuthenticate;

        buttonDeleteUser.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (Objects.equals(currentUser, "")) {
            Toast.makeText(getActivity(), "Qualcosa è andato storto! I dati dell'utente non sono disponibili.", Toast.LENGTH_SHORT).show();
            if(getActivity() != null){
                openFragment(new ProfileFragment());
            }
        } else {
            reAuthenticateUser(currentUser);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null) {
                    openFragment(new ProfileFragment());
                }
            }
        });

        return root;
    }

    private void reAuthenticateUser(FirebaseUser currentUser) {
        buttonReAuthenticate.setOnClickListener(v -> {
            userPwd = editTextUserPwd.getText().toString();

            if(TextUtils.isEmpty(userPwd)){
                Toast.makeText(getActivity(), "Serve la password per continuare!", Toast.LENGTH_SHORT).show();
                editTextUserPwd.setError("Richiesta di password corrente");
                editTextUserPwd.requestFocus();
                //return;
            } else {
                AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), userPwd);
                currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        editTextUserPwd.setEnabled(false);

                        buttonReAuthenticate.setEnabled(false);
                        buttonDeleteUser.setEnabled(true);

                        textViewAuthenticated.setText("Sei stato autenticato, adesso puoi eliminare il tuo profilo. Attenzione, questa azione è irreversibile!");

                        Toast.makeText(getActivity(), "La password è stata verificata." + "Adesso puoi eliminare il tuo profilo!", Toast.LENGTH_SHORT).show();

                        buttonDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.dark_red));

                        buttonDeleteUser.setOnClickListener(v1 -> showAlertDialog());
                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (Exception e){
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
        // Prima tenta di eliminare i dati dell'utente
        deleteUserData(currentUser, new OnUserDataDeletedListener() {
            @Override
            public void onSuccess() {
                // Solo dopo l'eliminazione dei dati, procedi con l'eliminazione dell'account
                currentUser.delete().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        // Dopo l'eliminazione dell'account, esegui il logout
                        FirebaseAuth.getInstance().signOut();
                        // Passa alla LoginActivity
                        Toast.makeText(getActivity(), "L'utente è stato eliminato!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        // Gestisci eventuali errori nell'eliminazione dell'account
                        Exception e = task.getException();
                        Log.e(TAG, "Errore nell'eliminazione dell'account.", e);
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception exception) {
                // Gestisci l'errore nell'eliminazione dei dati dell'utente
                Log.e(TAG, "Errore nell'eliminazione dei dati utente.", exception);
                Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserData(FirebaseUser currentUser, OnUserDataDeletedListener listener) {
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

    // Definizione dell'interfaccia OnUserDataDeletedListener da qualche parte nel tuo codice
    interface OnUserDataDeletedListener {
        void onSuccess();
        void onFailure(Exception exception);
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