/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.Messages;

/**
 * An abstract class containing default information and methods for all message types
 */
public abstract class BaseMessage {

    //Fields
    protected OpCodes opCode; //Message OpCode
    private boolean completed; //Determines if the message details are filled.

    //Constructor
    public BaseMessage() {
        completed = false;
    }

    /**
     * Changes state of current message to completed
     */
    public void complete()
    {
        completed = true;
    }
    //Getters
    public OpCodes getOpCode() {
        return opCode;
    }

    public boolean isCompleted()
    {
        return completed;
    }


}
