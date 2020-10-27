/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.Messages;

/**
 * A class representing a login message type.
 */
public class LoginMessage extends BaseMessage {

    //Fields
    private String username; //Login username
    private String password; //Login password

    //Constructor
    public LoginMessage() {
        this.opCode = OpCodes.LOGIN;
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
