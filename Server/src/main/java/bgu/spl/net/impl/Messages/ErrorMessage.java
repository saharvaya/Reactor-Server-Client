/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.Messages;

/**
 * A class representing an error message type.
 */
public class ErrorMessage extends BaseMessage {

    //Fields
    private OpCodes errorMessageOpCode; //Message OpCode this error message was sent for

    //Constructor
    public ErrorMessage(OpCodes errorMessageOpCode)
    {
        this.opCode = OpCodes.ERROR;
        this.errorMessageOpCode = errorMessageOpCode;
    }

    //Getters
    public OpCodes getErrorMessageOpCode()
    {
        return this.errorMessageOpCode;
    }
}
