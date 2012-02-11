package pl.edu.pjwstk.p2pp.kademlia;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.ErrorInterface;
import pl.edu.pjwstk.p2pp.GlobalConstants;
import pl.edu.pjwstk.p2pp.P2PPManager;
import pl.edu.pjwstk.p2pp.P2PPNodeCallback;
import pl.edu.pjwstk.p2pp.entities.P2PPEntity;
import pl.edu.pjwstk.p2pp.entities.Peer;
import pl.edu.pjwstk.p2pp.ice.STUNCommunicationListener;
import pl.edu.pjwstk.p2pp.ice.STUNService;
import pl.edu.pjwstk.p2pp.messages.Acknowledgment;
import pl.edu.pjwstk.p2pp.messages.Indication;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.indications.LeaveIndication;
import pl.edu.pjwstk.p2pp.messages.requests.*;
import pl.edu.pjwstk.p2pp.messages.responses.*;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.resources.MessageResourceObject;
import pl.edu.pjwstk.p2pp.resources.STUNServiceResourceObject;
import pl.edu.pjwstk.p2pp.resources.UserInfoResourceObject;
import pl.edu.pjwstk.p2pp.transactions.Transaction;
import pl.edu.pjwstk.p2pp.transactions.TransactionListener;
import pl.edu.pjwstk.p2pp.transactions.TransactionTable;
import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.NodeTimers;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * Class describing peer that uses Kademlia protocol.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * @author Konrad Adamczyk conrad.adamczyk@gmail.com
 *
 */
public class KademliaPeer extends Peer {

	private static Logger LOG = Logger.getLogger(KademliaPeer.class);

	/**
	 * Default time (seconds) of expiration of resource objects.
	 */
	public static final int DEFAULT_EXPIRES_SECONDS = 120;

	/**
	 * If not null, this peer has determined its server reflexive address. Access to it has to be synchronized.
	 */
	private AddressInfo serverReflexiveAddress;

	/**
	 * TransactionListener for bootstrapping.
	 */
	private TransactionListener bootstrapTransactionListener = new TransactionListener() {

		public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType,
				Request request, Response response, TransactionTable transactionTable, P2PPEntity node) {
			if (transactionState == Transaction.FAILURE_STATE) {

				// checks if there are more bootstrap candidates
				if (currentBootstrapCandidateIndex < bootstrapCandidates.size()) {

					try {
						P2POptions options = sharedManager.getOptions();

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Communication with bootstrap candidate nr" + currentBootstrapCandidateIndex
                                    + " has failed. Communication with another one starts.");
                        }

						// asks another bootstrap candidate for STUN server
						ResourceID stunResourceID = new ResourceID(P2PPUtils.hash(P2PPUtils.STUN_SERVICE_ID, options
								.getHashAlgorithm()));
						RLookup resourceLookup = new RLookup(P2PPUtils.STUN_CONTENT_TYPE, (byte) 0, stunResourceID,
								null);
						LookupObjectRequest lookupRequest = new LookupObjectRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false,
                                (byte) 255, null, sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null,
                                sharedManager.getPeerInfo(true, true), resourceLookup);
						PeerInfo chosenBootstrapCandidate = bootstrapCandidates.get(0);
						Vector<AddressInfo> candidateAddresses = chosenBootstrapCandidate.getAddressInfos();
						// TODO maybe inform someone about a fact that we are bootstrapped
						transactionTable.createTransactionAndFill(lookupRequest, transactionListener,
								candidateAddresses, sharedManager.getPeerIDAsBytes(), chosenBootstrapCandidate
										.getPeerID().getPeerIDBytes());
					} catch (NoSuchAlgorithmException e) {
						// TODO probably can't happen
						LOG.error("No algorithm!", e);
					}

				} // if there are no more bootstrap candidates
				else {

					BootstrapResponse bootstrapResponse = (BootstrapResponse) response;

                    if (bootstrapResponse == null) {
                        LOG.error("Could not communicate with bootstrap server.");
                    } else {

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("No response from any of bootstrap peers, so peer is now JOINED.");
                        }

                        // saves information about bootstrap candidates
                        bootstrapCandidates = bootstrapResponse.getPeersInOverlay();

                        // gets PeerInfo describing originator of BootstrapRequest (it's this peer.. BootstrapServer filled
                        // this object)
                        PeerInfo originatorPeerInfo = bootstrapResponse.getOriginatorPeerInfo();

                        // saves new peerID
                        byte[] ownNodeID = originatorPeerInfo.getPeerID().getPeerIDBytes();

                        sharedManager.setPeerIDAsBytes(ownNodeID);
                        PeerInfo ownPeerInfo = sharedManager.getPeerInfo(false, true);
                        // saves p2p options from bootstrap response
                        P2POptions options = bootstrapResponse.getP2POptions();
                        sharedManager.setOptions(options);

                        // Creates Owner object that will be used for creating ResourceObjects.
                        Owner owner = new Owner(ownNodeID);

                        // informs objects about overlay options
                        resourceManager.setProperties(options.getHashAlgorithm(), options.getHashAlgorithmLength(), owner,
                                DEFAULT_EXPIRES_SECONDS);
                        routingTable.setProperties(options.getBase(), options.getHashAlgorithmLength() * 8);
                        routingTable.setLocalPeerInfo(ownPeerInfo);
                        routingTable.setLocalPeerID(ownNodeID);

                        // Peer has joined.
                        state = JOINED_NODE_STATE;

                        // informs callback function about a fact that this peer has joined an overlay
                        callback.joinCallback();

                        if (LOG.isDebugEnabled()) LOG.debug("There are no peers in overlay. Peer is now JOINED.");

                        // creates UserInfoResource and resource objects for services
                        UserInfoResourceObject userInfo = new UserInfoResourceObject(new Owner(ownNodeID),
                                new Expires(120), null, new Certificate(false, new byte[] { 2, 2, 2, 2 }), ownPeerInfo.getAddressInfos());
                        userInfo.setValue(new ResourceObjectValue(ownPeerInfo.asBytes()));
                        publish(userInfo.getUnhashedID(), userInfo);

                        // gets resource objects for local services
                        List<ResourceObject> listOfServiceResources = resourceManager.getResourceObjectsForServices(ownPeerInfo);

                        if (LOG.isDebugEnabled()) LOG.debug("Publishes information about " + listOfServiceResources.size() + " services.");

                        // publishes objects describing services
                        for (ResourceObject current : listOfServiceResources) {
                            publish(current.getUnhashedID(), current);
                        }

                    }
				}

				// informs callback that bootstrapping ended with an error, so that this peer can't connect to an
				// overlay
				callback.errorCallback(null, P2PPNodeCallback.BOOTSTRAP_ERROR_CODE);
			} else {

			}
		}

	};

	/**
	 * Listener of lookup transactions. TODO Probably might be moved to peer class.
	 */
	private TransactionListener lookupObjectTransactionListener = new TransactionListener() {

		public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType,
				Request request, Response response, TransactionTable transactionTable, P2PPEntity node) {

			// if transaction ended properly
			if (transactionState == Transaction.TERMINATED_STATE) {

				// if response is of LookupObjectResponse type (not NextHop or something)
				if (response instanceof LookupObjectResponse) {
					LookupObjectResponse lookupResponse = (LookupObjectResponse) response;

					int responseCode = response.getResponseCodeAsInt();
					// if response code is OK, so in response should be a resource that was searched
					if (responseCode == Response.RESPONSE_CODE_OK) {
						Vector<ResourceObject> foundResources = lookupResponse.getResourceObject();

						if (foundResources.size() > 0 && foundResources.get(0) != null) {
							// TODO currently passes only the first of resources
							callback.lookupCallback(foundResources);
						}
					} // if response code is NOT_FOUND, error is set to callback
					else if (responseCode == Response.RESPONSE_CODE_NOT_FOUND) {

						ErrorInterface error = new ErrorInterface() {
							private Object value;

							public Object getValue() {
								return value;
							}

							public void setValue(Object value) {
								this.value = value;
							}
						};
						error.setValue(((LookupObjectRequest) request).getResourceLookup());

						callback.errorCallback(error, P2PPNodeCallback.RESOURCE_LOOKUP_ERROR_CODE);
					}
				} else if (response instanceof NextHopResponse) {

					if (LOG.isDebugEnabled()) LOG.debug("Received next hop response for lookup object");
					NextHopResponse nextHopResponse = (NextHopResponse) response;
					PeerInfo nextHop = nextHopResponse.getPeerInfo();

					request.decreaseTtl();

					transactionTable.createTransactionAndFill(request, lookupObjectTransactionListener, nextHop
							.getAddressInfos(), sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());

				}
			} // if transaction ended with an error
			else {
				ErrorInterface error = new ErrorInterface() {
					private Object value;
					public Object getValue() {return value;}
					public void setValue(Object value) {this.value = value;}
				};
				error.setValue(((LookupObjectRequest) request).getResourceLookup());
				callback.errorCallback(error, P2PPNodeCallback.RESOURCE_LOOKUP_ERROR_CODE);
			}

		}

	};

	/**
	 * Listener of communication with STUN server.
	 */
	private STUNCommunicationListener stunListener = new STUNCommunicationListener() {

		public void onAddressDetermined(AddressInfo addressInfo) {
            if (LOG.isDebugEnabled()) {
			    LOG.debug("\tPeer now knows its server reflexive address=" + addressInfo.getAddressAsString() + ":"
					+ addressInfo.getPort());
            }

			// saves determined address TODO synchronize?
			serverReflexiveAddress = addressInfo;

		}

		public void onError(int errorCode) {
			if (LOG.isDebugEnabled()) LOG.debug("Error during communication with STUN server.");
		}
	};

	private TransactionListener joinTransactionListener = new TransactionListener() {

		public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType,
				Request request, Response response, TransactionTable transactionTable, P2PPEntity node) {

            if (LOG.isTraceEnabled()) {
                LOG.trace("transactionID:"
                        + ByteUtils.bytesToLong(transactionID[0], transactionID[1], transactionID[2], transactionID[3])
                        + "; transactionState:" + transactionState + "; transactionType:" + TransactionType + "; request="
                        + request + "; response: " + response);
            }

			// if transaction ended properly
			if (transactionState == Transaction.TERMINATED_STATE) {
				if (response instanceof NextHopResponse) {

					NextHopResponse nextHopResponse = (NextHopResponse) response;
					PeerInfo nextHop = nextHopResponse.getNextHopPeerInfo();

					request.decreaseTtl();

					transactionTable.createTransactionAndFill(request, joinTransactionListener, nextHop
							.getAddressInfos(), sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());

				} else if (response instanceof JoinResponse) {

                    int responseCode = response.getResponseCodeAsInt();
                    if (responseCode == Response.RESPONSE_CODE_REQUEST_REJECTED) {
                        LOG.warn("JoinRequest has been rejected");
                        ErrorInterface error = new ErrorInterface() {
                            private Object value;
                            public Object getValue() {return value;}
                            public void setValue(Object value) {this.value = value;}
                        };
                        callback.errorCallback(error, P2PPNodeCallback.JOIN_ERROR_CODE);
                        return;
                    }

					JoinResponse joinResponse = (JoinResponse) response;

					try {
						// TODO what about false response? send new joinrequest? now assumes that always OK

						// what gets keepAlive time TODO how to store this?
						///Expires keepAliveTimer = joinResponse.getExpires();

						// Sends join request (with S flag set) to all neighbors from join response (sometimes there's
						// no
						// neighbors in response) TODO maybe add a flag that will request neighbor/routing table from
						// neighbors
						NeighborTable neighborsTable = joinResponse.getNeighborTable();
						if (neighborsTable != null) {
							Vector<PeerInfo> neighbors = neighborsTable.getNeighbors();
							for (int i = 0; i < neighbors.size(); i++) {
								PeerInfo currentNeighbor = neighbors.get(i);
								RequestOptions requestOptions = new RequestOptions(false, false, false, false, false,
										true, false);
								JoinRequest joinRequestWithSFlag = new JoinRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, true, false, (byte) 255, null,
                                        sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, requestOptions,
                                        sharedManager.getPeerInfo(true, true));
								// TODO maybe inform someone about that we are joined?
								transactionTable.createTransactionAndFill(joinRequestWithSFlag, transactionListener,
										currentNeighbor.getAddressInfos(), sharedManager.getPeerIDAsBytes(),
										currentNeighbor.getPeerID().getPeerIDBytes());
							}
						}

						// gets unhashedID that will be used as a value of UserInfo
						byte[] unhashedID = sharedManager.getUnhashedID();

						// moves to second part of joining state
						state = JOINED_NODE_STATE;

						// informs callback function about a fact that this peer has joined an overlay
						callback.joinCallback();

						PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);

						// creates UserInfoResourceObject to be published TODO signature should have different array,
						// certificate should be created another way (probably)
						UserInfoResourceObject userInfo = new UserInfoResourceObject(new Owner(unhashedID),
								new Expires(NodeTimers.RESOURCE_REFRESH_TIMER_SECONDS), new Signature(new byte[] { 9,
										9, 9, 9 }), new Certificate(false, new byte[] { 2, 2, 2, 2 }), sharedManager
										.getPeerInfo(true, true).getAddressInfos());
						userInfo.setValue(new ResourceObjectValue(ownPeerInfo.asBytes()));

						if (LOG.isDebugEnabled()) LOG.debug("Peer is now JOINED. UserInfo and STUN_SERVICE info resource will be published.");

						publish(userInfo.getUnhashedID(), userInfo);

						// gets resourceobjects for services and publishes it
						List<ResourceObject> resourcesForServices = resourceManager.getResourceObjectsForServices(ownPeerInfo);
						if (resourcesForServices != null) {
							for (ResourceObject currentResource : resourcesForServices) {
								publish(currentResource.getUnhashedID(), currentResource);
							}
						}

					} catch (NullPointerException e) {
						e.printStackTrace();
						// probably ignore because this will happen only if not valid joinresponse was received
					}
				}
			} // join transaction ended bad
			else {
                if (LOG.isDebugEnabled()) LOG.debug("Join transaction failed. Removing invalid peer from routing table and trying again");

				byte[] ownPeerID = sharedManager.getPeerIDAsBytes();
                routingTable.removePeerInfo(routingTable.getClosestTo(ownPeerID));

                state = JOIN_REQUEST_SENT_NODE_STATE;
				PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
				PeerInfo closestInfo = routingTable.getClosestTo(ownPeerID);

                if (closestInfo != null) {

                    if (LOG.isDebugEnabled()) {
                        if (ownPeerInfo.equals(closestInfo)) {
                            LOG.debug("Routing tables returns ownPeerInfo for closestTo(ownPeerID) -- This should not happen");
                        }
                        LOG.debug("Creating Join transaction and sending it to peer=" + closestInfo);
                    }



                    // creates transaction for join request which will be send to
                    JoinRequest joinRequest = new JoinRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, true, false, (byte) 255,
                            null, ownPeerID, GlobalConstants.isOverReliable, false, null, ownPeerInfo);
                    transactionTable.createTransactionAndFill(joinRequest, joinTransactionListener, closestInfo
                            .getAddressInfos(), ownPeerID, closestInfo.getPeerID().getPeerIDBytes());

                } else {

                    state = JOINED_NODE_STATE;
                    callback.joinCallback();
                    if (LOG.isDebugEnabled()) LOG.debug("Checked all candidates. There are no peers in overlay. Peer is now JOINED.");
                    UserInfoResourceObject userInfo = new UserInfoResourceObject(new Owner(sharedManager.getPeerIDAsBytes()), new Expires(120), null,
                            new Certificate(false, new byte[]{2, 2, 2, 2}), ownPeerInfo.getAddressInfos());
                    userInfo.setValue(new ResourceObjectValue(ownPeerInfo.asBytes()));
                    publish(userInfo.getUnhashedID(), userInfo);
                    List<ResourceObject> listOfServiceResources = resourceManager.getResourceObjectsForServices(ownPeerInfo);
                    if (LOG.isDebugEnabled()) LOG.debug("Publishes information about " + listOfServiceResources.size() + " services.");
                    for (ResourceObject current : listOfServiceResources) {
                        publish(current.getUnhashedID(), current);
                    }

                }

			}

		}

	};

	/**
	 * Constructor of peer using Kademlia protocol.
	 */
	public KademliaPeer() {
		routingTable = new KademliaRoutingTable(transactionTable);
		neighborTable = new KademliaNeighborTable();

		resourceManager.setRoutingTable(routingTable);
		resourceManager.setNeighborTable(neighborTable);
	}

	@Override
	protected boolean isBestKnownPeer(Request request) {
		boolean result = false;
		// TODO implement all checking (now only part of it was made).
		if (request instanceof JoinRequest) {

            if (LOG.isDebugEnabled()) {
			    LOG.debug("Received JoinRequest is checked whether can be satisfied. sourceID="
					+ ByteUtils.byteArrayToHexString(request.getSourceID()) + "");
            }

			// gets a hop closest to request originator
			PeerInfo nextHop = getNextHopForResourceID(request.getSourceID());
			// if there's no better hop, this hop will satisfy this request TODO is this OK?
			if (nextHop == null) {
				result = true;
			}
		} else if (request instanceof LookupPeerRequest) {
			//LookupPeerRequest lookupPeerRequest = (LookupPeerRequest) request;
            result = true;
		}/* else if (request instanceof ExchangeTableRequest) {
			ExchangeTableRequest exchangeTableRequest = (ExchangeTableRequest) request;
		} else if (request instanceof QueryRequest) {
			QueryRequest queryRequest = (QueryRequest) request;
		} else if (request instanceof ReplicateRequest) {
			ReplicateRequest replicateRequest = (ReplicateRequest) request;
		}*/ else if (request instanceof TransferRequest) {

			// TODO really?
			result = true;

		} else if (request instanceof PublishObjectRequest) {
			PublishObjectRequest publishObjectRequest = (PublishObjectRequest) request;

			// gets a hop closest to object's resourceID
			ResourceObject resource = publishObjectRequest.getResourceObject();
			PeerInfo nextHop = getNextHopForResourceID(resource.getResourceID().getResourceID());
			// if there's no better hop, this hop will satisfy this request
			if (nextHop == null) {
				result = true;
			}
		} else if (request instanceof LookupObjectRequest) {

            if (LOG.isDebugEnabled()) {
			    LOG.debug("Received LookupObjectRequest is checked whether can be satisfied. sourceID="
					+ ByteUtils.byteArrayToHexString(request.getSourceID()));
            }

			LookupObjectRequest lookupObjectRequest = (LookupObjectRequest) request;

			// checks if this peer is responsible for object for which this request is
			RLookup resourceLookup = lookupObjectRequest.getResourceLookup();
			result = resourceManager.isManaging(resourceLookup.getContentType(), resourceLookup.getContentSubtype(),
					resourceLookup.getResourceID(), resourceLookup.getOwner());

			// If this peer isn't responsible for searched object, it is checked whether there's a better peer to ask.
			if (!result) {
				// gets a hop closest to searched object's resourceID
				PeerInfo nextHop = getNextHopForResourceID(resourceLookup.getResourceID().getResourceID());

				// if there's no next hop
				if (nextHop == null) {
					result = true;
				}
			}

		} else if (request instanceof RemoveObjectRequest) {
			RemoveObjectRequest removeObjectRequest = (RemoveObjectRequest) request;
			// gets a hop closest to object's resourceID
			ResourceObject resource = removeObjectRequest.getResourceObject();
			PeerInfo nextHop = getNextHopForResourceID(resource.getResourceID().getResourceID());
			// if there's no better hop, this hop will satisfy this request
			if (nextHop == null) {
				result = true;
			}

		} /*else if (request instanceof TunnelRequest) {
			TunnelRequest tunnelRequest = (TunnelRequest) request;
		} else if (request instanceof ConnectRequest) {
			ConnectRequest connectRequest = (ConnectRequest) request;
		} else if (request instanceof InviteRequest) {
			InviteRequest inviteRequest = (InviteRequest) request;
        }*/ else if (request instanceof SendMessageRequest) {

            result = getNextHop(request) == null;

        }

		return result;
	}

	// API
	@Override
	public void leave() {
		// TODO probably should be done in a thread-safe way... now this method is invoked in another thread, and
		// creates the transaction... shouldn't work this way...

		if (LOG.isTraceEnabled()) LOG.trace("Leave from API invoked.");
		// leaving can happen only if peer is joined
		if (state == JOINED_NODE_STATE) {

			// removes all resources
			List<ResourceObject> allResources = resourceManager.removeAllResourceObjects();

			createLeaveIndicationTransactions(allResources);

			state = INITIAL_NODE_STATE;

			performLeaveTasks();

			callback.leaveCallback();

		} // TODO errorCallback()?
		else {
			if (LOG.isDebugEnabled()) {
                LOG.debug("Leave from API invoked but peer isn't joined.");
            }
		}

	}

	@Override
	public void publish(byte[] unhashedID, ResourceObject resourceObject) {
		// TODO probably should be done in a thread-safe way... now this method is invoked in another thread, and
		// creates the transaction... shouldn't work this way...

		try {
			// publishing is possible only when a peer is joined
			if (state == JOINED_NODE_STATE) {

                // Lets give developer a chance to play with the routing protocol
                if (resourceObject.getUnhashedID() != null && resourceObject.getResourceID() == null) {
                    // hashes given resourceID and sets it in given ResourceObject
                    byte[] hashedResourceID = P2PPUtils.hash(unhashedID, sharedManager.getOptions().getHashAlgorithm());
                    resourceObject.setResourceID(new ResourceID(hashedResourceID));
                    resourceObject.setUnhashedID(unhashedID);
                }

				// adds owner subobject to published resource
				resourceObject.setOwner(new Owner(sharedManager.getPeerIDAsBytes()));

				// adds self published resource to resource manager, so that manager will handle it (add to publish
				// table, republish before expires hits etc.)
				resourceManager.addSelfPublishedResourceObject(resourceObject);

			} else {
				if (LOG.isDebugEnabled()) {
                    LOG.debug("You can publish only if the peer has joined an overlay.");
                }
				// TODO probably onError callback
			}
		} catch (NoSuchAlgorithmException e) {
			LOG.error("Error while publishing resource " + resourceObject, e);
		}
	}

	@Override
	public void join(byte[] overlayID, String overlayPeerAddress, int overlayPeerPort) {

		// TODO probably should be done in a thread-safe way... now this method is invoked in another thread, and
		// creates the transaction... shouldn't work this way...

        if (LOG.isTraceEnabled()) {
            LOG.trace("Method invoked with " + "overlayAddress=" + overlayPeerAddress + ":" + overlayPeerPort + " in "
                    + state + " state.");
        }

		if (state == INITIAL_NODE_STATE) {
			// creates bootstrap request
			BootstrapRequest message = new BootstrapRequest(P2PPManager.CURRENT_PROTOCOL_VERSION, false, true, false, (byte) 255, null,
                    GlobalConstants.isOverReliable, GlobalConstants.isEncrypted, sharedManager.getPeerInfo(false, true));

			// creates AddressInfo object and adds it to a vector of addresses of bootstrap server
			AddressInfo bootstrapAddress = new AddressInfo((byte) 0, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 0,
                    (GlobalConstants.isOverReliable) ? AddressInfo.TCP_TRANSPORT_TYPE : AddressInfo.UDP_TRANSPORT_TYPE,
                    AddressInfo.HOST_ADDRESS_TYPE, overlayPeerPort, ByteUtils.stringIPAddressToBytes(overlayPeerAddress));
			Vector<AddressInfo> bootstrapAddressInfos = new Vector<AddressInfo>();
			bootstrapAddressInfos.add(bootstrapAddress);

            if (LOG.isTraceEnabled()) {
			    LOG.trace("Joining begins. Bootstrap server at " + overlayPeerAddress + ":" + overlayPeerPort);
            }

			// creates transaction for bootstrap request TODO Decide who will be the listener of this transaction.
			transactionTable.createTransactionAndFill(message, bootstrapTransactionListener, bootstrapAddressInfos,
					new byte[4], new byte[4]);
			state = BOOTSTRAPPING_NODE_STATE;

            if (LOG.isDebugEnabled()) {
			    LOG.debug("state changed to BOOTSTRAPPING");
            }

		} else {
            if (LOG.isDebugEnabled()) {
			    LOG.debug("Can't connect to an overlay because communication with one started already.");
            }
			callback.errorCallback(null, P2PPNodeCallback.BOOTSTRAP_ERROR_CODE);
		}

	}

	@Override
	public void lookup(byte contentType, byte contentSubtype, byte[] resourceID, Owner owner) {
		// TODO probably should be done in a thread-safe way... now this method is invoked in another thread, and
		// creates the transaction... shouldn't work this way...

		try {
			if (state == JOINED_NODE_STATE) {

				// hashes given resourceID
				byte[] hashedResourceID = P2PPUtils.hash(resourceID, sharedManager.getOptions().getHashAlgorithm());

				RLookup resourceLookup = new RLookup(contentType, contentSubtype, new ResourceID(hashedResourceID), owner);

				byte[] ownPeerID = sharedManager.getPeerIDAsBytes();

				LookupObjectRequest request = new LookupObjectRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null,
                        ownPeerID, GlobalConstants.isOverReliable, false, null, sharedManager.getPeerInfo(true, true), resourceLookup);

				PeerInfo nextHop = getNextHopForResourceID(hashedResourceID);

                if (nextHop == null) {

                    //nextHop = sharedManager.getPeerInfo(true, true);
                    List<ResourceObject> resources = resourceManager.getResourceObject(resourceLookup);
                    if (resources == null) {
                        if (LOG.isDebugEnabled()) LOG.debug("Resource not found.");
                        resources = new ArrayList<ResourceObject>();
                    }

                    callback.onDeliverRequest(request, resources);

                } else {

                    transactionTable.createTransactionAndFill(request, lookupObjectTransactionListener, nextHop.getAddressInfos(), ownPeerID,
                        nextHop.getPeerID().getPeerIDBytes());

                }

			} else {
				if (LOG.isDebugEnabled()) {
                    LOG.debug("You can make lookups only if the peer has joined an overlay.");
                }
			}
		} catch (NoSuchAlgorithmException e) {
			LOG.error("Not known hash algorithm.");
		}

	}

	@Override
	public void query() {
		// TODO probably should be done in a thread-safe way... now this method is invoked in another thread, and
		// creates the transaction... shouldn't work this way...
	}

	@Override
	public void remove(byte contentType, byte contentSubtype, byte[] resourceID, Owner owner) {
		// TODO probably should be done in a thread-safe way... now this method is invoked in another thread, and
		// creates the transaction... shouldn't work this way...
	}

    public void sendMessage(byte[] unhashedID, String protocol, byte[] message) {

        byte [] hashedID = null;

        try {
            hashedID = P2PPUtils.hash(unhashedID, sharedManager.getOptions().getHashAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error while hashing id", e);
        }

        PeerID peerID = new PeerID(hashedID);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending message protocol=" + protocol + " messageLength=" + message.length + " to peerID=" + peerID);
        }

        PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);

        final MessageResourceObject messageResourceObject = new MessageResourceObject();
        messageResourceObject.setMessageValue(message);
        messageResourceObject.setOwner(new Owner(peerID.getPeerIDBytes()));
        messageResourceObject.setResourceID(new ResourceID(peerID.getPeerIDBytes()));
        messageResourceObject.setUnhashedID(peerID.getPeerIDBytes());

        if (ownPeerInfo.getPeerID().equals(peerID)) {
            SendMessageRequest sendMessageRequest = new SendMessageRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null,
                    sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null, ownPeerInfo, ownPeerInfo.getPeerID(),
                    messageResourceObject, protocol);
            callback.onDeliverRequest(sendMessageRequest, new ArrayList<ResourceObject>(){{add(messageResourceObject);}});
            return;
        }

        PeerInfo nextHop = routingTable.getClosestTo(hashedID);
        if (nextHop != null) {

            SendMessageRequest sendMessageRequest = new SendMessageRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null,
                    sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null, ownPeerInfo, new PeerID(hashedID),
                    messageResourceObject, protocol);
            transactionTable.createTransactionAndFill(sendMessageRequest, transactionListener,
                    nextHop.getAddressInfos(), ownPeerInfo.getPeerID().getPeerIDBytes(),
                    nextHop.getPeerID().getPeerIDBytes());

        } else {
            callback.errorCallback(new ErrorInterface() {
                    private Object value;
                    public Object getValue() {return value;}
                    public void setValue(Object value) {this.value = value;}
                }, P2PPNodeCallback.USER_LOOKUP_ERROR_CODE);
        }

    }

	@Override
	public String[] getRoutingTableToString() {
		return routingTable.getEntriesDescription();
	}

	// API's end

	@Override
	protected void consume(Message message) {
		try {
			if (LOG.isDebugEnabled()) {
                LOG.debug("KademliaPeer starts consumption of a message of " + message.getClass().getName() + " type, state=" + state);
            }
			if (message instanceof P2PPMessage) {
				if (message instanceof Request) {
					// probably ignores return code
					onRequest((Request) message, true);
				} else if (message instanceof Response) {
					// TODO probably may be deleted, because transaction listeners are a better place to handle
					// responses (they have direct access to request and transaction)
					onResponseOrResponseACK((Response) message);
				} else if (message instanceof Indication) {
					onIndication((Indication) message);
				} else if (message instanceof Acknowledgment) {
					onAck((Acknowledgment) message);
				}
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO some kind of handling
		}

	}

	@Override
	protected void onForwardingRequest(Request request) {

        if (LOG.isDebugEnabled()) {
		    LOG.debug("Forwarding " + request.getClass().getName()
				+ " request, but before that onForwardingRequest() is called, so forwarding may not take place.");
        }

		// creates a list
		List<ResourceObject> resourcesList = null;

		// if request is of lookup type
		if (request instanceof LookupObjectRequest) {
			LookupObjectRequest lookupRequest = (LookupObjectRequest) request;
			// asks resource manager if there's searched object here (replicated or something)
			List<ResourceObject> resourceFound = resourceManager.getResourceObject(lookupRequest.getResourceLookup());

			if (resourceFound != null) {

				if (LOG.isDebugEnabled()) LOG.debug(resourceFound.size() + " replicated/cached resources found.");
				resourcesList = resourceFound;

			} else {

				if (LOG.isDebugEnabled()) LOG.debug("No replicated/cached object here.");
				resourcesList = new ArrayList<ResourceObject>(1);
				resourcesList.add(null);

			}

		}// if request is of publish type
		else if (request instanceof PublishObjectRequest) {

			PublishObjectRequest publishRequest = (PublishObjectRequest) request;
			ResourceObject publishedObject = publishRequest.getResourceObject();
			List<ResourceObject> foundResource = resourceManager.getResourceObject(publishedObject.getContentType(),
					publishedObject.getContentSubtype(), publishedObject.getResourceID(), publishedObject.getOwner());

			if (foundResource != null) {

				if (LOG.isDebugEnabled()) LOG.debug(foundResource.size() + " replicated/cached resources found.");
				resourcesList = foundResource;

			} else {

				if (LOG.isDebugEnabled()) LOG.debug("No replicated/cached object here.");
				resourcesList = new ArrayList<ResourceObject>(1);
				resourcesList.add(null);

			}

		} // if request is of any other type
		else {

			resourcesList = new ArrayList<ResourceObject>(1);
			resourcesList.add(null);

		}

		// invokes callback that will decide about how make a response for received request
		boolean continueNormally = callback.onForwardingRequest(request, resourcesList);

		// standard P2PP processing
		if (continueNormally) {

			// TODO probably if there's resource object in list, lookup response should be send, because object was
			// replicated/cached in such a case

			LOG.debug("Normal P2PP forwarding of " + request.getClass().getName() + " request.");

			// always returns next hop, because this (onForwardingRequest()) method couldn't be invoked elsewhere
			PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, isNodeAfterBootstrapping()); // (isNodeAfterBootstrapping(), true) ?
			PeerInfo nextHop = getNextHop(request);
            request.setSenderID(ownPeerInfo.getPeerID().getPeerIDBytes());
			Response nextHopResponse = request.createNextHopResponse(ownPeerInfo, nextHop);
			transactionTable.createTransactionAndFill(nextHopResponse, transactionListener, request.getPeerInfo()
					.getAddressInfos(), sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());

		} // processing as defined in Paulina's extension to P2PP callbacks
		else {

			LOG.debug("Forwarding " + request.getClass().getName() + " request in Paulina's extension way.");

			// handling of lookup request
			if (request instanceof LookupObjectRequest) {

				LookupObjectRequest lookupRequest = (LookupObjectRequest) request;

				PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, isNodeAfterBootstrapping());

				boolean[] responseCode = null;

				// if there's at least one non-null resource in list, it will be send
				if (Arrays.containsNonNullElement(resourcesList)) {
					responseCode = Response.RESPONSE_CODE_OK_BITS_ARRAY;
				} else {
					// TODO is this good? callback may replace a resource with null
					responseCode = Response.RESPONSE_CODE_NOT_FOUND_BITS_ARRAY;
					resourcesList = null;
				}

				LookupObjectResponse response = lookupRequest.createResponse(responseCode, ownPeerInfo, resourcesList);
				transactionTable.createTransactionAndFill(response, transactionListener, lookupRequest.getPeerInfo().getAddressInfos(),
                        ownPeerInfo.getPeerID().getPeerIDBytes(), lookupRequest.getPeerInfo().getPeerID().getPeerIDBytes());

			}// handling of publish request
			else if (request instanceof PublishObjectRequest) {
				PublishObjectRequest publishRequest = (PublishObjectRequest) request;

				PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, isNodeAfterBootstrapping());

				PublishObjectResponse response = publishRequest.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY,
						ownPeerInfo, DEFAULT_EXPIRES_SECONDS);

				transactionTable.createTransactionAndFill(response, transactionListener, publishRequest.getPeerInfo() .getAddressInfos(),
                        ownPeerInfo.getPeerID().getPeerIDBytes(), publishRequest.getPeerInfo().getPeerID().getPeerIDBytes());
			}

		}
	}

	@Override
	public String getOverlayAlgorithm() {
		return "Kademlia-SHA1-128";
	}

	@Override
	public int onRequest(Request request, boolean isReceived) {
		int returnCode = 0;

		// prevents from consuming a message that was already consumed
		if (isReceived) {
			if (transactionTable.wasAlreadyConsumed(request)) {
				// TODO is this the good code?
				return 0;
			}
		}

		switch (state) {
            case INITIAL_NODE_STATE:
                // probably ignore
                break;
            case BOOTSTRAPPING_NODE_STATE:
                // probably ignore
                break;
            case SEARCHING_FOR_STUN_SERVER_NODE_STATE:
                // probably ignore
                break;
            case STUN_SERVER_COMMUNICATION_NODE_STATE:
                // probably ignore
                break;
            case JOIN_REQUEST_SENT_NODE_STATE:
                if (request instanceof TransferRequest) {
                    onTransferRequest((TransferRequest) request);
                }
                break;
            case JOINED_NODE_STATE:
                if (LOG.isDebugEnabled()) LOG.debug("Peer received " + request.getClass().getName() + " in JOINED state.");

                if (request instanceof LookupObjectRequest) {
                    onLookupObjectRequest((LookupObjectRequest) request);
                } else if (request instanceof JoinRequest) {
                    onJoinRequest((JoinRequest) request);
                } else if (request instanceof PublishObjectRequest) {
                    returnCode = onPublishObjectRequest((PublishObjectRequest) request, isReceived);
                } else if (request instanceof TransferRequest) {
                    onTransferRequest((TransferRequest) request);
                } else if (request instanceof RemoveObjectRequest) {
                    onRemoveObjectRequest((RemoveObjectRequest) request);
                } else if (request instanceof LookupPeerRequest) {
                    onLookupPeerRequest((LookupPeerRequest) request);
                } else if (request instanceof SendMessageRequest) {
                    onSendMessageRequest((SendMessageRequest) request);
                }

                break;
            }

		return returnCode;
	}

	/**
	 *
	 * @param request
	 */
	private void onTransferRequest(TransferRequest request) {
		List<ResourceObject> transferredObjects = request.getResourceObjects();

		if (LOG.isDebugEnabled()) {
            LOG.debug(transferredObjects.size() + " objects transferred.");
        }

		for (ResourceObject currentResource : transferredObjects) {
			boolean alreadyHere = resourceManager.storeResourceObject(currentResource);
            if (alreadyHere && LOG.isDebugEnabled()) {
                LOG.debug("Resource is already stored: " + currentResource);
            }
		}

		TransferResponse response = request.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, sharedManager
				.getPeerIDAsBytes(), sharedManager.getPeerInfo(true, true));
		transactionTable.createTransactionAndFill(response, transactionListener, request.getPeerInfo()
				.getAddressInfos(), sharedManager.getPeerIDAsBytes(), request.getPeerInfo().getPeerID()
				.getPeerIDBytes());
	}

	/**
	 * Method invoked when RemoveObjectRequest was received. To be used in JOINED state.
	 *
	 * @param request
	 */
	private void onRemoveObjectRequest(RemoveObjectRequest request) {
		ResourceObject resourceObject = request.getResourceObject();

        if (LOG.isDebugEnabled()) {
		    LOG.debug("contentType=" + resourceObject.getContentType() + ", contentSubtype="
				+ resourceObject.getContentSubtype() + ", resourceID="
				+ ByteUtils.byteArrayToHexString(resourceObject.getResourceID().getResourceID()) + ", owner="
				+ ByteUtils.byteArrayToHexString(resourceObject.getOwner().getPeerIDValue()));
        }

		ResourceObject removedObject = resourceManager.removeResourceObject(resourceObject.getContentType(),
				resourceObject.getContentSubtype(), resourceObject.getResourceID(), resourceObject.getOwner());

		if (LOG.isDebugEnabled()) {
            LOG.debug("resourceObject=" + resourceObject + "; removedObject=" + removedObject);
        }

	}

    /**
     * Method invoked when PublishObjectRequest was received. To be used in JOINED state.
     *
     * @param request
     * @param isReceived True if given request was received through transport. False if this method was invoked by local entity
     *                   (i.e.
     * @return Returns 0 if resource being part of request, is stored in this entity. 1 otherwise.
     */
    private int onPublishObjectRequest(PublishObjectRequest request, boolean isReceived) {

        int resultCode = 1;
        ResourceObject resourceObject = request.getResourceObject();

        // ignores publish request with no resourceobject
        if (resourceObject != null) {
            boolean isAlreadyStored = resourceManager.isManaging(resourceObject);

            List<ResourceObject> listOfResources = null;

            // if object is already stored
            if (isAlreadyStored) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Published object is already stored here.");
                }
                // gets already stored resource object
                ResourceObject publishedObject = request.getResourceObject();
                listOfResources = resourceManager.getResourceObject(publishedObject.getContentType(), publishedObject
                        .getContentSubtype(), publishedObject.getResourceID(), publishedObject.getOwner());

            } else {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Published object isn't already stored here.");
                }
                // creates list of resources with one null
                listOfResources = new ArrayList<ResourceObject>(1);
                listOfResources.add(null);

            }

            // passes request and resource to callback function
            boolean continueStandardProcessing = callback.onDeliverRequest(request, listOfResources);

            // standard processing as defined in P2PP specification (draft 01)
            if (continueStandardProcessing) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Standard processing of PublishObjectRequest.");
                }

                PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Resource with value=" + resourceObject.getValueAsString() + " will be stored here.");
                }

                // adds object to resource manager (if it is stored there already, it is refreshed)
                // TODO is it interesting to know if given resource is already stored here?
                resourceManager.storeResourceObject(resourceObject);

                // if was received through transport, response will be generated
                if (isReceived) {
                    PublishObjectResponse response = request.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, ownPeerInfo, DEFAULT_EXPIRES_SECONDS);
                    transactionTable.createTransaction(response, transactionListener, request.getPeerInfo().getAddressInfos(),
                            ownPeerInfo.getPeerID().getPeerIDBytes(), request.getPeerInfo().getPeerID().getPeerIDBytes());
                }

            } // processing as defined in Paulina's extension to P2PP's callbacks
            else {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Non-standard processing of PublishObjectRequest.");
                }

                boolean containsOnlyNull = true;

                for (ResourceObject currentResource : listOfResources) {
                    if (currentResource != null) {
                        resourceManager.storeResourceObject(currentResource);
                        containsOnlyNull = false;
                    }
                }

                int expirationValue = DEFAULT_EXPIRES_SECONDS;

                // if callback has hid a resource
                if (containsOnlyNull) {
                    resultCode = 0;
                    expirationValue = Expires.EXPIRES_VALUE_NOT_REPUBLISH;
                }

                // if message was received over network, response has to be generated
                if (isReceived) {

                    PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);

                    Response response = request.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, ownPeerInfo, expirationValue);
                    transactionTable.createTransactionAndFill(response, transactionListener, request.getPeerInfo().getAddressInfos(),
                            ownPeerInfo.getPeerID().getPeerIDBytes(), request.getPeerInfo().getPeerID().getPeerIDBytes());
                }

            }
        } else {

            // TODO probably may be ignored because verification shouldn't pass a message without resource object
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received publish request with no ResourceObject.");
            }
        }

        return resultCode;

    }

	/**
	 * Method invoked when Response or ResponseACK was received. Analyses given response (or responseACK).
	 *
	 * @param response
	 * @throws NoSuchAlgorithmException
	 *             Thrown when somewhere in the code the hash method was used with wrong non existing hash function.
	 */
	private void onResponseOrResponseACK(Response response) throws NoSuchAlgorithmException {

		// Gets a transaction for given response.
		Transaction transaction = transactionTable.getTransaction(response);

		if (transaction == null) {
			// probably ignore, because generalAnalysis() should TODO change the info probably
			if (LOG.isDebugEnabled()) {
                LOG.debug("No transaction matching to " + response.getTransactionID().toString());
            }
		} else {
			// TODO probably remove transaction if reliable transport is used. or not (transaction table will do this)
			// transactionTable.removeMatchingTransaction(response);

			// passes received message to a transaction... it will know what to do
			transaction.setResponse(response);
			// next hop response doesn't have to be passed further
			if (response instanceof NextHopResponse) {
				return;
			}

			// reaction depending on current and new state
			switch (state) {
			case INITIAL_NODE_STATE:
				break;
			case BOOTSTRAPPING_NODE_STATE:
				// If received response for previously sent bootstrap request
				if (response instanceof BootstrapResponse) {
					onBootstrapResponse((BootstrapResponse) response);
				} else {
					if (LOG.isDebugEnabled()) {
                        LOG.debug("Peer received " + response.getClass().getName() + " in BOOTSTRAPPING state. It can't handle it now.");
                    }
				}
				break;
			case SEARCHING_FOR_STUN_SERVER_NODE_STATE:
				if (response instanceof LookupObjectResponse) {
					onSTUNServerLookup((LookupObjectResponse) response);
				} else {
					if (LOG.isDebugEnabled()) {
                        LOG.debug("Peer received " + response.getClass().getName() + " in SEARCHING_FOR_STUN_SERVER state. It can't handle it now.");
                    }
				}
				break;
			case STUN_SERVER_COMMUNICATION_NODE_STATE: {
				if (LOG.isDebugEnabled()) {
                    LOG.debug("Peer received " + response.getClass().getName() + " in STUN_SERVER_COMMUNICATION state. It can't handle it now.");
                }
			}
				break;
			case JOIN_REQUEST_SENT_NODE_STATE:
				if (response instanceof JoinResponse) {
					onJoinResponse((JoinResponse) response);
				} else {
					if (LOG.isDebugEnabled()) {
                        LOG.debug("Peer received " + response.getClass().getName() + " in JOIN_REQUEST_SENT state. It can't handle it now.");
                    }
				}

				break;
			case INFORMING_NEIGHBOURS_NODE_STATE: {
				if (response instanceof PublishObjectResponse) {
					onPublishObjectRespnoseBeforeJoined((PublishObjectResponse) response);
				} else {
					if (LOG.isDebugEnabled()) {
                        LOG.debug("Peer received " + response.getClass().getName() + " in INFORMING_NEIGHBOURS state. It can't handle it now.");
                    }
				}
			}
				break;
			case REQUESTING_NEIGHBOR_ROUTING_TABLES_NODE_STATE: {
				if (response instanceof ExchangeTableResponse) {
					onExchangeTableResponseBeforeJoined((ExchangeTableResponse) response);
				} else {
                    if (LOG.isDebugEnabled()) {
					    LOG.debug("Peer received " + response.getClass().getName() + " in REQUESTING_NEIGHBOUR_ROUTING_TABLES state. " +
                            "It can't handle it now.");
                    }
				}
			}
				break;
			case JOINED_NODE_STATE:
				if (LOG.isDebugEnabled()) {
                    LOG.debug("Peer received " + response.getClass().getName() + " in JOINED state.");
                }
				if (response instanceof PublishObjectResponse) {
					onPublishObjectResponse((PublishObjectResponse) response);
				} else if (response instanceof LookupObjectResponse) {
					onLookupObjectResponse((LookupObjectResponse) response);
				} else if (response instanceof TransferResponse) {
					onTransferResponse((TransferResponse) response);
                } else if (response instanceof SendMessageResponse) {
                    // Do nothing ...
				} else {
                    if (LOG.isDebugEnabled()) {
					    LOG.debug("Peer received " + response.getClass().getName() + " in JOINED state. It can't handle it now.");
                    }
				}
				break;
			}

		}

	}

	private void onTransferResponse(TransferResponse response) {
		// TODO Auto-generated method stub

	}

	/**
	 * Analyses {@link LookupObjectResponse}. To be used in JOINED state.
	 *
	 * @param response
	 */
	private void onLookupObjectResponse(LookupObjectResponse response) {
		if (LOG.isDebugEnabled()) {
            LOG.debug("Resource found=" + response.getResourceObject().toString());
        }

		// TODO decide whether there's a sense of using this place or maybe rather using TransactionListener

	}

	/**
	 * Analyses {@link PublishObjectResponse}. To be used in JOINED state.
	 *
	 * @param response
	 */
	private void onPublishObjectResponse(PublishObjectResponse response) {
		Expires expires = response.getExpires();
		if (LOG.isDebugEnabled()) {
            LOG.debug("Resource published. Should be republished before " + expires.getExpiresSeconds() + " seconds.");
        }
	}

	private void onExchangeTableResponseBeforeJoined(ExchangeTableResponse response) {
		// TODO Auto-generated method stub

	}

	/**
	 * Analyses publish object response received in SECOND_PART_OF_JOINING state. It is assumed that this is a response
	 * to publish of UserInfo resource object. It should contain {@link Expires} object.
	 *
	 * @param response
	 */
	private void onPublishObjectRespnoseBeforeJoined(PublishObjectResponse response) {
		@SuppressWarnings("unused")
		Expires expires = response.getExpires();

	}

	/**
	 * Method invoked when Indication was received. Analyses received indication.
	 *
	 * @param indication
	 */
	private void onIndication(Indication indication) {
		// TODO Auto-generated method stub

	}

	/**
	 * Analyzes received acknowledgment. TODO I'm not sure if this is needed (probably
         * {@link P2PPEntity#generalAnalysis(P2PPMessage)} handles it and ACK is not passed here)... or not
	 *
	 * @param ack
	 */
	private void onAck(Acknowledgment ack) {

		Transaction transaction = transactionTable.getTransaction(ack);

		// if there's a matching transaction for this ACK
		if (transaction != null) {
			transaction.setReceivedAck(ack);
		}
	}

	/**
	 * Analyses received {@link LookupObjectResponse} that was received in SEARCHING_FOR_STUN_SERVER_NODE_STATE. It is
	 * assumed that given response is regarding to STUN server content type.
	 *
	 * @param response
	 */
	private void onSTUNServerLookup(LookupObjectResponse response) {

		int responseCode = response.getResponseCodeAsInt();
        if (LOG.isDebugEnabled()) {
		    LOG.debug("KademliaPeer is searching for STUN server, received a LookupObjectResponse with code=" + responseCode);
        }

		if (responseCode == Response.RESPONSE_CODE_OK) {

			try {
				// Gets resource object from given response and gets PeerInfo of STUNServer service provider from it
				Vector<ResourceObject> resourceObjects = response.getResourceObject();
				STUNServiceResourceObject firstResourceObject = (STUNServiceResourceObject) resourceObjects.get(0);
				PeerInfo stunPeerInfo = firstResourceObject.getServiceOwnerPeerInfo();

                if (LOG.isDebugEnabled()) {
				    LOG.debug("There are " + resourceObjects.size() + " STUN servers described in response.");
                }

				// Checks if there's an address in response. There's an assumption that it is an address of STUN server.
				Vector<AddressInfo> addressInfos = stunPeerInfo.getAddressInfos();
				if (addressInfos != null) {
					state = STUN_SERVER_COMMUNICATION_NODE_STATE;
					// Starts communication with discovered STUN server.
					STUNService stunService = (STUNService) resourceManager.getService(P2PPUtils.STUN_CONTENT_TYPE);
					stunService.determineServerReflexiveAddress(stunListener, addressInfos);
				} // if there's no address
				else {
					// TODO probably ignore but maybe that's the place for trying to get
					// an address of different STUN server
				}

			} catch (NullPointerException e) {
				// TODO probably ignore but maybe that's the place for trying to get
				// an address of different STUN server
				LOG.warn("KademliaPeer is searching for STUN server, but received a bad response for his LookupObject request.", e);
			} catch (ClassCastException e) {
				// TODO probably ignore but maybe that's the place for trying to get
				// an address of different STUN server
				LOG.warn("KademliaPeer is in searching for STUN server, but received a bad response for his LookupObject request.", e);
			}
		} else {
			if (LOG.isDebugEnabled()) LOG.debug("STUN server wasn't found. ResponseCode=" + responseCode);
		}
	}

	/**
	 * Analyses given {@link LookupObjectRequest}. Intended for usage in JOINED_NODE_STATE.
	 *
	 * @param lookupRequest
	 *            Request to be analyzed.
	 */
	private void onLookupObjectRequest(LookupObjectRequest lookupRequest) {

		// gets RLookup object from request and its content type
		RLookup resourceLookup = lookupRequest.getResourceLookup();
		// int contentType = resourceLookup.getContentType();

		// checks whether request is for resource object or service
		List<ResourceObject> resources = resourceManager.getResourceObject(resourceLookup);

		// If there's no resource object for given request
		if (resources == null) {
			if (LOG.isDebugEnabled()) LOG.debug("Resource not found.");
			resources = new ArrayList<ResourceObject>(1);
			resources.add(null);
		}

		// informs callback about received lookup request TODO probably returned value is not interesting
		@SuppressWarnings("unused")
		boolean continueNormally = callback.onDeliverRequest(lookupRequest, resources);

		// TODO reaction on callback's returned value (in lookup request case) seems to be the same for true and false

		// checks if there's at least one resource object found (list of resources contains non-null element)
		int resourcesSize = resources.size();
		boolean resourceFound = false;
		for (int i = 0; i < resourcesSize; i++) {
			if (resources.get(i) != null) {
				resourceFound = true;
				break;
			}
		}

		// creates response with proper response code (if this peer didn't find a resource object, NOT FOUND (404 is
		// response code))
		boolean[] responseCode = null;
		if (resourceFound) {
			if (LOG.isDebugEnabled()) LOG.debug("Resource found (might have been added by callback).");
			responseCode = Response.RESPONSE_CODE_OK_BITS_ARRAY;
		} else {
			if (LOG.isDebugEnabled()) LOG.debug("Resource not found (might have been hidden by callback).");
			responseCode = Response.RESPONSE_CODE_NOT_FOUND_BITS_ARRAY;
		}

		// creates response
		LookupObjectResponse response = lookupRequest.createResponse(responseCode, sharedManager.getPeerInfo(true, true), resources);

		// creates transaction
		transactionTable.createTransaction(response, transactionListener, lookupRequest.getPeerInfo().getAddressInfos(),
						sharedManager.getPeerIDAsBytes(), response.getSourceID());

	}

    public void onLookupPeerRequest(LookupPeerRequest request) {

        PeerInfo requestPI = request.getPeerInfo();
        PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
        LookupPeerResponse response = request.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, ownPeerInfo);
        transactionTable.createTransaction(response, transactionListener, requestPI.getAddressInfos(), sharedManager.getPeerIDAsBytes(),
                request.getSourceID());

    }

	/**
	 * Analyses given {@link JoinRequest}. Intended for usage in JOINED_NODE_STATE.
	 *
	 * @param joinRequest
	 */
	private void onJoinRequest(JoinRequest joinRequest) {

		// gets request options object from request
		RequestOptions requestOptions = joinRequest.getRequestOptions();

		// if there's no request options object (i.e.it is a first JoinRequest sent by joining peer)
		// TODO good sequence of arguments? requestOptions won't be null?
		if (requestOptions == null || !requestOptions.getSFlag()) {
			// creates Expires object with a time after which a node is considered dead TODO really dead?
			Expires expires = new Expires(NodeTimers.KEEP_ALIVE_TIMER_SECONDS);
			// gets a vector of PeerInfo describing peers that will be returned to joining peer
			PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
			byte[] ownPeerID = sharedManager.getPeerIDAsBytes();

			PeerInfo joiningPeerInfo = joinRequest.getPeerInfo();
			byte[] joiningPeerID = joiningPeerInfo.getPeerID().getPeerIDBytes();
			Vector<AddressInfo> joiningPeerAddresses = joiningPeerInfo.getAddressInfos();

            PeerInfo closestPeer = this.routingTable.getClosestTo(joiningPeerID);
            // Equal peerID, different addresses. See analyzeOver(Un)Reliable -> updateTables
            if (closestPeer != null && Arrays.equals(joiningPeerID, closestPeer.getPeerID().getPeerIDBytes())) {
                AddressInfo joiningAddressInfo = joiningPeerAddresses.get(0);
                AddressInfo closestAddressInfo = closestPeer.getAddressInfos().get(0);
                System.err.println(joiningAddressInfo);
                System.err.println(closestAddressInfo);
                if (!Arrays.equals(joiningAddressInfo.getAddress(), closestAddressInfo.getAddress()) ||
                    joiningAddressInfo.getPort() != closestAddressInfo.getPort()) {
                    LOG.warn("Node id=" + joinRequest.getPeerInfo().getPeerID() +
                            " tried to join when there already is a node with this id in the overlay id=" + closestPeer.getPeerID());
                    JoinResponse response = joinRequest.createResponse(Response.RESPONSE_CODE_REQUEST_REJECTED_BITS_ARRAY, ownPeerInfo, expires, null, null);
                    transactionTable.createTransaction(response, transactionListener, joiningPeerAddresses, ownPeerID, joiningPeerID);
                    return;
                }
            }

			// creates response with expires object and a set of PeerInfos
			JoinResponse response = joinRequest.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, ownPeerInfo,
					expires, null, null);
			transactionTable.createTransaction(response, transactionListener, joiningPeerAddresses, ownPeerID, joiningPeerID);

			// asks resource manager if there are resources that should be transferred to joining peer
			List<ResourceObject> resourcesToTransfer = resourceManager.getResourceObjectsCloserTo(ownPeerID,
					joiningPeerID, this);

			// if there are resources that should be transferred
			if (resourcesToTransfer != null) {
				TransferRequest transferRequest = new TransferRequest(P2PPManager.CURRENT_PROTOCOL_VERSION, false, true, false, (byte) 255, null,
                        ownPeerID, GlobalConstants.isOverReliable, false, ownPeerInfo, resourcesToTransfer);
				transactionTable.createTransactionAndFill(transferRequest, transactionListener, joiningPeerAddresses,
						ownPeerID, joiningPeerID);
			}

		} else if (requestOptions.getSFlag()) {
			// TODO probably ignore because neighbor table isn't build in Kademlia
		}
	}

	/**
	 * Method invoked when {@link JoinResponse} was received. It analyses given response. To be used in
	 * JOIN_REQUEST_SENT or INFORMING_NEIGHBORS state.
	 *
	 * @param response
	 */
	private void onJoinResponse(JoinResponse response) {

	}

	/**
	 * Analyzes bootstrap response. To be used in BOOTSTRAPPING_NODE_STATE. TODO probably should be moved to bootstrap
	 * transaction listener
	 *
	 * @param response
	 * @throws NoSuchAlgorithmException
	 *             Thrown when somewhere in the code the hash method was used with non existing hash function.
	 */
	private void onBootstrapResponse(BootstrapResponse response) throws NoSuchAlgorithmException {

        // gets PeerInfo describing originator of BootstrapRequest (it's this peer.. BootstrapServer filled this object)
		PeerInfo originatorPeerInfo = response.getOriginatorPeerInfo();

		// saves new peerID
		final byte[] ownNodeID = originatorPeerInfo.getPeerID().getPeerIDBytes();

		sharedManager.setPeerIDAsBytes(ownNodeID);

		final PeerInfo ownPeerInfo = sharedManager.getPeerInfo(false, true);
		// saves p2p options from bootstrap response
		P2POptions options = response.getP2POptions();
		sharedManager.setOptions(options);

        // What we'll now do here is ensure that there's no candidate with the same ID
        Vector<PeerInfo> peersInOverlay = new Vector<PeerInfo>();
        Collection<PeerInfo> peersInOverlayReceived = response.getPeersInOverlay();
        for (PeerInfo pior : peersInOverlayReceived) {
            if (!Arrays.equals(pior.getPeerID().getPeerIDBytes(), ownPeerInfo.getPeerID().getPeerIDBytes())) {
                peersInOverlay.add(pior);
            }
        }

		// saves information about bootstrap candidates
		this.bootstrapCandidates = peersInOverlay;

		if (LOG.isDebugEnabled()) LOG.debug("Bootstrap response received. PeerID=" + ByteUtils.byteArrayToHexString(ownNodeID));

		// Creates Owner object that will be used for creating ResourceObjects.
		Owner owner = new Owner(ownNodeID);

		// informs objects about overlay options
		resourceManager.setProperties(options.getHashAlgorithm(), options.getHashAlgorithmLength(), owner, DEFAULT_EXPIRES_SECONDS);
		routingTable.setProperties(options.getBase(), options.getHashAlgorithmLength() * 8);
        routingTable.setLocalPeerInfo(ownPeerInfo);
		routingTable.setLocalPeerID(ownNodeID);

		int numOfPeers = bootstrapCandidates.size();

		// If there are already peers in overlay (returned in received
		// response).
		if (numOfPeers > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Peer received " + numOfPeers + " peers in bootstrap response. Now is in SEARCHING_FOR_STUN_SERVER state.");
            }

			// bootstrap candidates are added to routing/neighbor tables (probably already done in generalAnalysis)
			updateTables(bootstrapCandidates);

			// changes state
			state = SEARCHING_FOR_STUN_SERVER_NODE_STATE;

			// asks bootstrap candidate for STUN server
			ResourceID stunResourceID = new ResourceID(P2PPUtils.hash(P2PPUtils.STUN_SERVICE_ID, options.getHashAlgorithm()));
			RLookup resourceLookup = new RLookup(P2PPUtils.STUN_CONTENT_TYPE, (byte) 0, stunResourceID, null);
			LookupObjectRequest request = new LookupObjectRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null, ownNodeID,
                    GlobalConstants.isOverReliable, false, null, ownPeerInfo, resourceLookup);

            final AtomicInteger candidateNumber = new AtomicInteger(0);

            TransactionListener lookupSTUNTransactionListener = new TransactionListener() {

                public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType, Request request, Response response,
                                             TransactionTable transactionTable, P2PPEntity node) {

                    if (transactionState == Transaction.TERMINATED_STATE) {

                        if (response instanceof NextHopResponse) {
                            if (LOG.isDebugEnabled()) LOG.debug("Received next hop response for lookup stun resource object");
                            NextHopResponse nextHopResponse = (NextHopResponse) response;
                            PeerInfo nextHop = nextHopResponse.getPeerInfo();
                            transactionTable.createTransactionAndFill(request, this, nextHop.getAddressInfos(),
                                    sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());

                        }

                    } else {

                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Removing faulty peer from the routing table: " + bootstrapCandidates.get(candidateNumber.intValue()));
                        }
                        routingTable.removePeerInfo(bootstrapCandidates.get(candidateNumber.intValue()));

                        if (candidateNumber.incrementAndGet() < bootstrapCandidates.size()) {

                            PeerInfo chosenBootstrapCandidate = bootstrapCandidates.get(candidateNumber.intValue());
                            if (!ownPeerInfo.equals(chosenBootstrapCandidate)) {

                                Vector<AddressInfo> candidateAddresses = chosenBootstrapCandidate.getAddressInfos();
                                // TODO maybe inform someone about a fact that we are bootstrapped
                                transactionTable.createTransactionAndFill(request, this, candidateAddresses, sharedManager.getPeerIDAsBytes(),
                                        chosenBootstrapCandidate.getPeerID().getPeerIDBytes());

                            } else if (candidateNumber.intValue() == bootstrapCandidates.size() -1) {

                                state = JOINED_NODE_STATE;
                                callback.joinCallback();
                                if (LOG.isDebugEnabled()) LOG.debug("Checked all valid candidates. There are no peers in overlay. Peer is now JOINED.");
                                UserInfoResourceObject userInfo = new UserInfoResourceObject(new Owner(ownNodeID), new Expires(120), null,
                                        new Certificate(false, new byte[] { 2, 2, 2, 2 }), ownPeerInfo.getAddressInfos());
                                userInfo.setValue(new ResourceObjectValue(ownPeerInfo.asBytes()));
                                publish(userInfo.getUnhashedID(), userInfo);
                                List<ResourceObject> listOfServiceResources = resourceManager.getResourceObjectsForServices(ownPeerInfo);
                                if (LOG.isDebugEnabled()) LOG.debug("Publishes information about " + listOfServiceResources.size() + " services.");
                                for (ResourceObject current : listOfServiceResources) {
                                    publish(current.getUnhashedID(), current);
                                }

                            }

                        } else {

                            state = JOINED_NODE_STATE;
                            callback.joinCallback();
                            if (LOG.isDebugEnabled()) LOG.debug("Checked all candidates. There are no peers in overlay. Peer is now JOINED.");
                            UserInfoResourceObject userInfo = new UserInfoResourceObject(new Owner(ownNodeID), new Expires(120), null,
                                    new Certificate(false, new byte[] { 2, 2, 2, 2 }), ownPeerInfo.getAddressInfos());
                            userInfo.setValue(new ResourceObjectValue(ownPeerInfo.asBytes()));
                            publish(userInfo.getUnhashedID(), userInfo);
                            List<ResourceObject> listOfServiceResources = resourceManager.getResourceObjectsForServices(ownPeerInfo);
                            if (LOG.isDebugEnabled()) LOG.debug("Publishes information about " + listOfServiceResources.size() + " services.");
                            for (ResourceObject current : listOfServiceResources) {
                                publish(current.getUnhashedID(), current);
                            }

                        }

                    }

                }

            };

            PeerInfo chosenBootstrapCandidate = bootstrapCandidates.get(candidateNumber.intValue());
			Vector<AddressInfo> candidateAddresses = chosenBootstrapCandidate.getAddressInfos();
			// TODO maybe inform someone about a fact that we are bootstrapped
			transactionTable.createTransactionAndFill(request, lookupSTUNTransactionListener, candidateAddresses, sharedManager.getPeerIDAsBytes(),
                    chosenBootstrapCandidate.getPeerID().getPeerIDBytes());

		} else { // If there are no peers in overlay.
			// Peer has joined.
			state = JOINED_NODE_STATE;

			// informs callback function about a fact that this peer has joined an overlay
			callback.joinCallback();

			if (LOG.isDebugEnabled()) LOG.debug("There are no peers in overlay. Peer is now JOINED.");

			// creates UserInfoResource and resource objects for services
			UserInfoResourceObject userInfo = new UserInfoResourceObject(new Owner(ownNodeID), new Expires(120), null,
					new Certificate(false, new byte[] { 2, 2, 2, 2 }), ownPeerInfo.getAddressInfos());
			userInfo.setValue(new ResourceObjectValue(ownPeerInfo.asBytes()));
			publish(userInfo.getUnhashedID(), userInfo);

			// gets resource objects for local services
			List<ResourceObject> listOfServiceResources = resourceManager.getResourceObjectsForServices(ownPeerInfo);

			if (LOG.isDebugEnabled()) LOG.debug("Publishes information about " + listOfServiceResources.size() + " services.");

			// publishes objects describing services
			for (ResourceObject current : listOfServiceResources) {
				publish(current.getUnhashedID(), current);
			}

			// TODO Has to start TURN/STUN/ICE server.

		}

	}

    public void onSendMessageRequest(final SendMessageRequest request) {

        callback.onDeliverRequest(request, new ArrayList<ResourceObject>(){{add(request.getMessageResourceObject());}});

        PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
        SendMessageResponse response = request.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, ownPeerInfo);
        transactionTable.createTransaction(response, transactionListener, request.getPeerInfo().getAddressInfos(),
                sharedManager.getPeerIDAsBytes(), response.getSourceID());

    }

	@Override
	public void updateTables(Vector<PeerInfo> peerInfos) {
		if (peerInfos != null) {
			int size = peerInfos.size();
			for (int i = 0; i < size; i++) {
				PeerInfo currentPeerInfo = peerInfos.get(i);
				updateTables(currentPeerInfo);
			}
		}
	}

	@Override
	public void updateTables(PeerInfo peerInfo) {

		// handles a case when PeerInfo is of a peer that hasn't been bootstrapped (id is 4 bytes long)
		// TODO probably doing something bad when non-structural overlay algorithm is used
		if (peerInfo != null && peerInfo.getPeerID().getPeerIDBytes().length != 4) {

			// informs routing and neighbour tables about a PeerInfo
			routingTable.addPeerInfo(peerInfo);
			// neighborTable.addPeerInfo(peerInfo);

		}
	}

	@Override
    public void onTimeSlot() {

        routingTable.onTimeSlot();
        transactionTable.onTimeSlot(this);
        resourceManager.onTimeSlot(this);


        switch (state) {
            case STUN_SERVER_COMMUNICATION_NODE_STATE: {
                // checks if server reflexive address was determined (TODO synchronize? because address is determined in
                // STUNService's thread [that's how it will look like when STUN implementation will be ready]).
                if (serverReflexiveAddress != null) {
                    // adds determined address to shared manager
                    
                    // TODO ???
                    //sharedManager.addAddress(serverReflexiveAddress.getAddressAsString(), serverReflexiveAddress.getPort(),
                    //        serverReflexiveAddress.getHt(), serverReflexiveAddress.getTt());

                    // TODO is nullifying serverReflexiveAddress field needed?
                    serverReflexiveAddress = null;

                    // TODO in the future this should be changed to a state of determining relayed candidate
                    state = JOIN_REQUEST_SENT_NODE_STATE;

                    // gets a PeerInfo object describing remote Peer (closest to us) to which a Join request will be send
                    byte[] ownPeerID = sharedManager.getPeerIDAsBytes();
                    PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
                    PeerInfo closestInfo = routingTable.getClosestTo(ownPeerID);

                    if (LOG.isDebugEnabled() && ownPeerInfo.equals(closestInfo)) {
                        LOG.debug("Routing tables returns ownPeerInfo for closestTo(" + new String(ownPeerID) + ") -- This should not happen");
                    }

                    // TODO what about a case when closestInfo is null?

                    if (LOG.isDebugEnabled()) LOG.debug("Creating Join transaction and sending it to peer=" + closestInfo);

                    // creates transaction for join request which will be send to
                    JoinRequest joinRequest = new JoinRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, true, false, (byte) 255, null, ownPeerID,
                            GlobalConstants.isOverReliable, false, null, ownPeerInfo);
                    transactionTable.createTransactionAndFill(joinRequest, joinTransactionListener, closestInfo
                            .getAddressInfos(), ownPeerID, closestInfo.getPeerID().getPeerIDBytes());

                }
                break;
            }
            case JOINED_NODE_STATE: {
                break;
            }
        }
    }

	@Override
	protected PeerInfo getNextHop(Request request) {

		PeerInfo nextHop = null;

		// TODO implement all checking (now only part of it was made).
		if (request instanceof JoinRequest) {
			// gets next hop for join
			nextHop = getNextHopForResourceID(request.getPeerInfo().getPeerID().getPeerIDBytes());

		} /*else if (request instanceof LookupPeerRequest) {
			LookupPeerRequest lookupPeerRequest = (LookupPeerRequest) request;
		} else if (request instanceof ExchangeTableRequest) {
			ExchangeTableRequest exchangeTableRequest = (ExchangeTableRequest) request;
		} else if (request instanceof QueryRequest) {
			QueryRequest queryRequest = (QueryRequest) request;
		} else if (request instanceof ReplicateRequest) {
			ReplicateRequest replicateRequest = (ReplicateRequest) request;
		} else if (request instanceof TransferRequest) {
			TransferRequest transferRequest = (TransferRequest) request;
		}*/ else if (request instanceof PublishObjectRequest) {
			PublishObjectRequest publishObjectRequest = (PublishObjectRequest) request;

			// gets a hop closest to object's resourceID
			ResourceObject resource = publishObjectRequest.getResourceObject();
			nextHop = getNextHopForResourceID(resource.getResourceID().getResourceID());

		} else if (request instanceof LookupObjectRequest) {
			LookupObjectRequest lookupObjectRequest = (LookupObjectRequest) request;

			// gets next hop gf
			RLookup resourceLookup = lookupObjectRequest.getResourceLookup();
			nextHop = getNextHopForResourceID(resourceLookup.getResourceID().getResourceID());

		} /*else if (request instanceof RemoveObjectRequest) {
			RemoveObjectRequest removeObjectRequest = (RemoveObjectRequest) request;
		} else if (request instanceof TunnelRequest) {
			TunnelRequest tunnelRequest = (TunnelRequest) request;
		} else if (request instanceof ConnectRequest) {
			ConnectRequest connectRequest = (ConnectRequest) request;
		} else if (request instanceof InviteRequest) {
			InviteRequest inviteRequest = (InviteRequest) request;
		}*/ else if (request instanceof SendMessageRequest) {

            byte[] targetPeerIDBytes = ((SendMessageRequest) request).getTargPeerID().getPeerIDBytes();

            if (Arrays.equals(sharedManager.getPeerIDAsBytes(), targetPeerIDBytes)) {
                nextHop = null;
            } else {
                nextHop = routingTable.getClosestTo(targetPeerIDBytes);
            }

        }

		return nextHop;
	}

	@Override
	public boolean isNodeAfterBootstrapping() {
		if (state == INITIAL_NODE_STATE || state == BOOTSTRAPPING_NODE_STATE) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public PeerInfo getClosestTo(byte[] id) {
		return routingTable.getClosestTo(id);
	}

	@Override
	protected PeerInfo getNextHopForResourceID(byte[] id) {

		return routingTable.getNextHop(id);
	}

	@Override
	public BigInteger getDistance(String unhashedKey1, String unhashedKey2) throws IllegalStateException {

		// can return distance only when he is joined to an overlay (so that he knows overlay parameters)
		if (state == JOINED_NODE_STATE) {

			try {

				P2POptions options = sharedManager.getOptions();

				byte hashAlgorithmID = options.getHashAlgorithm();

				byte[] hashedKey1 = P2PPUtils.hash(unhashedKey1.getBytes(Charset.forName("UTF-8")), hashAlgorithmID);
				byte[] hashedKey2 = P2PPUtils.hash(unhashedKey2.getBytes(Charset.forName("UTF-8")), hashAlgorithmID);

				return KBucket.getDistanceBetween(hashedKey1, hashedKey2);

			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException("Corrupted hash is known by an overlay.");
			}
		} else {
			throw new IllegalStateException(
					"Peer can't return a distance between two nodes before he is joined to an overlay.");
		}
	}

	@Override
	public BigInteger getDistanceBetweenHashed(byte[] hashedKey1, byte[] hashedKey2) throws IllegalStateException {
		// can return distance only when he is joined to an overlay (so that he knows overlay parameters)
		if (state == JOINED_NODE_STATE) {

			return KBucket.getDistanceBetween(hashedKey1, hashedKey2);

		} else {
			throw new IllegalStateException(
					"Peer can't return a distance between two nodes before he is joined to an overlay.");
		}
	}

	/**
	 * Creates a list of LeaveIndication messages that can be send to known peers. Those indications are filled with
	 * given resources. There may be many indications, because some resources may be passed to one peer and some to some
	 * other peer. Then creates transactions for them, so that indications will be send. TODO currently passes everything to one peer
	 *
	 * @param listOfResources
	 *
	 */
	private void createLeaveIndicationTransactions(List<ResourceObject> listOfResources) {

		//List<LeaveIndication> leaveIndications = new ArrayList<LeaveIndication>();

		byte[] ownPeerID = sharedManager.getPeerIDAsBytes();
		PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);

		// gets first of resouces and searches closest peer
		PeerInfo closestPeerInfo = routingTable.getClosestTo(listOfResources.get(0).getResourceID().getResourceID());
		if (closestPeerInfo != null) {
			LeaveIndication indication = new LeaveIndication(P2PPManager.CURRENT_PROTOCOL_VERSION, true, false, (byte) 20, null, ownPeerID,
                    GlobalConstants.isOverReliable, false, ownPeerInfo, listOfResources);

			transactionTable.createTransactionAndFill(indication, transactionListener, closestPeerInfo
					.getAddressInfos(), ownPeerID, closestPeerInfo.getPeerID().getPeerIDBytes());
		}

	}

        @Override
        public Byte getRoutingAlgorithm() {
            return P2PPUtils.KADEMLIA_P2P_ALGORITHM;
        }
}
