package pl.edu.pjwstk.mteam.core;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This abstract class represents a node using P2PP. It provides higher layer
 * applications with P2P and pub-sub functionalities hiding issues such as NAT
 * traversal etc. Default behavior is trying to join overlay as a peer and
 * (if this can't be done) as a client.
 * <p>
 * Node object creating example:<br>
 *
 * <code> //Creating {@link pl.edu.pjwstk.mteam.core.NodeCallback} for Node<p>
 * <code> NodeCallback callback = new NodeCallback(){<p>
 * <code> 	    public void onDisconnect(Node node) {<br>
 * <code>			System.out.println("\nOnDisconnect invoked.....");<br>
 * <code>		}<p>

 * <code> 	    public void onOverlayError(Node node, Object sourceID, int errorCode) {<br>
 * <code> 	    	System.out.println("\onOverlayError invoked.....");<br>
 * <code> 	    }<p>
 *
 * <code> 	    public void onPubSubError(Node node, Object topicID, int errorCode) {<br>
 * <code> 	    	System.out.println("OnPubSubError for topic '"+topicID+"' callback invoked");<br>
 * <code> 	    }<p>

 * <code> 	 	public void onInsertObject(Node node, DHTObject object) {<br>
 * <code> 	    	System.out.println("\nOnInsert invoked.....");<br>
 * <code> 	    }<p>

 * <code> 	   	public void onJoin(Node node) {<br>
 * <code> 	    	System.out.println("\nOnJoin invoked.....");<br>
 * <code> 	   	}<p>

 * <code> 	    public void onObjectLookup(Node node, Object object) {<br>
 * <code> 	    	String res = (String)object;<br>
 * <code> 	    	System.out.println("\nOnObjectLookup result: "+res);<br>
 * <code> 	    }<p>

 * <code> 	    public void onUserLookup(Node node, Object userInfo) {<br>
 * <code> 	    	NodeInfo info = (NodeInfo)userInfo;<br>
 * <code> 	   		System.out.println("\nOnUserLookup result: "+info.getIP()+":"+info.getPort());<br>
 * <code> 	    }<p>

 * <code> 	    public void onTopicNotify(Node node, Object topicID, byte[] message) {<br>
 * <code> 	    	String id = (String)topicID;<br>
 * <code> 	    	String msg = (String)message;<br>
 * <code> 	    	System.out.println("Change notify callback invoked for topic '"+ id+"' (message: '"+msg+"')");<br>
 * <code> 	    }<p>

 * <code>		public void onTopicRemove(Node node, Object topicID) {<br>
 * <code>			System.out.println("Topic '"+topicID+"' removed callback invoked");<br>
 * <code> 		}<p>
 *
 * <code>		public void onTopicCreate(Node node, Object topicID) {<br>
 * <code>			System.out.println("Topic '"+topicID+"' created callback invoked");<br>
 * <code>		}<p>

 * <code>		public void onTopicSubscribe(Node node, Object topicID) {<br>
 * <code>			System.out.println("Topic '"+topicID+"' successful subscription callback invoked");<br>
 * <code>		}<p>

 * <code> 	    };<p>

 * <code> Node n = new P2PNode(callback);<br>
 * <code> n.setUserName("paulina");<br>
 * <code> n.setUdpPort(9060);<br>
 * <code> n.setTcpPort(9060);<p>
 * <code> //if node will be using pub-sub functionalities
 * <code> PubSubManager psmanager = new PubSubManagerImpl(p.getTcpPort()+1, n);
 *
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public abstract class Node implements P2PInterface{
	/**
	 * Value indicating that node will be communicating
	 * with other nodes in overlay using TCP
	 */
	public static final int TRANSPORT_TCP = 0;
	/**
	 * Value indicating that node will be communicating
	 * with other nodes in overlay using UDP
	 */
	public static final int TRANSPORT_UDP = 1;

	/**
	 * User-defined set of callback methods invoked after asynchronous
	 * operation is finished to pass the event to higher layer
	 */
	protected Vector<NodeCallback> nodeCallbacks;
	/**
	 * Object providing node with pub-sub functionalities
	 */
	protected AbstractCoreAlgorithm pubsubManager;
	/**
	 * Value indicating which protocol is used for communication with other nodes
	 * in overlay.<p>
	 * Accepted values are:<p>
	 * <li> {@link #TRANSPORT_TCP}
	 * <li> {@link #TRANSPORT_UDP}
	 */
	protected int transport;

	protected MessageListener pubsubMsgListener;

	/**
	 * Creates P2P node with default parameters
	 * @param nCallback user-defined set of callback methods invoked after asynchronous
	 * 				    operation is finished to pass the event to higher layer
	 */
	public Node(NodeCallback nCallback){
		nodeCallbacks = new Vector<NodeCallback>();
                if(nCallback!=null){
                    nodeCallbacks.add(nCallback);
                }
		pubsubManager = null;
	}

	/**
	 * Default value is 9083
	 * @param i new peer's port
	 */
	public abstract void setTcpPort(int i);

	/**
	 * Default value is 9083
	 * @param i new peer's port
	 */
	public abstract void setUdpPort(int i);

	/**
	 * Default value is 7080
	 * @param i new bootstrap server's port
	 */
	public abstract void setBootPort(int i);

	/**
	 * Default value is 127.0.0.1
	 * @param s new bootstrap server's IP
	 */
	public abstract void setBootIP(String s);

	/**
	 * Sets new username (also for connected user). Default value is 'default'
	 * @param s new username
	 */
	public abstract void setUserName(String s);

	/**
	 * Sets transport layer protocol for sending requests (default value is P2PPTransport.UDP)
	 * @param t acceptes values are: {@link #TRANSPORT_TCP}, {@link #TRANSPORT_UDP}
	 */
	public void setTransportType(int t){
		if(t == TRANSPORT_UDP || t == TRANSPORT_TCP){
			transport = t;
		}
	}

	/**
	 * Invoked to provide plain node with pub-sub functionalities.
	 * @param psmanager Pub-sub manager object.
	 */
	public void setPubSubCoreAlgorithm(AbstractCoreAlgorithm psmanager){
		pubsubManager = psmanager;
	}

	public void setMessageListener(MessageListener msgListener){
		pubsubMsgListener = msgListener;
	}

	/**
	 * Returns TCP port assigned to this node
	 */
	public abstract int getTcpPort();

	/**
	 * Returns UDP port assigned to this node
	 */
	public abstract int getUdpPort();

	/**
	 * Returns this node IP address string textual presentation
	 */
	public String getIP(){
            String ip = "127.0.0.1";
            
			ip = getWorkingInterfaceIP();
            return ip;
        }

	/**
	 * Returns node ID assigned to this node
	 */
	public abstract String getID();

	/**
	 * Returns user name (unhashed ID).
	 */
	public abstract String getUserName();

	public AbstractCoreAlgorithm getPubSubCoreAlgorithm(){
		return pubsubManager;
	}

	/**
	 * Returns publish-subscribe interface. Method can be used f.e. when user wants to invoke one of pub-sub methods.<br>
	 * Usage example:<p>
	 * <code> Node n = new P2PNode(nodeCallback);<br>
	 * <code> n.networkJoin();<br>
	 * <code> //when join operation is completed - create new topic<br>
	 * <code> boolean subscribe = true;
	 * <code> n.getPubSubInterface().createTopic("newTopic", subscribe);<p>
	 * @return Object, that provides node with pub-sub functionalities or
	 * 		   <code>null</code> if this object is not set.
	 */
	public PubSubInterface getPubSubInterface(){
		return pubsubManager.getCustomizableAlgorithm();
	}

	public void addCallback(NodeCallback nCallback){
            if(nCallback!=null)
		nodeCallbacks.add(nCallback);
	}

	/**
	 * @return Set of user-defined callback methods.
	 */
	public NodeCallback getCallback(){
		return nodeCallbacks.get(0);
	}

	public Vector<NodeCallback> getCallbacks(){
		return nodeCallbacks;
	}

	public abstract int getDistance(String key1, String key2);

	public abstract String getOverlayAlgorithm();

	public abstract void enableDebug();
        private String getWorkingInterfaceIP() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()){
                NetworkInterface ni = nets.nextElement();
                if(ni.isUp() && !ni.isLoopback()){
                    List<InterfaceAddress> ad = ni.getInterfaceAddresses();
                    return ad.get(ad.size()-1).getAddress().getHostAddress();
                }
            }
        } catch (SocketException ex) {

        }
        return "";
        }
        public abstract boolean isConnected();

}