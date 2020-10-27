/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.Messages;

import java.util.List;

/**
 * A class representing an acknowledgement message type
 */
public class AckMessage extends BaseMessage {

    //Fields
    private OpCodes ackMessageOpCode; //The message OpCode that is being acknowledged
    private AckTypes type; //The current ack message type
    private OptionalAckInfo info = null; //Will store an object containing additional info to include in ack message if type is not DEFAULT

    //Constructor
    public AckMessage(OpCodes referenceOpCde, AckTypes type, OptionalAckInfo optionalInfo) {
        super.opCode = OpCodes.ACK;
        this.ackMessageOpCode = referenceOpCde;
        this.type = type;
        if(optionalInfo != null)
            info = optionalInfo;
    }

    //An enum representing the different type of ack messages available
    public enum AckTypes {
        DEFAULT,
        FOLLOW_ACK,
        USERLIST_ACK,
        STAT_ACK;
    }

    //Getters
    public OpCodes getAckMessageOpCode()
    {
        return this.ackMessageOpCode;
    }

    public AckTypes getAckType()
    {
        return this.type;
    }

    public OptionalAckInfo getOptionalInfo()
    {
        return this.info;
    }

    //Nested classes containing additional information type to include in ack message.
    public static class FollowAckInfo implements OptionalAckInfo
    {
        private short numOfUsers;
        private List<String> usernameList;

        public FollowAckInfo(short numOfUsers, List<String> usernameList)
        {
            this.numOfUsers = numOfUsers;
            this.usernameList = usernameList;
        }

        public short getNumOfUsers()
        {
            return  this.numOfUsers;
        }

        public List<String> getUsernameList()
        {
            return this.usernameList;
        }
    }

    public static class StatusAckInfo implements OptionalAckInfo
    {
        private short numOfPosts;
        private short numOfFollowers;
        private short numOfFollowing;

        public StatusAckInfo(short numOfPosts, short numOfFollowers, short numOfFollowing)
        {
            this.numOfPosts = numOfPosts;
            this.numOfFollowers = numOfFollowers;
            this.numOfFollowing = numOfFollowing;
        }

        public short getNumOfPosts()
        {
            return this.numOfPosts;
        }

        public short getNumOfFollowers()
        {
            return this.numOfFollowers;
        }

        public short getNumOfFollowing()
        {
            return this.numOfFollowing;
        }
    }

    public static class UserlistAckInfo implements OptionalAckInfo
    {
        private short numOfUsers;
        private List<String> usernameList;

        public UserlistAckInfo(short numOfUsers, List<String> usernameList)
        {
            this.numOfUsers = numOfUsers;
            this.usernameList = usernameList;
        }

        public short getNumOfUsers()
        {
            return  this.numOfUsers;
        }

        public List<String> getUsernameList()
        {
            return this.usernameList;
        }
    }
}
