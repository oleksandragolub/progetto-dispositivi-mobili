package it.sal.disco.unimib.progettodispositivimobili.ui.profile;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.RegisterActivity;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentUpdateProfileBinding;

public class UpdateProfileFragment extends Fragment {

    FragmentUpdateProfileBinding binding;

    private TextInputEditText usernameEditText, dobEditText, descrizioneEditText;
    private TextView textChangePassword, textChangeEmail, textEliminaProfile;
    private String username, dob, gender, descrizione;
    private Button updateProfileButton, changeEmailButton, changePasswordButton;
    private RadioGroup radioGroupRegisterGender;

    private TextView btnBack;

    private RadioButton radioButtonRegisterGenderSelected;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentUpdateProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnBack = binding.txtBack;
        usernameEditText = binding.textViewUsername;
        dobEditText = binding.textViewDoB;
        radioGroupRegisterGender = binding.radioGroupRegisterGender;
        updateProfileButton = binding.profileUpdateBtn;
        changeEmailButton = binding.changeEmailBtn;
        changePasswordButton = binding.changePwBtn;

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        showProfile(currentUser);

        dobEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String textASDoB[] = dob.split("/");

                int day = Integer.parseInt(textASDoB[0]);
                int month = Integer.parseInt(textASDoB[1]) - 1;
                int year = Integer.parseInt(textASDoB[2]);

                DatePickerDialog picker;

                picker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dobEditText.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });


        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(currentUser);
            }
        });

        changeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null) {
                    openFragment(new UpdateEmailFragment());
                }
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null) {
                    openFragment(new UpdatePasswordFragment());
                }
            }
        });

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

    private void updateProfile(FirebaseUser currentUser) {
        int selectedGenderID = binding.radioGroupRegisterGender.getCheckedRadioButtonId();
        if (selectedGenderID != -1) {
            radioButtonRegisterGenderSelected = binding.getRoot().findViewById(selectedGenderID);
            gender = radioButtonRegisterGenderSelected.getText().toString();
        } else {
            // Gestisci il caso in cui nessun RadioButton sia selezionato
            Toast.makeText(getActivity(), "Seleziona il tuo genere", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(username)){
            Toast.makeText(getActivity(), "Inserisci il tuo username", Toast.LENGTH_SHORT).show();
            usernameEditText.setError("Username richiesto");
            usernameEditText.requestFocus();
            //return;
        } else if(TextUtils.isEmpty(dob)){
            Toast.makeText(getActivity(), "Inserisci la tua data di nascita", Toast.LENGTH_SHORT).show();
            dobEditText.setError("Data di nascita richiesta");
            dobEditText.requestFocus();
            //return;
        } else if(TextUtils.isEmpty(radioButtonRegisterGenderSelected.getText())){
            Toast.makeText(getActivity(), "Seleziona il tuo genere", Toast.LENGTH_SHORT).show();
            radioButtonRegisterGenderSelected.setError("Genere richiesto");
            radioButtonRegisterGenderSelected.requestFocus();
            //return;
        } else{
            gender = radioButtonRegisterGenderSelected.getText().toString();
            username = usernameEditText.getText().toString();
            dob = dobEditText.getText().toString();

            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(username, dob, gender);
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Utenti registrati");

            String userID = currentUser.getUid();

            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().
                                setDisplayName(username).build();
                        currentUser.updateProfile(profileUpdates);

                        Toast.makeText(getActivity(), "L'aggiornamento del profilo è completato con successo!", Toast.LENGTH_SHORT).show();

                        if(getActivity() != null) {
                            openFragment(new ProfileFragment());
                        }
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        }
    }

    private void openFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showProfile(FirebaseUser currentUser) {
        String userIDofRegistered = currentUser.getUid();

        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Utenti registrati");

        referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null){
                    username = readUserDetails.getUsername();
                    dob = readUserDetails.getDob();
                    gender = readUserDetails.getGender();

                    usernameEditText.setText(username);
                    dobEditText.setText(dob);

                    if(gender.equals("Maschio")){
                        radioButtonRegisterGenderSelected = binding.genderMale;
                    } else{
                        radioButtonRegisterGenderSelected = binding.genderFem;
                    }
                    radioButtonRegisterGenderSelected.setChecked(true);
                } else {
                    Toast.makeText(getActivity(), "Qualcosa è andato storto!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Qualcosa è andato storto!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}