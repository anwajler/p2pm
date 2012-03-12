/**
 * 
 */
package pl.edu.pjwstk.net.proto.TURN;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import pl.edu.pjwstk.net.TransportPacket;
import pl.edu.pjwstk.net.proto.ProtocolControl;
import pl.edu.pjwstk.net.proto.ProtocolReader;
import pl.edu.pjwstk.net.proto.ProtocolWriter;
import pl.edu.pjwstk.net.proto.UDP.UDPWorker;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.NonInterpretedMessage;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.proto.TURN
 */
public abstract class TURNUDPWorker extends UDPWorker implements ProtocolControl,
		ProtocolReader, ProtocolWriter {
	
    /**
     * Initialize UDP Worker
     *
     * @throws SocketException
     */
    protected void Initialize(boolean isReady) throws SocketException {
        super.Initialize(false);
        // do zrobienia
        
        //super.isReady = isReady;
    }
    
	/**
	 * @param localPort
	 * @throws SocketException
	 */
	public TURNUDPWorker(Integer localPort) throws SocketException {
		super(localPort);
	}

	/**
	 * @param localIP
	 * @param localPort
	 * @throws SocketException
	 */
	public TURNUDPWorker(InetAddress localIP, Integer localPort)
			throws SocketException {
		super(localIP, localPort);
	}

	/**
	 * @param message
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public TURNUDPWorker(Message message) throws SocketException,
			UnknownHostException {
		super(InetAddress.getByName(message
                .getSenderAddress()), message.getSenderPort());
		//do zrobienia
	}

	/**
	 * @param localIP
	 * @param localPort
	 * @param remoteIP
	 * @param remotePort
	 * @throws SocketException
	 */
	public TURNUDPWorker(InetAddress localIP, Integer localPort,
			InetAddress remoteIP, Integer remotePort) throws SocketException {
		super(localIP, localPort);
		//Do zrobienia
	}


	/* (non-Javadoc)
	 * @see pl.edu.pjwstk.net.proto.ProtocolReader#Receive()
	 */
	@Override
	public byte[] Receive() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see pl.edu.pjwstk.net.proto.ProtocolReader#ReceiveMessage()
	 */
	@Override
	public NonInterpretedMessage ReceiveMessage() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see pl.edu.pjwstk.net.proto.ProtocolReader#ReceivePacket()
	 */
	@Override
	public TransportPacket ReceivePacket() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see pl.edu.pjwstk.net.proto.ProtocolWriter#Send(java.net.InetAddress, java.lang.Integer, byte[])
	 */
	@Override
	public boolean Send(InetAddress remoteIP, Integer remotePort, byte[] packet)
			throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see pl.edu.pjwstk.net.proto.ProtocolWriter#Send(byte[])
	 */
	@Override
	public boolean Send(byte[] packet) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see pl.edu.pjwstk.net.proto.ProtocolWriter#SendMessage(pl.edu.pjwstk.p2pp.messages.Message)
	 */
	@Override
	public boolean SendMessage(Message messagez) {
		// TODO Auto-generated method stub
		return false;
	}
}
