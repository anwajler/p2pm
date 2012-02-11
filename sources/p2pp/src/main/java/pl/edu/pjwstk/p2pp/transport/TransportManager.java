package pl.edu.pjwstk.p2pp.transport;

import java.net.InetAddress;
import java.util.Vector;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.net.proto.ProtocolWorker;
import pl.edu.pjwstk.net.proto.SupportedEncryption;
import pl.edu.pjwstk.net.proto.SupportedProtocols;
import pl.edu.pjwstk.net.proto.UDP.UDPWorker;
import pl.edu.pjwstk.net.proto.TCP.TCPWorker;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.util.AbstractMessageFactory;

public class TransportManager extends Thread {
	private static Logger LOG = Logger.getLogger(TransportManager.class);

    private boolean running = true;

    private TransportFrontier frontier = new TransportFrontier();
	private Vector<Message> messages = new Vector<Message>();
	private Vector<AbstractMessageFactory> messageFactories = new Vector<AbstractMessageFactory>();
	private Vector<TransportWorker<? extends ProtocolWorker>> transportObjects = new Vector<TransportWorker<? extends ProtocolWorker>>();
	
	public TransportManager() {}
	
	public void receiveMessages(){
		for (TransportWorker<? extends ProtocolWorker> currentTransportWorker : transportObjects) {
			if (currentTransportWorker.isWorkerReady()) {
				for (Message currentMessage : currentTransportWorker.ReceiveMessages()) {
					if (LOG.isTraceEnabled()) LOG.trace("Message added to queue " + currentMessage);
					messages.add(currentMessage);
				}	
			}
		}		
	}
	
	public Message getMessageFromQueue(){
		Message message = null;
		receiveMessages();
		if(messages.size() > 0){
			messages.firstElement();
			message = messages.remove(0);
			if (LOG.isTraceEnabled()) LOG.trace("Message queue size = " + messages.size());
		} 
		return message;
	}
	
	public void setSourceIDLength(byte sourceIDLength){
        for (TransportWorker<? extends ProtocolWorker> transportWorker : this.transportObjects) {
            transportWorker.setSourceIDLength(sourceIDLength);
        }
	}
	
	public void setMessageStorage(MessageStorage messageStorage){
		//TODO Original code  
		//		for (CommunicationObject currentCommunicationObject : communicationObjects) {
		//		    currentCommunicationObject.setMessageStorage(messageStorage);
		//	    }		
	}
	
	private boolean addTransportWorker(SupportedProtocols protocol, InetAddress localAddress, Integer localPort, String enryptionKeys,
                                       String encryptionPass) {
		try {

			TransportWorker<? extends ProtocolWorker> transportWorker = null;
            if (SupportedProtocols.UDP == protocol) {
                if (localAddress == null) {
                    transportWorker = new TransportWorker<UDPWorker>(new UDPWorker(localPort));
                } else {
                    transportWorker = new TransportWorker<UDPWorker>(new UDPWorker(localAddress, localPort));
                }
            } else if (SupportedProtocols.TCP == protocol) {
                if (localAddress == null) {
                    transportWorker = new TransportWorker<TCPWorker>(new TCPWorker(localPort));
                } else {
                    transportWorker = new TransportWorker<TCPWorker>(new TCPWorker(localAddress, localPort));
                }
            } else if (SupportedProtocols.TCP_TLS == protocol) {
                transportWorker = new TransportWorker<TCPWorker>(new TCPWorker(localPort, SupportedEncryption.TLS, enryptionKeys, encryptionPass));
            } else if (SupportedProtocols.TCP_SSL == protocol) {
                transportWorker = new TransportWorker<TCPWorker>(new TCPWorker(localPort, SupportedEncryption.SSL, enryptionKeys, encryptionPass));
            }

            if (transportWorker != null) {
                for (AbstractMessageFactory currentFactory : messageFactories) {
                    transportWorker.addMessageFactory(currentFactory);
                }
                transportObjects.add(transportWorker);
            }
            
		} catch (Throwable e) {
			LOG.error("Error while adding transport worker", e);
			return false;
		}
		return true;
	}
	
	public void addMessageFactory(AbstractMessageFactory newFactory) {
		messageFactories.add(newFactory);
		for (TransportWorker<? extends ProtocolWorker> currentTransportWorker : transportObjects) {
			currentTransportWorker.addMessageFactory(newFactory);
		}
	}
	
	public void eventMessageToBeSend(Message message){
        this.frontier.add(message);
		/*for (TransportWorker<? extends ProtocolWorker> currentTransportWorker : transportObjects) {
			if (currentTransportWorker.isWorkerReady()) {
				currentTransportWorker.OnSend(message);
				//TODO all sending now message - working only because is only 1 worker
			}
		}*/
	}
	
	public boolean startListen(SupportedProtocols protocol, InetAddress localAddress, Integer localPort, String enryptionKeys, String encryptionPass){
		this.addTransportWorker(protocol, localAddress, localPort, enryptionKeys, encryptionPass);
		return true;		
	}

    public boolean startListen(SupportedProtocols protocol, InetAddress localAddress, Integer localPort) {
        return startListen(protocol, localAddress, localPort, "", "");
    }
	
	public boolean stopListen(SupportedProtocols protocol, InetAddress localAddress, Integer localPort){
	    // TODO deallocate and disconnect
		return true;
	}

    public void stopManager() {
        this.running = false;
    }

    public void run() {

        if (LOG.isDebugEnabled()) LOG.debug("Starting TransportManager");
        TransportWorker<? extends ProtocolWorker> currentTransportWorker = null;
        while (running) {
            if (this.isInterrupted()) {
                if (LOG.isDebugEnabled()) LOG.debug("Stopping TransportManager");
                break;
            }

            try {

                Message message;
                
                while ((message = this.frontier.poll()) != null) {

                    for (int i = 0; i < transportObjects.size(); i++) {
                        currentTransportWorker = transportObjects.get(i);
                        if (currentTransportWorker.isWorkerReady() && (currentTransportWorker.isReliable() == message.isOverReliable())) {
                            currentTransportWorker.OnSend(message);
                        }
                    }

                }

	            synchronized (this) {
	                wait(10);
	            }

            } catch (Throwable e) {
                LOG.error("Error while running TransportManager", e);
            }

        }

        if (LOG.isDebugEnabled()) LOG.debug("Stopping TransportManager");

    }

}
