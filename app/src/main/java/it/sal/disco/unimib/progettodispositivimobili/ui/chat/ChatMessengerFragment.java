package it.sal.disco.unimib.progettodispositivimobili.ui.chat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentChatMessengerBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.chat.messages.Message;
import it.sal.disco.unimib.progettodispositivimobili.ui.chat.messages.MessagesAdapter;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.other.DetailUserProfileFragment;

public class ChatMessengerFragment extends Fragment {

    private FragmentChatMessengerBinding binding;
    private String chatId;
    private String userId2;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatMessengerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getArguments() != null) {
            chatId = getArguments().getString("chatId");
            userId2 = getArguments().getString("userId2");
        }

        binding.buttonBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        if (userId2 == null) {
            loadChatDetails(chatId);
        } else {
            loadUserInfo(userId2);
        }

        loadMessages(chatId);

        binding.buttonSend.setOnClickListener(v -> {
            String message = binding.messageEt.getText().toString();
            if (message.isEmpty()) {
                Toast.makeText(getContext(), "Message field cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String date = simpleDateFormat.format(new Date());

            binding.messageEt.setText(""); // clearing the edit text
            sendMessage(chatId, message, date);
        });

        binding.profileIv.setOnClickListener(v -> openDetailUserProfileFragment(userId2));

        return root;
    }

    private void openDetailUserProfileFragment(String userId2) {
        DetailUserProfileFragment detailUserProfileFragment = new DetailUserProfileFragment();
        Bundle args = new Bundle();
        args.putString("user2", userId2);
        detailUserProfileFragment.setArguments(args);

        setChatVisibility(View.GONE);  // Hide chat views before the transaction

        getParentFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, detailUserProfileFragment)
                .addToBackStack(null)
                .commit();
    }

    private void setChatVisibility(int visibility) {
        binding.backBtn.setVisibility(visibility);
        binding.messagesRv.setVisibility(visibility);
        binding.messageEt.setVisibility(visibility);
        binding.sendMessageBtn.setVisibility(visibility);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadChatDetails(String chatId) {
        FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;

                        String userId1 = snapshot.child("user1").getValue(String.class);
                        userId2 = snapshot.child("user2").getValue(String.class);

                        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(userId1)) {
                            loadUserInfo(userId2);
                        } else {
                            loadUserInfo(userId1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load chat details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserInfo(String userId) {
        FirebaseDatabase.getInstance().getReference().child("Utenti registrati").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;

                        String userName = snapshot.child("username").getValue(String.class);
                        String profileImageUrl = snapshot.child("profileImage").getValue(String.class);

                        binding.nameTv.setText(userName);

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(requireContext()).load(profileImageUrl).into(binding.profileIv);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load user info", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMessage(String chatId, String message, String date) {
        if (chatId == null) return;

        HashMap<String, Object> messageInfo = new HashMap<>(); // Cambia il tipo del valore a Object
        messageInfo.put("text", message);
        messageInfo.put("ownerId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        messageInfo.put("date", date);
        messageInfo.put("isRead", false); // Nuovo messaggio non letto come booleano

        FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId)
                .child("messages").push().setValue(messageInfo);
    }


    private void loadMessages(String chatId) {
        if (chatId == null) return;

        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(chatId).child("messages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists() || binding == null) return;

                        List<Message> messages = new ArrayList<>();
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            String messageId = messageSnapshot.getKey();
                            String ownerId = messageSnapshot.child("ownerId").getValue(String.class);
                            String text = messageSnapshot.child("text").getValue(String.class);
                            String date = messageSnapshot.child("date").getValue(String.class);
                            Boolean isRead = messageSnapshot.child("isRead").getValue(Boolean.class);

                            // Aggiungi controlli di nullità
                            if (ownerId == null) ownerId = "";
                            if (text == null) text = "";
                            if (date == null) date = "";
                            if (isRead == null) isRead = false;

                            // Se è un messaggio non letto e non è stato inviato dall'utente corrente, impostalo come letto
                            if (!isRead && !ownerId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId)
                                        .child("messages").child(messageId).child("isRead").setValue(true);
                            }

                            messages.add(new Message(messageId, ownerId, text, date, isRead));
                        }

                        binding.messagesRv.setLayoutManager(new LinearLayoutManager(getContext()));
                        binding.messagesRv.setAdapter(new MessagesAdapter(messages));

                        // Scroll to the last message
                        if (messages.size() > 0) {
                            binding.messagesRv.scrollToPosition(messages.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load messages", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }



}
