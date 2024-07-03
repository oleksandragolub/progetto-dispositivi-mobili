package it.sal.disco.unimib.progettodispositivimobili.ui.start_app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.ui.main.MainActivity;
import it.sal.disco.unimib.progettodispositivimobili.ui.main.MainAdminActivity;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference reference;

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    TextView text_ForgotPassword, text_registerNow;


    private void signIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button btnGoogleSignIn = findViewById(R.id.btn_login_google);

        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            activityResultLauncher.launch(signInIntent);
        });
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                        firebaseAuthWithGoogle(signInAccount);
                    } catch (ApiException e) {
                        // Gestione dell'errore di autenticazione
                        handleSignInError(e);
                    }
                } else {
                    // Gestione della situazione in cui il risultato non è OK
                    Toast.makeText(LoginActivity.this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    private void handleSignInError(ApiException e) {
        // Ottieni il codice di errore e prepara un messaggio di errore appropriato
        int statusCode = e.getStatusCode();
        String errorMessage = "Failed to sign in: ";
        switch (statusCode) {
            case GoogleSignInStatusCodes.NETWORK_ERROR:
                errorMessage += "Network error";
                break;
            case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                errorMessage += "Sign in cancelled";
                break;
            case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                errorMessage += "Sign in failed";
                break;
            // Aggiungi altri casi se necessario
            default:
                errorMessage += "Unknown error";
                break;
        }

        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
        Log.e("GoogleSignIn", "Sign in error: " + e.getStatusCode());
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account){
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                currentUser = mAuth.getCurrentUser();

                // Verifica se l'utente esiste già
                reference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            // L'utente esiste, quindi si sta accedendo
                            Toast.makeText(LoginActivity.this, "Accesso nel proprio account effettuato con successo!", Toast.LENGTH_SHORT).show();
                        } else {
                            // L'utente non esiste, è una nuova registrazione
                            String uid = currentUser.getUid();
                            String textUsername = String.valueOf(account.getDisplayName());
                            String textEmail = String.valueOf(account.getEmail());
                            String textDoB = "";
                            String textGender = "";
                            Boolean emailVerificato = true;

                            // Aggiorna il database Firebase con i dettagli dell'utente
                            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(uid, textEmail, textUsername, textDoB, textGender, emailVerificato, "Google", "user", "");
                            // il tipo dell'utente puo' essere user oppure admin (inserito manualmente da console firebase)
                            reference.child(currentUser.getUid()).setValue(writeUserDetails);
                            Toast.makeText(LoginActivity.this, "Registrazione tramite Google effettuata con successo!", Toast.LENGTH_SHORT).show();
                        }
                        updateUI(currentUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Gestisci l'errore
                        Log.e(TAG, "Database error: " + databaseError.getMessage());
                    }
                });
            } else {
                // Gestisce gli errori di autenticazione, inclusi eventuali problemi di rete o credenziali errate
                if (task.getException() instanceof ApiException) {
                    ApiException apiException = (ApiException) task.getException();
                    handleSignInError(apiException);
                } else {
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(FirebaseUser currentUser){
        if (currentUser == null) {
            Log.e(TAG, "Tentativo di aggiornare UI quando currentUser è null");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Utenti registrati").child(currentUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ReadWriteUserDetails userDetails = dataSnapshot.getValue(ReadWriteUserDetails.class);
                    if (userDetails != null) {
                        if ("admin".equals(userDetails.getUserType())) {
                            // L'utente è un admin, avvia MainAdminActivity
                            Intent adminIntent = new Intent(LoginActivity.this, MainAdminActivity.class);
                            startActivity(adminIntent);
                            finish();
                        } else {
                            // L'utente non è un admin, avvia MainActivity
                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                        }
                    } else {
                        Log.e(TAG, "Dettagli utente non trovati nonostante dataSnapshot esista");
                        startMainActivity();
                    }
                } else {
                    Log.e(TAG, "Snapshot non esiste");
                    startMainActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Errore nel database: " + databaseError.getMessage());
                startMainActivity();
            }
        });
    }


    private void startMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Utenti registrati");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if(currentUser==null){
            signIn();
        } else {
            updateUI(currentUser);
        }

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        text_ForgotPassword = findViewById(R.id.text_forgot_pw);
        text_registerNow = findViewById(R.id.registerNow);
        buttonLogin = findViewById(R.id.btn_login);

        text_ForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
            startActivity(intent);
            finish();
        });

        text_registerNow.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        buttonLogin.setOnClickListener(v -> {
            String textEmail = String.valueOf(editTextEmail.getText());
            String textPassword = String.valueOf(editTextPassword.getText());

            //Questo pezzo di codice non serve più, questo caso è impossibile con il nuovo textWatcher (vedi sotto)
            /*
                if(TextUtils.isEmpty(textEmail)){
                    Toast.makeText(LoginActivity.this, "Inserisci la tua email", Toast.LENGTH_SHORT).show();
                    editTextEmail.setError("Email richiesta");
                    editTextEmail.requestFocus();
                    //return;
                }
             */
            if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                Toast.makeText(LoginActivity.this, "Re-inserisci la tua email", Toast.LENGTH_SHORT).show();
                editTextEmail.setError("Email valida richiesta");
                editTextEmail.requestFocus();
                //return;
            }
            //Codice superfluo #2
            /*
            else if(TextUtils.isEmpty(textPassword)){
                Toast.makeText(LoginActivity.this, "Inserisci la tua password", Toast.LENGTH_SHORT).show();
                editTextPassword.setError("Password richiesta");
                editTextPassword.requestFocus();
                //return;
            }
             */
             else {
                loginUser(textEmail, textPassword);
            }
        });

        editTextEmail.addTextChangedListener(textWatcher);
        editTextPassword.addTextChangedListener(textWatcher);
    }

    //Abilita/disabilita il bottone login se email e password sono inserite/vuote
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String textEmailWatcher = String.valueOf(editTextEmail.getText());
            String textPasswordWatcher = String.valueOf(editTextPassword.getText());

            buttonLogin.setEnabled(!textEmailWatcher.isEmpty() && !textPasswordWatcher.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            if (currentUser.isEmailVerified()) {
                                // Update the emailVerificato field in the database
                                updateEmailVerificationStatus(currentUser.getUid(), true);
                                // Proceed to main activity or user profile
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                // Inform user to verify email and do not proceed to main activity
                                showVerificationAlert();
                                mAuth.signOut(); // Optional: sign out user until they verify their email
                            }
                        }
                    } else {
                        // Handle failed login
                        handleLoginError(task.getException());
                    }
                });
    }

    private void updateEmailVerificationStatus(String userId, boolean isVerified) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Utenti registrati").child(userId);
        userRef.child("emailVerificato").setValue(isVerified)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Email verification status updated in database."))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update email verification status.", e));
    }

    private void showVerificationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email non verificata");
        builder.setMessage("Per favore verifica la tua email prima di accedere. Controlla la tua casella di posta per il link di verifica.");

        // Bottone "Cancella" per chiudere il dialog
        builder.setNegativeButton("Cancella", (dialog, which) -> dialog.dismiss());

        // Bottone "Invia di nuovo" per re-inviare il messaggio di verifica
        builder.setPositiveButton("Invia di nuovo", (dialog, which) -> {
            currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                currentUser.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Email di verifica inviata.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(LoginActivity.this, "Errore nell'invio dell'email di verifica.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void handleLoginError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            editTextEmail.setError("L'account non esiste oppure non è più valido.");
            editTextEmail.requestFocus();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            editTextEmail.setError("Le credenziali inserite sono sbagliate. Controlla bene e riprova.");
            editTextEmail.requestFocus();
            editTextPassword.setError("Le credenziali inserite sono sbagliate. Controlla bene e riprova.");
            editTextPassword.requestFocus();
        } else {
            Log.e(TAG, Objects.requireNonNull(exception.getMessage()));
            Toast.makeText(LoginActivity.this, "Errore di login: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}