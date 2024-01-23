package it.sal.disco.unimib.progettodispositivimobili.ui.profile;

import android.content.DialogInterface;
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

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import it.sal.disco.unimib.progettodispositivimobili.LoginActivity;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentDeleteProfileBinding;

public class DeleteProfileFragment extends Fragment {

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

        if (currentUser.equals("")) {
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
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                userPwd = editTextUserPwd.getText().toString();

                if(TextUtils.isEmpty(userPwd)){
                    Toast.makeText(getActivity(), "Serve la password per continuare!", Toast.LENGTH_SHORT).show();
                    editTextUserPwd.setError("Richiesta di password corrente");
                    editTextUserPwd.requestFocus();
                    //return;
                } else {
                    AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), userPwd);
                    currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                editTextUserPwd.setEnabled(false);

                                buttonReAuthenticate.setEnabled(false);
                                buttonDeleteUser.setEnabled(true);

                                textViewAuthenticated.setText("Sei stato autentificato. Adesso puoi eliminare il tuo profilo. Però stai attento, perchè questa azione è irreversibile!");

                                Toast.makeText(getActivity(), "La password è stata verificata." + "Adesso puoi eliminare il tuo profilo!", Toast.LENGTH_SHORT).show();

                                buttonDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.dark_red));

                                buttonDeleteUser.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showAlertDialog();
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Eliminazione dell'account e dei relativi dati?");
        builder.setMessage("Sei sicuro di voler eliminare il tuo account e dei relativi dati? Questa azione è irreversibile!");

        builder.setPositiveButton("Continua", (dialog, which) -> {
            deleteUser(currentUser);
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(getActivity() != null){
                    openFragment(new ProfileFragment());
                }
            }
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });

        alertDialog.show();
    }

    private void deleteUser(FirebaseUser currentUser) {
        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    // Effettua il logout da Firebase Auth
                    FirebaseAuth.getInstance().signOut();
                    // Passa alla LoginActivity
                    Toast.makeText(getActivity(), "L'utente è stato eiminato!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                        try {
                            throw task.getException();
                        } catch (Exception e){
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
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