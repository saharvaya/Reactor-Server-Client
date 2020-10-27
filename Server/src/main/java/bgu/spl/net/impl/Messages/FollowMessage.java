/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a follow-unfollow message type.
 */
public class FollowMessage extends  BaseMessage {

    //Fields
    private List<String> followUsernames; //A list of usernames to perform the operation for.
    private int numOfUsers; // Num of users to perform the operation for (should be followUsernames list size)
    private boolean follow; //True is follow, false for unfollow.

    //Constructor
    public FollowMessage()
    {
        this.opCode = OpCodes.FOLLOW;
        this.followUsernames = new ArrayList<>();
    }

    /**
     * Adds a username given as parametes to a list of users to perform the operation for.
     * @param username a username string to add
     */
    public void addUsernameToFollow(String username)
    {
        this.followUsernames.add(username);
    }

    /**
     * Determines if there are remaining users to add to the list
     * meaning the size of the list of users to perform the operation for is less than the num of users.
     * @return true if list is not full.
     */
    public boolean usernameListNotFull()
    {
        return followUsernames.size() < this.numOfUsers;
    }

    //Getters and Setters
    public List<String> getFollowUsernames()
    {
        return this.followUsernames;
    }

    public boolean isFollow()
    {
        return follow;
    }

    public void setFollowUnfollow(boolean follow)
    {
        this.follow = follow;
    }

    public void setNumOfUsers(int numOfUsers)
    {
        this.numOfUsers = numOfUsers;
    }
}
