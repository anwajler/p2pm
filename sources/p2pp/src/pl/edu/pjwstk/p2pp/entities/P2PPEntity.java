package pl.edu.pjwstk.p2pp.entities;

import java.util.Arrays;
import java.util.Vector;

import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.OutgoingMessagesListener;
import pl.edu.pjwstk.p2pp.debug.DebugFields;
import pl.edu.pjwstk.p2pp.debug.DebugInformation;
import pl.edu.pjwstk.p2pp.debug.processor.DebugWriter;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.writers.RawTextWriter;
import pl.edu.pjwstk.p2pp.messages.*;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.messages.responses.NextHopResponse;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.transactions.Transaction;
import pl.edu.pjwstk.p2pp.transactions.TransactionListener;
import pl.edu.pjwstk.p2pp.transactions.TransactionTable;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * <p>
 * Abstract superclass for all the entities that participate in P2PP communication.
 * </p>
 * <p>
 * An entity is capable of three main things:
 * <ul>
 * <li>receiving messages messages {@link #onReceive(pl.edu.pjwstk.p2pp.messages.Message)})</li>
 * <li>receiving commands from, for instance, command line ({@link #onCommand()} )</li>
 * <li>sending messages to entities listeners ({@link #fireOnSend(pl.edu.pjwstk.p2pp.messages.Message)})</li>
 * </ul>
 * </p>
 * <p>
 * {@link #canConsume(pl.edu.pjwstk.p2pp.messages.Message)} is invoked to check whether this entity can consume (will do something with received
 * message) the message, or not. This method has to be implemented in subclasses. For example, BootstrapServer may be
 * interested in BootstrapRequest messages, but not JoinRequests.
 * </p>
 * <p>
 * {@link #isBestKnownPeer(Request)} checks whether this entity can satisfy received request. It is up to subclasses to
 * decide whether the can satisfy given request or not.
 * </p>
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public abstract class P2PPEntity {

    private static Logger LOG = Logger.getLogger(P2PPEntity.class);

    /**
     * Entities listener used for sending messages.
     */
    protected OutgoingMessagesListener outgoingListener;

    /**
     * Manager of data shared between local entities.
     */
    protected EntitiesSharedDataManager sharedManager;

    /**
     * Transaction table for this entity.
     */
    protected TransactionTable transactionTable = new TransactionTable();

    /**
     * State of this entity.
     */
    protected int state;

    /**
     * General listener of transactions.
     */
    protected TransactionListener transactionListener = new TransactionListener() {

        public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType,
                                     Request request, Response response, TransactionTable transTable, P2PPEntity entity) {

            if (LOG.isTraceEnabled()) {
                LOG.trace("TODO transactionID=" + ByteUtils.byteArrayToHexString(transactionID) + "; transactionType=" + TransactionType +
                        "; transactionState=" + transactionState);
            }

            if (response instanceof NextHopResponse) {
                NextHopResponse nextHopResponse = (NextHopResponse) response;
                PeerInfo nextHop = nextHopResponse.getNextHopPeerInfo();

                if (request != null) request.decreaseTtl();

                transactionTable.createTransactionAndFill(request, transactionListener,
                        nextHop.getAddressInfos(), sharedManager.getPeerIDAsBytes(),
                        nextHop.getPeerID().getPeerIDBytes());
            }
        }

    };

    /**
     * Constructor of P2PPEntity.
     */
    public P2PPEntity() {}

    /**
     * Returns current state. Thread safe.
     *
     * @return
     */
    public int getState() {
        return state;
    }

    /**
     * @param state
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * Method that checks if this entity is capable of consuming given message. Received value depends only on type of
     * message and if it is acknowledgement. For instance, peer may consume join request, but not bootstrap request (it
     * may consume ACK for bootstrap request).
     *
     * @param message Received message.
     */
    protected abstract boolean canConsume(Message message);

    /**
     * <p>
     * Method invoked to inform this entity about received message. It checks whether this object can consume received
     * message (by using {@link #canConsume(pl.edu.pjwstk.p2pp.messages.Message)}). If it can, it is consumed.
     * </p>
     *
     * @param message Received message.
     * @return True if this entity was interested in this type of message and done something with it. False otherwise.
     */
    public final boolean onReceive(Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.getClass().getName() + " received a message of type " + message.getClass().getName());
        }

        boolean consumed = canConsume(message);
        if (consumed) {

            boolean mayBeConsumed = generalAnalysis((P2PPMessage) message);

            if (mayBeConsumed) {
                consume(message);
            }
        }
        return consumed;
    }

    /**
     * Analyses received message basing on 4.3 chapter of P2PP specification (draft 01). If message may be passed to
     * overlay layer (in other words, it will be consumed), true is returned. False otherwise.
     *
     * @param receivedMessage Message to be analysed.
     * @return True if message may be passed to overlay layer (may be consumed). False otherwise.
     */
    private boolean generalAnalysis(P2PPMessage receivedMessage) {
        boolean result;

        if (LOG.isTraceEnabled()) {
            LOG.trace("messageType=" + receivedMessage.getClass().getName() + ", entityState=" + state + ", sourceID="
                + ByteUtils.byteArrayToHexString(receivedMessage.getSourceID()) + ", responseID="
                + ByteUtils.byteArrayToHexString(receivedMessage.getResponseID()) + ", transactionID="
                + ByteUtils.byteArrayToHexString(receivedMessage.getTransactionID()) + ".");
        }

        try {

                if (receivedMessage.isOverReliable()) {
                    // analysis of a message received over reliable transport (specification 4.3, 7 steps).
                    result = analyzeOverReliable(receivedMessage, transactionTable);
                } else {
                    // analysis of a message received over unreliable transport (specification 4.3, 9 steps).
                    result = analyzeOverUnreliable(receivedMessage, transactionTable);
                }

        } catch (NullPointerException e) {
            if (LOG.isTraceEnabled()) LOG.trace("NullPointerException during general analysis of received message. Message is ignored.\n");
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    public final void writeDebugInformation(Message message, boolean received) {
        // Testing platform . Work in progress -->

        DebugInformation debugInfo = message.getDebugInformation();

        if (this instanceof Peer) {
            debugInfo.put(DebugFields.UNHASHED_ID, new String(sharedManager.getUnhashedID()));

            if (message instanceof TestEndMessage) {
                Peer p = (Peer) this;
                Byte routingAlgorithm = p.getRoutingAlgorithm();
                if (LOG.isTraceEnabled()) LOG.trace("--alg: " + routingAlgorithm);
                String[] routingTable = p.getRoutingTableToString();
                if (LOG.isTraceEnabled()) LOG.trace("---" + Arrays.asList(routingTable));
                if (null != routingTable) {
                    for (String record : routingTable) {
                        if (LOG.isTraceEnabled()) LOG.trace("----rt: " + record);
                    }
                }
            }

        } else {
            debugInfo.put(DebugFields.UNHASHED_ID, "");
        }

        debugInfo.put(DebugFields.SENT_OR_RECEIVED, (received) ? "received" : "sent");

        DebugWriter.writeDebugInformation(debugInfo);
        // <--
    }

    /**
     * Returns true if this entity is a node after bootstrapping process. This is necessary for
     * {@link #generalAnalysis(pl.edu.pjwstk.p2pp.messages.P2PPMessage)} to get proper PeerInfo objects.
     *
     * @return
     */
    public abstract boolean isNodeAfterBootstrapping();

    /**
     * Returns true if this entity is a node. False otherwise.
     *
     * @return
     */
    public boolean isNode() {
        return this instanceof Node;
    }

    public TransactionTable getTransactionTable() {
        return transactionTable;
    }

    public EntitiesSharedDataManager getSharedManager() {
        return sharedManager;
    }

    /**
     * Analyses a message received over unreliable transport. It is an implementation of a process described in chapter
     * 4.3 of P2PP specification (9 steps).
     *
     * @param p2ppMessage      Message to be analysed.
     * @param transactionTable Transaction table to be used in analysis.
     * @return
     */
    private boolean analyzeOverUnreliable(P2PPMessage p2ppMessage, TransactionTable transactionTable) {
        boolean result = false;

        // TODO this process is complex (to say the least) and should be checked...
        //
        // TODO repair found issues:
        // - sends two ACKs for a response (done)
        // -

        // step 1
        Transaction transaction = transactionTable.getTransaction(p2ppMessage);
        if (transaction != null) {

            if (LOG.isTraceEnabled()) {
                LOG.trace("Received message that matches transactionID=" + ByteUtils.byteArrayToHexString(transaction.getTransactionID()) +
                        "; isACK=" + p2ppMessage.isAcknowledgment());
            }
            result = true;

            if (p2ppMessage.isAcknowledgment()) {
                transaction.setReceivedAck((Acknowledgment) p2ppMessage);
            }

        } else {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Received message that doesn't match any transaction.");
            }
            short ttl = p2ppMessage.getTtl();
            // step 2
            if (ttl <= 0) {
                if (p2ppMessage instanceof Request) {
                    Request request = (Request) p2ppMessage;
                    Response response = request.createTTLHopsExceededResponse(sharedManager.getPeerInfo(true,
                            isNodeAfterBootstrapping()).getPeerID());
                    transactionTable.createTransaction(response, transactionListener, request.getPeerInfo()
                            .getAddressInfos(), sharedManager.getPeerIDAsBytes(), request.getSourceID());
                    // TODO is this all? shouldn't it be returning false?
                }
                // ignore else (step 2 says so).
            } else {
                // step 3
                if (p2ppMessage.isRecursive() && p2ppMessage instanceof Request) {
                    Request request = (Request) p2ppMessage;
                    if (isBestKnownPeer(request)) {
                        result = true;
                        // step 5
                        // end of step 5
                    } else {
                        // TODO add recursive stuff
                        // PeerInfo nextHop = getNextHop(nodeID);
                        // transactionTable.createTransactionAndFill(request, true, this, addressInfos);
                        // TODO proabably onForwardingRequest() callback
                    }
                }
                // step 4 (request using iterative manner)
                else if (!p2ppMessage.isRecursive() && p2ppMessage instanceof Request) {
                    Request request = (Request) p2ppMessage;
                    if (isBestKnownPeer(request)) {
                        result = true;
                        // step 5
                        // end of step 5
                    } else {

                        // received request will have next hop response
                        onForwardingRequest(request);
                    }
                }
                // TODO indications handling
                else if (!p2ppMessage.isRecursive() && p2ppMessage instanceof Indication) {
                    result = true;
                }
            }
        }
        // step 8
        updateTables(p2ppMessage.getPeerInfo());

        // step 9
        if (p2ppMessage instanceof Response) {
            Response response = (Response) p2ppMessage;

            if (transaction != null) {
                result = true;
            } else {
                // sends an ACK even though there's no matching transaction (spec says so)
                Acknowledgment ack = response.createACK(sharedManager.getPeerInfo(true, isNodeAfterBootstrapping())
                        .getPeerID().getPeerIDBytes());
                fireOnSend(ack);
            }
        }

        return result;
    }

    /**
     * <p>
     * Method invoked whenever request is being forwarded by P2PPEntity. This happens only when in peer mode, but
     * general analysis (i.e. {@link #generalAnalysis(pl.edu.pjwstk.p2pp.messages.P2PPMessage)}) has to have an access to this functionality.
     * </p>
     *
     * @param request
     */
    protected abstract void onForwardingRequest(Request request);

    /**
     * Updates routing and neighbour tables with given PeerInfo object.
     *
     * @param peerInfo
     */
    public abstract void updateTables(PeerInfo peerInfo);

    /**
     * Updates routing and neighbour tables with given vector of PeerInfo objects.
     *
     * @param peerInfos
     */
    public abstract void updateTables(Vector<PeerInfo> peerInfos);

    /**
     * <p>
     * Method checking whether this entity is best for satisfying given request. Has to implemented by classes
     * implementing P2P protocol (i.e. KademliaPeer, KademliaClient, ChordPeer etc.) and by servers (TODO probably they
     * ignore).
     * </p>
     * <p>
     * Returned true doesn't mean that, for instance, searched object is stored by this entity. Returned true means that
     * there will be response generated by this entity and local entity won't pass it to next hop.
     * </p>
     *
     * @param request Request for which the check will be done.
     * @return True if this entity can satisfy given request. False otherwise.
     */
    protected abstract boolean isBestKnownPeer(Request request);

    /**
     * TODO Analyses a message received over reliable transport. It is an implementation of a process described in
     * chapter 4.3 of P2PP specification (7 steps).
     *
     * @param p2ppMessage      Message to be analysed.
     * @param transactionTable Transaction table to be used in analysis.
     * @return True if message may be passed to overlay layer (may be consumed). False otherwise.
     */
    private boolean analyzeOverReliable(P2PPMessage p2ppMessage, TransactionTable transactionTable) {
        boolean result = false;

        Transaction transaction = transactionTable.getTransaction(p2ppMessage);

        if (LOG.isTraceEnabled()) {
            LOG.trace("analyzeOverRealiable transaction=" + transaction);
        }
        
        // step 1
        if (transaction != null) {

            if (LOG.isTraceEnabled()) {
                LOG.trace("Received message that matches transactionID=" + ByteUtils.byteArrayToHexString(transaction.getTransactionID()));
            }

            if (p2ppMessage instanceof Response && transaction.getType() == Transaction.REQUEST_TRANSACTION_TYPE) {
                //TODO step 7
                result = true;
            }

        } else {

            if (LOG.isTraceEnabled()) {
                LOG.trace("Received message that doesn't match any transaction.");
            }

            // step 2
            if (p2ppMessage.getTtl() <= 0) {

                if (LOG.isTraceEnabled()) LOG.trace("TTL = 0");
                Request request = (Request) p2ppMessage;
                Response response = request.createTTLHopsExceededResponse(sharedManager.getPeerInfo(true,
                        isNodeAfterBootstrapping()).getPeerID());
                transactionTable.createTransaction(response, transactionListener, request.getPeerInfo()
                        .getAddressInfos(), sharedManager.getPeerIDAsBytes(), request.getSourceID());

            } else {

                // step 3
                if (p2ppMessage.isRecursive() && p2ppMessage instanceof Request) {
                    boolean canSatisfy = isBestKnownPeer((Request) p2ppMessage);
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Request recursive canSatisfy=" + canSatisfy);
                    }
                    if (!canSatisfy) {
                        // what to do with that?
                        getNextHop((Request) p2ppMessage);
                    }
                } // step 4
                else if (!p2ppMessage.isRecursive() && p2ppMessage instanceof Request) {
                    Request request = (Request) p2ppMessage;
                    boolean canSatisfy = isBestKnownPeer(request);

                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Request non-recursive canSatisfy=" + canSatisfy);
                    }

                    if (canSatisfy) {
                        result = true;
                    } else {
                        // received request will have next hop response
                        onForwardingRequest(request);
                    }

                } /*else if (p2ppMessage instanceof Request) {
                    Request request = (Request) p2ppMessage;
                    if (isBestKnownPeer(request)) {
                        RequestOptions options = request.getRequestOptions();
                        if (options != null) {
                            // TODO check flags
                            if (options.getRFlag()) {
                                // TODO
                            }
                            if (options.getNFlag()) {
                                // TODO
                            }
                        }

                    }
                }*/
                else if (!p2ppMessage.isRecursive() && p2ppMessage instanceof Indication) {
                    // TODO indications handling
                    result = true;
                }

            }
        }

        // step 6
        updateTables(p2ppMessage.getPeerInfo());
        
        return result;
    }

    /**
     * <p>
     * Returns protocol-dependent next hop for given request. Null is returned if there's no PeerInfo that may be a next
     * hop. Next hop is a Peer that is not a local peer, not a request originator and is closer to something that
     * request refers to (closer to resource, when request is lookup type etc.).
     * </p>
     * <p>
     * Convenient method when request was received or resource is published. TODO is it true that it can't be used if
     * {@link #isBestKnownPeer(Request)} returned false.
     * </p>
     *
     * @param request Request for which a next hop will be determined.
     * @return Returns null if there's no next hop for given request.
     */
    protected abstract PeerInfo getNextHop(Request request);

    /**
     * Returns protocol-dependent next hop for given resource ID. Null is returned if there's no PeerInfo that may be a
     * next hop (next hop is a Peer that is not a local peer, not a request originator and is closer to given id).
     *
     * @param resourceID ID of resource
     * @return PeerInfo object describing a next hop. Null if there's no proper PeerInfo object in routing/neighbor
     *         tables (i.e. there's no PeerInfo object other than local one and given one. Moreover, there's no peer
     *         closer to given one. Therefore, null informs that local peer is best known peer for given ID).
     */
    protected abstract PeerInfo getNextHopForResourceID(byte[] resourceID);

    /**
     * Consumes received message. It is a place for subclasses to define the way they handle a message. It is invoked
     * only when a subclass returns true in {@link #canConsume(pl.edu.pjwstk.p2pp.messages.Message)}. It is invoked after
     * {@link #generalAnalysis(pl.edu.pjwstk.p2pp.messages.P2PPMessage)}.
     *
     * @param message Message to be consumed.
     */
    protected abstract void consume(Message message);

    /**
     * Method invoked when this entity has time for doing its things. TODO make not abstract and use super in
     * subclasses, so that transaction table may be onTimeSlotted here.
     */
    public abstract void onTimeSlot();

    /**
     * Sets outgoing listener for this entity. This listener will be used for sending messages.
     *
     * @param newListener Listener to be added.
     */
    public void setOutgoingListener(OutgoingMessagesListener newListener) {
        outgoingListener = newListener;
    }

    /**
     * Returns listener that is used for sending messages.
     *
     * @return
     */
    public OutgoingMessagesListener getOutgoingListener() {
        return outgoingListener;
    }

    /**
     * Informs outgoing messages listeners of a message to be send.
     */
    public void fireOnSend(Message message) {
		outgoingListener.onSend(message);
	}

	/**
     * Sets manager of data shared by all entities.
     *
     * @param sharedManager
     */
	public void setSharedDataManager(EntitiesSharedDataManager sharedManager) {
		this.sharedManager = sharedManager;
	}

    public void enableDebug() {
        byte[] unhashedID = this.sharedManager.getUnhashedID();
        String id = (unhashedID != null) ? new String(unhashedID) : "bootstrap";
        DebugWriter.setOption(DebugWriter.WRITER_SUBSYSTEM_SETTINGS, new Object[]{"logs/debug_" + id + ".log", RawTextWriter.OVERWRITE, 128});
        DebugWriter.makeInstance();
    }
}
