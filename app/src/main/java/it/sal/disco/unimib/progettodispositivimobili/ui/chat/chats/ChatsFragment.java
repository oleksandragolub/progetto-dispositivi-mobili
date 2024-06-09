package it.sal.disco.unimib.progettodispositivimobili.ui.chat.chats;

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

    private void loadChats(){
        ArrayList<Chat> chats = new ArrayList<>();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot chatsNode = snapshot.child("Utenti registrati").child(uid).child("chats");
                if (!chatsNode.exists() || chatsNode.getValue() == null) {
                    Toast.makeText(getContext(), "No chats available", Toast.LENGTH_SHORT).show();
                    return;
                }

                String chatsStr = chatsNode.getValue().toString();
                String[] chatsIds = chatsStr.split(",");
                if (chatsIds.length == 0) return;

                for (String chatId : chatsIds) {
                    DataSnapshot chatSnapshot = snapshot.child("Chats").child(chatId);
                    String userId1 = chatSnapshot.child("user1").getValue(String.class);
                    String userId2 = chatSnapshot.child("user2").getValue(String.class);

                    if (userId1 == null || userId2 == null) {
                        continue;
                    }

                    String chatUserId = (uid.equals(userId1)) ? userId2 : userId1;
                    DataSnapshot userNode = snapshot.child("Utenti registrati").child(chatUserId);
                    String chatName = userNode.child("username").getValue(String.class);
                    String userEmail = userNode.child("email").getValue(String.class);

                    if (chatName == null || userEmail == null) {
                        continue;
                    }

                    // Recupera l'ultimo messaggio
                    DataSnapshot messagesNode = chatSnapshot.child("messages");
                    String lastMessage = "";
                    String lastMessageOwnerId = "";
                    Boolean lastMessageRead = true;
                    if (messagesNode.exists()) {
                        for (DataSnapshot messageSnapshot : messagesNode.getChildren()) {
                            lastMessage = messageSnapshot.child("text").getValue(String.class);
                            lastMessageOwnerId = messageSnapshot.child("ownerId").getValue(String.class);
                            lastMessageRead = messageSnapshot.child("isRead").getValue(Boolean.class);

                            if (lastMessage == null) lastMessage = "";
                            if (lastMessageOwnerId == null) lastMessageOwnerId = "";
                            if (lastMessageRead == null) lastMessageRead = true;
                        }
                    }

                    Chat chat = new Chat(chatId, chatName, userId1, userId2, lastMessage, userEmail);
                    chat.setLastMessageOwnerId(lastMessageOwnerId);  // Imposta l'ID del proprietario dell'ultimo messaggio
                    chat.setLastMessageRead(lastMessageRead);  // Imposta lo stato di lettura dell'ultimo messaggio
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
