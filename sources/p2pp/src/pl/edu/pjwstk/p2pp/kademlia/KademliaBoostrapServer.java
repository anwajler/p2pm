package pl.edu.pjwstk.p2pp.kademlia;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.GlobalConstants;
import pl.edu.pjwstk.p2pp.entities.BootstrapServer;
import pl.edu.pjwstk.p2pp.entities.P2PPEntity;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.BootstrapRequest;
import pl.edu.pjwstk.p2pp.messages.requests.LookupPeerRequest;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.messages.responses.BootstrapResponse;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;
import pl.edu.pjwstk.p2pp.transactions.Transaction;
import pl.edu.pjwstk.p2pp.transactions.TransactionListener;
import pl.edu.pjwstk.p2pp.transactions.TransactionTable;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.NodeTimers;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;
import pl.edu.pjwstk.util.ByteArrayWrapper;

/**
 * Bootstrap server (as defined in P2PP specification [draft 01]) using Kademlia protocol. Bootstrap server ignores ACKs
 * for BootstrapResponses at the moment.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * @author Konrad Adamczyk conrad.adamczyk@gmail.com
 *
 */
public class KademliaBoostrapServer extends BootstrapServer {

	private final static Logger LOG = Logger.getLogger(KademliaBoostrapServer.class);

    private final static int CANDIDATES_TO_RETURN = 20;

    private Date lastPeerLookup = new Date();

	/**
	 * Creates bootstrap server that uses Kademlia protocol.
	 */
	public KademliaBoostrapServer() {

	}

	@Override
	protected void consume(Message message) {

		if (LOG.isDebugEnabled()) LOG.debug(message.getClass().getName() + " message received.");

        if (message instanceof Response) {

            Response response = (Response) message;
            Transaction transaction = transactionTable.getTransaction(response);
            if (transaction != null) {
                transaction.setResponse(response);
            }

        } else if (message instanceof BootstrapRequest) {
			BootstrapRequest request = (BootstrapRequest) message;

			// gets PeerInfo and UnhashedID of requesting peer
			PeerInfo requestingPeerInfo = request.getPeerInfo();
			UnhashedID unhashedID = requestingPeerInfo.getUnhashedID();

			// generates peerID for requesting peer (not sure if this is needed but AddressInfo object in response is
			// filled with sender's address)
			byte[] newPeerID = generatePeerID(unhashedID.getUnhashedIDValue());
			requestingPeerInfo.setPeerID(new PeerID(newPeerID));
			Vector<AddressInfo> addressInfos = requestingPeerInfo.getAddressInfos();
			AddressInfo address = addressInfos.get(0);
			address.setAddress(request.getSenderAddressAsBytes());

            if (LOG.isDebugEnabled()) {
			    LOG.debug("KademliaBootstrapServer has bootstrapped a node.\n\tNode info: unhashedID="
					+ new String(requestingPeerInfo.getUnhashedID().getUnhashedIDValue()) + "; address="
					+ message.getSenderAddress() + ":" + message.getSenderPort() + "; generated peerID="
					+ ByteUtils.byteArrayToHexString(newPeerID));
            }

			// creates response OK for received request
			BootstrapResponse response = request.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, sharedManager
					.getPeerInfo(false, false), sharedManager.getOptions(), newPeerID);

			// FIXME now number of candidates to return is hardcoded
			List<PeerInfo> candidates = getBootstrapCandidates(CANDIDATES_TO_RETURN, newPeerID);
			if (candidates != null) {
                for (PeerInfo candidateInfo : candidates) {
					response.addPeerInfo(candidateInfo);
				}
			}
			// saves requesting peer
			/* Paulina (17-12-09):
			 * Added checking, whether the requesting node was client or not because
			 * clients should not be returned as bootstrap candidates for other nodes.
			 * TODO: Not sure, whether at this stage node always knows, it will
			 *       have to join as client - needs consideration...
			 */
			if (request.isByPeer()) saveBootstrapCandidate(requestingPeerInfo);

			byte[] ownPeerId = new byte[4];
			ownPeerId[3] = 1;
			transactionTable.createTransactionAndFill(response, transactionListener, request.getPeerInfo()
					.getAddressInfos(), ownPeerId, newPeerID);

		}
	}

	/**
	 * Generates PeerID basing on unhashedID. TODO It is unique in this bootstrap server.
	 *
	 * @return
	 */
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
	public void onTimeSlot() {
		transactionTable.onTimeSlot(this);

        Date nowDate = new Date();
        if ((nowDate.getTime() - this.lastPeerLookup.getTime())/1000 > NodeTimers.PEER_LOOKUP_BOOTSTRAP_TIMER_SECONDS) {

            PeerInfo ownPeerInfo = sharedManager.getPeerInfo(false, false);

            List<PeerInfo> bcsToCheck = getBootstrapCandidates(2*CANDIDATES_TO_RETURN, new byte[0]);

            for (final PeerInfo bcPI : bcsToCheck) {

                final ByteArrayWrapper bcBAW = new ByteArrayWrapper(bcPI.getPeerID().getPeerIDBytes());

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
                                LOG.debug("Removing unresponsive peer from bootstrap candidates list: " + bcPI);
                            }

                            bootstrapCandidates.remove(bcBAW);

                        }

                    }

                };

                LookupPeerRequest request = new LookupPeerRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, false, false, (byte) 255, null,
                    sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null, ownPeerInfo);

                byte[] ownPeerId = new byte[] { 0, 0, 0, 1 };
                transactionTable.createTransactionAndFill(request, lookupPeerTransactionListener, bcPI.getAddressInfos(), ownPeerId,
                        bcPI.getPeerID().getPeerIDBytes());

            }

            this.lastPeerLookup = nowDate;
        }
	}

	@Override
	protected PeerInfo getNextHopForResourceID(byte[] id) {
		return null;
	}

	@Override
	public boolean isNodeAfterBootstrapping() {
		return false;
	}

	@Override
	public void updateTables(PeerInfo peerInfo) {}

	@Override
	public void updateTables(Vector<PeerInfo> peerInfos) {}

	@Override
	protected PeerInfo getNextHop(Request request) {
		return null;
	}

	@Override
	protected void onForwardingRequest(Request request) {}
}