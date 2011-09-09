package pl.edu.pjwstk.net.proto;

import java.io.IOException;

import pl.edu.pjwstk.net.TransportPacket;
import pl.edu.pjwstk.p2pp.messages.NonInterpretedMessage;

/**
 * Interface describes protocol properties and method needed to receive data from other hosts. 
 * @author Robert Strzelecki robert.strzelecki@codearch.eu
 *
 */
public interface ProtocolReader {
	
	/**
	 * Receives a packet from opened worker socket. When this method returns, the NonInterpretedMessage's buffer is filled with the data received. 
	 * @return NonInterpretedMessage
	 * @throws IOException
	 */
	NonInterpretedMessage ReceiveMessage() throws IOException;
	
	/**
	 * Receives a packet from opened worker socket. When this method returns, the TransportPacket's buffer is filled with the data received.
	 * @return TransportPacket
	 * @throws IOException
	 */
	TransportPacket ReceivePacket() throws IOException;
	
	/**
	 * Receives a packet from opened worker socket. When this method returns, buffer is filled with the data received.
	 * @return Array of bytes contains data received.
	 * @throws IOException
	 */
	byte[]  Receive() throws IOException;
}
