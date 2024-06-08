package it.sal.disco.unimib.progettodispositivimobili.ui.chat.new_chats;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.ui.profile.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentNewChatBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.chat.users.UserAdapter;

public class NewChatFragment extends Fragment {

    private FragmentNewChatBinding binding;
    private ArrayList<ReadWriteUserDetails> users = new ArrayList<>();
    private UserAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewChatBinding.inflate(inflater, container, false);

        adapter = new UserAdapter(users);
        binding.usersRv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.usersRv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        binding.usersRv.setAdapter(adapter);
        //binding.usersRv.setAdapter(new UsersAdapter(users));

        loadUsers();
        adapter.notifyDataSetChanged();
        binding.usersRv.invalidate();

        return binding.getRoot();
    }

    private void loadUsers() {

        FirebaseDatabase.getInstance().getReference().child("Utenti registrati").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    users.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            continue;
                        }
                        String uid = snapshot.getKey();
                        ReadWriteUserDetails user = snapshot.getValue(ReadWriteUserDetails.class);
                        users.add(new ReadWriteUserDetails (uid, user.getUsername(), user.getEmail(), user.getProfileImage()));
                    }
                    adapter.notifyDataSetChanged();
                    Log.d("SearchUserFragment", "Numero di utenti trovati: " + users.size());
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

