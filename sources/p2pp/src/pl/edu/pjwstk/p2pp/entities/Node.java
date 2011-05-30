package pl.edu.pjwstk.p2pp.entities;

import java.math.BigInteger;
import java.util.Vector;

import pl.edu.pjwstk.p2pp.GlobalConstants;
import pl.edu.pjwstk.p2pp.P2PPNodeCallback;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.objects.NeighborTable;
import pl.edu.pjwstk.p2pp.objects.Owner;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.objects.RoutingTable;

/**
 * Abstract class describing Node. It exposes asynchronous API as described for Application Layer in P2PP specification.
 * Asynchronous callbacks are set by
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 *
 */
public abstract class Node extends P2PPEntity {

	protected boolean parallelLookupsEnabled = GlobalConstants.parallelLookupsEnabled;
	protected boolean communicationOverReliable = GlobalConstants.isOverReliable;
	protected boolean communicationEncrypted = GlobalConstants.isEncrypted;

	/**
	 * Constant for initial state of node. It is a state before the communication begins.
	 */
	public static final int INITIAL_NODE_STATE = 0;
	/**
	 * Constant for node's state which takes place when bootstrap process was started. It started after BootstrapRequest
	 * was sent.
	 */
	public static final int BOOTSTRAPPING_NODE_STATE = 1;
	/**
	 * Constant for node's state which takes place when node is looking for STUN server. It is started when joining peer
	 * got bootstrap candidates.
	 */
	public static final int SEARCHING_FOR_STUN_SERVER_NODE_STATE = 2;
	/**
	 * Constant for node's state which takes place when node is communicating with for STUN server.
	 */
	public static final int STUN_SERVER_COMMUNICATION_NODE_STATE = 3;

	/**
	 * Constant for node's state which takes place when node is trying to determine its relayed address. TODO really?
	 */
	public static final int TURN_COMMUNICATION_NODE_STATE = 4;

	// TODO probably constant for communication with TURN and ICE server communication
	/**
	 * Constant for node's state which takes place when JoinRequest was sent.
	 */
	public static final int JOIN_REQUEST_SENT_NODE_STATE = 6;

	/**
	 * Constant for node's state which takes place after receiving first JoinResponse, when neighbours are informed
	 * about joining peer.
	 */
	public static final int INFORMING_NEIGHBOURS_NODE_STATE = 7;

	/**
	 * Constant for node's state which takes place when node requests neighbour and routing tables of its neighbours.
	 */
	public static final int REQUESTING_NEIGHBOR_ROUTING_TABLES_NODE_STATE = 8;

	/**
	 * Constant for node's state which takes place when node has sent a publish request with UserInfo.
	 */
	public static final int PUBLISHING_USER_INFO_NODE_STATE = 8;

	/**
	 * Constant for node's state which takes place when node has joined an overlay.
	 */
	public static final int JOINED_NODE_STATE = 9;

	/**
	 * Vector of PeerInfo objects describing bootstrap candidates received in response to BootstrapRequest.
	 */
	protected Vector<PeerInfo> bootstrapCandidates = new Vector<PeerInfo>();

	/**
	 * Index of currently used PeerInfo object describing bootstrap candidate. It is an index in vector of bootstrap
	 * candidates. Used for checking bootstrap candidates one-by-one. If one bootstrap candidate doesn't respond to
	 * LookupObjectRequest,
	 */
	protected int currentBootstrapCandidateIndex = 0;

	/**
	 * Callback.
	 */
	protected P2PPNodeCallback callback;

	protected NeighborTable neighborTable;
	protected RoutingTable routingTable;

	/**
	 * Creates node.
	 */
	public Node() {}

	/**
	 * Sets callback interface for this node.
	 *
	 * @param callback
	 */
	public void setCallback(P2PPNodeCallback callback) {
		this.callback = callback;
	}

	/**
	 * Returns callback interface being a part of this node.
	 *
	 * @return
	 */
	public P2PPNodeCallback getCallback() {
		return callback;
	}

	/**
	 * Returns a PeerInfo object describing a peer that is closest to given id. Returns null if this peer doesn't know
	 * of any remote peer. If there are two (or more) peers with the same distance to this one, random one of them is
	 * returned.
	 *
	 * @return
	 */
	public abstract PeerInfo getClosestTo(byte[] id);

	// API
	public abstract void query();

	/**
	 * Joins peer in the overlay.
	 *
	 * @param overlayID
	 *            ID of an overlay that this node wants to join. TODO exact format isn't determined yet
	 * @param overlayPeerAddress
	 *            Address of a peer from an overlay. IP or DNS name.
	 * @param overlayPeerPort
	 *            Port of a peer in overlay.
	 */
	public abstract void join(byte[] overlayID, String overlayPeerAddress, int overlayPeerPort);

	public abstract void leave();

	/**
	 * Publishes given ResourceObject with given resourceID. Given unhashedID is a key (in networks that don't use hash
	 * function) or data for hash function that will generate key.
	 *
	 * @param unhashedID
	 * @param resourceObject
	 */
	public abstract void publish(byte[] unhashedID, ResourceObject resourceObject);

	/**
	 * <p>
	 * Makes a lookup for given content type, content subtype, resourceID and optional owner (may be null).
	 * </p>
	 * <p>
	 * Originally (as it was described in P2PP specification draft 01), this method was with one parameter - resourceID.
	 * Salman's implementation has LookupObject in Node class and this method assumes that given resourceID is for
	 * StringValue content type. We want this our method to look for all types of resources.
	 * </p>
	 *
	 * @param contentType
	 * @param contentSubtype
	 * @param resourceID
	 * @param owner
	 */
	public abstract void lookup(byte contentType, byte contentSubtype, byte[] resourceID, Owner owner);

	public abstract void remove(byte contentType, byte contentSubtype, byte[] resourceID, Owner owner);

    public abstract void sendMessage(byte[] unhashedID, String protocol, byte[] message);

	/**
	 * Returns an array of String objects. Each of them contains a description of an entry in routing table. Returns
	 * null if there are no entries.
	 *
	 * @return
	 */
	public abstract String[] getRoutingTableToString();

	// end of API

	/**
	 * Method that returns distance between two unhashed IDs. Distance is overlay-protocol-and-hash-algorithm dependent.
	 *
	 * @param unhashedKey1
	 * @param unhashedKey2
	 */
	public abstract BigInteger getDistance(String unhashedKey1, String unhashedKey2) throws IllegalStateException;

	/**
	 * Method that returns distance between two hashed IDs. Distance is overlay-protocol-and-hash-algorithm dependent.
	 *
	 * @param hashedKey1
	 * @param hashedKey2
	 * @return
	 * @throws IllegalStateException
	 */
	public abstract BigInteger getDistanceBetweenHashed(byte[] hashedKey1, byte[] hashedKey2)
			throws IllegalStateException;

	/**
	 * Analyses given request.
	 *
	 * @param request
	 * @param isReceived
	 *            True if this request was received by transport. False, if it was invoked by this entity (f.e. publish
	 *            table wanted to publish something, there was no other peer better than local one, and it published it
	 *            internally but wanted to put a resource to local resource manager and invoke callback).
	 * @return returnCode Code that depends on the type of request. PublishObjectRequest informs if published resource
	 *         was added to resource manager, so that PublishTable will know if it should be added to list of published
	 *         objects (1==add, 0==don't add). TODO add more description for other types.
	 */
	public abstract int onRequest(Request request, boolean isReceived);

	/**
	 * Method that performs all the clearing and resetting of things that should happen when node leaves an overlay.
	 */
	public abstract void performLeaveTasks();

	/**
	 * Method that returns a description of overlay algorithm used by this node. Has to be implemented by
	 * protocol-specific subclasses. Description uses this scheme:<br/>
	 * &nbsp &nbsp p2pAlgorithmName-hashAlgorithmName-hashLength<br/>
	 * Example for Kademlia looks like this:<br/>
	 * &nbsp &nbsp Kademlia-SHA1-128
	 *
	 * @return
	 */
	public abstract String getOverlayAlgorithm();

	/**
	 * Sets routing table for this node.
	 *
	 * @param routingTable
	 */
	public void setRoutingTable(RoutingTable routingTable) {
		this.routingTable = routingTable;
	}

	/**
	 * Sets neighbor table for this node.
	 *
	 * @param neighborTable
	 */
	public void setNeighborTable(NeighborTable neighborTable) {
		this.neighborTable = neighborTable;
	}
}
