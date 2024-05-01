package it.sal.disco.unimib.progettodispositivimobili.ui.chats;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import it.sal.disco.unimib.progettodispositivimobili.R;

public class ChatViewHolder extends RecyclerView.ViewHolder{
    CircleImageView chat_profileImage_iv;
    TextView chat_username_iv, chat_email_iv;

    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);

        chat_profileImage_iv = itemView.findViewById(R.id.image_profile_pic);
        chat_username_iv = itemView.findViewById(R.id.username_text);
        chat_email_iv = itemView.findViewById(R.id.email_text);
    }
}
