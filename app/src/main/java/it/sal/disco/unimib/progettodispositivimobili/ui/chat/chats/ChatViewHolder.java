package it.sal.disco.unimib.progettodispositivimobili.ui.chat.chats;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import it.sal.disco.unimib.progettodispositivimobili.R;

public class ChatViewHolder extends RecyclerView.ViewHolder{
    CircleImageView chat_profileImage_iv;
    TextView chat_username_iv, last_message;
    View new_message_indicator;  // Aggiungi questo campo

    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);

        chat_profileImage_iv = itemView.findViewById(R.id.image_profile_pic);
        chat_username_iv = itemView.findViewById(R.id.username_text);
        last_message = itemView.findViewById(R.id.last_message);
        new_message_indicator = itemView.findViewById(R.id.new_message_indicator);  // Inizializza l'indicatore
    }
}