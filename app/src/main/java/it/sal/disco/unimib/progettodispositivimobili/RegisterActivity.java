package it.sal.disco.unimib.progettodispositivimobili;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private TextInputEditText editTextEmail, editTextPassword, editTextDoB, editTextConfirmPassword, editTextUsername;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;
    private DatePickerDialog picker;
    Button buttonReg;
    TextView text_loginNow;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        editTextUsername = findViewById(R.id.username);
        editTextEmail = findViewById(R.id.email);
        editTextDoB = findViewById(R.id.DoB);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.conferma_password);

        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender);
        radioGroupRegisterGender.clearCheck();

        text_loginNow = findViewById(R.id.loginNow);
        buttonReg = findViewById(R.id.btn_register);

        editTextDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        text_loginNow.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        buttonReg.setOnClickListener(v -> {

            database = FirebaseDatabase.getInstance();
            reference = database.getReference("Utenti registrati");

            int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
            radioButtonRegisterGenderSelected = findViewById(selectedGenderId);

            String textUsername = String.valueOf(editTextUsername.getText());
            String textEmail = String.valueOf(editTextEmail.getText());
            String textDoB = String.valueOf(editTextDoB.getText());
            String textConfermaPassword = String.valueOf(editTextConfirmPassword.getText());
            String textPassword = String.valueOf(editTextPassword.getText());
            String textGender;
            Boolean emailVerificato = false;

            if(TextUtils.isEmpty(textUsername)){
                Toast.makeText(RegisterActivity.this, "Inserisci il tuo username", Toast.LENGTH_SHORT).show();
                editTextUsername.setError("Username richiesto");
                editTextUsername.requestFocus();
                //return;
            } else if(TextUtils.isEmpty(textEmail)){
                Toast.makeText(RegisterActivity.this, "Inserisci la tua email", Toast.LENGTH_SHORT).show();
                editTextEmail.setError("Email richiesta");
                editTextEmail.requestFocus();
                //return;
            } else if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                Toast.makeText(RegisterActivity.this, "Re-inserisci la tua email", Toast.LENGTH_SHORT).show();
                editTextEmail.setError("Email valida richiesta");
                editTextEmail.requestFocus();
                //return;
            } else if(TextUtils.isEmpty(textDoB)){
                Toast.makeText(RegisterActivity.this, "Inserisci la tua data di nascita", Toast.LENGTH_SHORT).show();
                editTextDoB.setError("Data di nascita richiesta");
                editTextDoB.requestFocus();
                //return;
            } else if(radioGroupRegisterGender.getCheckedRadioButtonId() == -1){
                Toast.makeText(RegisterActivity.this, "Seleziona il tuo genere", Toast.LENGTH_SHORT).show();
                radioButtonRegisterGenderSelected.setError("Genere richiesto");
                radioButtonRegisterGenderSelected.requestFocus();
                //return;
            } else if(TextUtils.isEmpty(textPassword)){
                Toast.makeText(RegisterActivity.this, "Inserisci la tua password", Toast.LENGTH_SHORT).show();
                editTextPassword.setError("Password richiesta");
                editTextPassword.requestFocus();
                //return;
            } else if(textPassword.length() < 6){
                Toast.makeText(RegisterActivity.this, "La password deve essere di almeno 6 caratteri", Toast.LENGTH_SHORT).show();
                editTextPassword.setError("Password troppo corta");
                editTextPassword.requestFocus();
                //return;
            } else if(TextUtils.isEmpty(textConfermaPassword)){
                Toast.makeText(RegisterActivity.this, "Conferma la tua password", Toast.LENGTH_SHORT).show();
                editTextConfirmPassword.setError("Password di conferma richiesta");
                editTextConfirmPassword.requestFocus();
                //return;
            } else if(!textPassword.equals(textConfermaPassword)){
                Toast.makeText(RegisterActivity.this, "Re-inserisci la tua password", Toast.LENGTH_SHORT).show();
                editTextConfirmPassword.setError("Password di conferma richiesta");
                editTextConfirmPassword.requestFocus();
                editTextPassword.clearComposingText();
                editTextConfirmPassword.clearComposingText();
                //return;
            } else{
                textGender = String.valueOf(radioButtonRegisterGenderSelected.getText());
                registerUser(textUsername, textEmail, textDoB, textGender, textPassword, emailVerificato);
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser(String textUsername, String textEmail, String textDoB, String textGender, String textPassword, Boolean emailVerificato) {
        /*mAuth.createUserWithEmailAndPassword(textEmail, textPassword)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = mAuth.getCurrentUser();
                            Toast.makeText(RegisterActivity.this, "Registrazione effettuata con successo, verifica la tua email.", Toast.LENGTH_SHORT).show();
                            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textEmail, textUsername, textDoB, textGender);
                            reference.child(currentUser.getUid()).setValue(writeUserDetails);
                            currentUser.sendEmailVerification();
                            //Toast.makeText(RegisterActivity.this, "Registrazione effettuata con il successo. Verifica la tua email.", Toast.LENGTH_SHORT).show();
                        } else {
                            handleRegistrationError(task.getException());
                        }
                    }
                });*/

        mAuth.createUserWithEmailAndPassword(textEmail, textPassword)
                .addOnCompleteListener(RegisterActivity.this, task -> {
                    if (task.isSuccessful()) {
                        currentUser = mAuth.getCurrentUser();
                        Toast.makeText(RegisterActivity.this, "Registrazione effettuata con successo, verifica la tua email.", Toast.LENGTH_SHORT).show();
                        ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textEmail, textUsername, textDoB, textGender, emailVerificato);
                        reference.child(currentUser.getUid()).setValue(writeUserDetails);
                        currentUser.sendEmailVerification();
                        /*currentUser.reload().addOnCompleteListener(task1 -> {
                            if (currentUser.isEmailVerified()) {
                                // L'utente ha verificato l'email. Aggiorna il campo emailVerified nel database a true
                                reference.child("emailVerificato").setValue(true);
                            } else {
                                reference.child("emailVerificato").setValue(false);
                            }
                        });*/
                    } else {
                        handleRegistrationError(task.getException());
                    }
                });
    }



    private void handleRegistrationError(Exception exception) {
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            editTextPassword.setError("La tua password è troppo debole.");
            editTextPassword.requestFocus();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            editTextEmail.setError("L'email inserita non esiste oppure non è valida.");
            editTextEmail.requestFocus();
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            editTextEmail.setError("Questa email è già in uso.");
            editTextEmail.requestFocus();
        } else {
            Log.e(TAG, exception.getMessage());
            Toast.makeText(RegisterActivity.this, "Errore di registrazione: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}