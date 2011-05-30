/**
 * 
 */
package pl.edu.pjwstk.net.message.TURN.tools;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNMessageIntegrity;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNNonce;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNRealm;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNSoftware;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNUsername;
import pl.edu.pjwstk.net.message.TURN.TURNMessage;
import pl.edu.pjwstk.net.message.TURN.TURNMessageClass;
import pl.edu.pjwstk.net.message.TURN.TURNMessageMethod;
import pl.edu.pjwstk.net.message.TURN.attribute.TURNDontFragment;
import pl.edu.pjwstk.net.message.TURN.attribute.TURNLifetime;
import pl.edu.pjwstk.net.message.TURN.attribute.TURNRequestedTransport;
import pl.edu.pjwstk.net.message.TURN.factory.TURNMessageFactory;
import pl.edu.pjwstk.net.proto.SupportedProtocols;
import pl.edu.pjwstk.net.proto.UDP.UDPWorker;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com package
 *         pl.edu.pjwstk.net.message.TURN.tools
 */
public class TURNTestClient {

	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(TURNTestClient.class);
	private InetAddress protocolAddress;
	private int protocolPort;
	private UDPWorker udpWorker;
	private String turnUsername;
	@SuppressWarnings("unused")
	private String turnPassword;
	private String turnDomain;

	/**
	 * @param protocolAddress
	 * @param protocolPort
	 */
	public TURNTestClient(InetAddress protocolAddress, int protocolPort, String TURNUsername, String TURNPassword, String TURNDomain) {
		this.protocolAddress = protocolAddress;
		this.protocolPort = protocolPort;
		this.turnUsername = TURNUsername;
		this.turnPassword = TURNPassword;
		this.turnDomain = TURNDomain;
	}

	public void start() {

		try {
			udpWorker = new UDPWorker(null, 3478, this.protocolAddress,
					this.protocolPort);

			
			int working1 = 0;
			TURNMessage testrecMessage = null;
			TURNMessage testMessage2 = null;
			while (working1 < 2) {
				boolean working2 = true;
				if(working1 == 0)
				{
					TURNMessage testMessage = new TURNMessage();
					testMessage.setMessageClassandMethod(TURNMessageClass.REQUEST,
							TURNMessageMethod.ALLOCATE);

					STUNSoftware so1 = new STUNSoftware(testMessage,0);
					so1.setSoftware("mTeam 0.1 BETA");
					testMessage.add(so1);
					
					TURNLifetime sl1 = new TURNLifetime(testMessage,0);
					sl1.setLifeTime(3600);
					testMessage.add(sl1);
					
					TURNRequestedTransport rt1 = new TURNRequestedTransport(testMessage,0);
					rt1.setTransportType(SupportedProtocols.UDP);
					testMessage.add(rt1);
					
					TURNDontFragment df1 = new TURNDontFragment(testMessage, 0);
					testMessage.add(df1);
					
					udpWorker.Send(testMessage.toByte());
					logger.info("Packet sent to server");					
				}
				else
				{
					testMessage2 = new TURNMessage();
					//testMessage2.setTransactionID(testMessage.getTransactionID());
					testMessage2.setMessageClassandMethod(TURNMessageClass.REQUEST,
							TURNMessageMethod.ALLOCATE);
					STUNSoftware so2 = new STUNSoftware(testMessage2,0);
					so2.setSoftware("mTeam 0.1 BETA");
					testMessage2.add(so2);

					TURNLifetime sl2 = new TURNLifetime(testMessage2,0);
					sl2.setLifeTime(3600);
					testMessage2.add(sl2);
					
					TURNRequestedTransport rt2 = new TURNRequestedTransport(testMessage2,0);
					rt2.setTransportType(SupportedProtocols.UDP);
					testMessage2.add(rt2);
					
					TURNDontFragment df2 = new TURNDontFragment(testMessage2, 0);
					testMessage2.add(df2);
					STUNUsername sa2 = new STUNUsername(testMessage2,0);
					sa2.setUsername(this.turnUsername);
					testMessage2.add(sa2);
					STUNRealm ra1 = new STUNRealm(testMessage2,0);
					ra1.setRealm(this.turnDomain);
					testMessage2.add(ra1);
					
					STUNNonce sn1 = new STUNNonce(testMessage2,0); 
					sn1.setAttribute(testrecMessage.getAttribute(STUNAttributeType.NONCE).getAttribute());

					testMessage2.add(sn1);
					
					STUNMessageIntegrity mi1 = new STUNMessageIntegrity(testMessage2, 0);
					testMessage2.add(mi1);
					mi1.computeValue(testMessage2);

					udpWorker.Send(testMessage2.toByte());
					logger.info("Packet sent to server (testMessage2)");
				}
				testrecMessage = null;
				while (working2) {
					try {
						byte[] bytes = udpWorker.Receive();
						ExtendedBitSet ebs = new ExtendedBitSet(
								bytes.length * 8, false);
						ebs.set(0, bytes);
						testrecMessage = (TURNMessage) ((new TURNMessageFactory())
								.interpret(ebs, 0, 0).firstElement());
						logger
								.debug("Is TURN Message? = "
										+ testrecMessage.getMessageVersion()
												.toString());
						logger
						.debug("Is valid Message? = "
								+ testrecMessage.isValid());
					} catch (SocketTimeoutException ste) {
					} catch (IOException e) {
						logger.error(e);
					}
					if (testrecMessage != null) working2 = false;
				}
				working1++;				
			}
		} catch (SocketException e1) {
			logger.error(e1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
