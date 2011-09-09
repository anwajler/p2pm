package pl.edu.pjwstk.p2pp.resources;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.ErrorInterface;
import pl.edu.pjwstk.p2pp.GlobalConstants;
import pl.edu.pjwstk.p2pp.P2PPNodeCallback;
import pl.edu.pjwstk.p2pp.entities.EntitiesSharedDataManager;
import pl.edu.pjwstk.p2pp.entities.Node;
import pl.edu.pjwstk.p2pp.entities.P2PPEntity;
import pl.edu.pjwstk.p2pp.kademlia.KademliaPeer;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.PublishObjectRequest;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.messages.responses.NextHopResponse;
import pl.edu.pjwstk.p2pp.messages.responses.PublishObjectResponse;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.Expires;
import pl.edu.pjwstk.p2pp.objects.NeighborTable;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceID;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.objects.RoutingTable;
import pl.edu.pjwstk.p2pp.transactions.Transaction;
import pl.edu.pjwstk.p2pp.transactions.TransactionListener;
import pl.edu.pjwstk.p2pp.transactions.TransactionTable;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * <p>
 * This array keeps track of ResourceObjects published by local node. ResourceObjects are published by peer in the
 * overlay. It must periodically refresh the resource-objects before the passage of their refresh time interval
 * (described in Expires object which is a part of ResourceObject). Resource objects are added by using
 * {@link #addSelfPublishedResourceObject(ResourceObject)} method.
 * </p>
 * <p>
 * Resource objects are (re)published with {@link TransactionListener} being part of this class. This listener is
 * informed when transaction ends. If ends good (with TERMINATED state [not FAILURE]), should contain
 * {@link NextHopResponse} or {@link PublishObjectResponse}. If {@link NextHopResponse} was received, new transaction is
 * created with the same listener.
 * </p>
 * <p>
 * ResourceObjects will be refreshed in a moment that we call a refresh deadline. This moment is just before a moment of
 * expiration. Refresh deadline is reached when ((1 / REFRESH_DEADLINE_DIVISOR) * expires_seconds) or less is left to
 * expiration time.
 * </p>
 * <p>
 * ResourceObjects are kept in four collections:
 * <ul>
 * <li>Vector of objects waiting to be published (those that were added in
 * {@link #addSelfPublishedResourceObject(ResourceObject)}), but no PublishObjectRequest was sent for them.</li>
 * <li>Hashtable of resource objects that were added using {@link #addSelfPublishedResourceObject(ResourceObject)} and
 * {@link PublishObjectRequest} was sent for them. TransactionID (as Long) is a key in that hashtable.</li>
 * <li>Hashtable of resource objects that were published at least once, have to be republished and PublishObjectRequest
 * was sent for them. They were in array of published objects, but refresh deadline was hit for them.</li>
 * <li>Vector of published ResourceObjects.</li>
 * </ul>
 * </p>
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * @see ResourceObject
 * @see Expires
 */
public class PublishTable {

	private static Logger LOG = Logger.getLogger(PublishTable.class);

	/**
	 * Divisor of refresh time. Used for determining if a resource needs to be republished.
	 */
	public static final int REFRESH_DEADLINE_DIVISOR = 6;

	private RoutingTable routingTable;

	@SuppressWarnings("unused")
	private NeighborTable neighborTable;

	/**
	 * Vector of published objects. TODO probably should be changed to map, or something, to search faster.
	 */
	private Vector<ResourceObject> objectsPublished = new Vector<ResourceObject>();

	/**
	 * Vector of resource objects that are waiting to be published for the first time.
	 */
	final private Vector<ResourceObject> objectsWaitingToBePublished = new Vector<ResourceObject>();

	/**
	 * Hashtable of transactionIDs (as Long) as keys and republished ResourceObject as value. It is made this way,
	 * because when publish request is send, transactionID must be stored just to know that publish response, that will
	 * be received, contains Expires object for matching resource object.
	 */
	private Hashtable<Long, ResourceObject> republishedObjectsWaitingForResponse = new Hashtable<Long, ResourceObject>();

	/**
	 * Hashtable of transactionIDs (as Long) as keys and first-published ResourceObject as value. It is made this way,
	 * because when publish request is send, transactionID must be stored just to know that publish response, that will
	 * be received, contains Expires object for matching resource object.
	 */
	private Hashtable<Long, ResourceObject> firstPublishedObjectsWaitingForResponse = new Hashtable<Long, ResourceObject>();

	/**
	 * Listener used for publish transactions.
	 */
	private TransactionListener transactionsListener = new TransactionListener() {

		public void transactionEnded(byte[] transactionID, byte transactionState, byte type, Request request,
				Response response, TransactionTable transactionTable, P2PPEntity localEntity) {

            if (LOG.isTraceEnabled()) {
			    LOG.trace("transactionID=" + ByteUtils.byteArrayToHexString(transactionID) + ", transactionState="
					+ transactionState);
            }

			// converts transactionID to Long to get Resource from hashtable
			Long transactionIDAsLong = ByteUtils.bytesToLong(transactionID[0], transactionID[1], transactionID[2],
					transactionID[3]);

			// if transaction didn't fail
			if (transactionState == Transaction.TERMINATED_STATE) {
				if (LOG.isTraceEnabled()) LOG.trace("Transaction didn't fail.");

				// checks if ended transaction is for republished object (non-null is returned in that case)
				boolean isForRepublished = true;
				ResourceObject resource = republishedObjectsWaitingForResponse.get(transactionIDAsLong);
				if (resource == null) {
					isForRepublished = false;
					resource = firstPublishedObjectsWaitingForResponse.get(transactionIDAsLong);

				}

				// if resource matching transaction was found
				if (resource != null) {

                    if (LOG.isTraceEnabled()) {
					    LOG.trace("Resource "
							+ resource.toString()
							+ " matches transactionID="
							+ ByteUtils.bytesToLong(transactionID[0], transactionID[1], transactionID[2],
									transactionID[3]));
                    }

					// if ended transaction was for republished resource
					if (isForRepublished) {
						// analysis is made for a hashtable of republished resource objects waiting for response
						analyseEndedTransaction(republishedObjectsWaitingForResponse, transactionIDAsLong, response,
								transactionTable, localEntity, request, resource, false);
					} else {
						// analysis is made for a hashtable of first-time published resource objects waiting for
						// response
						analyseEndedTransaction(firstPublishedObjectsWaitingForResponse, transactionIDAsLong, response,
								transactionTable, localEntity, request, resource, true);
					}

				} else {

					if (LOG.isTraceEnabled()) LOG.trace("There's no resource matching transaction.");

				}
			} // end of if for non-failure state
			// if transaction did fail
			else {

				Node localNode = (Node) localEntity;

				// if transaction failed for object published for the first time, object is removed from waiting
				// hashtable and callback is informed
				ResourceObject resource = firstPublishedObjectsWaitingForResponse.remove(transactionIDAsLong);
				// if transaction failed for republished resource
				if (resource == null) {
					// removes resource from republished resources waiting for response and passes it to objects
					// published (onTimeSlot() will create new publish transaction for them)
					resource = republishedObjectsWaitingForResponse.remove(transactionIDAsLong);
					objectsPublished.add(resource);
				}

				ErrorInterface errorInterface = new ErrorInterface() {

					private Object value;

					public void setValue(Object value) {
						this.value = value;
					}

					public Object getValue() {
						return value;
					}

				};
				errorInterface.setValue(resource);

				localNode.getCallback().errorCallback(errorInterface, P2PPNodeCallback.INSERT_ERROR_CODE);
			}
		}

	};

	public void setRoutingTable(RoutingTable routingTable) {
		this.routingTable = routingTable;
	}

	public void setNeighborTable(NeighborTable neighborTable) {
		this.neighborTable = neighborTable;
	}

	/**
	 *
	 * @param resourcesWaitingForResponse
	 * @param transactionID
	 * @param response
	 * @param transactionTable
	 * @param localEntity
	 * @param request
	 * @param resource
	 * @param informCallback
	 */
	private void analyseEndedTransaction(Hashtable<Long, ResourceObject> resourcesWaitingForResponse,
			Long transactionID, Response response, TransactionTable transactionTable, P2PPEntity localEntity,
			Request request, ResourceObject resource, boolean informCallback) {
		// if response contains next hop information
		if (response instanceof NextHopResponse) {
			// resource is removed from hashtable and is put with new transactionID
			ResourceObject removed = resourcesWaitingForResponse.remove(transactionID);

			NextHopResponse nextHopResponse = (NextHopResponse) response;
			PeerInfo nextHop = nextHopResponse.getNextHopPeerInfo();

            if (LOG.isTraceEnabled()) {
			    LOG.trace("Transaction=" + transactionID + " ends but new is created for nextHopID="
					+ ByteUtils.byteArrayToHexString(nextHop.getPeerID().getPeerIDBytes()) + " removed=" + removed);
            }

			Transaction transaction = transactionTable.createTransactionAndFill(request, transactionsListener, nextHop
					.getAddressInfos(), localEntity.getSharedManager().getPeerIDAsBytes(), nextHop.getPeerID()
					.getPeerIDBytes());

			byte[] transactionIDOfNextHopTransaction = transaction.getTransactionID();

			resourcesWaitingForResponse.put(ByteUtils.bytesToLong(transactionIDOfNextHopTransaction[0],
					transactionIDOfNextHopTransaction[1], transactionIDOfNextHopTransaction[2],
					transactionIDOfNextHopTransaction[3]), resource);

		}// if response is for PublishObjectRequest
		else if (response instanceof PublishObjectResponse) {

			PublishObjectResponse publishResponse = (PublishObjectResponse) response;

			// gets Expires object from response
			Expires expires = publishResponse.getExpires();

			// if there's expires object in response
			if (expires != null) {

				@SuppressWarnings("unused")
				ResourceObject removed = resourcesWaitingForResponse.remove(transactionID);

				// if Expires doesn't contain value that prevents resource from being republished
				if (expires.getExpiresSeconds() != Expires.EXPIRES_VALUE_NOT_REPUBLISH) {

					// passes expires object to resource
					resource.setExpires(expires);

					// moves resource to a list of published objects
					objectsPublished.add(resource);
					resource.refresh();
				} // else is not needed because object should be only deleted

				if (informCallback) {
					// notifies callback about a fact that resource is now published (publish table is a part only
					// of a
					// node)
					Node localPeer = (Node) localEntity;
					localPeer.getCallback().publishCallback(resource.getContentType(), resource.getContentSubtype(),
							resource.getUnhashedID(), resource.getValue().getValue());

				}
				if (LOG.isTraceEnabled()) LOG.trace("Proper response. Expires set for resource. ");
			}

		} else {
			// TODO what to do when there's no expires object in response? Pass it somewhere?
		}
	}

	/**
	 * Creates table of objects published by a peer.
	 */
	public PublishTable() {

	}

	/**
	 * Adds self-published resource object to this table. Returns true if given object was added, false if not (i.e.
	 * this object was already in publish table).
	 *
	 * @param resourceObject
	 *            Resource object to be added to this table. Expires subobject (if any) will be nullified (just to be
	 *            sure that there are no bad data).
	 * @return
	 */
	public boolean addSelfPublishedResourceObject(ResourceObject resourceObject) {
        if (LOG.isTraceEnabled()) {
		    LOG.trace("Adds self published ResourceObject of contentType=" + resourceObject.getContentType()
				+ ", subtype=" + resourceObject.getContentSubtype());
        }
		// TODO probably somewhere here a synchronization should be
		boolean alreadyContains = checkIfAlreadyContainsResource(resourceObject);

		if (!alreadyContains) {
			if (resourceObject.getExpires() != null) {
				resourceObject.setExpires(null);
			}
			objectsWaitingToBePublished.add(resourceObject);
		}
		return true;
	}

	/**
	 * Removes given resource object from this table. Returns true if there was this object in this table. False
	 * otherwise.
	 *
	 * @param resourceObject
	 * @return
	 */
	public boolean removeResourceObject(ResourceObject resourceObject) {

		// TODO implement

		return true;
	}

	/**
	 * Returns true if this publish table (any of vectors inside) already contains given ResourceObject. Returns false
	 * otherwise.
	 *
	 * @param resourceObject
	 * @return
	 */
	private boolean checkIfAlreadyContainsResource(ResourceObject resourceObject) {
		boolean contains = false;

		ResourceID resourceID = resourceObject.getResourceID();
		byte contentType = resourceObject.getContentType();
		byte contentSubtype = resourceObject.getContentSubtype();

		for (ResourceObject currentResource : objectsPublished) {
			ResourceID currentResourceID = currentResource.getResourceID();
			byte currentContentType = currentResource.getContentType();
			byte currentContentSubtype = currentResource.getContentSubtype();

			// compares given resource with current one
			if (contentType == currentContentType && contentSubtype == currentContentSubtype
					&& resourceID.equals(currentResourceID)) {
				contains = true;
				break;
			}
		}

		// waiting-objects vector is checked only if published-objects vector doesn't contain given resource object
		if (!contains) {
            synchronized (objectsWaitingToBePublished) {
                for (ResourceObject currentResource : objectsWaitingToBePublished) {
                    ResourceID currentResourceID = currentResource.getResourceID();
                    byte currentContentType = currentResource.getContentType();
                    byte currentContentSubtype = currentResource.getContentSubtype();

                    // compares given resource with current one
                    if (contentType == currentContentType && contentSubtype == currentContentSubtype
                            && resourceID.equals(currentResourceID)) {
                        contains = true;
                        break;
                    }
                }
            }
		}

		// TODO probably hashtables should be also checked

		return contains;
	}

	/**
	 * Gives time slot for this table.
	 * <p>
	 * Creates an empty vector of resources that will be published. It is filled with published objects that need to be
	 * republished,
	 * </p>
	 *
	 * @param node
	 *            Node that owns this publish table.
	 * @param resourceManager
	 *            ResourceManager that this publish table is part of.
	 *
	 */
	public void onTimeSlot(Node node, ResourceManager resourceManager) {
        try {

            long currentTime = System.currentTimeMillis();

            // array of resource objects that will be republished
            ArrayList<ResourceObject> objectsToRePublishNow = new ArrayList<ResourceObject>();

            // gets shared manager, so that ownPeerInfo and transaction table may be taken
            EntitiesSharedDataManager sharedManager = node.getSharedManager();

            TransactionTable transactionTable = node.getTransactionTable();

            // LOOKING FOR RESOURCE OBJECTS THAT NEED TO BE REPUBLISHED. Those objects are removed from published and added
            // to vector of objects that will be published now
            for (ResourceObject currentResource : objectsPublished) {
                // resources without Expires subobject aren't checked (TODO why? is this needed?)
                Expires expires = currentResource.getExpires();
                if (expires != null) {

                    // if resource should be refreshed, it is copied to resources that will be published
                    if (shouldBeRefreshed(currentTime, currentResource.getMomentOfLastRefresh(), currentResource
                            .getExpires().getExpiresSeconds())) {
                        // TODO check if this array contains it already... or maybe not, because adding prevents from adding
                        // duplicates
                        objectsToRePublishNow.add(currentResource);
                    }
                }
            }

            /*
             * Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "PublishTable.onTimeSlot()",
             * "\tobjectsPublished.size()=" + objectsPublished.size() + ", objectsWaitingToBePublished.size()=" +
             * objectsWaitingToBePublished.size() + ", objectsToRePublishNow.size()=" + objectsToRePublishNow.size());
             */

            // ResourceObjects that need to be republished are removed from published objects
            objectsPublished.removeAll(objectsToRePublishNow);

            /*
             * Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "PublishTable.onTimeSlot()",
             * "\tobjectsPublished.size()=" + objectsPublished.size() + ", objectsWaitingToBePublished.size()=" +
             * objectsWaitingToBePublished.size() + ", objectsToRePublishNow.size()=" + objectsToRePublishNow.size());
             */

            // republishes resource objects
            for (ResourceObject currentResource : objectsToRePublishNow) {

                PeerInfo nextHop = routingTable.getNextHop(currentResource.getResourceID().getResourceID());

                // if there's a better peer to send this message to
                if (nextHop != null) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Resource (contentType=" + currentResource.getContentType() + ", subtype="
                            + currentResource.getContentSubtype() + ") will be (re)published to peerID="
                            + ByteUtils.byteArrayToHexString(nextHop.getPeerID().getPeerIDBytes()));
                    }
                    PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
                    PublishObjectRequest publishRequest = new PublishObjectRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255,
                            null, sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null, ownPeerInfo, currentResource);
                    Transaction transaction = transactionTable.createTransactionAndFill(publishRequest,
                            transactionsListener, nextHop.getAddressInfos(), ownPeerInfo.getPeerID().getPeerIDBytes(),
                            nextHop.getPeerID().getPeerIDBytes());
                    byte[] transactionID = transaction.getTransactionID();
                    republishedObjectsWaitingForResponse.put(ByteUtils.bytesToLong(transactionID[0], transactionID[1],
                            transactionID[2], transactionID[3]), currentResource);
                } // if we are the best peer for storing current resource
                else {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Resource (contentType=" + currentResource.getContentType() + ", subtype="
                            + currentResource.getContentSubtype() + ") will be (re)published locally");
                    }
                    resourceManager.storeResourceObject(currentResource);
                    objectsPublished.add(currentResource);
                    currentResource.setExpires(new Expires(KademliaPeer.DEFAULT_EXPIRES_SECONDS));
                }

            }

            // publishes objects that need to be published for the first time
            synchronized (objectsWaitingToBePublished) {
                for (ResourceObject currentResource : objectsWaitingToBePublished) {

                    PeerInfo nextHop = routingTable.getNextHop(currentResource.getResourceID().getResourceID());

                    // if there's a better peer to send this message to
                    if (nextHop != null) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Resource (contentType=" + currentResource.getContentType() + ", subtype="
                                + currentResource.getContentSubtype() + ") will be (re)published to peerID="
                                + ByteUtils.byteArrayToHexString(nextHop.getPeerID().getPeerIDBytes()));
                        }
                        PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
                        PublishObjectRequest publishRequest = new PublishObjectRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255,
                                null, sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null, ownPeerInfo, currentResource);
                        Transaction transaction = transactionTable.createTransactionAndFill(publishRequest,
                                transactionsListener, nextHop.getAddressInfos(), ownPeerInfo.getPeerID().getPeerIDBytes(),
                                nextHop.getPeerID().getPeerIDBytes());
                        byte[] transactionID = transaction.getTransactionID();
                        firstPublishedObjectsWaitingForResponse.put(ByteUtils.bytesToLong(transactionID[0], transactionID[1],
                                transactionID[2], transactionID[3]), currentResource);
                    } // if we are the best peer for storing current resource
                    else {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Resource (contentType=" + currentResource.getContentType() + ", subtype="
                                + currentResource.getContentSubtype() + ") will be published locally");
                        }

                        // adds object to resource manager and to published objects
                        currentResource.setExpires(new Expires(KademliaPeer.DEFAULT_EXPIRES_SECONDS));
                        // resourceManager.storeResourceObject(currentResource);

                        // informs callback
                        node.getCallback().publishCallback(currentResource.getContentType(),
                                currentResource.getContentSubtype(), currentResource.getUnhashedID(),
                                currentResource.getValue().getValue());

                        // creates a request that won't be send, but has to be passed to onDeliverRequest()
                        PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
                        PublishObjectRequest publishRequest = new PublishObjectRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255,
                                null, sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null, ownPeerInfo, currentResource);

                        // passes request to node with info that it isn't a received request, but it is locally generated
                        int code = node.onRequest(publishRequest, false);
                        if (code == 1) {
                            objectsPublished.add(currentResource);
                        }

                        if (LOG.isTraceEnabled()) LOG.trace("Callback informed.");

                    }

                }

                // removes waiting objects
                objectsWaitingToBePublished.clear();
            }

        } catch (Throwable e) {
            LOG.error("Error while running onTimeSlot" ,e);
        }
	}

	/**
	 * Returns true if ResourceObject described with given parameters should be refreshed (refresh deadline was
	 * reached). False otherwise.
	 *
	 * @param currentTime
	 * @param timeOfLastPublish
	 * @param expires
	 *            Time (seconds) after which ResourceObject is considered dead.
	 * @return
	 */
	public static boolean shouldBeRefreshed(long currentTime, long timeOfLastPublish, int expires) {

		boolean shouldBeRefreshed = false;

		long expiresMillis = expires * 1000;
		long expirationTime = timeOfLastPublish + expiresMillis;
		long deadline = expirationTime - ((1 / REFRESH_DEADLINE_DIVISOR) * expiresMillis);
		// should be refreshed if deadline was met
		if (currentTime >= deadline) {
			shouldBeRefreshed = true;
		}

		return shouldBeRefreshed;
	}

	/**
	 * Removes all resource objects from this table.
	 */
	public void removeAllResourceObjects() {

		objectsPublished.clear();

		objectsWaitingToBePublished.clear();

		republishedObjectsWaitingForResponse.clear();

		firstPublishedObjectsWaitingForResponse.clear();

	}
}
