package it.sal.disco.unimib.progettodispositivimobili.ui.chat.chats;

public class Chat {

    private String chat_id, chat_name, userId1, userId2;
    private String userEmail;
    private String lastMessage;
    private String lastMessageOwnerId;
    private boolean lastMessageRead;

    public Chat(){
        // Costruttore vuoto richiesto da Firebase
    }

    public Chat(String chat_id, String chat_name, String userId1, String userId2){
        this.chat_id = chat_id;
        this.chat_name = chat_name;
        this.userId1 = userId1;
        this.userId2 = userId2;
    }

    public Chat(String chat_id, String chat_name, String userId1, String userId2, String userEmail) {
        this.chat_id = chat_id;
        this.chat_name = chat_name;
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.userEmail = userEmail;
    }
    public Chat(String chat_id, String chat_name, String userId1, String userId2, String lastMessage, String userEmail) {
        this.chat_id = chat_id;
        this.chat_name = chat_name;
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.userEmail = userEmail;
        this.lastMessage = lastMessage;
    }

    // Getter e setter per lastMessageOwnerId
    public String getLastMessageOwnerId() {
        return lastMessageOwnerId;
    }

    public void setLastMessageOwnerId(String lastMessageOwnerId) {
        this.lastMessageOwnerId = lastMessageOwnerId;
    }

    // Getter e setter per lastMessageRead
    public boolean isLastMessageRead() {
        return lastMessageRead;
    }

    public void setLastMessageRead(boolean lastMessageRead) {
        this.lastMessageRead = lastMessageRead;
    }

    // Getter e setter per lastMessage
    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getChat_name() {
        return chat_name;
    }

    public void setChat_name(String chat_name) {
        this.chat_name = chat_name;
    }

    public String getUserId1() {
        return userId1;
    }

    public void setUserId1(String userId1) {
        this.userId1 = userId1;
    }

    public String getUserId2() {
        return userId2;
    }

    public void setUserId2(String userId2) {
        this.userId2 = userId2;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }


}
