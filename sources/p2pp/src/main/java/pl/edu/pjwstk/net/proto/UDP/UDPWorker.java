package pl.edu.pjwstk.net.proto.UDP;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.net.TransportPacket;
import pl.edu.pjwstk.net.proto.ProtocolControl;
import pl.edu.pjwstk.net.proto.ProtocolReader;
import pl.edu.pjwstk.net.proto.ProtocolWorker;
import pl.edu.pjwstk.net.proto.ProtocolWriter;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.NonInterpretedMessage;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * UDP protocol worker implementation.
 *
 * @author Robert Strzelecki robert.strzelecki@codearch.eu
 * @author Konrad Adamczyk conrad.adamczyk@gmail.com
 *
 */
public class UDPWorker extends ProtocolWorker implements ProtocolControl, ProtocolReader, ProtocolWriter {

    private final static Logger LOG = Logger.getLogger(UDPWorker.class);

    private DatagramSocket datagramSocket;

    private BlockingQueue<TransportPacket> receivedPackets = new LinkedBlockingQueue<TransportPacket>(100);

    /**
     * Initialize UDP Worker
     *
     * @throws SocketException
     */
    protected void Initialize(boolean isReady) throws SocketException {
        datagramSocket.setSoTimeout(1);
        localAddress = datagramSocket.getLocalAddress();
        localPort = datagramSocket.getLocalPort();
        this.start();
        super.isReady = isReady;
    }

    /**
     * Creates a datagram socket, bound to the specified local address. The local port must be between 0 and 65535 inclusive.
     * IP address is 0.0.0.0 so the socket will be bound to the wildcard address, an IP address chosen by the kernel.
     *
     * @param localPort - local port to use
     * @throws SocketException
     * @see pl.edu.pjwstk.net.proto.ProtocolWorker#ProtocolWorker(Integer localPort)
     */
    public UDPWorker(Integer localPort) throws SocketException {
        super(localPort);
        datagramSocket = new DatagramSocket(localPort);
        Initialize(true);
    }

    /**
     * Creates a datagram socket, bound to the specified local address. The local port must be between 0 and 65535 inclusive.
     * If the IP address is 0.0.0.0, the socket will be bound to the wildcard address, an IP address chosen by the kernel.
     *
     * @param localIP   - local interface IP address to bind
     * @param localPort - local port to use
     * @throws SocketException
     * @see pl.edu.pjwstk.net.proto.ProtocolWorker#ProtocolWorker(InetAddress localIP, Integer localPort)
     */
    public UDPWorker(InetAddress localIP, Integer localPort) throws SocketException {
        super(localIP, localPort);
        datagramSocket = new DatagramSocket(localPort, localIP);
        Initialize(true);
    }

    /**
     * Creates a datagram socket, bound to the specified local address in message. The local port must be between 0 and 65535 inclusive.
     * If the IP address is 0.0.0.0, the socket will be bound to the wildcard address, an IP address chosen by the kernel.
     *
     * @param message - message to send
     * @throws SocketException
     * @throws UnknownHostException
     * @see pl.edu.pjwstk.net.proto.ProtocolWorker#ProtocolWorker(Message message)
     */
    public UDPWorker(Message message) throws SocketException, UnknownHostException {
        super(message);
        this.datagramSocket = new DatagramSocket(message.getSenderPort(), InetAddress.getByName(message.getSenderAddress()));
        this.datagramSocket.connect(InetAddress.getByName(message.getReceiverAddress()), message.getReceiverPort());
        Initialize(true);
        SendMessage(message);
    }

    /**
     * Creates a datagram socket, bound to the specified local address in message. The local port must be between 0 and 65535 inclusive.
     * If the IP address is 0.0.0.0, the socket will be bound to the wildcard address, an IP address chosen by the kernel.
     *
     * @param localIP    - local interface IP address to bind
     * @param localPort  - local port to use
     * @param remoteIP   - remote IP address to bind
     * @param remotePort - remote port to bind
     * @throws SocketException
     * @see pl.edu.pjwstk.net.proto.ProtocolWorker#ProtocolWorker(InetAddress localIP, Integer localPort, InetAddress remoteIP, Integer remotePort)
     */
    public UDPWorker(InetAddress localIP, Integer localPort, InetAddress remoteIP, Integer remotePort) throws SocketException {
        super(localIP, localPort, remoteIP, remotePort);
        datagramSocket = new DatagramSocket(localPort, localIP);
        datagramSocket.connect(remoteIP, remotePort);
        Initialize(true);
    }

    /* (non-Javadoc)
      * @see pl.edu.pjwstk.net.proto.ProtocolWriter#Send(byte[])
      */

    public boolean Send(byte[] packet) throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length);

        datagramSocket.send(datagramPacket);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Message (size = " + datagramPacket.getLength() + ") sent over UDP(connected socket) to address="
                    + datagramSocket.getInetAddress().getHostAddress() + ":" + datagramSocket.getPort());
        }

        return false;
    }

    /* (non-Javadoc)
      * @see pl.edu.pjwstk.net.proto.ProtocolWriter#Send(java.net.InetAddress, java.lang.Integer, byte[])
      */

    public boolean Send(InetAddress remoteIP, Integer remotePort, byte[] packet) throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length, remoteIP, remotePort);

        datagramSocket.send(datagramPacket);

        if (LOG.isDebugEnabled()) {
            StringBuilder debugMessage = new StringBuilder(packet.getClass().getName());
            debugMessage.append(" message sent over UDP(not connected socket) to address=");
            debugMessage.append(remoteIP.getHostAddress());
            debugMessage.append(":");
            debugMessage.append(remotePort.toString());
            LOG.debug(debugMessage.toString());
        }

        return false;
    }

    /* (non-Javadoc)
      * @see pl.edu.pjwstk.net.proto.ProtocolWriter#SendMessage(pl.edu.pjwstk.p2pp.messages.Message)
      */

    public boolean SendMessage(Message message) {
        try {
            // checks if message has to be send over UDP - TODO move to generic class
            if (!message.isOverReliable() && !message.isEncrypted()) {
            } else {
                return false;
            }

            if (LOG.isTraceEnabled()) LOG.trace("message to send=" + message.toString());
            if (message instanceof P2PPMessage) {
                if (!((P2PPMessage) message).verify()) {
                    if (LOG.isDebugEnabled()) LOG.debug("Corrupted P2PPMessage will be send.");
                }
            }
            byte[] bytes = message.asBytes();

            if (datagramSocket.isConnected()) {
                Send(bytes);
            } else {
                //TODO Remove - only for compatibility with previous version
                Send(InetAddress.getByName(message.getReceiverAddress()), message.getReceiverPort(), bytes);
            }

        } catch (IOException e) {
            LOG.error("IO exception while sending. Message from exception=" + e.getMessage());
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
      * @see pl.edu.pjwstk.net.proto.ProtocolControl#isReliable()
      */

    public boolean isReliable() {
        return isReliable;
    }

    public boolean isEncrypted() {
        return false;
    }

    /* (non-Javadoc)
      * @see pl.edu.pjwstk.net.proto.ProtocolReader#Receive()
      */

    public byte[] Receive() throws IOException {
        return ReceivePacket().packetBody;
    }

    /* (non-Javadoc)
      * @see pl.edu.pjwstk.net.proto.ProtocolReader#ReceivePacket()
      */

    public TransportPacket ReceivePacket() throws IOException {
        byte[] buffer = new byte[PACKET_SIZE_MAX];

        DatagramPacket packet = new DatagramPacket(buffer, PACKET_SIZE_MAX);

        datagramSocket.receive(packet);

        if (LOG.isDebugEnabled()) {
            StringBuilder debugMessage = new StringBuilder("\tReceived UDP packet of size=");
            debugMessage.append(packet.getLength());
            debugMessage.append(" from ");
            debugMessage.append(packet.getAddress().getHostName());
            debugMessage.append(":");
            debugMessage.append(packet.getPort());
            LOG.debug(debugMessage.toString());
        }

        byte[] receivedData = ByteUtils.subarray(buffer, 0, packet.getLength());
        return new TransportPacket(packet.getAddress(), packet.getPort(), receivedData);
    }

    private TransportPacket getReceivedPacket() {
        TransportPacket transportPacket = null;

        try {
            transportPacket = this.receivedPackets.poll(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.error("Error while receiving packet", e);
        }

        return transportPacket;
    }

    /* (non-Javadoc)
      * @see pl.edu.pjwstk.net.proto.ProtocolReader#ReceiveMessage()
      */

    public NonInterpretedMessage ReceiveMessage() throws IOException {
        TransportPacket packet = this.getReceivedPacket();
        NonInterpretedMessage message = null;

        if (packet != null) {
            message = new NonInterpretedMessage(localAddress.getHostAddress(), localPort, packet.remoteAddress.getHostAddress(),
                    packet.remotePort, isReliable, false, packet.packetBody);
        }

        return message;
    }

    /* (non-Javadoc)
      * @see pl.edu.pjwstk.net.proto.ProtocolControl#isMessageStateMachine()
      */
	public boolean isMessageStateMachine() {		
		return true;
	}

	public boolean isWorkerReady() {
		return isReady;
	}

    public void run() {
        try {

            while (true) {

                try {

                    TransportPacket receivedPacket = this.ReceivePacket();
                    if (receivedPacket != null) {
                        if (!this.receivedPackets.offer(receivedPacket, 1, TimeUnit.SECONDS)) {
                            LOG.warn("Buffer is full. Dropping packet " + receivedPacket);
                        }
                    }

                } catch (SocketTimeoutException e) {
			        // Ignore because this says only that there's nothing in socket.
                } catch (Throwable e) {
                    LOG.error("Error while running UDPWorker", e);
                }

            }

        } catch (Throwable e) {
            LOG.error("Error while running UDPWorker. Loop broken. This should not happen", e);
        }
    }

}
