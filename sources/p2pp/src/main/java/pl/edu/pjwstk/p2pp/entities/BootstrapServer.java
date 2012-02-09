package pl.edu.pjwstk.p2pp.entities;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.BootstrapRequest;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.util.ByteArrayWrapper;

/**
 * Class representing bootstrap server as defined in P2PP specification (draft 01). It handles bootstrap requests and
 * TODO probably ack for bootstrap responses.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * @author Konrad Adamczyk conrad.adamczyk@gmail.com
 */
public abstract class BootstrapServer extends P2PPEntity {

    private static Logger LOG = Logger.getLogger(BootstrapServer.class);

    /**
     * Hashmap of bootstrap candidates. PeerInfo objects describing them are hold against their hashed IDs (nodeID).
     */
    protected final ConcurrentHashMap<ByteArrayWrapper, PeerInfo> bootstrapCandidates = new ConcurrentHashMap<ByteArrayWrapper, PeerInfo>();

    /**
     * Creates bootstrap server.
     */
    public BootstrapServer() {}

    @Override
    protected boolean isBestKnownPeer(Request request) {
        return request instanceof BootstrapRequest;
    }

    @Override
    protected boolean canConsume(Message message) {
        boolean canConsume = false;

        if (message instanceof P2PPMessage) {
            P2PPMessage p2ppMessage = (P2PPMessage) message;
            byte requestType = p2ppMessage.getRequestOrResponseType();
            boolean[] messageType = p2ppMessage.getMessageType();

            if (requestType == P2PPMessage.BOOTSTRAP_MESSAGE_TYPE
                    || requestType == P2PPMessage.LOOKUP_NEIGHBOUR_MESSAGE_TYPE) {
                // They also have to be requests...
                if (Arrays.equals(messageType, P2PPMessage.REQUEST_MESSAGE_TYPE)) {
                    canConsume = true;
                } // ... or acknowledgments to responseACKs
                else if (p2ppMessage.isAcknowledgment()
                        && Arrays.equals(messageType, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE)) {
                    canConsume = true;
                }
            } else if (requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE) {
                canConsume = true;
            }
        }
        return canConsume;
    }

    /**
     * Each of returned strings contains information about bootstrapped peers.
     *
     * @return
     */
    public String[] getDescriptionOfBootstrappedPeers() {
        ArrayList<String> descriptions = new ArrayList<String>(bootstrapCandidates.size());
        synchronized (bootstrapCandidates) {
            Set<ByteArrayWrapper> bootstrapCandidatesKeys = bootstrapCandidates.keySet();
            for (ByteArrayWrapper wrappedID : bootstrapCandidatesKeys) {
                PeerInfo currentBootstrappedNode = bootstrapCandidates.get(wrappedID);
                String currentDesc = ByteUtils.byteArrayToHexString(currentBootstrappedNode.getPeerID()
                        .getPeerIDBytes());
                descriptions.add(currentDesc);
            }
        }
        //return descriptions.toArray(new String[0]);
        return (String[]) descriptions.toArray();
    }

    protected boolean isBoostrappedCandidate(PeerInfo peerInfo) {
        ByteArrayWrapper wrappedID = new ByteArrayWrapper(peerInfo.getPeerID().getPeerIDBytes());
        return this.bootstrapCandidates.containsKey(wrappedID);
    }

    protected void removeBootrappedCandidate(PeerInfo peerInfo) {
        ByteArrayWrapper wrappedID = new ByteArrayWrapper(peerInfo.getPeerID().getPeerIDBytes());
        this.bootstrapCandidates.remove(wrappedID);
    }

    /**
     * Adds PeerInfo object describing bootstrap candidate.
     *
     * @param peerInfo
     */
    protected void saveBootstrapCandidate(PeerInfo peerInfo) {
        ByteArrayWrapper wrappedID = new ByteArrayWrapper(peerInfo.getPeerID().getPeerIDBytes());
        if (this.bootstrapCandidates.containsKey(wrappedID)) {
            LOG.warn("Tried to bootstrap a peer that was already bootstrapped");
        } else {
            this.bootstrapCandidates.put(wrappedID, peerInfo);
        }
    }

    /**
     * Returns list of bootstrap candidates. If there's less candidates than wanted, vector contains all known
     * candidates.
     *
     * @param numberOfCandidates  How many candidates should be returned.
     * @param bootstrappingPeerID Hashed ID of peer that wants to be bootstrapped and know about bootstrap candidates.
     * @return bootstrap candidates
     */
    protected List<PeerInfo> getBootstrapCandidates(int numberOfCandidates, byte[] bootstrappingPeerID) {
        List<PeerInfo> candidates = new ArrayList<PeerInfo>();
        int i = 0;
        for (Map.Entry<ByteArrayWrapper, PeerInfo> bcEntry : this.bootstrapCandidates.entrySet()) {
            // prevents from adding to many candidates
            if (i == numberOfCandidates) break;
            // prevents from returning PeerInfo for peer that is the same as bootstrapping peer
            if (!Arrays.equals(bcEntry.getKey().getByteArray(), bootstrappingPeerID)) {
                System.err.println(bcEntry.getValue());
                candidates.add(bcEntry.getValue());
                i++;
            }
        }

        return candidates;
    }

    protected List<PeerInfo> getBootstrapCandidates(byte[] bootstrappingPeerID) {
        return getBootstrapCandidates(bootstrapCandidates.size(), bootstrappingPeerID);
    }
}
