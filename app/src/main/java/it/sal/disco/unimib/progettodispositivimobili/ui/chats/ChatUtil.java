package it.sal.disco.unimib.progettodispositivimobili.ui.chats;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import it.sal.disco.unimib.progettodispositivimobili.ReadWriteUserDetails;

public class ChatUtil {

    public static void createChat(ReadWriteUserDetails user){
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        HashMap<String, String> chatInfo = new HashMap<>();
        chatInfo.put("user1", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        chatInfo.put("user2", user.getUserId());

        String chatId = generateChatId(uid, user.getUserId());
        FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId).setValue(chatInfo);

        addChatIdToUser(uid, chatId);
        addChatIdToUser(user.getUserId(), chatId);
    }

    public static String generateChatId(String userId1, String userId2){
        String sumUser1User2 = userId1 + userId2;
        char[] charArray = sumUser1User2.toCharArray();
        Arrays.sort(charArray);

        return new String(charArray);
    }

    private static void addChatIdToUser(String uid, String chatId) {
        FirebaseDatabase.getInstance().getReference().child("Utenti registrati").child(uid).child("chats").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Recupera l'attuale stringa di chat, se esiste
                        String existingChats = (task.getResult().getValue() != null) ? task.getResult().getValue().toString() : "";

                        // Verifica se l'ID chat è già presente
                        if (!existingChats.contains(chatId)) {
                            // Aggiungi l'ID chat solo se non è già presente
                            String updatedChats = addIdToStr(existingChats, chatId);
                            FirebaseDatabase.getInstance().getReference().child("Utenti registrati").child(uid).child("chats")
                                    .setValue(updatedChats);
                        }
                    }
                });
    }


    private static String addIdToStr(String str, String chatId) {
        // Aggiunge una virgola solo se la stringa non è vuota
        str += (str.isEmpty()) ? chatId : ("," + chatId);
        return str;
    }


}
