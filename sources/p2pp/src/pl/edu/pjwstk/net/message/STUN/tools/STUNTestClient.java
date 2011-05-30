package pl.edu.pjwstk.net.message.STUN.tools;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import pl.edu.pjwstk.net.message.STUN.STUNMessage;
import pl.edu.pjwstk.net.message.STUN.STUNMessageClass;
import pl.edu.pjwstk.net.message.STUN.STUNMessageMethod;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNUsername;
import pl.edu.pjwstk.net.message.STUN.factory.STUNMessageFactory;
import pl.edu.pjwstk.net.proto.UDP.UDPWorker;
import pl.edu.pjwstk.types.ExtendedBitSet;

public class STUNTestClient {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNTestClient.class);
	private InetAddress protocolAddress;
	private int protocolPort;
	private UDPWorker udpWorker;
	/**
	 * @param protocolAddress
	 * @param protocolPort
	 */
	public STUNTestClient(InetAddress protocolAddress, int protocolPort) {
		this.protocolAddress = protocolAddress;
		this.protocolPort = protocolPort;
		
		
	}
	
	public void start(){

		try {
			udpWorker = new UDPWorker(null,2600,this.protocolAddress, this.protocolPort);
			STUNMessage testMessage = new STUNMessage();
			testMessage.setMessageClassandMethod(STUNMessageClass.REQUEST, STUNMessageMethod.BINDING);
			STUNUsername sa1 = new STUNUsername(testMessage,0);
			sa1.setUsername("rstrzele");
			testMessage.add(sa1);
			
			//STUNChangeRequest scr = new STUNChangeRequest(testMessage,0);
			//testMessage.add(scr);
			
			//logger.info("Message header = " + testMessage.getHeader().toStringBytes());
			//logger.info("Message attributes = " + testMessage.getAttributes().toStringBytes());
			udpWorker.Send(testMessage.toByte());
			logger.info("Packet sent to server");
			boolean working = true;

			while(working){
				try {
					byte[] bytes = udpWorker.Receive();
					ExtendedBitSet ebs = new ExtendedBitSet(bytes.length*8,false);
					ebs.set(0,bytes);
					STUNMessage testrecMessage = (STUNMessage)((new STUNMessageFactory()).interpret(ebs, 0, 0).firstElement());
					logger.debug("Is STUN Message? = " + testrecMessage.getMessageVersion().toString());
				} catch (SocketTimeoutException ste){ 
				} catch (IOException e) {
					logger.error(e);
				}
				//working = false;
			}
		} catch (SocketException e1) {
			logger.error(e1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
