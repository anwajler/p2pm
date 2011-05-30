package pl.edu.pjwstk.p2pp.socialcircle;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.entities.BootstrapServer;
import pl.edu.pjwstk.p2pp.kademlia.KademliaBoostrapServer;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.requests.BootstrapRequest;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.messages.responses.BootstrapResponse;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.socialcircle.messages.requests.SocialLookupNeighbourRequest;
import pl.edu.pjwstk.p2pp.socialcircle.messages.responses.SocialLookupNeighbourResponse;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

public class SocialCircleBootstrapServer extends BootstrapServer {

    private static Logger logger = org.apache.log4j.Logger.getLogger(KademliaBoostrapServer.class);

    @Override
    protected void consume(Message message) {

        if (logger.isDebugEnabled()) {
            logger.debug(message.getClass().getName() + " message received.");
        }

        if (message instanceof BootstrapRequest) {
            BootstrapRequest request = (BootstrapRequest) message;

            PeerInfo requestingPeerInfo = request.getPeerInfo();
            byte[] PeerID = requestingPeerInfo.getPeerID().getPeerIDBytes();
            Vector<AddressInfo> addressInfos = requestingPeerInfo.getAddressInfos();
            AddressInfo address = (AddressInfo) addressInfos.get(0);
            address.setAddress(request.getSenderAddressAsBytes());

            logger.debug("SocialCircleBootstrapServer has bootstrapped a node.\n\tNode info: unhashedID="
                    + new String(requestingPeerInfo.getUnhashedID().getUnhashedIDValue()) + "; address="
                    + message.getSenderAddress() + ":" + message.getSenderPort() + ";  peerID="
                    + ByteUtils.byteArrayToHexString(PeerID));

            BootstrapResponse response = request.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, sharedManager.getPeerInfo(false, false),
                    sharedManager.getOptions(), PeerID);

            List<PeerInfo> candidates = getBootstrapCandidates(1, PeerID);
			if (candidates != null) {
				int size = candidates.size();
				for (int i = 0; i < size; i++) {
					PeerInfo candidateInfo = (PeerInfo) candidates.get(i);
					response.addPeerInfo(candidateInfo);
				}
			}

            if (request.isByPeer()) {
                saveBootstrapCandidate(requestingPeerInfo);
            }

            byte[] ownPeerId = new byte[4];
            ownPeerId[3] = 1;
            transactionTable.createTransactionAndFill(response, transactionListener, request.getPeerInfo().getAddressInfos(), ownPeerId, PeerID);

        } else if (message instanceof SocialLookupNeighbourRequest) {

            SocialLookupNeighbourRequest request = (SocialLookupNeighbourRequest) message;
            PeerInfo requestingPeerInfo = request.getPeerInfo();
            byte[] PeerID = requestingPeerInfo.getPeerID().getPeerIDBytes();

            PeerInfo requestedNeighbour = request.getPhysicalNeighbour();
            PeerInfo foundNeighbour = requestedNeighbour;
            
            List<PeerInfo> bootstrapCandidates = getBootstrapCandidates(PeerID);
            for (PeerInfo bootstrapCandidate : bootstrapCandidates) {
                if (requestedNeighbour.getUnhashedID().equals(bootstrapCandidate.getUnhashedID())) {
                    foundNeighbour = bootstrapCandidate; break;
                }
            }
            
            SocialLookupNeighbourResponse response = request.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, sharedManager.getOptions(), 
                    sharedManager.getPeerInfo(false, false), foundNeighbour);
            
            byte[] ownPeerId = new byte[4]; ownPeerId[3] = 1;
            transactionTable.createTransactionAndFill(response, transactionListener, request.getPeerInfo().getAddressInfos(), ownPeerId, PeerID);

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
    public boolean isNodeAfterBootstrapping() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected boolean isBestKnownPeer(Request request) {
        if (request instanceof BootstrapRequest ||
                request instanceof SocialLookupNeighbourRequest) {
            return true;
        } else {
            return false;
        }
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
