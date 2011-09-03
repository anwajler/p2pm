/**
 * Contains objects related to publish-subscribe transport layer.
 */
package pl.edu.pjwstk.mteam.pubsub.transport;

import pl.edu.pjwstk.mteam.core.MessageListener;
import pl.edu.pjwstk.mteam.core.NetworkObject;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.pubsub.logging.Logger;

import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessageListener;
import pl.edu.pjwstk.mteam.pubsub.message.indication.PubSubIndication;
import pl.edu.pjwstk.mteam.pubsub.message.request.PubSubRequest;
import pl.edu.pjwstk.mteam.pubsub.message.response.PubSubResponse;

/**
 * Class representing publish-subscribe transport layer. It enables sending and 
 * receiving messages directly or using overlay routing algorithm. 
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class PubSubTransport implements MessageListener{
	private static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.transport.PubSubTransport");
	/**
	 * Value indicating, that the message should be encapsulated inside P2P resource
	 * object.
	 */
	public static final byte ROUTING_OVERLAY = 0;
	/**
	 * Value indicating, that the message should be sent directly to node specified
	 * in message destination section.
	 */
	public static final byte ROUTING_DIRECT = 1;
	
	/**
	 * Object, that uses this transport for communication. All incoming messages are 
	 * passed to this object.
	 */
	private PubSubMessageListener pubsubManager;
	
	/**
	 * Node, used sending/receiving publish-subscribe messages routed via overlay.
	 */
	private Node node;
	
	/**
	 * Port number for incoming publish-subscribe messages, which were sent 
	 * directly to this node.
	 */
	private int port;
	
	/**
	 * Listens for incoming publish-subscribe messages, that were sent 
	 * directly to this node. 
	 */
	private TCPReader tcpReader;
	
	/**
	 * Used for sending publish-subscribe messages directly.
	 */
	private TCPWriter tcpWriter;
	
	/**
	 * Creates PubSubTransport object.
	 * @param listener Object, which incoming messages will be passed to.
	 * @param portNum Publish-subscribe port number.
	 */
	public PubSubTransport(PubSubMessageListener listener, int portNum, Node n){
		pubsubManager = listener;
		port = portNum;
		node = n;
		tcpWriter = new TCPWriter();
		tcpReader = new TCPReader(port, this);
	}
	
	/**
	 * 
	 * @return Publish-subscribe port number.
	 */
	public int getPort(){
		return port;
	}
	
	/**
	 * Sends the message using overlay routing algorithm. It encapsulates 
	 * publish-subscribe message inside resource object's value. And sends 
	 * it inside 'insert' request.
	 * @param msg Outgoing publish-subscribe message.
	 * @param key Object key used by overlay routing algorithm. 
	 */
	public void sendThroughOverlay(PubSubMessage msg, String key){
		logger.debug("Sending mesage through overlay: source: "+msg.getSourceInfo().getName()+", destination: "+msg.getDestinationInfo().getName()+", msgType: "+msg.getType());
		NetworkObject object = new NetworkObject(NetworkObject.TYPE_PUBSUB, 
				                                 key, msg.encode());
		node.insert(object);
	}
        
        public void sendThroughOverlayMessage(PubSubMessage msg, String key){
                logger.debug("Sending mesage through overlay sendMessage: source: "+msg.getSourceInfo().getName()+", destination: "+msg.getDestinationInfo().getName()+", msgType: "+msg.getType());
		node.sendMessage(msg.getDestinationInfo().getName(), msg.encode());
        }
	
	/**
	 * Sends the message directly to specified node.
	 * @param msg Outgoing publish-subscribe message.
         * @return Value indicating, whether sending message was successful
	 */
	public boolean sendDirectly(PubSubMessage msg){
		//logger.debug("Sending message directly....: "+msg.toString());
		//return tcpWriter.sendMessage(msg);
                //--------------
                //logger.debug("Sending mesage through overlay....");
		//NetworkObject object = new NetworkObject(NetworkObject.TYPE_PUBSUB,
		//		                                 msg.getTopicID(), msg.encode());
		//node.insert(object);
                //return true;
                sendThroughOverlayMessage(msg, msg.getDestinationInfo().getID());
                return true;
	}
	
	/**
	 * Parses messages, which this node is destination for, determines, whether 
	 * it is a request, indication or response and invokes appropriate onDeliver...
	 * method from PubSubMessageListener.
	 * @param msgBytes Raw message bytes.
	 * @return If this message was encapsulated inside resource object, it informs 
	 * P2PP/RELOAD layer, whether it should insert this object into resource
	 * table or not.
	 */
        
	public boolean onDeliverMessage(byte[] msgBytes) {
                PubSubMessage msg;
                try{msg = PubSubMessage.parseMessage(msgBytes);
                if(msg instanceof PubSubRequest){
	       return pubsubManager.onDeliverRequest((PubSubRequest)msg);
		}
		else if(msg instanceof PubSubIndication){
	       return pubsubManager.onDeliverIndication((PubSubIndication)msg);
		}
		else{
	       return pubsubManager.onDeliverResponse((PubSubResponse)msg);  
                }
                }finally{
                    msg = null;
                    msgBytes = null;
                }
                
	}

	/**
	 * Parses messages, which this node is forwarder for, determines, whether 
	 * it is a request, indication or response and invokes appropriate onForwarding...
	 * method from PubSubMessageListener.
	 * @param msgBytes Raw message bytes.
	 * @return If this message was encapsulated inside resource object, it informs 
	 * P2PP/RELOAD layer, whether it should forward or discard the message.
	 */
	public boolean onForwardingMessage(byte[] msgBytes) {
		PubSubMessage msg = PubSubMessage.parseMessage(msgBytes);
		if(msg instanceof PubSubRequest)
	       return pubsubManager.onForwardingRequest((PubSubRequest)msg);
		return true;
	}
	
	public void start(){
		logger.debug("Starting pub-sub message transport layer");
		tcpReader.start();
	}

	public void stop(){
		logger.debug("Stopping pub-sub message transport layer");
		tcpReader.setRunning(false);
		tcpReader = new TCPReader(port, this);
	}

    public boolean isRunning() {
        return this.tcpReader.isRunning();
    }
}
