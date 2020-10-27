/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.Messages;

/**
 * A class representing a Notification message type.
 */
public class NotificationMessage extends BaseMessage{

    private String postingUsername; //The sender of the notification
    private String content; //The content of the notification message
    private NotificationType type; // The type of the current notification (PM/Public)

    //Enum representing the Type of the Notification message
    private enum NotificationType {
        PM(0),
        PUBLIC(1);

        private byte value;
        NotificationType(int value)
        {
            this.value = (byte) value;
        }

        //Return the byte value of the notification type
        public byte getType()
        {
            return this.value;
        }
    }

    //Constructor
    public NotificationMessage(String postingUsername, String content, boolean PM)
    {
        this.opCode = OpCodes.NOTIFICATION;
        this.postingUsername = postingUsername;
        this.content = content;
        this.type = (PM) ? NotificationType.PM : NotificationType.PUBLIC;
    }

    //Getters
    public byte getNotificationType()
    {
        return this.type.getType();
    }

    public String getPostingUsername() {
        return postingUsername;
    }

    public String getContent() {
        return content;
    }
}
