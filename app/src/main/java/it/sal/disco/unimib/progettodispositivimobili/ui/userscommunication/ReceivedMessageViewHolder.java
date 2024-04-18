package it.sal.disco.unimib.progettodispositivimobili.ui.userscommunication;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.sal.disco.unimib.progettodispositivimobili.R;

public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
    TextView messageText, messageTime;
    ImageView profileImage;

    public ReceivedMessageViewHolder(View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.textMessage);
        messageTime = itemView.findViewById(R.id.textDateTime);
        profileImage = itemView.findViewById(R.id.imageProfile);
    }

    public void bind(Message message) {
        messageText.setText(message.getMessage());
        // Converti il timestamp in una stringa leggibile
        String formattedTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(message.getTimestamp()));
        messageTime.setText(formattedTime);

        // Qui puoi anche gestire il caricamento dell'immagine del profilo se necessario
        // Ad esempio, usando Picasso o Glide
        // Picasso.get().load(userProfileImageUrl).into(profileImage);
    }
}