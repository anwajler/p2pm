package pl.edu.pjwstk.p2pp.kademlia;

import java.math.BigInteger;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.entities.P2PPEntity;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.LookupPeerRequest;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.transactions.Transaction;
import pl.edu.pjwstk.p2pp.transactions.TransactionListener;
import pl.edu.pjwstk.p2pp.transactions.TransactionTable;
import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Class representing K-bucket as defined in specification of Kademlia protocol. It is a list of PeerInfo objects. Non
 * thread safe.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 *
 */
public class KBucket {

    private static final Logger logger = org.apache.log4j.Logger.getLogger(KBucket.class);

	/**
	 * K being a limit of nodes in this bucket.
	 */
	private int k;

	/**
	 * List of information about node (at the moment I'm not sure what's the exact information)
	 */
	private Vector<PeerInfo> list;
    private Map<PeerInfo, Date> pings;
    private final Stack<PeerInfo> cache = new Stack<PeerInfo>();


	/**
	 * Constructor of KBucket with given limit (k).
	 *
	 * @param k
	 *            Limit of nodes in this bucket.
	 */
	public KBucket(int k) {
		this.k = k;
        this.list = new Vector<PeerInfo>(k);
        this.pings = new ConcurrentHashMap<PeerInfo,Date>(k);

	}

	/**
	 * Method that adds a PeerInfo object to this bucket. This process is like in Kademlia specification. If given PeerInfo is
	 * already in the this k-bucket, it moves it to the tail of the list. If the node is not already in this k-bucket
	 * and the bucket has fewer than k entries, then given PeerInfo is just inserted at the tail of the list. If the
	 * appropriate k-bucket is full, however, then the recipient places the new contact in a replacement cache of nodes eligible
     * to replace stale k-bucket entries.
	 *
	 * @param newPeerInfo
	 */
    public void addPeerInfo(PeerInfo newPeerInfo) {
        int listSize = list.size();

        byte[] nodeID = newPeerInfo.getPeerID().getPeerIDBytes();

		// checks if bucket isn't full
		if (listSize < k || this.list.contains(newPeerInfo)) {
            if (logger.isTraceEnabled()) logger.trace(listSize + " < " + k + " || this.list.contains(" + newPeerInfo + ")");
			// checks if this bucket already contains PeerInfo (PeerID is checked)
			int peerInfoIndex = getPeerInfoIndex(list, nodeID);
			if (peerInfoIndex > -1) {
				moveToTail(peerInfoIndex, list);
			} else {
                if (logger.isTraceEnabled()) logger.trace("Adding new entry to k-bucket " + newPeerInfo);
				list.add(newPeerInfo);
			}
            pings.put(newPeerInfo, new Date());

		} else {

            if (logger.isTraceEnabled()) logger.trace("K-bucket is full. Adding peerInfo=" + newPeerInfo + "to the replacement cache");

            if (this.cache.size() <= k) {
                if (this.cache.contains(newPeerInfo)) {
                    if (logger.isTraceEnabled()) logger.trace("PeerInfo already present in the cache. Removing so to push to the top");
                    this.cache.remove(newPeerInfo);
                }
                this.cache.push(newPeerInfo);
            } else {
                if (logger.isTraceEnabled()) logger.trace("K-bucket is full. Removing earliest added entry and pushing the new one");
                this.cache.remove(0);
                this.cache.push(newPeerInfo);
            }

        }
    }

    /**
	 * Method that adds a PeerInfo object to this bucket. This process is like in Kademlia specification. If given PeerInfo is
	 * already in the this k-bucket, it moves it to the tail of the list. If the node is not already in this k-bucket
	 * and the bucket has fewer than k entries, then given PeerInfo is just inserted at the tail of the list. If the
	 * appropriate k-bucket is full, however, then the recipient pings the k-bucket’s least-recently seen node to decide
	 * what to do. If the least-recently seen node fails to respond, it is evicted from the k-bucket and the new sender
	 * inserted at the tail. Otherwise, if the least-recently seen node responds, it is moved to the tail of the list,
	 * and the new sender’s contact is discarded.
	 *
	 * @param newPeerInfo
	 */
	public void addPeerInfo(TransactionTable transactionTable, PeerInfo localPeerInfo, final PeerInfo newPeerInfo) {
		int listSize = list.size();

		// checks if bucket isn't full
		if (listSize < k) {
			// checks if this bucket already contains PeerInfo (PeerID is checked)
			byte[] nodeID = newPeerInfo.getPeerID().getPeerIDBytes();
			int peerInfoIndex = getPeerInfoIndex(list, nodeID);
			if (peerInfoIndex > -1) {
				moveToTail(peerInfoIndex, list);
			} else {
                if (logger.isTraceEnabled()) logger.trace("Adding new entry to k-bucket " + newPeerInfo);
				list.add(newPeerInfo);
			}
            pings.put(newPeerInfo, new Date());

		} else {

            final PeerInfo leastSeen = this.list.get(0);

            TransactionListener lookupPeerTransactionListener = new TransactionListener() {

                public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType, Request request, Response response,
                                             TransactionTable transactionTable, P2PPEntity node) {

                    if (logger.isTraceEnabled()) {
                        logger.trace("transactionID=" + ByteUtils.byteArrayToHexString(transactionID) + "; transactionState=" + transactionState);
                    }

                    if (transactionState == Transaction.TERMINATED_STATE) {

                        if (logger.isTraceEnabled()) {
                            logger.trace("Peer PONGs back " + leastSeen);
                        }

                        list.add(leastSeen); // moveToTail ?

                    } else {

                        if (logger.isTraceEnabled()) {
                            logger.trace("Removing unresponsive peer " + leastSeen + " from the bucket and adding new one " + newPeerInfo);
                        }

                        list.remove(leastSeen);
                        list.add(newPeerInfo);

                    }

                }

            };

            byte[] localPeerIDBytes = localPeerInfo.getPeerID().getPeerIDBytes();

            if (logger.isTraceEnabled()) logger.trace("PINGing peer " + leastSeen);
            LookupPeerRequest request = new LookupPeerRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null,
                    localPeerIDBytes, false, false, null, localPeerInfo);

            transactionTable.createTransactionAndFill(request, lookupPeerTransactionListener, leastSeen.getAddressInfos(), localPeerIDBytes,
                    leastSeen.getPeerID().getPeerIDBytes());

        }
	}

    public void removePeerInfo(PeerInfo peerInfo) {

        if (logger.isTraceEnabled()) logger.trace("Removing peerInfo=" + peerInfo + " from the k-bucket");

        this.list.remove(peerInfo);
        this.pings.remove(peerInfo);

        synchronized (this.cache) {
            if (!this.cache.isEmpty()) {
                PeerInfo cachedPI = this.cache.pop();
                if (logger.isTraceEnabled()) logger.trace("Replacing evicted node with most recently cached one " + cachedPI);
                this.list.add(cachedPI);
                this.pings.put(cachedPI, new Date());
            }
        }
    }

	/**
	 * Moves given index to a tail of Vector (becomes an element with the highest index).
	 */
	public static void moveToTail(int index, Vector<PeerInfo> vector) {
		PeerInfo object = vector.remove(index);
		vector.add(object);
        if (logger.isTraceEnabled()) logger.trace("Moved peerInfo=" + object + " at index=" + index + " to the list's tail");
	}

	/**
	 * Checks whether given vector contains a PeerInfo object (vector has to consist of only PeerInfo objects) with
	 * given nodeID.
	 *
	 * @return -1 if there's no such a PeerInfo. If there's similar PeerInfo, its index is returned.
	 */
	private static int getPeerInfoIndex(Vector<PeerInfo> vector, byte[] nodeID) {
		int vectorSize = vector.size();
		// iterates through the vector
		for (int i = 0; i < vectorSize; i++) {
			PeerInfo currentPeerInfo = vector.get(i);
			byte[] currentPeerID = currentPeerInfo.getPeerID().getPeerIDBytes();

			// checks if peerID is equal to the one looked for
			if (Arrays.equals(nodeID, currentPeerID)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns a node that is closest to given ID. Returns null if there's no data about peers in this bucket.
	 *
	 * @param id
	 * @return
	 */
	public PeerInfo getClosestTo(BigInteger id) {
		int listSize = list.size();
		if (listSize == 0) {
			return null;
		} else {
			// checks distance between all nodes and given one (remembers best one)
			PeerInfo closest = list.get(0);
			BigInteger closestDistance = getDistanceBetween(id, KademliaRoutingTable.idToBigInteger(closest.getPeerID()
					.getPeerIDBytes()));
			for (int i = 1; i < listSize; i++) {
				PeerInfo current = list.get(i);
				BigInteger currentIDAsBigInteger = KademliaRoutingTable.idToBigInteger(current.getPeerID()
						.getPeerIDBytes());
				BigInteger currentDistance = getDistanceBetween(currentIDAsBigInteger, id);
				// if current distance is shorter then previous best, this
				// distance becomes best one
				if (currentDistance.compareTo(closestDistance) < 0) {
					closest = current;
					closestDistance = currentDistance;
				}
			}
			return closest;
		}
	}

	/**
	 * Returns distance between two given IDs, as defined by Kademlia protocol. It doesn't matter which ID is first -
	 * distance will be the same.
	 *
	 * @param firstID
	 * @param secondID
	 * @return
	 */
	public static BigInteger getDistanceBetween(BigInteger firstID, BigInteger secondID) {
		return firstID.xor(secondID);
	}

	/**
	 * Returns distance between two given nodeIDs (using Kademlia protocol).
	 *
	 * @param firstNodeID
	 * @param secondNodeID
	 * @return
	 */
	public static BigInteger getDistanceBetween(byte[] firstNodeID, byte[] secondNodeID) {
		BigInteger firstAsBigInteger = new BigInteger(firstNodeID);
		BigInteger secondAsBigInteger = new BigInteger(secondNodeID);
		return firstAsBigInteger.xor(secondAsBigInteger);
	}

	/**
	 * Returns PeerInfo object at given index in this bucket. If there's no such a PeerInfo, null is returned.
	 *
	 * @param index
	 * @return
	 */
	public PeerInfo getEntryAtIndex(int index) {
		if (index >= list.size()) {
			return null;
		} else {
			return list.get(index);
		}
	}

	/**
	 * Returns number of PeerInfos in this k-bucket.
	 *
	 * @return
	 */
	public int getSize() {
		return list.size();
	}

    public Date getPingDate(PeerInfo peerInfo) {
        return this.pings.get(peerInfo);
    }
}
