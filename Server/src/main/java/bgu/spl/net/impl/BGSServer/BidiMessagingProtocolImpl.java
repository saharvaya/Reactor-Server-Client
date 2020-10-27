/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.Messages.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<BaseMessage> {

    //Fields
    private int connectionID; //Current logged client connectionID
    private boolean shouldTerminate; //Determines if current user ended his session
    private boolean loggedIn; //Determines whether current user is logged in
    private ServerData data; // Reference to shared server data object
    private User activeUser; // Currently active user
    private ConnectionsImpl<BaseMessage> connections; //Reference to server connections

    //Constructor
    public BidiMessagingProtocolImpl(ServerData data)
    {
        this.data = data;
        shouldTerminate = false;
        this.loggedIn = false;
    }

    @Override
    public void start(int connectionId, Connections<BaseMessage> serverConnections) {
        this.connectionID = connectionId;
        if(connections == null)
            connections = (ConnectionsImpl<BaseMessage>) serverConnections;
    }

    @Override
    public void process(BaseMessage message) {
        boolean processed = false;
        OpCodes messageOpCode = message.getOpCode();

        //Determines the type of message needed to be processed by the message OpCode
        switch (messageOpCode) {
            case REGISTER: processRegisterRequest((RegisterMessage) message);
                processed = true;
                break;
            case LOGIN: processLoginRequest((LoginMessage) message);
                processed = true;
                break;
            case LOGOUT: processLogoutRequest((LogoutMessage)message);
                processed = true;
                break;
        }

        if(!processed) {
            //Determines the type of message needed to be processed by the message OpCode, valid only after a user has logged in
            if (loggedIn) {
                switch (messageOpCode) {
                    case FOLLOW: processFollowRequest((FollowMessage)message);
                        break;
                    case POST: processPostRequest((PostMessage) message);
                        break;
                    case PM: processPMRequest((PMMessage) message);
                        break;
                    case USERLIST: processUserlistRequest((UserlistMessage) message);
                        break;
                    case STAT: processStatMessage((StatMessage) message);
                        break;
                }
            } else sendError(message);
        }
    }

    /**
     * Checks if Server Data can enable user registration with message provided details.
     * If Server Data has successfully registered the user, Sends ack message to user client, else sends an error.
     * @param message A register message to process, containing registration details.
     */
    private void processRegisterRequest(RegisterMessage message)
    {
        // Try to register user with provided details
        if(data.registerUser(message.getUsername(), message.getPassword())) {
            sendAck(message, AckMessage.AckTypes.DEFAULT, null);
        }
        else sendError(message);
    }

    /**
     * Checks if Server Data can enable user logging with message provided details.
     * If Server Data has successfully logged the user, Sends ack message to user client and updated current active user.
     * Else, If an active user is already present or logging details were not validated, sends an error.
     * @param message A login message to process, containing logging details.
     */
    private void processLoginRequest(LoginMessage message)
    {
        //Do only if no current active user exists
        if(activeUser == null) {
            if (!data.loginUser(message.getUsername(), message.getPassword(), connectionID))
                sendError(message);
            else {
                // User successfully logged, set current active user details
                this.activeUser = data.getLoggedUser(message.getUsername());
                this.loggedIn = connections.isActiveConnection(connectionID) && activeUser != null;
                if(loggedIn) {
                    sendAck(message, AckMessage.AckTypes.DEFAULT, null);
                    //Notify the user on missed messages that he suppose to receive
                    notifyOnMissedMessages();
                }
            }
        } else sendError(message);
    }

    /**
     * Checks for messages that a newly logged in user suppose to receive and missed while he was logged off.
     * Will send the user all messages happened after that last message he has already seen
     * if the user is following that user, or the user is tagged in the message, or its a private message.
     */
    private void notifyOnMissedMessages() {
        //Check with each user that the active user got a message from.
       for(Entry<User, Timestamp> entry : activeUser.getLatestMessagesTime().entrySet())
       {
           //Determine if the active user is following current iterated user.
           boolean isFollowing = activeUser.getFollowing().contains(entry.getKey());
           //Gets a list of message details (message and timestamp) from iterated user happened after the last message seen by active user
           List<ServerData.MessageDetails> followingNewMessages = data.getUserMessages(entry.getKey()).stream().filter(u -> u.getTimestamp().after(entry.getValue())).collect(Collectors.toList());
           followingNewMessages.sort(Comparator.comparing(ServerData.MessageDetails::getTimestamp));
           for(ServerData.MessageDetails messageDetails : followingNewMessages)
           {
               //Checks if the message is a PM message or it is a message the user was tagged in
               CommunicationMessage message = messageDetails.getMessage();
               boolean isPM = ((message.getClass() == PMMessage.class) && ((PMMessage)message).getRecipientUsername().equals(activeUser.getUsername()));
               boolean isTagged = (message.getClass() == PostMessage.class) && ((PostMessage)message).getTaggedUsers().contains(activeUser.getUsername());
               //Send the notification about the message if it is a private message, he is tagged in the message or if he is following the user.
               if(isPM || isTagged || (isFollowing && (message.getClass() != PMMessage.class))) {
                   sendNotification(connectionID, messageDetails.getMessage() instanceof PMMessage, entry.getKey().getUsername(), messageDetails.getMessage().getContent());
                   //Updated last message received timestamp from iterated user
                   activeUser.updateLatestMessageTime(entry.getKey());
               }
           }
       }
    }

    /**
     * Checks if active user is present, sends an error if no user is logged in order to logout.
     * Else, sends a logout confirmation to client, disconnects user from Connections and Server Data objects.
     * Changes the current state of the session to terminated.
     * @param message A logout message.
     */
    private void processLogoutRequest(LogoutMessage message)
    {
        if(activeUser != null) {
            if (!data.hasLoggedInUsers())
                sendError(message);
            else if (loggedIn) {
                    sendAck(message, AckMessage.AckTypes.DEFAULT, null);
                    //Perform disconnection processes
                synchronized (activeUser) {
                    loggedIn = false;
                    data.disconnectUser(activeUser.getUsername());
                    connections.disconnect(connectionID);
                    shouldTerminate = true;
                }
            }
        } else sendError(message);
    }

    /**
     * Follows/Unfollows a list of users recieved in the message details, sends a notification back to user with successful
     * User count and usernames list that the operation succeeded for.
     * @param message A follow message, containing the details needed to make a user follow/unfollow a list of other users.
     */
    private void processFollowRequest(FollowMessage message) {
        List<String> succeeded = new ArrayList<>();
        for(String username : message.getFollowUsernames())
        {
            if(message.isFollow()) {
                //Checks the current user to be followed is registered
                User toFollow = data.getRegisteredUser(username);
                if(toFollow != null && activeUser.addFollow(toFollow)) {
                    //Added the user to be followed to the following list of the active user
                    synchronized (data.getRegisteredUser(toFollow.getUsername()).getFollowers()) {
                        data.getRegisteredUser(toFollow.getUsername()).getFollowers().add(activeUser);
                    }
                    succeeded.add(toFollow.getUsername());
                }
            } else { //Message is unfollow
                //Checks the current user to be unfollowed is registered
                User toUnfollow = activeUser.getFollowed(username);
                if(toUnfollow != null && activeUser.removeFollow(toUnfollow))
                {
                    //Removed the user to be unfollowed to the following list of the active user
                    synchronized (data.getRegisteredUser(toUnfollow.getUsername()).getFollowers()) {
                        data.getRegisteredUser(toUnfollow.getUsername()).getFollowers().remove(activeUser);
                    }
                    succeeded.add(toUnfollow.getUsername());
                }
            }
        }
        if(succeeded.size() == 0) //Follow/Unfollow operation failed for all usernames on the list
            sendError(message);
        else sendAck(message, AckMessage.AckTypes.FOLLOW_ACK, new AckMessage.FollowAckInfo((short)succeeded.size(), succeeded));
    }

    /**
     * Stores the message in Server Data for current active user messages list.
     * Locates the message recipients, aka tagged users in message content or active user followers.
     * Sends a notification to every user currently logged, and updates latest message timestamp from active user for recipient.
     * @param message A post message with post content
     */
    private void processPostRequest(PostMessage message) {
        data.storeMessage(activeUser, message);
        List<String> recipients = new ArrayList<>(findTaggedUsers(message.getContent()));
        message.setTaggedUsers(new ArrayList<>(recipients)); // Adds all users tagged in the messages to recipient list
        //Adds all active user followers to recipient list
        synchronized (data.getRegisteredUser(activeUser.getUsername()).getFollowers()) {
            data.getRegisteredUser(activeUser.getUsername()).getFollowers().forEach((user) -> recipients.add(recipients.contains(user.getUsername()) ? null : user.getUsername()));
        }

        for(String username : recipients)
        {
            if(username != null) {
                User recipient = data.getLoggedUser(username);
                User registered = data.getRegisteredUser(username);
                if (recipient != null) { // Recipient is logged in
                    synchronized (recipient) {
                        sendNotification(data.getLoggedUserConnID(username), false, activeUser.getUsername(), message.getContent());
                    }
                    recipient.updateLatestMessageTime(activeUser);
                } else if (registered != null) {
                    registered.updateLatestMessageTime(activeUser);
                }
            }
        }
        activeUser.incrementPostCount(); //Updates the current post count +1 of the active user.
        sendAck(message, AckMessage.AckTypes.DEFAULT, null);
    }

    /**
     * Finds all users tagged in message content with the notation @<username>.
     * @param content Message content to parse
     * @return A list of usernames tagged in the message content
     */
    private List<String> findTaggedUsers(String content)
    {
        String currentContent = content;
        List<String> taggedUsers = new ArrayList<>();
        //Parses message content to locate @<username> and adds the names found to tagged users name list
        int tagIndex = currentContent.indexOf('@');

        while(currentContent.length() > 0 && tagIndex != -1) {
            currentContent = currentContent.substring(tagIndex+1);
            int endUsernameIndex = currentContent.indexOf(' ');
            taggedUsers.add(currentContent.substring(0, (endUsernameIndex == -1) ? currentContent.length() : endUsernameIndex));
            currentContent = currentContent.substring((endUsernameIndex == -1) ? currentContent.length() : endUsernameIndex);
            tagIndex = currentContent.indexOf('@');
        }
        //Remove duplicates
        HashSet<String> taggedSet = new HashSet<>(taggedUsers);
        taggedUsers = new ArrayList<>(taggedSet );
        return taggedUsers;
    }

    /**
     * Stores the message in Server Data for current active user messages list.
     * Gets the privates message recipient and sends a notification if he is logged else updates latest message timestamp from active user for the recipient.
     * @param message A private message with post content and recipient username
     */
    private void processPMRequest(PMMessage message) {
        data.storeMessage(activeUser, message);
        User recipient = data.getLoggedUser(message.getRecipientUsername());
        User registered = data.getRegisteredUser(message.getRecipientUsername());
        if(recipient != null) {
            synchronized (recipient) {
                sendNotification(data.getLoggedUserConnID(message.getRecipientUsername()), true, activeUser.getUsername(), message.getContent());
            }
            sendAck(message, AckMessage.AckTypes.DEFAULT, null);
            recipient.updateLatestMessageTime(activeUser);
        } else if(registered == null)
            sendError(message);
        else {
            sendAck(message, AckMessage.AckTypes.DEFAULT, null);
            registered.updateLatestMessageTime(activeUser);
        }
    }

    /**
     * Sends a notification with the Server Data user list information (count and usernames list).
     * @param message A userlist message to process
     */
    private void processUserlistRequest(UserlistMessage message) {
        List<String> users = data.getRegisteredUsernames();
        sendAck(message, AckMessage.AckTypes.USERLIST_ACK, new AckMessage.UserlistAckInfo((short)users.size(), users));
    }

    /**
     * Checks if the username asked for statistics in the message is registered (exists in Server Data).
     * If the user exists, send a notification with user statistics, Posts count, Followers count, Following count.
     * @param message A statistics message to process
     */
    private void processStatMessage(StatMessage message) {
        User user = data.getRegisteredUser(message.getUsername());
        if(user == null)
            sendError(message);
        else sendAck(message, AckMessage.AckTypes.STAT_ACK, new AckMessage.StatusAckInfo((short)user.getPostCount(), (short)user.getFollowers().size(), (short)user.getFollowing().size()));
    }

    /**
     * Sends an error notification to the current active user connection ID via Connection object.
     * @param message Message to send the error for.
     */
    private void sendError(BaseMessage message) {
        connections.send(connectionID, new ErrorMessage(message.getOpCode()));
    }

    /**
     * Sends an acknowledgement notification to the current active user connection ID via Connection object.
     * @param message Message to acknowledge
     * @param type Acknowledgement type
     * @param optionalInfo Additional info dependent on the acknowledgement type.
     */
    private void sendAck(BaseMessage message, AckMessage.AckTypes type, OptionalAckInfo optionalInfo) {
        connections.send(connectionID, new AckMessage(message.getOpCode(), type, optionalInfo));
    }

    /**
     * Sends a notification to a specific connected connection ID via Connection object with the sender user details.
     * @param connID connection ID to send to notification to.
     * @param PM Determines whether the notification is a private message
     * @param postingUser The username sent the notification
     * @param content Notification message content
     */
    private void sendNotification(int connID, boolean PM, String postingUser, String content) {
        connections.send(connID, new NotificationMessage(postingUser, content, PM));
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}

