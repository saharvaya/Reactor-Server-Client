/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.Messages;

/**
 * A class representing a userlist message
 */
public class UserlistMessage extends BaseMessage {

    //Constructor
    public  UserlistMessage()
    {
        this.opCode = OpCodes.USERLIST;
    }
}
