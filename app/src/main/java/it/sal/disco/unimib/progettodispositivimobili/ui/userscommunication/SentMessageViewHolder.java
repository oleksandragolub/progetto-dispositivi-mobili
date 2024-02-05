package it.sal.disco.unimib.progettodispositivimobili.ui.userscommunication;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.sal.disco.unimib.progettodispositivimobili.R;

public class SentMessageViewHolder extends RecyclerView.ViewHolder {
    TextView messageText, messageTime;

    public SentMessageViewHolder(View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.textMessage);
        messageTime = itemView.findViewById(R.id.textDateTime);
    }

    public void bind(Message message) {
        messageText.setText(message.getMessage());
        // Converti il timestamp in una stringa leggibile
        String formattedTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(message.getTimestamp()));
        messageTime.setText(formattedTime);
    }
}
