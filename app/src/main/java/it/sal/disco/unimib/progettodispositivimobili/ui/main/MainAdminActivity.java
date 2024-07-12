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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsAvanzatoInfoFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsMarvelDetailFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsMarvelViewFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.ComicsAdminFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_admin.ComicsApiAdminFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsApiUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.fragments_user.ComicsUserFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.characters.CharacterInfoFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.api_comics.ComicsInfoFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.other.DetailUserProfileFragment;
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

public class MainAdminActivity extends AppCompatActivity implements View.OnCreateContextMenuListener {

    private static final String TAG = "MainAdminActivity";
    private FragmentManager fragmentManager;
    private GoogleSignInClient mGoogleSignInClient;
    private BottomNavigationView bottomNavigationView;
    private MaterialToolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
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
                } else if (id == R.id.detailUserProfileFragment) {
                    openFragment(new DetailUserProfileFragment());
                    return true;
                } else if (id == R.id.navigation_character_info) {
                    openFragment(new CharacterInfoFragment());
                    return true;
                } else if (id == R.id.navigation_comics_avanzato) {
                    openFragment(new ComicsAvanzatoInfoFragment());
                    return true;
                } else if (id == R.id.nav_marvel_comics_detail) {
                    openFragment(new ComicsMarvelDetailFragment());
                    return true;
                } else if (id == R.id.navigation_api_comics_admin) {
                    openFragment(new ComicsApiAdminFragment());
                    return true;
                } else if (id == R.id.navigation_comics_admin) {
                    openFragment(new ComicsAdminFragment());
                    return true;
                }
                return false;
            }
        });

        if(currentUser == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        fragmentManager = getSupportFragmentManager();
        if (currentUser == null) {
            navigateToLogin();
        } else {
            if (savedInstanceState == null) {
                openFragment(new HomeAdminFragment());
            }
        }

        // Verifica se l'intent contiene l'extra per mostrare direttamente il ProfileFragment
        if (getIntent().getBooleanExtra("showProfileFragment", false)) {
            openFragment(new ProfileFragment());
            // Imposta anche l'elemento della BottomNavigationView su quello corrispondente, se necessario
            bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);  // Use the correct ID
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
        } else if (id == R.id.nav_marvel_comics_detail) {
            openFragment(new ComicsMarvelDetailFragment());
            //return true;
        } else if (id == R.id.navigation_api_comics_user) {
            openFragment(new ComicsApiUserFragment());
            //return true;
        } else if (id == R.id.navigation_comics_user) {
            openFragment(new ComicsUserFragment());
            //return true;
        } else if (id == R.id.navigation_logout) {
            logOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

   /* private void logOut() {
        FirebaseAuth.getInstance().signOut();
        signOutFromGoogle();
        navigateToLogin();
    }*/

    private void logOut() {
        // Rimuovi i listener del database prima del logout
       /* ProfileFragment profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (profileFragment != null) {
            profileFragment.removeFirebaseListeners();
        }*/

        FirebaseAuth.getInstance().signOut();
        signOutFromGoogle();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainAdminActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}