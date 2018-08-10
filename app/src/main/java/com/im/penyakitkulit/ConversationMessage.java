package com.im.penyakitkulit;

import java.io.Serializable;

/**
 * Encapsulates a conversation message including the text and user.
 */
public class ConversationMessage implements Serializable {

    private final String messageText;
    private final String user;

    /**
     * Creates a conversation message.
     * @param messageText Text inside the message.
     * @param user Creator of the message.
     */
    public ConversationMessage(String messageText, String user) {
        this.messageText = messageText;
        this.user = user;
    }

    public String getMessageText(){return messageText;}
    public String getUser(){return user;}
}
