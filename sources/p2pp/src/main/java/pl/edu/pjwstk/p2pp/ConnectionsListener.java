package pl.edu.pjwstk.p2pp;

import java.net.Socket;

/**
 * Interface for listeners of connections.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public interface ConnectionsListener {

	/**
	 * Method invoked when connection was accepted.
	 * 
	 * @param socket
	 *            Socket for communication with connected client.
	 */
	public void connectionAccepted(Socket socket);

	/**
	 * Fired when a socket for connections wasn't created.
	 */
	public void socketNotCreated();

}
