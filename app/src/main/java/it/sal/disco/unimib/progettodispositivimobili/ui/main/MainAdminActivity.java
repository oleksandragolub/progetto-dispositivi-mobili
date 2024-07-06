package it.sal.disco.unimib.progettodispositivimobili.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.databinding.ActivityAdminMainBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsAvanzatoInfoFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.CharacterInfoFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsInfoFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.marvel.ApiClient;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.marvel.ApiService;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.marvel.Comix;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.marvel.ComicResponse;
import it.sal.disco.unimib.progettodispositivimobili.ui.start_app.LoginActivity;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.CategoryAddAdminFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.ComicsPdfDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.ComicsPdfEditFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.ComicsPdfViewFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.chat.chats.ChatsFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.home.HomeAdminFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.preferiti.PreferitiFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.own.ProfileFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.other.SearchUserFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainAdminActivity extends AppCompatActivity implements View.OnCreateContextMenuListener {

    private static final String PUBLIC_KEY = "93e5146b36c6609ec6a87d8104728ed2";
    private static final String PRIVATE_KEY = "80e7b32472204a8f30779ecb3e20815e84384d7b";
    private static final String TAG = "MainActivity";
    private ActivityAdminMainBinding binding;
    private FragmentManager fragmentManager;
    private GoogleSignInClient mGoogleSignInClient;
    BottomNavigationView bottomNavigationView;
    MaterialToolbar toolbar;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    private boolean isProfileFormComplete = false;

    private void configureGoogleSignIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    private void signOutFromGoogle() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());
        googleSignInClient.signOut().addOnCompleteListener(this, task -> Log.d("GoogleSignOut", "User signed out from Google"));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        configureGoogleSignIn();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        toolbar = findViewById(R.id.top_appbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_navigation_admin);
        bottomNavigationView.setBackground(null);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_admin_home) {
                    openFragment(new HomeAdminFragment());
                    return true;
                } else if (id == R.id.navigation_category_admin) {
                    openFragment(new CategoryAddAdminFragment());
                    return true;
                } else if (id == R.id.navigation_edit_comics) {
                    openFragment(new ComicsPdfEditFragment());
                    return true;
                } else if (id == R.id.navigation_detail_comics) {
                    openFragment(new ComicsPdfDetailFragment());
                    return true;
                } else if (id == R.id.navigation_view_comics) {
                    openFragment(new ComicsPdfViewFragment());
                    return true;
                } else if (id == R.id.navigation_ricerca) {
                    openFragment(new ComicsInfoFragment());
                    return true;
                } else if (id == R.id.navigation_preferiti) {
                    openFragment(new PreferitiFragment());
                    return true;
                } else if (id == R.id.navigation_profile) {
                    openFragment(new ProfileFragment());
                    return true;
                } else if (id == R.id.searchUserFragment) {
                    openFragment(new SearchUserFragment());
                    return true;
                } else if (id == R.id.chatsFragment) {
                    openFragment(new ChatsFragment());
                    return true;
                } else if (id == R.id.navigation_character_info) {
                    openFragment(new CharacterInfoFragment());
                    return true;
                } else if (id == R.id.navigation_comics_avanzato) {
                    openFragment(new ComicsAvanzatoInfoFragment());
                    return true;
                }
                return false;
            }
        });

        fragmentManager = getSupportFragmentManager();
        if (currentUser == null) {
            navigateToLogin();
        } else {
            // Carica il frammento di default
            openFragment(new HomeAdminFragment());
        }


        if(currentUser == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }


        // Verifica se l'intent contiene l'extra per mostrare direttamente il ProfileFragment
        if (getIntent().getBooleanExtra("showProfileFragment", false)) {
            openFragment(new ProfileFragment());
            // Imposta anche l'elemento della BottomNavigationView su quello corrispondente, se necessario
            bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        }

        //metto temporaneamente qua, serve per configurare l'api e prelevare il contenuto
        // ma non so ancora come salvare quello che è stato trovato e se funziona
        //incluse le api-key che non vanno inserite direttamente nel codice ma non so come.
        /*
        private static final String publicKey = "0b15cb829ed0192799209be00f95e553";
        private static final String privateKey = "966d2baf127271cc1af5bff030e9998be0df51af";

        MarvelApiConfig marvelApiConfig = new MarvelApiConfig.Builder(publicKey, privateKey).debug().build();
        ComicApiClient comicApiClient = new ComicApiClient(marvelApiConfig);
        ComicsQuery query = ComicsQuery.Builder.create().withOffset(0).withLimit(10).build();
        MarvelResponse<ComicsDto> all = comicApiClient.getAll(query);
         */

        long ts = System.currentTimeMillis();
        String hash = getMd5(ts + PRIVATE_KEY + PUBLIC_KEY);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ComicResponse> call = apiService.getComics(ts, PUBLIC_KEY, hash, "comic", "comic", true, "thisMonth", "", 100);
        call.enqueue(new Callback<ComicResponse>() {
            @Override
            public void onResponse(Call<ComicResponse> call, Response<ComicResponse> response) {
                if (response.isSuccessful()) {
                    List<Comix> comixes = response.body().getData().getResults();
                    if (comixes.isEmpty()) {
                        Log.e(TAG, "No comixes found");
                    } else {
                        // Visualizza i fumetti come desideri, ad esempio:
                        StringBuilder builder = new StringBuilder();
                        for (Comix comix : comixes) {
                            builder.append(comix.getTitle()).append("\n");
                        }
                        Log.d(TAG, builder.toString());
                        // textView.setText(builder.toString()); // Aggiorna questa linea per mostrare i risultati nel layout
                    }
                } else {
                    Log.e(TAG, "Request not successful");
                }
            }

            @Override
            public void onFailure(Call<ComicResponse> call, Throwable t) {
                Log.e(TAG, "Request failed", t);
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainAdminActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }




   private void openFragment(Fragment fragment) {
       FragmentTransaction transaction = fragmentManager.beginTransaction();
       transaction.replace(R.id.nav_host_fragment, fragment);
       transaction.addToBackStack(null);
       transaction.commit();
   }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_custom_icon) {
            View menuItemView = findViewById(R.id.action_custom_icon);
            PopupMenu popupMenu = new PopupMenu(this, menuItemView);
            popupMenu.inflate(R.menu.popup_menu);
            popupMenu.setOnMenuItemClickListener(this::handleMenuSelection);
            popupMenu.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean handleMenuSelection(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.navigation_preferiti) {
            openFragment(new PreferitiFragment());
            //return true;
        } else if(id == R.id.searchUserFragment) {
            openFragment(new SearchUserFragment());
            //return true;
        } else if (id == R.id.chatsFragment) {
            openFragment(new ChatsFragment());
            //return true;
        } else if (id == R.id.navigation_character_info) {
            openFragment(new CharacterInfoFragment());
            //return true;
        } else if (id == R.id.navigation_comics_avanzato) {
            openFragment(new ComicsAvanzatoInfoFragment());
            //return true;
        } else if (id == R.id.navigation_logout) {
            // Effettua il logout da Firebase Auth
            FirebaseAuth.getInstance().signOut();
            // Effettua il logout da Google Sign-In
            signOutFromGoogle();
            // Passa alla LoginActivity
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}