package it.sal.disco.unimib.progettodispositivimobili.ui.chat.users;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import it.sal.disco.unimib.progettodispositivimobili.R;

public class UserViewHolder extends RecyclerView.ViewHolder {

    CircleImageView profileImage_iv;
    TextView username_iv, email_iv;


    public UserViewHolder(@NonNull View itemView) {
        super(itemView);

        profileImage_iv = itemView.findViewById(R.id.image_profile_pic);
        username_iv = itemView.findViewById(R.id.username_text);
        email_iv = itemView.findViewById(R.id.email_text);
    }
}
