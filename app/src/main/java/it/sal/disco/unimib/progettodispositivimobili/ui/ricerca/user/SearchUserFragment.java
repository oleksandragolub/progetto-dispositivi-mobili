package it.sal.disco.unimib.progettodispositivimobili.ui.ricerca.user;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentSearchUserBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.ProfileFragment;

public class SearchUserFragment extends Fragment implements SearchUserRecyclerAdapter.OnUserClickListener{

    private List<ReadWriteUserDetails> searchList = new ArrayList<>();
    RecyclerView recyclerView;
    SearchUserRecyclerAdapter dataAdapter;

    FragmentSearchUserBinding binding;
    TextInputEditText searchInput;
    ImageButton searchButton;
    TextView btnBack;

    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inizializzazione
        btnBack = binding.txtBack;
        searchInput = binding.textViewUsername;
        searchButton = binding.imageSearchUser;
        recyclerView = binding.cercaUsernameRecyclerView;

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Utenti registrati");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dataAdapter = new SearchUserRecyclerAdapter(searchList, (SearchUserRecyclerAdapter.OnUserClickListener) this); // Passa this come listener
        recyclerView.setAdapter(dataAdapter);


        searchButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ricerca in corso...", Toast.LENGTH_SHORT).show();
            performSearch();
        });

        btnBack.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new ProfileFragment());
            }
        });

        return root;
    }

    private void performSearch() {
        String searchTerm = searchInput.getText().toString().trim().toLowerCase();
        if(searchTerm.isEmpty() || searchTerm.length() < 2) {
            searchInput.setError("Inserisci un'email valida");
        } else {
            reference.orderByChild("email")
                    .startAt(searchTerm).endAt(searchTerm + "\uf8ff")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                searchList.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    ReadWriteUserDetails user = snapshot.getValue(ReadWriteUserDetails.class);
                                    searchList.add(user);
                                }
                                dataAdapter.notifyDataSetChanged();
                                Log.d("SearchUserFragment", "Numero di utenti trovati: " + searchList.size());
                            } else {
                                Log.d("SearchUserFragment", "Nessun utente trovato.");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Errore nella ricerca", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onUserClick(ReadWriteUserDetails user) {
        DetailUserProfileFragment profileFragment = new DetailUserProfileFragment();
        Bundle args = new Bundle();
        args.putString("userId", user.getUserId()); // Assicurati che getUserId() non restituisca null
        profileFragment.setArguments(args);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, profileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        Log.d("SearchUserFragment", "Passing User ID: " + user.getUserId());
    }

    private void openProfilePage(ReadWriteUserDetails user) {
        // Crea una nuova istanza della pagina di profilo e passa i dettagli dell'utente
        DetailUserProfileFragment profileFragment = new DetailUserProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable("userDetails", user);
        profileFragment.setArguments(args);

        // Sostituisci il fragment corrente con la pagina di profilo
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, profileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
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


