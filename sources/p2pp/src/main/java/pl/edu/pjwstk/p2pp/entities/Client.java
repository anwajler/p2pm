package pl.edu.pjwstk.p2pp.entities;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import pl.edu.pjwstk.p2pp.GlobalConstants;
import pl.edu.pjwstk.p2pp.P2PPManager;
import pl.edu.pjwstk.p2pp.P2PPNodeCallback;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.BootstrapRequest;
import pl.edu.pjwstk.p2pp.messages.requests.JoinRequest;
import pl.edu.pjwstk.p2pp.messages.requests.LookupObjectRequest;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.messages.responses.BootstrapResponse;
import pl.edu.pjwstk.p2pp.messages.responses.JoinResponse;
import pl.edu.pjwstk.p2pp.messages.responses.NextHopResponse;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.Certificate;
import pl.edu.pjwstk.p2pp.objects.Expires;
import pl.edu.pjwstk.p2pp.objects.Owner;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RLookup;
import pl.edu.pjwstk.p2pp.objects.ResourceID;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.objects.ResourceObjectValue;
import pl.edu.pjwstk.p2pp.objects.Signature;
import pl.edu.pjwstk.p2pp.resources.UserInfoResourceObject;
import pl.edu.pjwstk.p2pp.transactions.Transaction;
import pl.edu.pjwstk.p2pp.transactions.TransactionListener;
import pl.edu.pjwstk.p2pp.transactions.TransactionTable;
import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.NodeTimers;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * Implementation on the basis of KademliaPeer code by Maciej Skorupka s3874@pjwstk.edu.pl
 *
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */

public class Client extends Node {

	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Client.class);

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

                        if (logger.isDebugEnabled()) {
						    logger.debug("Communication with bootstrap candidate nr" + currentBootstrapCandidateIndex
								+ " has failed. Communication with another one starts.");
                        }

						// asks another bootstrap candidate for STUN server
						ResourceID stunResourceID = new ResourceID(P2PPUtils.hash(P2PPUtils.STUN_SERVICE_ID, options
								.getHashAlgorithm()));
						RLookup resourceLookup = new RLookup(P2PPUtils.STUN_CONTENT_TYPE, (byte) 0, stunResourceID,
								null);
						LookupObjectRequest lookupRequest = new LookupObjectRequest(
								P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null,
								sharedManager.getPeerIDAsBytes(), false, false, null, sharedManager.getPeerInfo(true,
										true), resourceLookup);
						PeerInfo chosenBootstrapCandidate = (PeerInfo) bootstrapCandidates.get(0);
						Vector<AddressInfo> candidateAddresses = chosenBootstrapCandidate.getAddressInfos();
						// TODO maybe inform someone about a fact that we are bootstrapped
						transactionTable.createTransactionAndFill(lookupRequest, transactionListener,
								candidateAddresses, sharedManager.getPeerIDAsBytes(), chosenBootstrapCandidate
										.getPeerID().getPeerIDBytes());
					} catch (NoSuchAlgorithmException e) {
						// TODO probably can't happen
						if (logger.isDebugEnabled())logger.debug("No algorithm!");
					}

				} // if there are no more bootstrap candidates
				else {
					if (logger.isDebugEnabled()) logger.debug("No response from any of bootstrap peers, so peer client can't join");

					// informs callback that bootstrapping ended with an error, so that this client can't
					//connect to the overlay
					callback.errorCallback(null, P2PPNodeCallback.BOOTSTRAP_ERROR_CODE);
			    }
			}
		}

	};

	private TransactionListener joinTransactionListener = new TransactionListener() {

		public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType,
				Request request, Response response, TransactionTable transactionTable, P2PPEntity node) {

            if (logger.isTraceEnabled()) {
			    logger.trace("transactionID:"
					+ ByteUtils.bytesToLong(transactionID[0], transactionID[1], transactionID[2], transactionID[3])
					+ "; transactionState:" + transactionState + "; transactionType:" + TransactionType + "; request="
					+ request + "; response: " + response);
            }

			// if transaction ended properly
			if (transactionState == Transaction.TERMINATED_STATE) {
				if (response instanceof NextHopResponse) {

					NextHopResponse nextHopResponse = (NextHopResponse) response;
					PeerInfo nextHop = nextHopResponse.getNextHopPeerInfo();

					// TTL -1?
					transactionTable.createTransactionAndFill(request, joinTransactionListener, nextHop
							.getAddressInfos(), sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());

				} else if (response instanceof JoinResponse) {

					JoinResponse joinResponse = (JoinResponse) response;

					try {
						// TODO what about false response? send new joinrequest? now assumes that always OK

						// what gets keepAlive time TODO how to store this?
						@SuppressWarnings("unused")
						Expires keepAliveTimer = joinResponse.getExpires();

						// Sends join request (with S flag set) to all neighbors from join response (sometimes there's
						// no
						// neighbors in response) TODO maybe add a flag that will request neighbor/routing table from
						// neighbors
						/* Probably we can make use of this information as a list of 'backup peers'
						 *
						 * NeighborTable neighborsTable = joinResponse.getNeighborTable();
						if (neighborsTable != null) {
							Vector<PeerInfo> neighbors = neighborsTable.getNeighbors();
							for (int i = 0; i < neighbors.size(); i++) {
								PeerInfo currentNeighbor = neighbors.get(i);
								RequestOptions requestOptions = new RequestOptions(false, false, false, false, false,
										true, false);
								JoinRequest joinRequestWithSFlag = new JoinRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1,
										true, false, (byte) 255, null, sharedManager.getPeerIDAsBytes(), false, false,
										requestOptions, sharedManager.getPeerInfo(true, true));
								// TODO maybe inform someone about that we are joined?
								transactionTable.createTransactionAndFill(joinRequestWithSFlag, transactionListener,
										currentNeighbor.getAddressInfos(), sharedManager.getPeerIDAsBytes(),
										currentNeighbor.getPeerID().getPeerIDBytes());
							}
						}*/

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

						if (logger.isDebugEnabled()) logger.debug("Client is now JOINED. UserInfo info resource will be published.");

						// publishes userinfo object
						publish(userInfo.getUnhashedID(), userInfo);

					} catch (NullPointerException e) {
						e.printStackTrace();
						// probably ignore because this will happen only if not valid joinresponse was received
					}
				}
			} // join transaction ended bad
			else {
				if (logger.isDebugEnabled()) logger.debug("Join transaction failed");
			}

		}

	};

	public Client() {
		super();
	}

	@Override
	public PeerInfo getClosestTo(byte[] id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger getDistance(String unhashedKey1, String unhashedKey2)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger getDistanceBetweenHashed(byte[] hashedKey1,
			byte[] hashedKey2) throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOverlayAlgorithm() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getRoutingTableToString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void join(byte[] overlayID, String overlayPeerAddress, int overlayPeerPort) {

        if (logger.isTraceEnabled()) {
		    logger.trace("Method invoked with " + "overlayAddress=" + overlayPeerAddress + ":" + overlayPeerPort + " in "
				+ state + " state.");
        }

		if (state == INITIAL_NODE_STATE) {
			// creates bootstrap request
			BootstrapRequest message = new BootstrapRequest(P2PPManager.CURRENT_PROTOCOL_VERSION, false, false, false,
					(byte) 255, null, GlobalConstants.isOverReliable, GlobalConstants.isEncrypted, sharedManager
							.getPeerInfo(false, true));

			// creates AddressInfo object and adds it to a vector of addresses of bootstrap server
			AddressInfo bootstrapAddress = new AddressInfo((byte) 0, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 0,
					AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.HOST_ADDRESS_TYPE, overlayPeerPort, ByteUtils
							.stringIPAddressToBytes(overlayPeerAddress));
			Vector<AddressInfo> bootstrapAddressInfos = new Vector<AddressInfo>();
			bootstrapAddressInfos.add(bootstrapAddress);

            if (logger.isTraceEnabled()) {
			    logger.trace("Joining begins. Bootstrap server at " + overlayPeerAddress + ":" + overlayPeerPort);
            }
			// creates transaction for bootstrap request TODO Decide who will be the listener of this transaction.
			transactionTable.createTransactionAndFill(message, bootstrapTransactionListener, bootstrapAddressInfos,
					new byte[4], new byte[4]);
			state = BOOTSTRAPPING_NODE_STATE;
			if (logger.isDebugEnabled()) logger.debug("state changed to BOOTSTRAPPING");
		} else {
			if (logger.isDebugEnabled()) {
                logger.debug("Can't connect to an overlay because " + "communication with one started already.");
            }
			callback.errorCallback(null, P2PPNodeCallback.BOOTSTRAP_ERROR_CODE);
		}
	}

	@Override
	public void leave() {
		// TODO Auto-generated method stub

	}

	@Override
	public void lookup(byte contentType, byte contentSubtype,
			byte[] resourceID, Owner owner) {
		// TODO Auto-generated method stub

	}

	@Override
	public int onRequest(Request request, boolean isReceived) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void performLeaveTasks() {
		// TODO Auto-generated method stub

	}

	@Override
	public void publish(byte[] unhashedID, ResourceObject resourceObject) {
		// TODO Auto-generated method stub

	}

	@Override
	public void query() {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(byte contentType, byte contentSubtype, byte[] resourceID, Owner owner) {
		// TODO Auto-generated method stub

	}

    public void sendMessage(byte[] unhashedID, String protocol, byte[] message) {}

	@Override
	protected boolean canConsume(Message message) {
		boolean canConsume = false;
		if (message instanceof P2PPMessage) {
			P2PPMessage p2ppMessage = (P2PPMessage) message;
			boolean[] messageType = p2ppMessage.getMessageType();
			// TODO more handling for indications

			if (Arrays.equals(messageType, P2PPMessage.RESPONSE_MESSAGE_TYPE)||
				Arrays.equals(messageType, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE)){
				canConsume = true;
			}
		}
		return canConsume;
	}

	@Override
	protected void consume(Message message) {
		try {
            if (logger.isDebugEnabled()) {
			    logger.debug("Client starts consumption of a message of " + message.getClass().getName()
					+ " type, state=" + state);
            }
			// TODO probably may be deleted, because transaction listeners are a better place to handle
			// responses (they have direct access to request and transaction)
			onResponseOrResponseACK((Response) message);
		}catch (NoSuchAlgorithmException e) {
			// TODO some kind of handling
		}
	}

	protected void onResponseOrResponseACK(Response response) throws NoSuchAlgorithmException {
		// Gets a transaction for given response.
		Transaction transaction = transactionTable.getTransaction(response);

		if (transaction == null) {
			// probably ignore, because generalAnalysis() should TODO change the info probably
			if (logger.isDebugEnabled()) {
                logger.debug("No transaction matching to " + response.getTransactionID().toString());
            }
		} else {
			// TODO probably remove transaction if reliable transport is used. or not (transaction table will do this)
			// transactionTable.removeMatchingTransaction(response);

			// passes received message to a transaction... it will know what to do
			transaction.setResponse(response);

			// reaction depending on current and new state
			switch (state) {
			case INITIAL_NODE_STATE:
				break;
			case BOOTSTRAPPING_NODE_STATE:
				// If received response for previously sent bootstrap request
				if (response instanceof BootstrapResponse) {
					onBootstrapResponse((BootstrapResponse) response);
				} else {
                    if (logger.isDebugEnabled()) {
					    logger.debug("Client received " + response.getClass().getName() + " in BOOTSTRAPPING "
							+ "state. It can't handle " + "it now.");
                    }
				}
				break;
			}
		}
	}

	private void onBootstrapResponse(BootstrapResponse response) throws NoSuchAlgorithmException {
		// saves information about bootstrap candidates
		bootstrapCandidates = response.getPeersInOverlay();

		// gets PeerInfo describing originator of BootstrapRequest (it's this peer.. BootstrapServer filled this object)
		PeerInfo originatorPeerInfo = response.getOriginatorPeerInfo();

		// saves new peerID
		byte[] ownNodeID = originatorPeerInfo.getPeerID().getPeerIDBytes();
		sharedManager.setPeerIDAsBytes(ownNodeID);
		PeerInfo ownPeerInfo = sharedManager.getPeerInfo(false, true);
		// saves p2p options from bootstrap response
		P2POptions options = response.getP2POptions();
		sharedManager.setOptions(options);

		if (logger.isDebugEnabled()) {
            logger.debug("Bootstrap response received. ClientID=" + ByteUtils.byteArrayToHexString(ownNodeID));
        }

		int numOfPeers = bootstrapCandidates.size();

		// If there are already peers in overlay (returned in received
		// response).
		if (numOfPeers > 0) {
			if (logger.isDebugEnabled()) {
                logger.debug("Peer received " + numOfPeers + " peers in bootstrap response.");
            }
			// changes state
			state = JOIN_REQUEST_SENT_NODE_STATE;

			if (logger.isDebugEnabled()) {
                logger.debug("Creating Join transaction and sending it to peer=" + bootstrapCandidates.get(0));
            }

			byte[] ownPeerID = sharedManager.getPeerIDAsBytes();

			// creates transaction for join request which will be send to
			JoinRequest joinRequest = new JoinRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, false, (byte) 255,
					null, ownPeerID, false, false, null, ownPeerInfo);
			transactionTable.createTransactionAndFill(joinRequest, joinTransactionListener, bootstrapCandidates.get(0)
					.getAddressInfos(), ownPeerID, bootstrapCandidates.get(0).getPeerID().getPeerIDBytes());
		}
		else {
            //no peers in overlay to join to
			if (logger.isDebugEnabled()) {
                logger.debug("Can't connect to an overlay because there is no peer for the client to join to.");
            }
			callback.errorCallback(null, P2PPNodeCallback.NAT_ERROR_CODE);
		}
	}

	@Override
	protected PeerInfo getNextHop(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PeerInfo getNextHopForResourceID(byte[] id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean isBestKnownPeer(Request request) {
		return true;
	}

	@Override
	public boolean isNodeAfterBootstrapping() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onForwardingRequest(Request request) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTimeSlot() {
		transactionTable.onTimeSlot(this);
	}

	@Override
	public void updateTables(PeerInfo peerInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateTables(Vector<PeerInfo> peerInfos) {
		// TODO Auto-generated method stub

	}

}
