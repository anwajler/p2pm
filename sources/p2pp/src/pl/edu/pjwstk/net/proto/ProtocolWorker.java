package pl.edu.pjwstk.net.proto;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;

/**
 * Abstract class contains all methods needed to communicate with host via SupportedProtocols. 
 * @author Robert Strzelecki robert.strzelecki@codearch.eu
 *
 */
public abstract class ProtocolWorker extends Thread implements ProtocolControl, ProtocolReader, ProtocolWriter{

    public static final int PACKET_SIZE_MAX = 65536;

	protected InetAddress localAddress;
	protected Integer localPort;
	protected InetAddress remoteAddress;
	protected Integer remotePort;
	protected Vector<AddressInfo> routingVector;
	protected boolean isReliable;
	protected boolean isReady;
	
	/**
	 * Initialize ProtocolWorker object.
	 * @throws SocketException
	 */
	private void Initialize() throws SocketException{}
	
	/**
	 * Creates a socket, bound to the specified local address. The local port must be between 0 and 65535 inclusive. 
	 * IP address is 0.0.0.0 so the socket will be bound to the wildcard address, an IP address chosen by the kernel. 
	 * @param localPort - local port to use
	 * @throws SocketException
	 */
	public ProtocolWorker(Integer localPort) throws SocketException{
		Initialize();
	}
	
	/**
	 * Creates a socket, bound to the specified local address. The local port must be between 0 and 65535 inclusive. 
	 * If the IP address is 0.0.0.0, the socket will be bound to the wildcard address, an IP address chosen by the kernel. 
	 * @param localIP - local interface IP address to bind
	 * @param localPort - local port to use
	 * @throws SocketException
	 */
	public ProtocolWorker(InetAddress localIP, Integer localPort) throws SocketException{
		Initialize();
	}
	
	/**
	 * Creates a socket, bound to the specified local address in message. The local port must be between 0 and 65535 inclusive. 
	 * If the IP address is 0.0.0.0, the socket will be bound to the wildcard address, an IP address chosen by the kernel.	 
	 * @param message - message to be sent
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public ProtocolWorker(Message message) throws SocketException, UnknownHostException{
		Initialize();
	}

	/**
	 * Creates a socket, bound to the specified local address and connected to specified remote address. The local and remote port must be between 0 and 65535 inclusive. 
	 * If the IP address is 0.0.0.0, the socket will be bound to the wildcard address, an IP address chosen by the kernel.	 
	 * @param localIP - local interface IP address to bind
	 * @param localPort - local port to use
	 * @param remoteIP - remote IP address to bind
	 * @param remotePort - remote port to bind
	 * @throws SocketException
	 */
	public ProtocolWorker(InetAddress localIP, Integer localPort, InetAddress remoteIP, Integer remotePort) throws SocketException{		
		Initialize();
	}	
}
