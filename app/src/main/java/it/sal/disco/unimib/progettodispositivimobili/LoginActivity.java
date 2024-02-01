package it.sal.disco.unimib.progettodispositivimobili;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

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

        AppCompatButton btnGoogleSignIn = findViewById(R.id.btn_login_google);

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

                String textUsername = "";
                String textEmail = String.valueOf(account.getEmail());
                String textDoB = "";
                String textGender = "";
                Boolean emailVerificato = true;

                // Aggiorna il database Firebase per segnare l'email dell'utente come verificata
                ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textEmail, textUsername, textDoB, textGender, emailVerificato);
                reference.child(currentUser.getUid()).setValue(writeUserDetails);
                Toast.makeText(LoginActivity.this, "Registrazione tramite Google effettuata con successo!", Toast.LENGTH_SHORT).show();
                updateUI(currentUser);
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
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
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

            if(TextUtils.isEmpty(textEmail)){
                Toast.makeText(LoginActivity.this, "Inserisci la tua email", Toast.LENGTH_SHORT).show();
                editTextEmail.setError("Email richiesta");
                editTextEmail.requestFocus();
                //return;
            } else if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                Toast.makeText(LoginActivity.this, "Re-inserisci la tua email", Toast.LENGTH_SHORT).show();
                editTextEmail.setError("Email valida richiesta");
                editTextEmail.requestFocus();
                //return;
            } else if(TextUtils.isEmpty(textPassword)){
                Toast.makeText(LoginActivity.this, "Inserisci la tua password", Toast.LENGTH_SHORT).show();
                editTextPassword.setError("Password richiesta");
                editTextPassword.requestFocus();
                //return;
            } else {
                loginUser(textEmail, textPassword);
            }
        });
    }

    private void loginUser(String textEmail, String textPassword) {
        mAuth.signInWithEmailAndPassword(textEmail, textPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser = mAuth.getCurrentUser();
                        // Controlla che l'email è stata verificata prima che l'utente riesce di entrare nel suo account
                        if (Objects.requireNonNull(currentUser).isEmailVerified()) {
                            // L'utente ha verificato l'email. Aggiorna il campo emailVerified nel database a true
                            reference.child(currentUser.getUid()).child("emailVerificato").setValue(true);
                            Toast.makeText(getApplicationContext(), "Login effettuato con il successo!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            reference.child(currentUser.getUid()).child("emailVerificato").setValue(false);
                            showAlertDialog();
                            mAuth.signOut();
                        }

                    } else {
                        handleLoginError(task.getException());
                    }
                });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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