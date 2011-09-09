package pl.edu.pjwstk.net.proto;

import java.io.IOException;
import java.net.InetAddress;

import pl.edu.pjwstk.p2pp.messages.Message;

/**
 * Interface describes protocol properties and method needed to send data to other hosts. 
 * @author Robert Strzelecki robert.strzelecki@codearch.eu
 *
 */
public interface ProtocolWriter {
	
	/**
	 * Sends a packet from socket. 
	 * The Message includes information indicating the data to be sent, its length, the IP address of the remote host, and the port number on the remote host. 
	 * @param message - message to be sent
	 * @return
	 */
	boolean SendMessage(Message message);
	
	/**
	 * Sends a packet from socket. 
	 * The parameters includes information indicating the data to be sent, its length, the IP address of the remote host, and the port number on the remote host. 
	 * @param remoteIP - remote IP address
	 * @param remotePort - remote port 
	 * @param packet - data to be sent
	 * @return
	 * @throws IOException
	 */
	boolean Send(InetAddress remoteIP, Integer remotePort, byte[] packet) throws IOException;
	
	/**
	 * Only for reliable protocols and unreliable (connected) protocols. Remote IP address and remote port must be defined and connected. 
	 * @param packet - data to be sent
	 * @return true - packet sent/false - socket/transmit error
	 * @throws IOException
	 */
	boolean Send(byte[] packet) throws IOException;
}
