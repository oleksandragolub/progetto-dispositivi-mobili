package it.sal.disco.unimib.progettodispositivimobili.ui.chat.users;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.ui.chat.chats.ChatUtil;

public class UserAdapter extends  RecyclerView.Adapter<UserViewHolder>{

    private ArrayList<ReadWriteUserDetails> users = new ArrayList<>();

    public UserAdapter(ArrayList<ReadWriteUserDetails> users){
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        ReadWriteUserDetails user = users.get(position);
        Log.d("UserAdapter", "User: " + user.getUsername() + ", Email: " + user.getEmail() );

        holder.username_iv.setText(user.getUsername());
        holder.email_iv.setText(user.getEmail());

        // Check if profileImage is not null and not empty before using it
        if (user.getDataImage() != null && !user.getDataImage().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(user.getDataImage()).into(holder.profileImage_iv);
        } /*else {
            // Set a default image or remove the image if null
            holder.profileImage_iv.setImageResource(R.drawable.default_profile);  // Ensure you have a default image in drawable
        }*/

        holder.itemView.setOnClickListener(v -> {
            ChatUtil.createChat(user);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

}
