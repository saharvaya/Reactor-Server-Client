/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.ConnectionHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T> {

    //Fields
    private Map<Integer, ConnectionHandler<T>> connections; //Maps connection ID's to Client connection handlers
    private static AtomicInteger currentConnectionID; //Stores the latest connection ID given by this class.

    //Constructor
    public ConnectionsImpl() {
        currentConnectionID = new AtomicInteger(1);
        connections = new ConcurrentHashMap<>();
    }

    /**
     * Sends a T typed message to given connection ID
     * @param connectionId the connection id to send to.
     * @param msg the T type message to send
     * @return return true if message was sent, false otherwise
     */
    @Override
    public boolean send(int connectionId, T msg) {
        if (connections.containsKey(connectionId)) {
            connections.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    /**
     * Sends a T typed message to every connected client.
     * @param msg the T type message to send
     */
    @Override
    public void broadcast(T msg) {
        connections.forEach( (connID, connection) -> connection.send(msg) );
    }

    /**
     * Removes a connected client by connection ID
     * @param connectionId the connection ID to remove reference to.
     */
    @Override
    public void disconnect(int connectionId) {
        connections.remove(connectionId);
    }

    /**
     * Adds a new connection to the map
     * @param connectionID the clients connection ID.
     * @param connection client specific connection handler to connect.
     */
    public void addConnection(int connectionID, ConnectionHandler<T> connection) {
        connections.put(connectionID, connection);
    }

    /**
     * Increments the currentConnectionID field
     * @return The new value of currentConnectionID as a new unique identifier
     */
    public int getUniqueConnectionId(){
        return currentConnectionID.getAndIncrement();
    }

    /**
     * Checks if a given connection ID is currently active
     * @param connectionID the connection ID the checks connection for
     * @return True, if the given connection ID is present, false otherwise
     */
    public boolean isActiveConnection(int connectionID)
    {
        return connections.containsKey(connectionID);
    }
}