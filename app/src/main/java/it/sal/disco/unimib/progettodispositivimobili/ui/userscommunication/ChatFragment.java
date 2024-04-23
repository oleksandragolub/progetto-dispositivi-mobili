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


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentChatBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.ProfileFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.ricerca.user.SearchUserFragment;


public class ChatFragment extends Fragment {
    FragmentChatBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private GoogleSignInClient mGoogleSignInClient;

    private DatabaseReference databaseReference;
    private ArrayList<Message> messagesList;
    private MessagesAdapter adapter;
    private String currentUserId, otherUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        setupGoogleSignIn();

        if (getArguments() != null) {
            otherUserId = getArguments().getString("otherUserId");
        }

        // LinearLayoutManager with stackFromEnd set to true to show latest messages at the bottom
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        messagesList = new ArrayList<>();
        adapter = new MessagesAdapter(getContext(), messagesList);
        binding.chatRecyclerView.setAdapter(adapter);

        currentUserId = currentUser.getUid();
        String chatRoom = currentUserId + otherUserId;
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats").child(chatRoom).child("Messages");

        loadMessages();
        setupSendMessage();

        binding.imageBack.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new SearchUserFragment());
            }
        });

        return root;
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
    }

    private void loadMessages() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messagesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        messagesList.add(message);
                    }
                }
                adapter.notifyDataSetChanged();
                binding.chatRecyclerView.scrollToPosition(messagesList.size() - 1);
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
        reference.setValue(newMessage).addOnSuccessListener(aVoid -> {
            messagesList.add(newMessage);
            adapter.notifyItemInserted(messagesList.size() - 1);
            binding.chatRecyclerView.smoothScrollToPosition(messagesList.size() - 1);
        });
    }*/

    private void sendMessage(String senderId, String receiverId, String message) {
        DatabaseReference reference = databaseReference.push();
        long timestamp = System.currentTimeMillis();
        Message newMessage = new Message(senderId, receiverId, message, timestamp);
        reference.setValue(newMessage); // Rimuovi la parte che aggiunge manualmente alla lista
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