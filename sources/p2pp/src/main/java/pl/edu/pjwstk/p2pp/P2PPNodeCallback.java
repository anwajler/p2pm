package pl.edu.pjwstk.p2pp;

import java.util.List;
import java.util.Vector;

import pl.edu.pjwstk.p2pp.entities.Node;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;

/**
 * Interface implemented by listeners of P2PP node communication.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public interface P2PPNodeCallback {

	public static final int BOOTSTRAP_ERROR_CODE = -1;
	public static final int INSERT_ERROR_CODE = -2;
	public static final int RESOURCE_LOOKUP_ERROR_CODE = -3;
	public static final int USER_LOOKUP_ERROR_CODE = -4;
	public static final int NAT_ERROR_CODE = -5;
    public static final int JOIN_ERROR_CODE = -6;

	// standard P2PP API methods

	/**
	 * Informs of query completed.
	 */
	public void queryCallback(byte[] overlayID, byte p2pAlgorithm, byte hashAlgorithm, short hashAlgorithmLength);

	/**
	 * Informs of join completed.
	 */
	public void joinCallback();

	/**
	 * Informs of leave completed.
	 */
	public void leaveCallback();

	/**
	 * Informs of publish completed.
	 * 
	 * @param contentType
	 * @param contentSubtype
	 * @param unhashedKey
	 *            Unhashed key that was used in publish() method on Node object.
	 * @param resourceObjectValue
	 *            Value of an object that was published.
	 * @see Node
	 */
	public void publishCallback(byte contentType, byte contentSubtype, byte[] unhashedKey, byte[] resourceObjectValue);

	/**
	 * Informs of remove completed.
	 */
	public void removeCallback();

	/**
	 * Informs of completed lookup.
	 * 
	 * @param resourceObjects
	 *            Vector of {@link ResourceObject} objects that were found.
	 */
	public void lookupCallback(Vector<ResourceObject> resourceObjects);

    public void removeCallback(ResourceObject resourceObject);

	/**
	 * Method invoked when there's an error concerning API's methods.
	 * 
	 * @param errorObject
	 * @param errorCode
	 */
	public void errorCallback(ErrorInterface errorObject, int errorCode);

	// end of standard P2PP API methods

	// Paulina's extensions to P2PP callbacks
	/**
	 * <p>
	 * Usually invoked directly after receiving request and determining that this node is message destination.
	 * </p>
	 * <p>
	 * In case of lookup requests – after searching for the objects in peer's resource table (figure 3.2.1.1). If node
	 * stores requested objects there, they are passed to higher layer using 'objectList' parameter. Higher-layer
	 * application may decide to send these objects in lookup response, or pass a different ones using the same
	 * parameter. After callback invocation P2PP/RELOAD checks returned value. If this value is 'true' it simply returns
	 * objects found in resource table in response (or 'NOT FOUND' message if none of requested objects is stored
	 * there). Otherwise response message contents depend on the second callback parameter's value (objects defined by
	 * higher layer are sent to request originator). This MAY be used by application built on top of P2PP/RELOAD for
	 * example to create different 'views' of node's resource table, depending on lookup request originator. Higher
	 * layer MAY hide object's value from some users or send different values to them.
	 * </p>
	 * <p>
	 * Similar procedure is performed after receiving insert request (figure 3.2.1.2) although returned value
	 * interpretation is slightly different. When this value is 'true', objects encapsulated in insert request are
	 * simply put into peer's resource table. Otherwise either different objects are inserted or only P2PP/RELOAD
	 * response is sent. For both insert and lookup requests application layer MAY modify object list elements' values
	 * or replace them with null. It MUST NOT modify resource id, type or data model. For all request types it also MUST
	 * NOT alter object list size.
	 * </p>
	 * 
	 * 
	 * @param request
	 *            Received P2PP request.
	 * @param objectList
	 *            Value interpretation is message type dependent. List implementation must enable null attribute in set
	 *            method.
	 */
	public boolean onDeliverRequest(Request request, List<ResourceObject> objectList);

	/**
	 * <p>
	 * Usually invoked directly after receiving P2PP request (and determining that this node is not message
	 * destination), before forwarding it to other node or sending 302 response.
	 * </p>
	 * <p>
	 * In case of lookup requests (figure 3.2.1.3) – after checking, if requested object isn't for example cached (or
	 * replicated) here. If such object is found, P2PP/RELOAD passes it to higher layer using 'objectList' parameter.
	 * Higher-layer application may decide to send this object in P2PP lookup response, or pass a different one using
	 * the same parameter. After callback invocation P2PP/RELOAD checks returned value. If this value is 'true' it
	 * simply forwards the message or returns cached/replicated objects in standard response. Otherwise the message is
	 * discarded, and P2PP/RELOAD response containing objects defined by higher layer is sent.
	 * </p>
	 * <p>
	 * Figure 3.2.1.4 shows procedure performed in case of receiving insert request. If returned value is 'true', the
	 * message is forwarded. Otherwise node discards the message and sends insert response to its originator.
	 * </p>
	 * <p>
	 * For both insert and lookup requests application layer MAY modify object list elements' values or replace them
	 * with null. It MUST NOT modify resource id, type or data model. For all request types it also MUST NOT alter
	 * object list size.
	 * </p>
	 * 
	 * 
	 * @param request
	 *            Received P2PP request.
	 * @param objectList
	 *            Value interpretation is message type dependent.
	 * @return
	 */
	public boolean onForwardingRequest(Request request, List<ResourceObject> objectList);

	/**
	 * Invoked after accepting other node’s join request and sending OK reply to it (figure 3.2.1.2).[TODO: after a
	 * successful 200 transaction ??? ]
	 * 
	 * @param newNode
	 *            Object containing information such as ID, IP address, and port number.
	 * @param nodeType
	 *            Value indicating, whether new node is peer or client.
	 * @return
	 */
	public boolean onNeighborJoin(PeerInfo newNode, int nodeType);

	/**
	 * 
	 * @param newNode
	 * @param nodeType
	 */
	public void onNeighborLeave(PeerInfo newNode, int nodeType);

	// end of Paulina's extensions to P2PP callbacks
}
