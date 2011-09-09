package pl.edu.pjwstk.p2pp.socialcircle;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import javax.xml.parsers.*;

import org.apache.log4j.Logger;
import org.w3c.dom.*;

import pl.edu.pjwstk.p2pp.ErrorInterface;
import pl.edu.pjwstk.p2pp.GlobalConstants;
import pl.edu.pjwstk.p2pp.P2PPManager;
import pl.edu.pjwstk.p2pp.P2PPNodeCallback;
import pl.edu.pjwstk.p2pp.entities.P2PPEntity;
import pl.edu.pjwstk.p2pp.entities.Peer;
import pl.edu.pjwstk.p2pp.kademlia.KademliaBoostrapServer;
import pl.edu.pjwstk.p2pp.messages.Acknowledgment;
import pl.edu.pjwstk.p2pp.messages.Indication;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.BootstrapRequest;
import pl.edu.pjwstk.p2pp.messages.requests.LookupObjectRequest;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.messages.responses.BootstrapResponse;
import pl.edu.pjwstk.p2pp.messages.responses.ExchangeTableResponse;
import pl.edu.pjwstk.p2pp.messages.responses.JoinResponse;
import pl.edu.pjwstk.p2pp.messages.responses.LookupObjectResponse;
import pl.edu.pjwstk.p2pp.messages.responses.NextHopResponse;
import pl.edu.pjwstk.p2pp.messages.responses.PublishObjectResponse;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.messages.responses.TransferResponse;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.socialcircle.messages.requests.SocialCircleSetUpRequest;
import pl.edu.pjwstk.p2pp.socialcircle.messages.requests.SocialLookupNeighbourRequest;
import pl.edu.pjwstk.p2pp.socialcircle.messages.responses.SocialCircleSetUpResponse;
import pl.edu.pjwstk.p2pp.socialcircle.messages.responses.SocialLookupNeighbourResponse;
import pl.edu.pjwstk.p2pp.transactions.Transaction;
import pl.edu.pjwstk.p2pp.transactions.TransactionListener;
import pl.edu.pjwstk.p2pp.transactions.TransactionTable;
import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;



public class SocialCirclePeer extends Peer {

    private static Logger logger = org.apache.log4j.Logger.getLogger(KademliaBoostrapServer.class);

    public static final int DEFAULT_EXPIRES_SECONDS = 120;

    public static final int SEARCHING_NEIGHBOURS_STATE = 10;
    public static final int SETTING_UP_STATE = 11;

    private SocialCirclePhysicalNeighborTable physicalNeighbourTable;


    public SocialCirclePeer() {
        routingTable = new SocialCircleRoutingTable();
        neighborTable = new SocialCircleNeighborTable();
        this.physicalNeighbourTable = new SocialCirclePhysicalNeighborTable();

        resourceManager.setRoutingTable(routingTable);
        resourceManager.setNeighborTable(neighborTable);
    }

    @Override
    public void parseSocialFile(String unhashedID, String socialFile) {
        Vector<PeerInfo> neighbors = new Vector<PeerInfo>();

        try {
            // loading file
            File file = new File(socialFile);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            if (logger.isDebugEnabled()) logger.debug("Root element " + doc.getDocumentElement().getNodeName());

            // edges overview
            NodeList edgeLst = doc.getElementsByTagName("edge");
            if (logger.isDebugEnabled()) logger.debug("Information of all edges");

            for (int s = 0; s < edgeLst.getLength(); s++) {

                Node fstEdge = edgeLst.item(s);

                if (fstEdge.getNodeType() == Node.ELEMENT_NODE) {

                    Element fstElmnt = (Element) fstEdge;

                    //String id = fstElmnt.getAttribute("id");
                    //      System.out.println("edge Id: "+id);

                    String source = fstElmnt.getAttribute("source");
                    //      System.out.println("source: "+source);

                    String target = fstElmnt.getAttribute("target");
                    //      System.out.println("target: "+target);

                    // finding neighbors/friends
                    if (source.equals(unhashedID)) {

                        byte[] peerIDBytes = null;
                        byte[] targetBytes = target.getBytes();
                        try {
                            peerIDBytes = P2PPUtils.hash(targetBytes, sharedManager.getOptions().getHashAlgorithm());
                        } catch (NoSuchAlgorithmException e) {
                            logger.error("Error while generating hashed id from target " + new String(targetBytes), e);
                        }

                        PeerID pid = new PeerID(peerIDBytes);
                        Uptime uptime = new Uptime(0);
                        AddressInfo neighborAddress = new AddressInfo((byte) 0, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 0,
                                AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.HOST_ADDRESS_TYPE, 0, ByteUtils.stringIPAddressToBytes("0.0.0.0"));
                        UnhashedID uhid = new UnhashedID(target.getBytes());
                        PeerInfo peerinfo = new PeerInfo(pid, uptime, neighborAddress, uhid);

                        neighbors.add(peerinfo);

                    }

                    if (target.equals(unhashedID)) {

                        byte[] peerIDBytes = null;
                        byte[] sourceBytes = source.getBytes();
                        try {
                            peerIDBytes = P2PPUtils.hash(sourceBytes, sharedManager.getOptions().getHashAlgorithm());
                        } catch (NoSuchAlgorithmException e) {
                            logger.error("Error while generating hashed id from source " + new String(sourceBytes), e);
                        }

                        PeerID pid = new PeerID(peerIDBytes);
                        Uptime uptime = new Uptime(0);
                        AddressInfo neighborAddress = new AddressInfo((byte) 0, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 0,
                                AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.HOST_ADDRESS_TYPE, 0, ByteUtils.stringIPAddressToBytes("0.0.0.0"));
                        UnhashedID uhid = new UnhashedID(source.getBytes());
                        PeerInfo peerinfo = new PeerInfo(pid, uptime, neighborAddress, uhid);

                        neighbors.add(peerinfo);
                    }
                }
            }

            if (logger.isDebugEnabled()) logger.debug("Neighbours:");
            for (PeerInfo neighbor : neighbors) {
                if (logger.isDebugEnabled()) logger.debug("neighbour: " + neighbor.getUnhashedID());
            }

            this.physicalNeighbourTable.setNeighbors(neighbors);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Byte getRoutingAlgorithm() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PeerInfo getClosestTo(byte[] id) {
        return routingTable.getClosestTo(id);
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

        Vector<SocialPath> paths = ((SocialCircleRoutingTable)this.routingTable).getPaths();
        String[] result = new String[ 3 + paths.size() ];
        result[0] = "Vset:";
        result[1] = new String(sharedManager.getUnhashedID());

        for(PeerInfo pi : this.neighborTable.getNeighbors()) {
            result[1] += " " + new String(pi.getUnhashedID().getUnhashedIDValue());
        }

        result[2] = "Paths:";
        int i = 3;
        for(SocialPath path : paths) {
            String endA  = new String( path.getEndpointA().getUnhashedID().getUnhashedIDValue() );
            String endB  = new String( path.getEndpointB().getUnhashedID().getUnhashedIDValue() );
            String nextA = new String( path.getNextA().getUnhashedID().getUnhashedIDValue() );
            String nextB = new String( path.getNextB().getUnhashedID().getUnhashedIDValue() );
            BigInteger pid = new BigInteger (1, path.getPathId().getPathIDValue());
            result[i++] = endA + " " + endB + " " + nextA + " "+ nextB + " " + pid;
        }
        return result;
    }


    @Override
    public void join(byte[] overlayID, String overlayPeerAddress, int overlayPeerPort) {
        // TODO probably should be done in a thread-safe way... now this method is invoked in another thread, and
        // creates the transaction... shouldn't work this way...

        if (logger.isTraceEnabled()) {
            logger.trace("Method invoked with overlayAddress=" + overlayPeerAddress + ":" + overlayPeerPort + " in " + state + " state.");
        }

        if (state == INITIAL_NODE_STATE) {
            byte[] ownNodeID = null;
            byte[] unhashedID = sharedManager.getUnhashedID();
            try {
                ownNodeID = P2PPUtils.hash(unhashedID, sharedManager.getOptions().getHashAlgorithm());
            } catch (NoSuchAlgorithmException e) {
                logger.error("Error while generating hashed id from " + new String(unhashedID), e);
            }

            sharedManager.setPeerIDAsBytes(ownNodeID);

            // creates bootstrap request
            BootstrapRequest message = new BootstrapRequest(P2PPManager.CURRENT_PROTOCOL_VERSION, false, true, false,
                    (byte) 255, null, GlobalConstants.isOverReliable, GlobalConstants.isEncrypted, sharedManager.getPeerInfo(true, true));

            // creates AddressInfo object and adds it to a vector of addresses of bootstrap server
            AddressInfo bootstrapAddress = new AddressInfo((byte) 0, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 0,
                    AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.HOST_ADDRESS_TYPE, overlayPeerPort, ByteUtils.stringIPAddressToBytes(overlayPeerAddress));
            Vector<AddressInfo> bootstrapAddressInfos = new Vector<AddressInfo>();
            bootstrapAddressInfos.add(bootstrapAddress);

            if (logger.isTraceEnabled()) logger.trace("Joining begins. Bootstrap server at " + overlayPeerAddress + ":" + overlayPeerPort);
            // creates transaction for bootstrap request TODO Decide who will be the listener of this transaction.
            transactionTable.createTransactionAndFill(message, bootstrapTransactionListener, bootstrapAddressInfos,
                    new byte[4], new byte[4]);
            state = BOOTSTRAPPING_NODE_STATE;
            if (logger.isDebugEnabled()) logger.debug("state changed to BOOTSTRAPPING");
        } else {
            if (logger.isDebugEnabled()) logger.debug("Can't connect to an overlay because communication with one started already.");
            callback.errorCallback(null, P2PPNodeCallback.BOOTSTRAP_ERROR_CODE);
        }

    }
    /**
     * TransactionListener for bootstrapping. TODO Probably might be moved to peer class.
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
                        ResourceID stunResourceID = new ResourceID(P2PPUtils.hash(P2PPUtils.STUN_SERVICE_ID, options.getHashAlgorithm()));
                        RLookup resourceLookup = new RLookup(P2PPUtils.STUN_CONTENT_TYPE, (byte) 0, stunResourceID,
                                null);
                        LookupObjectRequest lookupRequest = new LookupObjectRequest(
                                P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null,
                                sharedManager.getPeerIDAsBytes(), false, false, null, sharedManager.getPeerInfo(true,
                                true), resourceLookup);
                        PeerInfo chosenBootstrapCandidate = bootstrapCandidates.get(0);
                        Vector<AddressInfo> candidateAddresses = chosenBootstrapCandidate.getAddressInfos();
                        // TODO maybe inform someone about a fact that we are bootstrapped
                        transactionTable.createTransactionAndFill(lookupRequest, transactionListener,
                                candidateAddresses, sharedManager.getPeerIDAsBytes(), chosenBootstrapCandidate.getPeerID().getPeerIDBytes());
                    } catch (NoSuchAlgorithmException e) {
                        // TODO probably can't happen
                        logger.error("No algorithm!");
                    }

                } // if there are no more bootstrap candidates
                else {
                    if (logger.isDebugEnabled()) logger.debug("No response from any of bootstrap peers, so peer is now JOINED.");

                    BootstrapResponse bootstrapResponse = (BootstrapResponse) response;
                    PeerInfo originatorPeerInfo = bootstrapResponse.getOriginatorPeerInfo();
                    byte[] ownNodeID = originatorPeerInfo.getPeerID().getPeerIDBytes();

                    sharedManager.setPeerIDAsBytes(ownNodeID);
                    P2POptions options = bootstrapResponse.getP2POptions();
                    sharedManager.setOptions(options);

                    Owner owner = new Owner(ownNodeID);

                    resourceManager.setProperties(options.getHashAlgorithm(), options.getHashAlgorithmLength(), owner, DEFAULT_EXPIRES_SECONDS);
                    routingTable.setProperties(options.getBase(), options.getHashAlgorithmLength() * 8);
                    routingTable.setLocalPeerID(ownNodeID);

                    /*// creates UserInfoResource and resource objects for services
                    UserInfoResourceObject userInfo = new UserInfoResourceObject(new Owner(ownNodeID), new Expires(120), null,
                    new Certificate(false, new byte[] { 2, 2, 2, 2 }), ownPeerInfo .getAddressInfos());
                    userInfo.setValue(new ResourceObjectValue(ownPeerInfo.asBytes()));
                    publish(userInfo.getUnhashedID(), userInfo);

                    // gets resource objects for local services
                    List<ResourceObject> listOfServiceResources = resourceManager.getResourceObjectsForServices(ownPeerInfo);

                    logger.debug("Publishes information about " + listOfServiceResources.size() + " services.");

                    // publishes objects describing services
                    for (ResourceObject current : listOfServiceResources) {
                    publish(current.getUnhashedID(), current);
                    }*/
                }

                callback.errorCallback(null, P2PPNodeCallback.BOOTSTRAP_ERROR_CODE);
            } else {
            }
        }
    };

    private TransactionListener setUpTransactionListener = new TransactionListener() {

        public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType,
                                     Request request, Response response, TransactionTable transTable, P2PPEntity node) {

            if (Transaction.TERMINATED_STATE == transactionState) {
                if (state == SETTING_UP_STATE || state == JOINED_NODE_STATE) {
                    if (response instanceof SocialCircleSetUpResponse) {

                        SocialCircleSetUpResponse suresponse = (SocialCircleSetUpResponse) response;
                        PeerInfo originatorPeerInfo = suresponse.getPeerInfo();
                        byte[] originatorPeerID = originatorPeerInfo.getPeerID().getPeerIDBytes();
                        Vector<PeerInfo> vset = suresponse.getVSet();

                        PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
                        byte[] ownPeerID = sharedManager.getPeerIDAsBytes();


                        for (PeerInfo peerInfo : vset) {

                            byte[] peerIDBytes = peerInfo.getPeerID().getPeerIDBytes();
                            if (Arrays.equals(ownPeerID, peerIDBytes)) continue;
                            // to avoid sending setup_req for the second time to the same node
                            if (Arrays.equals(originatorPeerID, peerIDBytes)) continue;

                            if (logger.isDebugEnabled()) logger.debug("Adding " + peerInfo.getUnhashedID() + " to the routing table");
                            // TODO : check if this is physical neighbor?
                            BigInteger distance = SocialPath.calculateDistance(ownPeerInfo, peerInfo, true);
                            SocialPath neighbourPath = new SocialPath(new PathID(distance.toByteArray()), ownPeerInfo, peerInfo, ownPeerInfo, originatorPeerInfo);
                            ((SocialCircleRoutingTable) routingTable).addSocialPath(neighbourPath);

                            if (state == SETTING_UP_STATE) {
                                SocialCircleSetUpRequest scsurequest = new SocialCircleSetUpRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true,
                                        false, (byte) 255, null, ownPeerID, false, false, ownPeerInfo);

                                if (logger.isDebugEnabled()) logger.debug("Sending SetupRequest to: " + peerInfo.getUnhashedID());
                                transactionTable.createTransactionAndFill(scsurequest, setUpTransactionListener, peerInfo.getAddressInfos(), ownPeerID,
                                        peerIDBytes);
                            }

                        }

                        if (state == SETTING_UP_STATE) {
                            state = JOINED_NODE_STATE;
                        }

                    } else if (response instanceof NextHopResponse) {

                        NextHopResponse nextHopResponse = (NextHopResponse) response;
                        PeerInfo nextHop = nextHopResponse.getNextHopPeerInfo();

                        if (logger.isDebugEnabled()) logger.debug("Received next hop response for setup_req. nextHopID=" + nextHop.getPeerID());

                        transactionTable.createTransactionAndFill(request, setUpTransactionListener, nextHop
                                .getAddressInfos(), sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());

                    }
                }

            } else {
                logger.error("Error while terminating setup_req transaction (transactionState="+transactionState+")");
                ErrorInterface error = new ErrorInterface() {
                    private Object value;
                    public Object getValue(){return value;}
                    public void setValue(Object value){this.value = value;}
                };
                callback.errorCallback(error, -7);
            }

        }

    };

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
        int returnCode = 0;

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
            case SEARCHING_NEIGHBOURS_STATE:
            case SETTING_UP_STATE:
                break;
            case JOIN_REQUEST_SENT_NODE_STATE:
                // probably ignore
                break;
            case JOINED_NODE_STATE:
                if (request instanceof SocialCircleSetUpRequest) {
                    onSocialCircleSetUpRequest((SocialCircleSetUpRequest) request);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Peer received " + request.getClass().getName() + " in SETTING_UP state. It can't handle it now.");
                    }
                }
                break;
        }



        return returnCode;
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
    protected boolean canConsume(Message receivedMessage) {
        boolean canConsume = false;
        if (receivedMessage instanceof P2PPMessage) {
            P2PPMessage p2ppMessage = (P2PPMessage) receivedMessage;
            byte requestType = p2ppMessage.getRequestOrResponseType();
            boolean[] messageType = p2ppMessage.getMessageType();
            // TODO more handling for indications
            // handling for non-acknowledgment messages
            if (!p2ppMessage.isAcknowledgment()) {
                if (Arrays.equals(messageType, P2PPMessage.REQUEST_MESSAGE_TYPE)) {
                    if (requestType == P2PPMessage.JOIN_MESSAGE_TYPE || requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
                            || requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE
                            || requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE
                            || requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE
                            || requestType == P2PPMessage.QUERY_MESSAGE_TYPE
                            || requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE
                            || requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE
                            || requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE
                            || requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE
                            || requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE
                            || requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE
                            || requestType == P2PPMessage.CONNECT_MESSAGE_TYPE
                            || requestType == P2PPMessage.GET_DIAGNOSTICS_MESSAGE_TYPE
                            || requestType == P2PPMessage.LOOKUP_NEIGHBOUR_MESSAGE_TYPE
                            || requestType == P2PPMessage.SET_UP_MESSAGE_TYPE) {
                        canConsume = true;
                    }
                } else if (Arrays.equals(messageType, P2PPMessage.RESPONSE_MESSAGE_TYPE)
                        || Arrays.equals(messageType, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE)) {
                    if (requestType == P2PPMessage.ENROLL_MESSAGE_TYPE
                            || requestType == P2PPMessage.AUTHENTICATE_MESSAGE_TYPE
                            || requestType == P2PPMessage.BOOTSTRAP_MESSAGE_TYPE
                            || requestType == P2PPMessage.JOIN_MESSAGE_TYPE
                            || requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
                            || requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE
                            || requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE
                            || requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE
                            || requestType == P2PPMessage.QUERY_MESSAGE_TYPE
                            || requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE
                            || requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE
                            || requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE
                            || requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE
                            || requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE
                            || requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE
                            || requestType == P2PPMessage.CONNECT_MESSAGE_TYPE
                            || requestType == P2PPMessage.GET_DIAGNOSTICS_MESSAGE_TYPE
                            || requestType == P2PPMessage.LOOKUP_NEIGHBOUR_MESSAGE_TYPE
                            || requestType == P2PPMessage.SET_UP_MESSAGE_TYPE) {
                        canConsume = true;
                    }
                } // end of is response or responseACK
            } // end of if isn't Ack
            // if is ACK
            else {
                if (Arrays.equals(messageType, P2PPMessage.REQUEST_MESSAGE_TYPE)) {
                    if (// TODO what about ACK for bootstrap response? probably only responseACK is generated, so
                        // there's no need for consuming received ACK for bootstrap response
                            requestType == P2PPMessage.JOIN_MESSAGE_TYPE || requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
                                    || requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE
                                    || requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE
                                    || requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE
                                    || requestType == P2PPMessage.QUERY_MESSAGE_TYPE
                                    || requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE
                                    || requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE
                                    || requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE
                                    || requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE
                                    || requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE
                                    || requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE
                                    || requestType == P2PPMessage.CONNECT_MESSAGE_TYPE
                                    || requestType == P2PPMessage.GET_DIAGNOSTICS_MESSAGE_TYPE
                                    || requestType == P2PPMessage.LOOKUP_NEIGHBOUR_MESSAGE_TYPE
                                    || requestType == P2PPMessage.SET_UP_MESSAGE_TYPE) {
                        canConsume = true;
                    }
                } else if (Arrays.equals(messageType, P2PPMessage.RESPONSE_MESSAGE_TYPE)
                        || Arrays.equals(messageType, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE)) {
                    if (requestType == P2PPMessage.ENROLL_MESSAGE_TYPE
                            || requestType == P2PPMessage.AUTHENTICATE_MESSAGE_TYPE
                            || requestType == P2PPMessage.JOIN_MESSAGE_TYPE
                            || requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
                            || requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE
                            || requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE
                            || requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE
                            || requestType == P2PPMessage.QUERY_MESSAGE_TYPE
                            || requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE
                            || requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE
                            || requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE
                            || requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE
                            || requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE
                            || requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE
                            || requestType == P2PPMessage.CONNECT_MESSAGE_TYPE
                            || requestType == P2PPMessage.GET_DIAGNOSTICS_MESSAGE_TYPE
                            || requestType == P2PPMessage.LOOKUP_NEIGHBOUR_MESSAGE_TYPE
                            || requestType == P2PPMessage.SET_UP_MESSAGE_TYPE) {
                        canConsume = true;
                    }
                } // end of is response or responseACK
            } // end of is ACK
        } // end of if is P2PPMessage
        return canConsume;
    }

    @Override
    protected void consume(Message message) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("SocialCirclePeer starts consumption of a message of " + message.getClass().getName() + " type, state=" + state);
            }
            if (message instanceof P2PPMessage) {
                if (message instanceof Request) {
                    // probably ignores return code
                    @SuppressWarnings("unused")
					int returnCode = onRequest((Request) message, true);
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
            e.printStackTrace();
        }
    }

    @Override
    protected PeerInfo getNextHop(Request request) {

         if (request instanceof SocialCircleSetUpRequest) {
             return routingTable.getNextHop(request.getPeerInfo().getPeerID().getPeerIDBytes());
         }
         // TODO: more cases

        return null;
    }

    @Override
    protected PeerInfo getNextHopForResourceID(byte[] id) {
        return routingTable.getNextHop(id);
    }

    @Override
    protected boolean isBestKnownPeer(Request request) {
        boolean result = false;

        if (logger.isDebugEnabled()) {
            logger.debug("Received " + request.getClass().getSimpleName() + " is checked whether can be satisfied. sourceID="
                + ByteUtils.byteArrayToHexString(request.getSourceID()));
        }

        if (request instanceof SocialCircleSetUpRequest) {

            PeerInfo nextHop = getNextHop(request);
            if (nextHop == null) {
                result = true;
            }

            //    if (this.physicalNeighbourTable.hasNeighbor(originatorPeerInfo)) result = true;
        }

        return result;
    }

    @Override
    public boolean isNodeAfterBootstrapping() {
        return state != INITIAL_NODE_STATE && state != BOOTSTRAPPING_NODE_STATE;
    }

    @Override
    protected void onForwardingRequest(Request request) {
        // TODO Auto-generated method stub
        if (logger.isDebugEnabled()) logger.debug("TODO: FORWARDING");

        PeerInfo nextHop = getNextHop(request);
        PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);

        request.setSenderID(ownPeerInfo.getPeerID().getPeerIDBytes());
        Response nextHopResponse = request.createNextHopResponse(ownPeerInfo, nextHop);

        transactionTable.createTransactionAndFill(nextHopResponse, transactionListener, request.getPeerInfo().getAddressInfos(),
                sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());

        /*  // for debuging and testing only
        PeerInfo reqPeerInfo = request.getPeerInfo();
        BigInteger rBI = new BigInteger(1, reqPeerInfo.getPeerID().getPeerIDBytes());

        PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
        BigInteger ownBI = new BigInteger(1, ownPeerInfo.getPeerID().getPeerIDBytes());
        Vector<PeerInfo> vset = ((SocialCircleNeighborTable)neighborTable).getVset(ownPeerInfo);

        vset.add(reqPeerInfo);

        BigInteger[] bigIDs = new BigInteger[vset.size()];

        int n = 0;
        for(PeerInfo pi : vset) {
            bigIDs[n++] = new BigInteger(1, pi.getPeerID().getPeerIDBytes());
        }

        for(BigInteger bi : bigIDs) {
            System.out.println("Big:" + bi.toString());
        }

        BigInteger [] xors = new BigInteger[bigIDs.length];
        int k=0;
        for(BigInteger bi : bigIDs) {
            System.out.println("reqBI.xor.bi: "+ rBI.xor(bi));
            xors[k++] = rBI.xor(bi);
        }

        PeerInfo nextHop = getNextHop(request);
        BigInteger nextBI = new BigInteger(1, nextHop.getPeerID().getPeerIDBytes());
        System.out.println("nextHopBI"+nextBI);
        System.out.println("nH:"+nextHop.toString());
        System.out.println("nh:"+ ByteUtils.byteArrayToHexString(nextHop.getUnhashedID().getUnhashedIDValue()));

        BigInteger closest = xors[0];
        for(BigInteger bi : xors) {
            if (closest.compareTo(bi) < 0 )
                closest = bi;
        }

        System.out.println("closest: "+closest);
        */
    }

    @Override
    public void onTimeSlot() {

        transactionTable.onTimeSlot(this);

        resourceManager.onTimeSlot(this);

        switch (state) {
            /*
            case STUN_SERVER_COMMUNICATION_NODE_STATE: {
            // checks if server reflexive address was determined (TODO synchronize? because address is determined in
            // STUNService's thread [that's how it will look like when STUN implementation will be ready]).
            if (serverReflexiveAddress != null) {
            // adds determined address to shared manager
            sharedManager.addAddress(serverReflexiveAddress.getAddressAsString(), serverReflexiveAddress.getPort(),
            serverReflexiveAddress.getHt(), serverReflexiveAddress.getTt());

            // TODO is nullifying serverReflexiveAddress field needed?
            serverReflexiveAddress = null;

            // TODO in the future this should be changed to a state of determining relayed candidate
            state = JOIN_REQUEST_SENT_NODE_STATE;

            // gets a PeerInfo object describing remote Peer (closest to us) to which a Join request will be send
            byte[] ownPeerID = sharedManager.getPeerIDAsBytes();
            PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
            PeerInfo closestInfo = routingTable.getClosestTo(ownPeerID);

            // TODO what about a case when closestInfo is null?

            logger.debug("Creating Join transaction and sending it to peer=" + closestInfo);

            // creates transaction for join request which will be send to
            JoinRequest joinRequest = new JoinRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, true, false, (byte) 255,
            null, ownPeerID, false, false, null, ownPeerInfo);
            transactionTable.createTransactionAndFill(joinRequest, joinTransactionListener, closestInfo
            .getAddressInfos(), ownPeerID, closestInfo.getPeerID().getPeerIDBytes());

            }
            break;
            }
             */
            case JOINED_NODE_STATE: {
                break;
            }
        }
    }

    @Override
    public void updateTables(PeerInfo peerInfo) {
        // TODO Auto-generated method stub
    }

    @Override
    public void updateTables(Vector<PeerInfo> peerInfos) {
        // TODO Auto-generated method stub
    }

    private void onSocialCircleSetUpRequest(SocialCircleSetUpRequest request) {

        //PeerID senderPeerID = new PeerID(request.getSenderID());

        PeerInfo originatorPeerInfo = request.getPeerInfo();
        //PeerID originatorPeerID = originatorPeerInfo.getPeerID();
        PeerInfo ownPeerInfo = this.sharedManager.getPeerInfo(true, true);

        ((SocialCircleNeighborTable) this.neighborTable).addNeighbour(originatorPeerInfo, ownPeerInfo);

        //TODO : if neighbour table has originatorPeerInfo then it is our direct neighbor - no need to calculate next hop
        /*boolean isPhysical = this.physicalNeighbourTable.hasNeighbor(originatorPeerInfo);

        PeerInfo closestPeer = routingTable.getClosestTo(originatorPeerID.getPeerIDBytes());
        if (closestPeer == null) {
            closestPeer = ownPeerInfo;
        } */

        // TODO: correct - not necessarily physical?
        PathID pathID;
        if(this.physicalNeighbourTable.hasNeighbor(originatorPeerInfo)) {
            pathID = new PathID(BigInteger.ZERO.toByteArray());
        } else{
            pathID = new PathID(SocialPath.calculateDistance(ownPeerInfo, originatorPeerInfo, false).toByteArray());
        }
        SocialPath neighbourPath  = new SocialPath(pathID, ownPeerInfo, originatorPeerInfo, ownPeerInfo,
                originatorPeerInfo);
        ((SocialCircleRoutingTable) this.routingTable).addSocialPath(neighbourPath);

        /*System.out.println();
        String[] eds = ((SocialCircleRoutingTable) this.routingTable).getEntriesDescription();
        for (String ed : eds)  System.out.println("::"+ed);
        System.out.println("------------------------------------------------------------------------");*/

        Vector<PeerInfo> vset = ((SocialCircleNeighborTable) this.neighborTable).getVset(ownPeerInfo);

        //for (PeerInfo peerInfo : vset) System.out.println("::"+ peerInfo); System.out.println();

        SocialCircleSetUpResponse response = request.createResponse(this.sharedManager.getPeerInfo(true, true), vset);

        transactionTable.createTransaction(response, transactionListener, request.getPeerInfo().getAddressInfos(), sharedManager.getPeerIDAsBytes(),
                response.getSourceID());

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
            if (logger.isDebugEnabled()) {
                logger.debug("No transaction matching to " + response.getTransactionID().toString());
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
                        if (logger.isDebugEnabled()) {
                            logger.debug("Peer received " + response.getClass().getName() + " in BOOTSTRAPPING state. It can't handle it now.");
                        }
                    }
                    break;
                case SEARCHING_FOR_STUN_SERVER_NODE_STATE:
                    if (response instanceof LookupObjectResponse) {
//					onSTUNServerLookup((LookupObjectResponse) response);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Peer received " + response.getClass().getName() + " in SEARCHING_FOR_STUN_SERVER "
                                + "state. It can't handle it now.");
                        }
                    }
                    break;
                case STUN_SERVER_COMMUNICATION_NODE_STATE: {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Peer received " + response.getClass().getName() + " in STUN_SERVER_COMMUNICATION state. "
                            + "It can't handle it now.");
                    }
                }
                break;
                case JOIN_REQUEST_SENT_NODE_STATE:
                    if (response instanceof JoinResponse) {
//					onJoinResponse((JoinResponse) response);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Peer received " + response.getClass().getName() + " in JOIN_REQUEST_SENT state. "
                                + "It can't handle it now.");
                        }
                    }

                    break;
                case INFORMING_NEIGHBOURS_NODE_STATE: {
                    if (response instanceof PublishObjectResponse) {
//					onPublishObjectRespnoseBeforeJoined((PublishObjectResponse) response);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Peer received " + response.getClass().getName() + " in INFORMING_NEIGHBOURS state. "
                                + "It can't handle it now.");
                        }
                    }
                }
                break;
                case REQUESTING_NEIGHBOR_ROUTING_TABLES_NODE_STATE: {
                    if (response instanceof ExchangeTableResponse) {
//					onExchangeTableResponseBeforeJoined((ExchangeTableResponse) response);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Peer received " + response.getClass().getName()
                                + " in REQUESTING_NEIGHBOUR_ROUTING_TABLES state. " + "It can't handle it now.");
                        }
                    }
                }
                break;
                case JOINED_NODE_STATE:
                    if (logger.isDebugEnabled()) {
                        logger.debug("Peer received " + response.getClass().getName() + " in JOINED state.");
                    }
                    if (response instanceof PublishObjectResponse) {
//					onPublishObjectResponse((PublishObjectResponse) response);
                    } else if (response instanceof LookupObjectResponse) {
//					onLookupObjectResponse((LookupObjectResponse) response);
                    } else if (response instanceof TransferResponse) {
//					onTransferResponse((TransferResponse) response);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Peer received " + response.getClass().getName() + " in JOINED state. It can't handle it now.");
                        }
                    }
                    break;

                case SEARCHING_NEIGHBOURS_STATE:
                    if (logger.isDebugEnabled()) logger.debug("Peer received " + response.getClass().getName() + " in SEARCHING_NEIGHBOURS state.");
                    if (response instanceof SocialLookupNeighbourResponse) {
                        onSocialCircleLookupNeighbourResponse((SocialLookupNeighbourResponse) response);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Peer received " + response.getClass().getName() + " in SEARCHING_NEIGHBOURS state. It can't handle it now.");
                        }
                    }
                    break;
                case SETTING_UP_STATE:
                    if (response instanceof SocialLookupNeighbourResponse) {
                        onSocialCircleLookupNeighbourResponseSettingUp((SocialLookupNeighbourResponse) response);
                    }
                    break;
            }

        }

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
     * Analyzes bootstrap response. To be used in BOOTSTRAPPING_NODE_STATE. TODO probably should be moved to bootstrap
     * transaction listener
     *
     * @param response
     * @throws NoSuchAlgorithmException
     *             Thrown when somewhere in the code the hash method was used with non existing hash function.
     */
    private void onBootstrapResponse(BootstrapResponse response) throws NoSuchAlgorithmException {

        // saves information about bootstrap candidates
        bootstrapCandidates = response.getPeersInOverlay();

        // gets PeerInfo describing originator of BootstrapRequest (it's this peer.. BootstrapServer filled this object)
        PeerInfo originatorPeerInfo = response.getOriginatorPeerInfo();

        // saves new peerID
        byte[] ownNodeID = originatorPeerInfo.getPeerID().getPeerIDBytes();

        sharedManager.setPeerIDAsBytes(ownNodeID);

        PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
        // saves p2p options from bootstrap response
        P2POptions options = response.getP2POptions();
        sharedManager.setOptions(options);

        if (logger.isDebugEnabled()) logger.debug("Bootstrap response received. PeerID=" + ByteUtils.byteArrayToHexString(ownNodeID));

        // Creates Owner object that will be used for creating ResourceObjects.
        Owner owner = new Owner(ownNodeID);

        // informs objects about overlay options
        resourceManager.setProperties(options.getHashAlgorithm(), options.getHashAlgorithmLength(), owner,
                DEFAULT_EXPIRES_SECONDS);
        routingTable.setProperties(options.getBase(), options.getHashAlgorithmLength() * 8);
        routingTable.setLocalPeerID(ownNodeID);

        int numOfPeers = bootstrapCandidates.size();

        // If there are already peers in overlay (returned in received
        // response).
        if (numOfPeers > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Peer received " + numOfPeers + " peers in bootstrap response. Now is in SEARCHING_NEIGHBOURS_STATE.");
            }

            state = SEARCHING_NEIGHBOURS_STATE;

            //   BigInteger ownIdBI = new BigInteger(1, ownPeerInfo.getUnhashedID().asBytes());
            //   BigInteger distance = ownIdBI.xor(ownIdBI);
            SocialPath selfpath = new SocialPath(new PathID(BigInteger.ZERO.toByteArray()), ownPeerInfo, ownPeerInfo, ownPeerInfo, ownPeerInfo);
            ((SocialCircleRoutingTable) routingTable).addSocialPath(selfpath);

            Vector<PeerInfo> neighbours = this.physicalNeighbourTable.getNeighbors();
            Vector<AddressInfo> bootstrapAddressInfos = response.getBootstrapPeerInfo().getAddressInfos();

            for (PeerInfo neighbour : neighbours) {

                SocialLookupNeighbourRequest scnrequest = new SocialLookupNeighbourRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1,
                        false, true, true, (byte) 255, null, false, false, sharedManager.getPeerInfo(true, true), neighbour);

                if (logger.isDebugEnabled()) logger.debug("Looking up neighbour: " + neighbour.getUnhashedID());
                transactionTable.createTransactionAndFill(scnrequest, transactionListener, bootstrapAddressInfos,
                        sharedManager.getPeerIDAsBytes(), response.getPeerInfo().getPeerID().getPeerIDBytes());
            }

            // we can't reset NeighbourTable. We always know our friends (physical neighbors)
            // neighborTable.leaveReset();

        } // If there are no peers in overlay.
        else {
            SocialPath selfpath = new SocialPath(new PathID(BigInteger.ZERO.toByteArray()), ownPeerInfo, ownPeerInfo, ownPeerInfo, ownPeerInfo);
            ((SocialCircleRoutingTable) routingTable).addSocialPath(selfpath);

            if (logger.isDebugEnabled()) logger.debug("Peer has received no peers in bootstrap response. Now is in JOINED_NODE_STATE.");
            state = JOINED_NODE_STATE;

        }

    }

    /**
     * Updating tables + setup request.
     *
     * @param response
     * @throws NoSuchAlgorithmException
     */
    private void onSocialCircleLookupNeighbourResponse(SocialLookupNeighbourResponse response) throws NoSuchAlgorithmException {

        SocialLookupNeighbourResponse slnresponse = response;
        PeerInfo physicalNeighbour = slnresponse.getPhysicalNeighbour();
        if (physicalNeighbour != null) {
            Vector<AddressInfo> pnAddressInfos = physicalNeighbour.getAddressInfos();
            if (pnAddressInfos != null) {
                for (AddressInfo pnAddressInfo : pnAddressInfos) {

                    if (null == pnAddressInfo) {
                        continue;
                    }
                    String address = pnAddressInfo.getAddressAsString();
                    if ("".equals(address) || "0.0.0.0".equals(address)) {
                        continue;
                    }

                    PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);

                    try {
                        // Updating physical neighbour's PeerInfo in the physical neighbours' table with actual addresses
                        this.physicalNeighbourTable.replaceNeighbour(physicalNeighbour);

                        // Sending PeerInfo to the neighbour table
                        ((SocialCircleNeighborTable) neighborTable).addNeighbour(physicalNeighbour, ownPeerInfo);

                        // Setting path to the physical neighbour
                        BigInteger distance = SocialPath.calculateDistance(ownPeerInfo, physicalNeighbour, true);
                        SocialPath neighbourPath = new SocialPath(new PathID(distance.toByteArray()), ownPeerInfo, physicalNeighbour, ownPeerInfo, physicalNeighbour);
                        ((SocialCircleRoutingTable) routingTable).addSocialPath(neighbourPath);

                        this.state = SETTING_UP_STATE;
                        if (logger.isDebugEnabled()) logger.debug("state changed to SETTING_UP_STATE");

                        byte[] ownPeerID = sharedManager.getPeerIDAsBytes();
                        SocialCircleSetUpRequest scsurequest = new SocialCircleSetUpRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true,
                                false, (byte) 255, null, ownPeerID, false, false, ownPeerInfo);

                        transactionTable.createTransactionAndFill(scsurequest, setUpTransactionListener, physicalNeighbour.getAddressInfos(), ownPeerID,
                                physicalNeighbour.getPeerID().getPeerIDBytes());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * Only updating tables.
     *
     * @param response
     * @throws NoSuchAlgorithmException
     */
    private void onSocialCircleLookupNeighbourResponseSettingUp(SocialLookupNeighbourResponse response) throws NoSuchAlgorithmException {

        SocialLookupNeighbourResponse slnresponse = response;
        PeerInfo physicalNeighbour = slnresponse.getPhysicalNeighbour();
        if (physicalNeighbour != null) {
            Vector<AddressInfo> pnAddressInfos = physicalNeighbour.getAddressInfos();
            if (pnAddressInfos != null) {
                for (AddressInfo pnAddressInfo : pnAddressInfos) {

                    if (null == pnAddressInfo) {
                        continue;
                    }
                    String address = pnAddressInfo.getAddressAsString();
                    if ("".equals(address) || "0.0.0.0".equals(address)) {
                        continue;
                    }

                    // Updating physical neighbour's PeerInfo with actual addresses
                    this.physicalNeighbourTable.replaceNeighbour(physicalNeighbour);

                    PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
                    // Sending PeerInfo to neighbour table
                    ((SocialCircleNeighborTable) neighborTable).addNeighbour(physicalNeighbour, ownPeerInfo);

                    // Setting path to the physical neighbour
                    BigInteger distance = SocialPath.calculateDistance(ownPeerInfo, physicalNeighbour, true);
                    SocialPath neighbourPath = new SocialPath(new PathID(distance.toByteArray()), ownPeerInfo, physicalNeighbour, new PeerInfo(),
                            physicalNeighbour);
                    ((SocialCircleRoutingTable) routingTable).addSocialPath(neighbourPath);
                }
            }
        }

    }
}
