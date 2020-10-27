/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.Messages;

/**
 * A class representing a Statistics message for a certaion user
 */
public class StatMessage extends BaseMessage {

    //Fields
    private String username; //Username to get statistics for

    //Constructor
    public StatMessage()
    {
        this.opCode = OpCodes.STAT;
    }

    //Getters and Setters
    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getUsername()
    {
        return this.username;
    }
}
