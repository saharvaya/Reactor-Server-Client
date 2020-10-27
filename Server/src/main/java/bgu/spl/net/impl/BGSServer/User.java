/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.BGSServer;

import java.util.List;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class User {

    //Fields
    private String username; //Users username
    private String password; //Users password
    private volatile boolean logged; //Current user state (logged on/off)
    private int postCount; //Users current posts count
    private List<User> following; //A list of users the current user is following.
    private List<User> followers; //A list of users the current user if followed by.
    private Map<User,Timestamp> latestMessagesTime; //Maps users the current users received messages from to the latest received message timestamp
    private Timestamp registrationTime; //Users registration to Server Data time

    //Constructor
    public User(String username, String password)
    {
        this.username = username;
        this.password = password;
        this.following = new CopyOnWriteArrayList<>();
        this.followers = new CopyOnWriteArrayList<>();
        this.latestMessagesTime = new ConcurrentHashMap<>();
        this.logged = false;
        this.postCount = 0;
        this.registrationTime = new Timestamp(System.currentTimeMillis());
    }

    /**
     * @param username A username to look if the current user is following
     * @return The user if found, else null
     */
    public User getFollowed(String username)
    {
        for(User user : following)
            if(user.getUsername().equals(username))
                return user;
        return  null;
    }

    /**
     * Adds a new user to follow by the current user.
     * @param user the user to follow
     * @return true if not already following the requested user, false otherwise
     */
    public boolean addFollow(User user)
    {
        if(!this.following.contains(user) && !user.equals(this)) {
            this.following.add(user);
            return this.latestMessagesTime.putIfAbsent(user, new Timestamp(System.currentTimeMillis())) == null;
        }
        return false;
    }

    /**
     * Removes a user that is followed by the current user.
     * @param user the user to follow
     * @return true if not already following the requested user, false otherwise
     */
    public boolean removeFollow(User user)
    {
        if(this.following.contains(user)) {
            following.remove(user);
            this.latestMessagesTime.remove(user);
            return true;
        }
        return false;
    }

    /**
     * Updated the latest message time to current time from given parameter user
     * @param user user to update latest  received message time for.
     */
    public void updateLatestMessageTime(User user)
    {
        if(logged)
            latestMessagesTime.putIfAbsent(user, new Timestamp(System.currentTimeMillis()));
        else latestMessagesTime.computeIfAbsent(user, messageTime -> new Timestamp(Long.MIN_VALUE));
    }

    /**
     * @return Current users post count.
     */
    public int getPostCount()
    {
        return this.postCount;
    }

    /**
     * Increments current user post count
     */
    public synchronized void incrementPostCount()
    {
        this.postCount++;
    }

    /**
     * Checks if the current user is equal to given object
     * @param other the object to check equality with
     * @return true if object and user are equal, else false.
     */
    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof User))
            return false;

        User otherUser = (User) other;
        return otherUser.getUsername().equals(this.getUsername());
    }


    //Getters
    public Map<User,Timestamp> getLatestMessagesTime()
    {
        return this.latestMessagesTime;
    }

    public List<User> getFollowers()
    {
        return this.followers;
    }

    public List<User> getFollowing()
    {
        return this.following;
    }

    public String getUsername()
    {
        return this.username;
    }

    public String getPassword()
    {
        return this.password;
    }

    public void setLogin(boolean logged)
    {
        this.logged = logged;
    }

    public Timestamp getRegistrationTime()
    {
        return registrationTime;
    }
}
