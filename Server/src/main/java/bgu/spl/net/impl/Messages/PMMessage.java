/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.Messages;

/**
 * A class representing a PM message communication type
 */
public class PMMessage extends BaseMessage implements CommunicationMessage {

    //Fields
    private String recipientUsername; //Private message recipient
    private String content; //Message content

    //Constructor
    public PMMessage()
    {
        this.opCode = OpCodes.PM;
    }

    //Getters and Setters
    public void setRecipientUsername(String username)
    {
        this.recipientUsername = username;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    public String getRecipientUsername()
    {
        return recipientUsername;
    }
}
