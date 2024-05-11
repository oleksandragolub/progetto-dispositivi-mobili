package it.sal.disco.unimib.progettodispositivimobili.ui.chats;

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

import it.sal.disco.unimib.progettodispositivimobili.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentChatsBinding;

public class ChatsFragment extends Fragment {
    private FragmentChatsBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        loadChats();

        return binding.getRoot();
    }

     /*  ArrayList<Chat> chats = new ArrayList<>();
        chats.add(new Chat("123", "Test chat 1", "12323", "123456"));
        chats.add(new Chat("123", "Test chat 2", "123123", "123456"));
        chats.add(new Chat("123", "Test chat 3", "123123", "123456"));*/
        private void loadChats(){
            ArrayList<Chat> chats = new ArrayList<>();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // This should also be checked for null in real-world apps.
            FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ReadWriteUserDetails userDetails = snapshot.getValue(ReadWriteUserDetails.class);
                    DataSnapshot chatsNode = snapshot.child("Utenti registrati").child(uid).child("chats");
                    if (!chatsNode.exists() || chatsNode.getValue() == null) {
                        Toast.makeText(getContext(), "No chats available", Toast.LENGTH_SHORT).show();
                        return; // No data available, exit the method early
                    }

                    String chatsStr = chatsNode.getValue().toString();
                    String[] chatsIds = chatsStr.split(",");
                    if (chatsIds.length == 0) return; // No chat IDs to process, exit early

                    for (String chatId : chatsIds) {
                        DataSnapshot chatSnapshot = snapshot.child("Chats").child(chatId);
                        String userId1 = chatSnapshot.child("user1").getValue(String.class); // Safely get the value as String
                        String userId2 = chatSnapshot.child("user2").getValue(String.class); // Safely get the value as String

                        if (userId1 == null || userId2 == null) {
                            continue; // Missing user data, skip this chat
                        }

                        String chatUserId = (uid.equals(userId1)) ? userId2 : userId1;
                        DataSnapshot userNode = snapshot.child("Utenti registrati").child(chatUserId);
                        String chatName = userNode.child("username").getValue(String.class); // Safely get the value as String

                        String userEmail = userNode.child("email").getValue(String.class);
                        Log.d("ChatsFragment", "Email utente: " + userEmail);

                        if (chatName == null || userEmail == null) {
                            continue; // Username is missing, skip this chat
                        }

                        Chat chat = new Chat(chatId, chatName, userId1, userId2, userEmail);
                        chats.add(chat);
                    }

                    binding.chatsRv.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.chatsRv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
                    binding.chatsRv.setAdapter(new ChatAdapter(chats));
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to get user chats", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
