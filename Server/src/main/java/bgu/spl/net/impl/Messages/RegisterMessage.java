/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.Messages;

/**
 * A class representing a register message type.
 */
public class RegisterMessage extends BaseMessage{

    //Fields
    private String username; //Username to register with
    private String password; //Password to register with

    //Constructor
    public RegisterMessage()
    {
        this.opCode = OpCodes.REGISTER;
    }

    //Getters and Setters
    public String getUsername()
    {
        return this.username;
    }

    public String getPassword()
    {
        return this.password;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
