package it.sal.disco.unimib.progettodispositivimobili.ui.chat.chats;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.chat.ChatMessengerFragment;

public class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder> {

    private ArrayList<Chat> chats;

    public ChatAdapter(ArrayList<Chat> chats) {
        this.chats = chats;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user_chat_recycler_row, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.chat_username_iv.setText(chats.get(position).getChat_name());

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userId;
        if (!chats.get(position).getUserId1().equals(currentUserId)) {
            userId = chats.get(position).getUserId1();
        } else {
            userId = chats.get(position).getUserId2();
        }

        String lastMessage = chats.get(position).getLastMessage();
        String lastMessageOwnerId = chats.get(position).getLastMessageOwnerId();
        if (lastMessage != null) {
            if (lastMessageOwnerId.equals(currentUserId)) {
                holder.last_message.setText("Tu: " + lastMessage);
            } else {
                holder.last_message.setText(chats.get(position).getChat_name() + ": " + lastMessage);
            }
        } else {
            holder.last_message.setText("");
        }

        // Controlla se ci sono nuovi messaggi non letti
        if (lastMessageOwnerId.equals(currentUserId)) {
            holder.new_message_indicator.setVisibility(View.GONE);
        } else {
            // Supponiamo che tu abbia un campo `isRead` per ogni messaggio
            boolean isRead = chats.get(position).isLastMessageRead();
            if (isRead) {
                holder.new_message_indicator.setVisibility(View.GONE);
            } else {
                holder.new_message_indicator.setVisibility(View.VISIBLE);
            }
        }

        FirebaseDatabase.getInstance().getReference().child("Utenti registrati").child(userId)
                .child("profileImage").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        try {
                            String profileImageUrl = task.getResult().getValue().toString();

                            if (!profileImageUrl.isEmpty())
                                Glide.with(holder.itemView.getContext()).load(profileImageUrl).into(holder.chat_profileImage_iv);
                        } catch (Exception e) {
                            Toast.makeText(holder.itemView.getContext(), "Failed to get profile image link", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        holder.itemView.setOnClickListener(v -> {
            String chatId = chats.get(position).getChat_id();
            String userId2 = !chats.get(position).getUserId1().equals(currentUserId) ? chats.get(position).getUserId1() : chats.get(position).getUserId2();

            ChatMessengerFragment fragment = new ChatMessengerFragment();
            Bundle args = new Bundle();
            args.putString("chatId", chatId);
            args.putString("userId2", userId2);
            fragment.setArguments(args);

            FragmentActivity activity = (FragmentActivity) holder.itemView.getContext();
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }
}
