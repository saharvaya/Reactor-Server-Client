/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.Messages;

import  java.util.List;

/**
 * A class representing a Public post message communication type
 */
public class PostMessage extends BaseMessage implements CommunicationMessage {

    //Fields
    private String content; //The message content
    private List<String> taggedUsers; //A list of users tagged by the notation @<username> in this message content

    //Constructor
    public PostMessage()
    {
        this.opCode = OpCodes.POST;
    }

    //Getters and Setters
    @Override
    public String getContent()
    {
        return this.content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public void setTaggedUsers(List<String> tagged)
    {
        this.taggedUsers = tagged;
    }

    public List<String> getTaggedUsers(){
        return  taggedUsers;
    }

}
