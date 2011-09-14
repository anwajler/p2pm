package pl.edu.pjwstk.p2pp.superpeer;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.GlobalConstants;
import pl.edu.pjwstk.p2pp.entities.BootstrapServer;
import pl.edu.pjwstk.p2pp.entities.P2PPEntity;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.indications.LeaveIndication;
import pl.edu.pjwstk.p2pp.messages.requests.BootstrapRequest;
import pl.edu.pjwstk.p2pp.messages.requests.LookupPeerRequest;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.messages.responses.BootstrapResponse;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.superpeer.messages.indications.IndexPeerIndication;
import pl.edu.pjwstk.p2pp.transactions.Transaction;
import pl.edu.pjwstk.p2pp.transactions.TransactionListener;
import pl.edu.pjwstk.p2pp.transactions.TransactionTable;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.NodeTimers;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;
import pl.edu.pjwstk.util.ByteArrayWrapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SuperPeerBootstrapServer extends BootstrapServer {

    private final static Logger LOG = org.apache.log4j.Logger.getLogger(SuperPeerBootstrapServer.class);

    private Date lastPeerLookup = new Date();
    private final Hashtable<ByteArrayWrapper, SuperPeerEntry> superPeers = new Hashtable<ByteArrayWrapper, SuperPeerEntry>();

    private Collection<PeerInfo> getSuperPeers() {
        Collection<PeerInfo> superPeers = new ArrayList<PeerInfo>();
        for (Map.Entry<ByteArrayWrapper, SuperPeerEntry> entry : this.superPeers.entrySet()) {
            superPeers.add(entry.getValue().getPeerInfo());
        }
        return superPeers;
    }

    private boolean bootstrapAsSuperPeer(PeerInfo peerInfo) {
        return this.superPeers.isEmpty();
    }

    private void saveSuperPeer(final PeerInfo peerInfo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving super peer " + peerInfo);
        }
        ByteArrayWrapper wrappedID = new ByteArrayWrapper(peerInfo.getPeerID().getPeerIDBytes());
        SuperPeerEntry previousSuperPeerEntry = this.superPeers.put(wrappedID, new SuperPeerEntry(peerInfo));
        if (previousSuperPeerEntry != null) {
            LOG.warn("Bootstrapped a super peer that was bootstrapped already somewhere in the past.");
        }
    }

    private void removeSuperPeer(ByteArrayWrapper superPeerBAW) {
        // TODO Inform population ?
        this.superPeers.remove(superPeerBAW);
    }

    private SuperPeerEntry getLeastPopulatedSuperPeer() {
        SuperPeerEntry result = null;
        int minPopulation = Integer.MAX_VALUE;
        for (Map.Entry<ByteArrayWrapper, SuperPeerEntry> entry : this.superPeers.entrySet()) {
            SuperPeerEntry superPeerEntry = entry.getValue();
            int currentPopulation = superPeerEntry.getPopulationSize();
            if (currentPopulation <= minPopulation) {
                minPopulation = currentPopulation;
                result = superPeerEntry;
            }
        }
        return result;
    }

    private byte[] generatePeerID(byte[] unhashedID) {
        byte[] peerID = null;
        MessageDigest digest = null;
        try {
            P2POptions options = sharedManager.getOptions();
            switch (options.getHashAlgorithm()) {
                case P2PPUtils.SHA1_HASH_ALGORITHM:
                    digest = MessageDigest.getInstance("SHA-1");
                    peerID = digest.digest(unhashedID);
                    break;
                case P2PPUtils.SHA1_256_HASH_ALGORITHM:
                    digest = MessageDigest.getInstance("SHA256");
                    peerID = digest.digest(unhashedID);
                    break;
                case P2PPUtils.SHA1_512_HASH_ALGORITHM:
                    digest = MessageDigest.getInstance("SHA512");
                    peerID = digest.digest(unhashedID);
                    break;
                case P2PPUtils.MD4_HASH_ALGORITHM:
                    digest = MessageDigest.getInstance("MD4");
                    peerID = digest.digest(unhashedID);
                    break;
                case P2PPUtils.MD5_HASH_ALGORITHM:
                    digest = MessageDigest.getInstance("MD5");
                    peerID = digest.digest(unhashedID);
                    break;
                case P2PPUtils.NONE_HASH_ALGORITHM:
                    // TODO do something?
                    break;
            }
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return peerID;
    }

    @Override
    protected void consume(Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(message.getClass().getName() + " message received");
        }

        if (message instanceof Response) {

            Response response = (Response) message;
            Transaction transaction = transactionTable.getTransaction(response);
            if (transaction != null) {
                transaction.setResponse(response);
            }

        } else if (message instanceof BootstrapRequest) {

            BootstrapRequest request = (BootstrapRequest) message;

            PeerInfo requestingPeerInfo = request.getPeerInfo();
            UnhashedID unhashedID = requestingPeerInfo.getUnhashedID();

            byte[] peerID = generatePeerID(unhashedID.getUnhashedIDValue());
			requestingPeerInfo.setPeerID(new PeerID(peerID));

            //byte[] peerID = requestingPeerInfo.getPeerID().getPeerIDBytes();
            Vector<AddressInfo> addressInfos = requestingPeerInfo.getAddressInfos();
            AddressInfo address = addressInfos.get(0);
            address.setAddress(request.getSenderAddressAsBytes());

            byte[] ownPeerId = new byte[4];
            ownPeerId[3] = 1;

            /*if (isBoostrappedCandidate(requestingPeerInfo)) {

                if (LOG.isDebugEnabled()) {
                    StringBuilder strb = new StringBuilder("Node has already been bootstrapped unhashedID=");
                    strb.append(ByteUtils.byteArrayToHexString(requestingPeerInfo.getUnhashedID().getUnhashedIDValue()));
                    strb.append("; address=");
                    strb.append(message.getSenderAddress());
                    strb.append(":");
                    strb.append(message.getSenderPort());
                    strb.append(";  peerID=");
                    strb.append(ByteUtils.byteArrayToHexString(peerID));
                    LOG.debug(strb.toString());
                }

                BootstrapResponse response = request.createResponse(Response.RESPONSE_CODE_REQUEST_REJECTED_BITS_ARRAY,
                        sharedManager.getPeerInfo(false, false), sharedManager.getOptions(), peerID);

                transactionTable.createTransactionAndFill(response, transactionListener, requestingPeerInfo.getAddressInfos(), ownPeerId, peerID);

            } else {*/

                BootstrapResponse response = request.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, sharedManager.getPeerInfo(false, false),
                        sharedManager.getOptions(), peerID);

                PeerInfo superPeerInfo = null;
                boolean bootstrapAsSuperPeer = bootstrapAsSuperPeer(requestingPeerInfo);
                if (bootstrapAsSuperPeer) {
                    Collection<PeerInfo> superPeers = getSuperPeers();
                    if (!superPeers.isEmpty()) {
                        for (PeerInfo superPeer : superPeers) {
                            response.addPeerInfo(superPeer);
                        }
                    }
                    saveBootstrapCandidate(requestingPeerInfo);
                    saveSuperPeer(requestingPeerInfo);
                } else {
                    SuperPeerEntry superPeerEntry = getLeastPopulatedSuperPeer();
                    superPeerInfo = superPeerEntry.getPeerInfo();
                    response.addPeerInfo(superPeerInfo);
                    superPeerEntry.addPopulation(request.getPeerInfo());
                    saveBootstrapCandidate(requestingPeerInfo);
                }

                if (LOG.isDebugEnabled()) {
                    StringBuilder strb = new StringBuilder("SuperPeerBootstrapServer has bootstrapped a node unhashedID=");
                    strb.append(ByteUtils.byteArrayToHexString(requestingPeerInfo.getUnhashedID().getUnhashedIDValue()));
                    strb.append("; address=");
                    strb.append(message.getSenderAddress());
                    strb.append(":");
                    strb.append(message.getSenderPort());
                    strb.append(";  peerID=");
                    strb.append(ByteUtils.byteArrayToHexString(peerID));
                    LOG.debug(strb.toString());
                }

                transactionTable.createTransactionAndFill(response, transactionListener, requestingPeerInfo.getAddressInfos(), ownPeerId, peerID);

                /*if (!bootstrapAsSuperPeer && superPeerInfo != null) {
                    IndexPeerIndication ipr = new IndexPeerIndication(P2PPMessage.P2PP_PROTOCOL_VERSION_1, true, false, (byte) 255, null,
                            GlobalConstants.isOverReliable, false, requestingPeerInfo);
                    transactionTable.createTransactionAndFill(ipr, transactionListener, superPeerInfo.getAddressInfos(), ownPeerId, peerID);
                }*/

            //}

        } else if (message instanceof LeaveIndication) {

            PeerInfo leavingPeerInfo = ((LeaveIndication) message).getPeerInfo();

            for (Map.Entry<ByteArrayWrapper, SuperPeerEntry> entry : this.superPeers.entrySet()) {
                entry.getValue().removePopulation(leavingPeerInfo);
            }

        }
    }

    @Override
    protected PeerInfo getNextHop(Request request) {
        return null;
    }

    @Override
    protected PeerInfo getNextHopForResourceID(byte[] id) {
        return null;
    }

    @Override
    public boolean isNodeAfterBootstrapping() {
        return false;
    }

    /*@Override
	protected boolean isBestKnownPeer(Request request) {
        return request instanceof BootstrapRequest;
	}*/

    @Override
    protected void onForwardingRequest(Request request) {
    }

    @Override
	public void onTimeSlot() {
		transactionTable.onTimeSlot(this);

        // Suppose for a time they are always available
        /*Date nowDate = new Date();
        if ((nowDate.getTime() - this.lastPeerLookup.getTime())/1000 > NodeTimers.PEER_LOOKUP_BOOTSTRAP_TIMER_SECONDS) {

            PeerInfo ownPeerInfo = sharedManager.getPeerInfo(false, false);

            for (Map.Entry<ByteArrayWrapper, SuperPeerEntry> spEntry : this.superPeers.entrySet()) {

                final ByteArrayWrapper spBAW = spEntry.getKey();
                final PeerInfo spPI = spEntry.getValue().getPeerInfo();

                TransactionListener lookupPeerTransactionListener = new TransactionListener() {

                    public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType, Request request, Response response,
                                                 TransactionTable transactionTable, P2PPEntity node) {

                        if (LOG.isTraceEnabled()) {
                            LOG.trace("transactionID="+ByteUtils.byteArrayToHexString(transactionID)+"; transactionState="+transactionState);
                        }

                        if (transactionState == Transaction.TERMINATED_STATE) {

                            if (LOG.isTraceEnabled()) {
                                LOG.trace("Transaction terminated transactionID=" + ByteUtils.byteArrayToHexString(transactionID) +
                                        "; transactionType=" + TransactionType + "; transactionState=" + transactionState);
                            }

                        } else {

                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Removing unresponsive super peer from the list: " + spPI);
                            }

                            removeSuperPeer(spBAW);
                            removeBootrappedCandidate(spPI);

                        }

                    }

                };

                LookupPeerRequest request = new LookupPeerRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, false, false, (byte) 255, null,
                    sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null, ownPeerInfo);

                byte[] ownPeerId = new byte[] { 0, 0, 0, 1 };
                transactionTable.createTransactionAndFill(request, lookupPeerTransactionListener, spPI.getAddressInfos(), ownPeerId,
                        spPI.getPeerID().getPeerIDBytes());

            }

            this.lastPeerLookup = nowDate;
        }*/
	}

    @Override
    public void updateTables(PeerInfo peerInfo) {
    }

    @Override
    public void updateTables(Vector<PeerInfo> peerInfos) {
    }

    @Override
    protected void saveBootstrapCandidate(PeerInfo peerInfo) {
        ByteArrayWrapper wrappedID = new ByteArrayWrapper(peerInfo.getPeerID().getPeerIDBytes());
        if (this.bootstrapCandidates.containsKey(wrappedID)) {
            LOG.warn("Bootstrapped a peer that was already bootstrapped in the past");
        }
        this.bootstrapCandidates.put(wrappedID, peerInfo);
    }
}

class SuperPeerEntry {
    private PeerInfo peerInfo;
    private List<PeerInfo> population;

    public SuperPeerEntry(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
        this.population = new ArrayList<PeerInfo>();
    }

    public void addPopulation(PeerInfo pi) {
        if (!this.population.contains(pi)) {
            this.population.add(pi);
        }
    }

    public void removePopulation(PeerInfo pi) {
        this.population.remove(pi);
    }

    public PeerInfo getPeerInfo() {
        return this.peerInfo;
    }

    public List<PeerInfo> getPopulation() {
        return this.population;
    }

    public int getPopulationSize() {
        return this.population.size();
    }
}