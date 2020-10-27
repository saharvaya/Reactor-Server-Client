/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.impl.Messages.CommunicationMessage;

import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class representing all the server data, registered and logged users and Communication message list
 */
public class ServerData {

    //Fields
    private Map<String,List<MessageDetails>> messages; //Maps usernames to a list of their messages
    private Map<String,UserSession> loggedUsers; //Maps Connection ID to a logged user.
    private Map<String,User> registeredUsers; //Maps usernames to registered users

    //Constructor
    public ServerData()
    {
        messages = new ConcurrentHashMap<>();
        loggedUsers = new ConcurrentHashMap<>();
        registeredUsers = new ConcurrentHashMap<>();
    }

    /**
     * Registers a user with given registration details (Add to registered map).
     * @param username A username to register the user with
     * @param password A password to register with
     * @return true if registration succeeded, false otherwise (A user with the same username was already registered)
     */
    public boolean registerUser(String username, String password)
    {
        if(userRegistered(username))
            return false;
        return registeredUsers.putIfAbsent(username, new User(username, password)) == null;
    }

    /**
     * Login a user with given logging details (Add to logged users map).
     * @param username A username to login the user with
     * @param password A password to login with
     * @return true if login succeeded, false otherwise (A user with the same username is already logged)
     */
    public boolean loginUser(String username, String password, int connectionID)
    {
        if(!userLoggedIn(username)) {
        User user = registeredUsers.getOrDefault(username, null);
            if (user != null && user.getUsername().equals(username) && user.getPassword().equals(password)) {
                user.setLogin(true);
                return loggedUsers.putIfAbsent(user.getUsername(), new UserSession(user, connectionID)) == null;
            }
        }
        return false;
    }

    /**
     * Stores a communication message in a list of messages corresponding to the message sender
     * @param sender the user sent the message
     * @param message the message to store
     */
    public void storeMessage(User sender, CommunicationMessage message)
    {
        List<MessageDetails> userMessages = messages.computeIfAbsent(sender.getUsername(), m -> new ArrayList<>());
        userMessages.add(new MessageDetails(new Timestamp(System.currentTimeMillis()), message));
    }

    /**
     * @param username A username to check if registered
     * @return The user if the username is registered, else null
     */
    public User getRegisteredUser(String username)
    {
        return registeredUsers.getOrDefault(username, null);
    }

    /**
     * @param username A username to check if logged
     * @return The user if the username is logged else null
     */
    public User getLoggedUser(String username)
    {
        UserSession user = loggedUsers.getOrDefault(username, null);
        return (user != null) ? user.getUser() : null;
    }

    /**
     * @return if the server has logged in users currently
     */
    public boolean hasLoggedInUsers()
    {
        return loggedUsers.keySet().size() != 0;
    }

    /**
     * @param username to check if registered
     * @return true if registered, false otherwise
     */
    private boolean userRegistered(String username)
    {
        return registeredUsers.containsKey(username);
    }

    /**
     * @param username to check if logged
     * @return true if logged, false otherwise
     */
    private boolean userLoggedIn(String username)
    {
        return loggedUsers.containsKey(username);
    }

    /**
     * @param user A user to get the list of messages for
     * @return A list of messages posted by the user given as parameter
     */
    public List<MessageDetails> getUserMessages(User user)
    {
        return messages.get(user.getUsername());
    }

    /**
     * @return A sorted list of usernames ordered by the registration time
     */
    public List<String> getRegisteredUsernames()
    {
        ArrayList<String> registered = new ArrayList<>(registeredUsers.keySet());
        registered.sort(Comparator.comparing(u -> registeredUsers.get(u).getRegistrationTime()));
        return registered;
    }

    /**
     * @param username logged username to get the connection ID for
     * @return an integer representing the connection ID for the username
     */
    public int getLoggedUserConnID(String username)
    {
        return loggedUsers.get(username).getConnectionID();
    }

    /**
     * Removes the {@code username} from the logged users map
     * and sets it login status to false
     * @param username a username to disconnect
     */
    public void disconnectUser(String username)
    {
        loggedUsers.remove(username).getUser().setLogin(false);
    }

    /**
     * A private nested class representing a login user session
     */
    private class UserSession {

        //Fields
        private User user; // The user logged
        private int connectionID; //The connection ID for the current user connection

        //COnstructor
        public UserSession (User user, int connectionID)
        {
            this.user = user;
            this.connectionID = connectionID;
        }

        //Getters
        public User getUser()
        {
            return this.user;
        }

        public int getConnectionID()
        {
            return this.connectionID;
        }
    }

    /**
     * A nested class representing communication message details
     */
    public class MessageDetails {

        //Fields
        private CommunicationMessage message; //The communication message
        private Timestamp timestamp; //The timestamp the message was sent in

        //Constructor
        public MessageDetails (Timestamp timestamp, CommunicationMessage message)
        {
            this.timestamp = timestamp;
            this.message = message;
        }

        //Getters
        public Timestamp getTimestamp()
        {
            return this.timestamp;
        }

        public CommunicationMessage getMessage()
        {
            return this.message;
        }
    }
}
