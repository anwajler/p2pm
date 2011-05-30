package pl.edu.pjwstk.net.message.STUN.tools;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import pl.edu.pjwstk.net.TransportPacket;
import pl.edu.pjwstk.net.proto.UDP.UDPWorker;

public class STUNTestServer {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNTestServer.class);
	private InetAddress protocolAddress;
	private int protocolPort;
	private UDPWorker udpWorker;
	/**
	 * @param protocolAddress
	 * @param protocolPort
	 */
	public STUNTestServer(InetAddress protocolAddress, int protocolPort) {
		this.protocolAddress = protocolAddress;
		this.protocolPort = protocolPort;
		
	}
	
	public void start(){
		logger.info("Starting server");
		TransportPacket trpacket;
		try {
			udpWorker = new UDPWorker(this.protocolAddress, this.protocolPort);
			logger.info("Server started successful");
			while(true){
				try {
					trpacket = udpWorker.ReceivePacket();
					logger.info("Received packet " 
							+ trpacket.remoteAddress.getHostAddress() 
							+ " " + trpacket.remotePort);
				} catch (SocketTimeoutException ste) {
				} catch (IOException e) {
					logger.error(e);
				}
			}
		} catch (SocketException e1) {
			logger.error(e1);
		}

	}
}
