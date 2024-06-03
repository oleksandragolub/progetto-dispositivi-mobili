package it.sal.disco.unimib.progettodispositivimobili;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import it.sal.disco.unimib.progettodispositivimobili.databinding.ActivityMainBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.CategoryUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsPdfDetailUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsPdfListUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsPdfViewUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.chats.ChatsFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.home.HomeFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.new_chat.NewChatFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.preferiti.PreferitiFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.ProfileFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.ricerca.comics.RicercaFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.ricerca.user.SearchUserFragment;

public class MainActivity extends AppCompatActivity implements View.OnCreateContextMenuListener {

    private ActivityMainBinding binding;
    private FragmentManager fragmentManager;
    private GoogleSignInClient mGoogleSignInClient;
    BottomNavigationView bottomNavigationView;
    MaterialToolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //variabile per tracciare se il form del profilo è stato completato
    private boolean isProfileFormComplete = false;

    private void configureGoogleSignIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    private void signOutFromGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> Log.d("GoogleSignOut", "User signed out from Google"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureGoogleSignIn();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        toolbar = findViewById(R.id.top_appbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackground(null);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    openFragment(new HomeFragment());
                    return true;
                } else if (id == R.id.navigation_category_user) {
                    openFragment(new CategoryUserFragment());
                    return true;
                } else if (id == R.id.navigation_comics_user) {
                    openFragment(new ComicsPdfListUserFragment());
                    return true;
                } else if (id == R.id.navigation_detail_comics_user) {
                    openFragment(new ComicsPdfDetailUserFragment());
                    return true;
                } else if (id == R.id.navigation_view_comics_user) {
                    openFragment(new ComicsPdfViewUserFragment());
                    return true;
                } else if (id == R.id.navigation_preferiti) {
                    openFragment(new PreferitiFragment());
                    return true;
                } else if (id == R.id.navigation_ricerca) {
                    openFragment(new RicercaFragment());
                    return true;
                } else if (id == R.id.navigation_profile) {
                    openFragment(new ProfileFragment());
                    return true;
                } else if (id == R.id.searchUserFragment) {
                    openFragment(new SearchUserFragment());
                    return true;
                } else if (id == R.id.newChatFragment) {
                    openFragment(new NewChatFragment());
                    return true;
                } else if (id == R.id.chatsFragment) {
                    openFragment(new ChatsFragment());
                    return true;
                }
                return false;
            }
        });

        fragmentManager = getSupportFragmentManager();
        openFragment(new HomeFragment());

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

    }


    private void openFragment(Fragment fragment){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null); // Aggiungi il frammento al back stack
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
        } else if (id == R.id.searchUserFragment) {
            openFragment(new SearchUserFragment());
            //return true;
        } else if (id == R.id.newChatFragment) {
            openFragment(new NewChatFragment());
            //return true;
        } else if (id == R.id.chatsFragment) {
            openFragment(new ChatsFragment());
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