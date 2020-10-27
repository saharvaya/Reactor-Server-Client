/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.api.bidi;

public interface BidiMessagingProtocol<T>  {
	/**
	 * Used to initiate the current client protocol with it's personal connection ID and the connections implementation
	**/
    void start(int connectionId, Connections<T> connections);

	/**
	 * Processes a message by the protocol implementation.
	 * @param message, A message to process by teh current protocol
	 */
	void process(T message);
	
	/**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
}
