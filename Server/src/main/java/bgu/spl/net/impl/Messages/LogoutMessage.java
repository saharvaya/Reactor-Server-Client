/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.Messages;

/**
 * A class representing a logout message type.
 */
public class LogoutMessage extends BaseMessage {

    //Constructor
    public LogoutMessage()
    {
        this.opCode = OpCodes.LOGOUT;
    }

}
