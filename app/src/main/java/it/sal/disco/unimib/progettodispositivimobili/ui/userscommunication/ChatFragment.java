package it.sal.disco.unimib.progettodispositivimobili.ui.userscommunication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentChatBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.ProfileFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.ricerca.user.SearchUserFragment;


public class ChatFragment extends Fragment {
        FragmentChatBinding binding;
        private DatabaseReference databaseReference;
        private ArrayList<Message> messagesList;
        private MessagesAdapter adapter;
        private String currentUserId, otherUserId;

        private ImageButton imageBack;

        TextView btnBack;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {

            binding = FragmentChatBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            if (getArguments() != null) {
                otherUserId = getArguments().getString("otherUserId");
            }

            binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            // Inizializzazione dell'ArrayList dei messaggi
            messagesList = new ArrayList<>();

            // Imposta l'adapter per il RecyclerView
            adapter = new MessagesAdapter(getContext(), messagesList);
            binding.chatRecyclerView.setAdapter(adapter); // Assicurati che chatRecyclerView sia l'ID corretto

            // Inizializza il riferimento al database di Firebase
            databaseReference = FirebaseDatabase.getInstance().getReference("Messages");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // otherUserId dovrebbe essere ottenuto dall'intento o passato al fragment

            loadMessages();
            setupSendMessage();

            binding.imageBack.setOnClickListener(v -> {
                if(getActivity() != null) {
                    openFragment(new SearchUserFragment());
                }
            });


            return root;
        }

    private void loadMessages() {
        messagesList = new ArrayList<>();
        // Supponiamo che adapter sia già stato inizializzato e impostato su RecyclerView
        // Aggiungi ValueEventListener al databaseReference per caricare i messaggi e ascoltare le nuove aggiunte

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messagesList.clear();
                Message message = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    message = snapshot.getValue(Message.class);
                    if ((message.getSenderId().equals(currentUserId) && message.getReceiverId().equals(otherUserId)) ||
                            (message.getSenderId().equals(otherUserId) && message.getReceiverId().equals(currentUserId))) {
                        messagesList.add(message);
                    }
                }
                adapter.notifyDataSetChanged();
                Log.d("ChatFragment", "Loaded message: " + message.getMessage());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load messages.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSendMessage() {
        binding.buttonSend.setOnClickListener(v -> {
            String msg = binding.inputMessage.getText().toString();
            if (!msg.isEmpty()) {
                sendMessage(currentUserId, otherUserId, msg);
                binding.inputMessage.setText("");
            }
        });
    }

   /* private void sendMessage(String senderId, String receiverId, String message) {
        DatabaseReference reference = databaseReference.push();
        long timestamp = System.currentTimeMillis();
        Message newMessage = new Message(senderId, receiverId, message, timestamp);
        reference.setValue(newMessage);
    }*/

    private void sendMessage(String senderId, String receiverId, String message) {
        DatabaseReference reference = databaseReference.push();
        long timestamp = System.currentTimeMillis();
        Message newMessage = new Message(senderId, receiverId, message, timestamp);
        reference.setValue(newMessage).addOnSuccessListener(aVoid -> {
            messagesList.add(newMessage);
            adapter.notifyItemInserted(messagesList.size() - 1);
            binding.chatRecyclerView.smoothScrollToPosition(messagesList.size() - 1);
        });
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