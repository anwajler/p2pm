



package pl.edu.pjwstk.mteam.p2p;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.BindException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import pl.edu.pjwstk.mteam.core.NetworkObject;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.core.NodeCallback;
import pl.edu.pjwstk.p2pp.P2PPNodeCallback;
import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.logging.Logger;
import pl.edu.pjwstk.p2pp.ErrorInterface;
import pl.edu.pjwstk.p2pp.P2PPManager;
import pl.edu.pjwstk.p2pp.debug.processor.DebugWriter;
import pl.edu.pjwstk.p2pp.entities.P2PPEntity;
import pl.edu.pjwstk.p2pp.entities.Peer;
import pl.edu.pjwstk.p2pp.ice.STUNService;
import pl.edu.pjwstk.p2pp.kademlia.KademliaNeighborTable;
import pl.edu.pjwstk.p2pp.kademlia.KademliaPeer;
import pl.edu.pjwstk.p2pp.kademlia.KademliaRoutingTable;
import pl.edu.pjwstk.p2pp.messages.requests.PublishObjectRequest;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.messages.requests.SendMessageRequest;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.resources.MessageResourceObject;
import pl.edu.pjwstk.p2pp.resources.StringValueResourceObject;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerNeighbourTable;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerPeer;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerRoutingTable;
import pl.edu.pjwstk.p2pp.superpeer.messages.requests.IndexRequest;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.P2PPMessageFactory;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

public class P2PNode extends Node{
	public static enum RoutingAlgorithm {KADEMLIA, SUPERPEER};

	private static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.p2p.P2PNode");

	public static int DEFAULT_TCP_PORT = 0;
	public static int DEFAULT_UDP_PORT = 0;
	public static int DEFAULT_SSL_PORT = 0;
	public static int DEFAULT_TLS_PORT = 0;
	public static int DEFAULT_DTLS_PORT = 0;
	/*TODO: Set the appropriate file path below*/
	public static String DEFAULT_ENCRYPTION_KEY = "./p2pkeys";

	public static String DEFAULT_ENCRYPTION_PASS = "passphrase";
	public static String DEFAULT_SERVER_REFLEXIVE_ADDRESS = "10.20.0.1";
	public static int DEFAULT_SERVER_REFLEXIVE_PORT = 9030;

	/**
	 * Object responsible for interaction with overlay
	 */
	private P2PPManager node;
	/**
	 * Set of callback methods invoked directly by P2PP layer. Used f.e. for hiding
	 * NAT traversal issues from higher layers.
	 */
	private P2PPNodeCallback callback;
	/**
	 * Self reference for passing this object to NodeCallback methods
	 */
	private Node self;

	private byte[] overlayID = new byte[0];

	private String bootIP = "127.0.0.1";

	private int bootPort = 9000;

	private boolean isConnected = false;

	private Vector<P2PPEntity> entities;

	private RoutingAlgorithm routingAlgorithm;

	public P2PNode(NodeCallback nCallback){
		super(nCallback);
		self = this;
		transport = super.TRANSPORT_TCP;
		callback = new P2PPNodeCallback() {

			@Override
			public void joinCallback() {
				logger.debug("P2PNode - joinCallback");
				isConnected = true;
				//starting transport layer listener for pub-sub communication
				if(pubsubManager != null){
					//Initialization of the appropriate Customizable Algorithm component
					((CoreAlgorithm)pubsubManager).init();
					//Initialization of the publish-subscribe transport component
					pubsubMsgListener.start();
				}
				for (NodeCallback listener : nodeCallbacks) {
					listener.onJoin(self);
				}
				//nodeCallback.onJoin(self);
			}

			@Override
			public void leaveCallback() {
				if(pubsubManager != null)
					pubsubMsgListener.stop();
				isConnected = false;
				node.stop();
				for (NodeCallback listener : nodeCallbacks) {
					listener.onDisconnect(self);
				}
				//nodeCallback.onDisconnect(self);
			}

			@Override
			public void lookupCallback(Vector<ResourceObject> resourceObjects) {
				logger.debug("Lookup object callback invoked");
				for (NodeCallback listener : nodeCallbacks) {
					listener.onObjectLookup(self, resourceObjects);
				}
				//nodeCallback.onObjectLookup(self, resourceObjects);
			}

			@Override
			public boolean onDeliverRequest(Request req, List<ResourceObject> objectList) {
				//logger.info("invoking onDeliverRequest");
				boolean result = true;
				if(req instanceof PublishObjectRequest){
					PublishObjectRequest preq = (PublishObjectRequest)req;
					ResourceObject obj = preq.getResourceObject();
					if(obj.getContentType() == P2PPUtils.MESSAGE_CONTENT_TYPE){
						byte protocol = ((MessageResourceObject)obj).getContentSubtype();
						if(protocol == NetworkObject.TYPE_PUBSUB && pubsubManager != null){
							logger.trace("[P2PNode] Delivering publish-subscribe message");
							result = pubsubMsgListener.onDeliverMessage(((MessageResourceObject)obj).getMessageValue());
							objectList.remove(0);
							objectList.add(null);
						}
						else if(protocol != NetworkObject.TYPE_PUBSUB){
							if(protocol == NetworkObject.TYPE_PROTOTRUST)
								logger.trace("[P2PNode] Delivering prototrust message");
							List<NetworkObject> msgObjects = new Vector<NetworkObject>();
							msgObjects.add(new NetworkObject(obj.getContentSubtype(),
									                         new String(obj.getUnhashedID()),
									                         ((MessageResourceObject)obj).getMessageValue()));
							for (NodeCallback listener : nodeCallbacks) {
								result = listener.onDeliverRequest(msgObjects);
								if (result == false)
									break;
							}
							//result = nodeCallback.onDeliverRequest(msgObjects);
						}
					}
				}
				/*SUPERPEERFIX*/
				else if(req instanceof IndexRequest){
					IndexRequest preq = (IndexRequest)req;
					ResourceObject obj = preq.getResourceObject();
					if(obj.getContentType() == P2PPUtils.MESSAGE_CONTENT_TYPE){
						byte protocol = ((MessageResourceObject)obj).getContentSubtype();
						if(protocol == NetworkObject.TYPE_PUBSUB && pubsubManager != null){
							result = pubsubMsgListener.onDeliverMessage(((MessageResourceObject)obj).getMessageValue());
							objectList.remove(0);
							objectList.add(null);
						}
						else if(protocol != NetworkObject.TYPE_PUBSUB){
							if(protocol == NetworkObject.TYPE_PROTOTRUST)
								logger.trace("[P2PNode] Delivering prototrust message");
							List<NetworkObject> msgObjects = new Vector<NetworkObject>();
							msgObjects.add(new NetworkObject(obj.getContentSubtype(),
									                         new String(obj.getUnhashedID()),
									                         ((MessageResourceObject)obj).getMessageValue()));
							for (NodeCallback listener : nodeCallbacks) {
								result = listener.onDeliverRequest(msgObjects);
								if (result == false)
									break;
							}
							//result = nodeCallback.onDeliverRequest(msgObjects);
						}
					}
				}
                                else if(req instanceof SendMessageRequest){
                                    SendMessageRequest preq = (SendMessageRequest)req;
                                    ResourceObject obj = preq.getMessageResourceObject();
                                    if(obj.getContentType() == P2PPUtils.MESSAGE_CONTENT_TYPE){
                                        byte protocol = ((MessageResourceObject)obj).getContentSubtype();
                                        if(protocol == NetworkObject.TYPE_MESSAGEOBJECT && pubsubManager != null){
							result = pubsubMsgListener.onDeliverMessage(((MessageResourceObject)obj).getMessageValue());
							objectList.remove(0);
							objectList.add(null);
						}
						else if(protocol != NetworkObject.TYPE_PUBSUB){
							if(protocol == NetworkObject.TYPE_PROTOTRUST)
								logger.trace("[P2PNode] Delivering prototrust message");
							List<NetworkObject> msgObjects = new Vector<NetworkObject>();
							msgObjects.add(new NetworkObject(obj.getContentSubtype(),
									                         new String(obj.getUnhashedID()),
									                         ((MessageResourceObject)obj).getMessageValue()));
							for (NodeCallback listener : nodeCallbacks) {
								result = listener.onDeliverRequest(msgObjects);
								if (result == false)
									break;
							}
							//result = nodeCallback.onDeliverRequest(msgObjects);
						}
                                    }
                                }
				return result;
			}

			@Override
			public boolean onForwardingRequest(Request req, List<ResourceObject> objectList) {
				boolean result = true;
				if(req instanceof PublishObjectRequest){
					ResourceObject obj = objectList.get(0);
					if(obj instanceof MessageResourceObject){
						logger.trace("Forwarding encapsulated message");
						byte protocol = ((MessageResourceObject)obj).getContentSubtype();
						if(protocol == NetworkObject.TYPE_PUBSUB && pubsubManager != null){
							result = pubsubMsgListener.onForwardingMessage(((MessageResourceObject)obj).getMessageValue());
							objectList.remove(0);
							objectList.add(null);
						}
						else if(protocol != NetworkObject.TYPE_PUBSUB){
							if(protocol == NetworkObject.TYPE_PROTOTRUST)
								logger.trace("[P2PNode] Forwarding prototrust message");
							List<NetworkObject> msgObjects = new Vector<NetworkObject>();
							NetworkObject msgObject;
							for(ResourceObject resourceObject : objectList) {
								msgObject = new NetworkObject(((MessageResourceObject)resourceObject).getContentSubtype(),
										                                     new String(resourceObject.getUnhashedID()),
										                                     ((MessageResourceObject)resourceObject).getMessageValue());
								msgObjects.add(msgObject);
							}
							for (NodeCallback listener : nodeCallbacks) {
								result = listener.onForwardingRequest(msgObjects);
								if (result == false)
									break;
							}
							//result = nodeCallback.onForwardingRequest(msgObjects);
						}
					}
				}
				return result;
			}

			@Override
			public boolean onNeighborJoin(PeerInfo arg0, int arg1) {
				//TODO: ask, where the types (peer/client) are defined
				logger.trace("New node joined: "+arg0);
				return true;
			}

			@Override
			public void onNeighborLeave(PeerInfo node, int nodeType) {
				logger.trace("Node left: "+node);
			}

			@Override
			public void queryCallback(byte[] arg0, byte arg1, byte arg2, short arg3) {
				// TODO Auto-generated method stub
			}

			@Override
			public void removeCallback() {
				// TODO Auto-generated method stub
			}

			@Override
			public void errorCallback(ErrorInterface errorObject, int errorCode) {
				switch(errorCode){
				case P2PPNodeCallback.BOOTSTRAP_ERROR_CODE:
					logger.trace("Bootstap error...");
					break;
				case P2PPNodeCallback.NAT_ERROR_CODE:
					logger.trace("NAT error...");
					//TODO: Currently P2PP NAT traversal is not available
					break;
				case P2PPNodeCallback.RESOURCE_LOOKUP_ERROR_CODE:
					logger.trace("Resource not found...");
					for (NodeCallback listener : nodeCallbacks) {
						listener.onOverlayError(self, null, errorCode);
					}
					//nodeCallback.onOverlayError(self, null, errorCode);
					break;
				case P2PPNodeCallback.USER_LOOKUP_ERROR_CODE:
					logger.fatal("User not found...",new Throwable());
                                        
					for (NodeCallback listener : nodeCallbacks) {
						listener.onOverlayError(self, null, errorCode);
					}
					//nodeCallback.onOverlayError(self, null, errorCode);
					break;
				case P2PPNodeCallback.INSERT_ERROR_CODE:
					logger.trace("Could not insert object...");
					for (NodeCallback listener : nodeCallbacks) {
						listener.onOverlayError(self, null, errorCode);
					}
					//nodeCallback.onOverlayError(self, null, errorCode);
					break;
				}
			}

			@Override
			public void publishCallback(byte contentType, byte contentSubtype, byte[] key, byte[] resourceObjectValue) {
				if(contentType == P2PPUtils.MESSAGE_CONTENT_TYPE){
					if(contentSubtype == NetworkObject.TYPE_PUBSUB){
						logger.trace("Publish-subscribe request reached its destination....");
					}
				}
				else if (contentType ==P2PPUtils.USER_INFO_CONTENT_TYPE){
					logger.trace("User info published");
				}
				else if (contentType ==P2PPUtils.STUN_CONTENT_TYPE){
					logger.trace("STUN info published");
				}
				else{
					NetworkObject obj = new NetworkObject((int)contentType, new String(key), resourceObjectValue);
					for (NodeCallback listener : nodeCallbacks) {
						listener.onInsertObject(self, obj);
					}
					//nodeCallback.onInsertObject(self, obj);
					logger.trace("Invoking insert callback....");
				}
			}

			@Override
			public void removeCallback(ResourceObject arg0) {
				// TODO Auto-generated method stub

			}

		};
	}

	public P2PNode(NodeCallback nCallback, RoutingAlgorithm p2palgorithm){
		this(nCallback);
		routingAlgorithm = p2palgorithm;
		if(p2palgorithm == RoutingAlgorithm.KADEMLIA){
			// creates P2PP manager
			node = new P2PPManager(DEFAULT_TCP_PORT, DEFAULT_UDP_PORT, DEFAULT_SSL_PORT, DEFAULT_TLS_PORT,
					               DEFAULT_DTLS_PORT, DEFAULT_ENCRYPTION_KEY, DEFAULT_ENCRYPTION_PASS, new P2PPMessageFactory(),
					               new byte[0]);
			Peer peer = new KademliaPeer();
			// adds services to manager
			//ICEService iceService = new ICEService(true);
			STUNService stunService = new STUNService(true, AddressInfo.UDP_TRANSPORT_TYPE);
			// TODO to be deleted when ICE implementation is ready
			STUNService.serverReflexiveAddress = DEFAULT_SERVER_REFLEXIVE_ADDRESS;
			STUNService.serverReflexivePort = DEFAULT_SERVER_REFLEXIVE_PORT;
			// TURNService turnService = new TURNService(true);
			// peer.addService(iceService);
	        peer.addService(stunService);
	        // peer.addService(turnService);
	        peer.setCallback(callback);

	        node.addEntity(peer);
	        entities = new Vector<P2PPEntity>();
	        entities.add(peer);
		}else if(p2palgorithm == RoutingAlgorithm.SUPERPEER){
			// creates P2PP manager
			node = new P2PPManager(DEFAULT_TCP_PORT, DEFAULT_UDP_PORT, DEFAULT_SSL_PORT, DEFAULT_TLS_PORT,
		               			   DEFAULT_DTLS_PORT, DEFAULT_ENCRYPTION_KEY, DEFAULT_ENCRYPTION_PASS, new P2PPMessageFactory(), new byte[0]);
			node.setOptions(new P2POptions(P2PPUtils.SHA1_HASH_ALGORITHM, (byte)20,
                    P2PPUtils.SUPERPEER_P2P_ALGORITHM, (byte)2, overlayID));
			Peer peer = new SuperPeerPeer();
            // ICEService iceService = new ICEService(true);
            STUNService stunService = new STUNService(true, AddressInfo.UDP_TRANSPORT_TYPE);
            // TURNService turnService = new TURNService(true);
            // peer.addService(iceService);
            peer.addService(stunService);
            node.addEntity(peer);
            // peer.addResource(new STUNPortResource(9898));
            entities = new Vector<P2PPEntity>();
            entities.add(peer);
            peer.setCallback(callback);
		}
	}

	public CoreAlgorithm getPubSubCoreAlgorithm(){
		return (CoreAlgorithm)super.getPubSubCoreAlgorithm();
	}

	public void enableDebug() {
        for (P2PPEntity entity : entities) {
            entity.enableDebug();
        }
	}

	@Override
	public String getID(){
		return ByteUtils.byteArrayToHexString(node.getSharedManager().getPeerIDAsBytes());
	}

	@Override
	public int getTcpPort() {
		return node.getTcpPort();
	}

	@Override
	public int getUdpPort() {
		return node.getUdpPort();
	}

	@Override
	public String getUserName() {
		String name = "";
		try {
			name = new String(node.getSharedManager().getUnhashedID(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.trace("Unsupported encoding");
			e.printStackTrace();
		}
		return name;
	}

	public String getBootIP(){
		return bootIP;
	}

	public int getBootPort(){
		return bootPort;
	}

	@Override
	public void setBootIP(String s) {
		bootIP = s;
	}

	@Override
	public void setBootPort(int i) {
		bootPort = i;
	}

	@Override
	public void setTcpPort(int i) {
		if(!isConnected){
			if(routingAlgorithm == RoutingAlgorithm.KADEMLIA){
				node = new P2PPManager(i, node.getUdpPort(), node.getSslPort(), node.getTlsPort(), node.getDtlsPort(), DEFAULT_ENCRYPTION_KEY, DEFAULT_ENCRYPTION_PASS,
									   new P2PPMessageFactory(),
					                   node.getSharedManager().getUnhashedID());
				node.addEntity(entities.get(0));
			}
			else if(routingAlgorithm == RoutingAlgorithm.SUPERPEER){
				node = new P2PPManager(i, node.getUdpPort(), node.getSslPort(), node.getTlsPort(), node.getDtlsPort(), DEFAULT_ENCRYPTION_KEY, DEFAULT_ENCRYPTION_PASS,
						new P2PPMessageFactory(), node.getSharedManager().getUnhashedID());
				node.setOptions(new P2POptions(P2PPUtils.SHA1_HASH_ALGORITHM, (byte)20,
	                    		P2PPUtils.SUPERPEER_P2P_ALGORITHM, (byte)2, overlayID));
				node.addEntity(entities.get(0));
			}
		}
	}

	@Override
	public void setUdpPort(int i) {
		if(!isConnected){
			if(routingAlgorithm == RoutingAlgorithm.KADEMLIA){
				node = new P2PPManager(node.getTcpPort(), i, node.getSslPort(), node.getTlsPort(), node.getDtlsPort(),
						               DEFAULT_ENCRYPTION_KEY, DEFAULT_ENCRYPTION_PASS, new P2PPMessageFactory(),
									   node.getSharedManager().getUnhashedID());
				node.addEntity(entities.get(0));
			}
			else if(routingAlgorithm == RoutingAlgorithm.SUPERPEER){
				node = new P2PPManager(node.getTcpPort(), i, node.getSslPort(), node.getTlsPort(), node.getDtlsPort(),
						DEFAULT_ENCRYPTION_KEY, DEFAULT_ENCRYPTION_PASS, new P2PPMessageFactory(),
						   node.getSharedManager().getUnhashedID());
				node.setOptions(new P2POptions(P2PPUtils.SHA1_HASH_ALGORITHM, (byte)20,
	                    		P2PPUtils.SUPERPEER_P2P_ALGORITHM, (byte)2, overlayID));
				node.addEntity(entities.get(0));
			}
		}
	}

	@Override
	public void setUserName(String s) {
		if(isConnected == false){
			if(routingAlgorithm == RoutingAlgorithm.KADEMLIA){
				node = new P2PPManager(node.getTcpPort(), node.getUdpPort(), node.getSslPort(), node.getTlsPort(), node.getDtlsPort(),
						DEFAULT_ENCRYPTION_KEY, DEFAULT_ENCRYPTION_PASS, new P2PPMessageFactory(),
									   s.getBytes());
				node.addEntity(entities.get(0));
			}
			else if(routingAlgorithm == RoutingAlgorithm.SUPERPEER){
				node = new P2PPManager(node.getTcpPort(), node.getUdpPort(), node.getSslPort(), node.getTlsPort(), node.getDtlsPort(),
						DEFAULT_ENCRYPTION_KEY, DEFAULT_ENCRYPTION_PASS, new P2PPMessageFactory(),
						               s.getBytes());
				node.setOptions(new P2POptions(P2PPUtils.SHA1_HASH_ALGORITHM, (byte)20,
	                    		P2PPUtils.SUPERPEER_P2P_ALGORITHM, (byte)2, overlayID));
				node.addEntity(entities.get(0));
			}
		}
	}

	public void setServerReflexiveAddress(String srvrip){
		logger.trace("Setting server reflexive address: "+srvrip);
		STUNService.serverReflexiveAddress = srvrip;
	}

	public void setServerReflexivePort(int srvrport){
		logger.trace("Setting server reflexive port: "+srvrport);
		STUNService.serverReflexivePort = srvrport;
	}

	@Override
	public void networkJoin() {
		try {
			node.start();
		} catch (BindException e) {
			logger.error("[P2PNode::networkJoin] Address already in use");
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		Vector<P2PPEntity> entities = node.getEntities();

        for (int i = 0; i < entities.size(); i++) {
            P2PPEntity current = (P2PPEntity) entities.get(i);
            if (current instanceof pl.edu.pjwstk.p2pp.entities.Node) {
            	logger.trace("Joining network...");
                ((pl.edu.pjwstk.p2pp.entities.Node) current).join(overlayID, bootIP, bootPort);
            }
        }
	}

	@Override
	public void networkLeave() {
		Vector<P2PPEntity> entities = node.getEntities();
		for (P2PPEntity current : entities) {
            if (current instanceof pl.edu.pjwstk.p2pp.entities.Node) {
                ((pl.edu.pjwstk.p2pp.entities.Node) current).leave();
                logger.trace("[P2PNode]Trying to leave network");
                break;
            }
        }
	}
        
        public void sendMessage(String peerId, byte[] msg){
            Vector<P2PPEntity> entities = node.getEntities();
            for (P2PPEntity current : entities) {

                if (current instanceof pl.edu.pjwstk.p2pp.entities.Node) {
                    logger.warn("--- sending message to "+peerId+", peerIDBytes: "+Arrays.toString(peerId.getBytes()));
                    ((pl.edu.pjwstk.p2pp.entities.Node) current).sendMessage(peerId.getBytes(), " ", msg);
                    break;
                }
            }
        }

	@Override
	public void insert(NetworkObject object) {
		String resourceID = object.getKey();
		Vector<P2PPEntity> entities = node.getEntities();

		switch(object.getType()){
		case NetworkObject.TYPE_PUBSUB:
			byte[] resourceObjectByteValue = object.getValue();
			MessageResourceObject msgobj = new MessageResourceObject((byte)NetworkObject.TYPE_PUBSUB);
			msgobj.setMessageValue(resourceObjectByteValue);

	        for (P2PPEntity current : entities) {
	            if (current instanceof pl.edu.pjwstk.p2pp.entities.Node) {
	                ((pl.edu.pjwstk.p2pp.entities.Node) current).publish(resourceID.getBytes(),
	                		                                             msgobj);
	                break;
	            }
	        }
			break;
		case NetworkObject.TYPE_BYTEARRAY:
			String resourceObjectValue = "";
			try {
				resourceObjectValue = new String(object.getValue(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.trace("Unsupported encoding");
				e.printStackTrace();
			}

	        for (P2PPEntity current : entities) {
	            if (current instanceof pl.edu.pjwstk.p2pp.entities.Node) {
					((pl.edu.pjwstk.p2pp.entities.Node) current).publish(resourceID.getBytes(),
								                                         new StringValueResourceObject(resourceID,
						                                                                                   resourceObjectValue));
	                break;
	            }
	        }
			break;
		case NetworkObject.TYPE_PROTOTRUST:
			resourceObjectByteValue = object.getValue();
			MessageResourceObject pmsgobj = new MessageResourceObject((byte)NetworkObject.TYPE_PROTOTRUST);
			pmsgobj.setMessageValue(resourceObjectByteValue);

			for (P2PPEntity current : entities) {
				if (current instanceof pl.edu.pjwstk.p2pp.entities.Node) {
					((pl.edu.pjwstk.p2pp.entities.Node) current).publish(resourceID.getBytes(),
                		                                                 pmsgobj);
					break;
				}
			}
		break;
		}

	}

	@Override
	public void networkLookupObject(Object params) {
		String key = (String)params;
		Vector<P2PPEntity> entities = node.getEntities();
		for (int i = 0; i < entities.size(); i++) {
            P2PPEntity current = (P2PPEntity) entities.get(i);
            if (current instanceof pl.edu.pjwstk.p2pp.entities.Node) {
                ((pl.edu.pjwstk.p2pp.entities.Node) current).lookup(P2PPUtils.STRING_VALUE_CONTENT_TYPE, (byte) 0, key.getBytes(), null);
            }
		}
	}

	@Override
	public void networkLookupUser(Object params) {
		//TODO: Ask, whether this is implemented
		String key = (String)params;
		Vector<P2PPEntity> entities = node.getEntities();
		for (int i = 0; i < entities.size(); i++) {
            P2PPEntity current = (P2PPEntity) entities.get(i);
            if (current instanceof pl.edu.pjwstk.p2pp.entities.Node) {
                ((pl.edu.pjwstk.p2pp.entities.Node) current).lookup(P2PPUtils.USER_INFO_CONTENT_TYPE, (byte) 0, key.getBytes(), null);
            }
		}
	}

	@Override
	public void remove(Object objectID) {
		NetworkObject newObj = new NetworkObject(NetworkObject.TYPE_BYTEARRAY, (String)objectID, new byte[0]);
		insert(newObj);
	}

	@Override
	public void update(Object objectID, byte[] value) {
		NetworkObject newObj = new NetworkObject(NetworkObject.TYPE_BYTEARRAY, (String)objectID, value);
		insert(newObj);
	}

	@Override
	public int getDistance(String key1, String key2) {
		Vector<P2PPEntity> entities = node.getEntities();
		BigInteger distance = BigInteger.ZERO;
		for (int i = 0; i < entities.size(); i++) {
            P2PPEntity current = (P2PPEntity) entities.get(i);
            if (current instanceof KademliaPeer) {
                distance = ((KademliaPeer) current).getDistance(key1, key2);
            }
            else if(current instanceof SuperPeerPeer){
            	/*SUPERPEERFIX - BigInteger.ZERO;*/
            	distance = ((SuperPeerPeer) current).getDistance(key1, key2);
            }
		}
		return distance.intValue();
	}

	public String getOverlayAlgorithm(){
		//FIXME: replace this with querying the overlay for the algorithm
		return "whatever";
	}
        public boolean isConnected(){
		return isConnected;
	}

}
