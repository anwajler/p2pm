package pl.edu.pjwstk.p2pp.kademlia;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.GlobalConstants;
import pl.edu.pjwstk.p2pp.entities.P2PPEntity;
import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.LookupPeerRequest;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.transactions.Transaction;
import pl.edu.pjwstk.p2pp.transactions.TransactionListener;
import pl.edu.pjwstk.p2pp.transactions.TransactionTable;
import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.NodeTimers;

/**
 * Routing table general object compatible with Kademlia protocol.
 * TODO probably something should be done to accomplish deleting of 'dead' peers.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 *
 */
public class KademliaRoutingTable extends RoutingTable {

	private static final Logger logger = org.apache.log4j.Logger.getLogger(KademliaRoutingTable.class);

	/**
	 * K as defined by Kademlia protocol.
	 */
	private static final int DEFAULT_K = 20;

	/**
	 * Vector of KBucket objects. Each index represents a bucket with PeerInfos
	 */
	private Vector<KBucket> buckets = new Vector<KBucket>();

    private Date lastPing = new Date();
    private TransactionTable transactionTable;

	/**
	 * Constructor of routing table compatible with Kademlia protocol.
	 */
    public KademliaRoutingTable() {}

	public KademliaRoutingTable(TransactionTable transactionTable) {
        this.transactionTable = transactionTable;
	}

    public Collection<KBucket> getEntries() {
        return this.buckets;
    }

	@Override
	protected int getNumOfEntries() {
		int size = buckets.size();
		int numOfEntries = 0;
		for (int i = 0; i < size; i++) {
			numOfEntries += buckets.get(i).getSize();
		}
		return numOfEntries;
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {

		throw new UnsupportedGeneralObjectException("RoutingTable can't contain " + subobject.getClass().getName()
				+ " objects.");

	}

	@Override
	public PeerInfo getNextHop(byte[] id) {
        if (logger.isTraceEnabled()) {
		    logger.trace("Method invoked with id=" + ByteUtils.byteArrayToHexString(id) + ".");
        }

		// checks if given nodeID is the same as set in routing table
		if (Arrays.equals(id, localPeerIDBytes)) {
            if (logger.isDebugEnabled()) {
			    logger.debug("Method returns null, because given id is the same as local peer's id.");
            }
			return null;
		} else {
			PeerInfo closest = getClosestTo(id);

			// handles a case when there's no closest peer (ie. there's no peer in this table)
			if (closest != null) {
				BigInteger localAsBigInteger = idToBigInteger(localPeerIDBytes);
				BigInteger closestAsBigInteger = idToBigInteger(closest.getPeerID().getPeerIDBytes());
				BigInteger givenAsBigInteger = idToBigInteger(id);

				// computes distance localPeer-givenID and closestPeer-givenID
				BigInteger distanceFromLocal = KBucket.getDistanceBetween(localAsBigInteger, givenAsBigInteger);
				BigInteger distanceFromClosest = KBucket.getDistanceBetween(closestAsBigInteger, givenAsBigInteger);

                if (logger.isTraceEnabled()) {
				    logger.trace("Distance from local = "+distanceFromLocal);
				    logger.trace("Distance from closest = "+distanceFromClosest);
                }

				// if local peer's ID is closer to given id
				if (distanceFromLocal.compareTo(distanceFromClosest) < 0) {
                    if (logger.isTraceEnabled()) {
					    logger.trace("Method returns null, because this peer doesn't know of a peer that is closer to given id than local peer.");
                    }
					return null;
				} // if closest-known peer is closer to given id
				else {
                    if (logger.isTraceEnabled()) {
					    logger.trace("Method returns closest="
							+ ByteUtils.byteArrayToHexString(closest.getPeerID().getPeerIDBytes()));
                    }
					return closest;
				}
			} else {
                if (logger.isTraceEnabled()) {
				    logger.trace("Method returns null, because this peer doesn't know of a peer that is closer to given id than local peer.");
                }
				return null;
			}

		}
	}

	@Override
	public void setProperties(int algorithmBase, int keyLength) {
		super.setProperties(algorithmBase, keyLength);

		for (int i = 0; i < keyLength; i++) {
			buckets.add(new KBucket(DEFAULT_K));
		}
	}

	/**
	 * Converts nodeID in byte form to BigInteger.
	 *
	 * @return
	 */
	public static BigInteger idToBigInteger(byte[] nodeID) {
		/* Paulina (14-01-10):
		 * Added fixed sign to eliminate negative id's and distances
		 */
		return new BigInteger(1, nodeID);
	}

	@Override
	public PeerInfo getClosestTo(byte[] id) {
		// TODO probably should be done more wisely... at the moment it iteratively checks all the buckets

		// checks if given id is compatible with Kademlia
		if (id.length * 8 == keyLength) {

			BigInteger idAsBigInteger = idToBigInteger(id);
			PeerInfo closestPeer = null;
			BigInteger shortestDistance = null;
			BigInteger closestAsBigInteger = null;

			// Iteratively gets closest peers from buckets. Looks for one that is closest to given id.
			for (KBucket currentBucket : buckets) {

				// Asks current bucket about peer closest to given id.
				PeerInfo currentPeer = currentBucket.getClosestTo(idAsBigInteger);

				// if current bucket has any peers (i.e. KBucket.getClosestTo() didn't return null)
				if (currentPeer != null) {
                    if (logger.isTraceEnabled()) logger.trace("currentPeer=" + currentPeer);
					// if closestPeer variable isn't initialised
					if (closestPeer == null) {

						BigInteger currentPeerAsBigInteger = idToBigInteger(currentPeer.getPeerID().getPeerIDBytes());

						// computes distance between current peer and given id
						BigInteger currentPeerDistance = KBucket.getDistanceBetween(currentPeerAsBigInteger,
								idAsBigInteger);

						// current peer becomes closest one
						closestPeer = currentPeer;
						closestAsBigInteger = currentPeerAsBigInteger;
						shortestDistance = currentPeerDistance;

                        if (Arrays.equals(id, currentPeer.getPeerID().getPeerIDBytes())) break;

					}// if currentPeer variable is initialised
					else {

                        if (Arrays.equals(id, currentPeer.getPeerID().getPeerIDBytes())) {
                            closestPeer = currentPeer;
                            break;
                        }

						BigInteger currentPeerAsBigInteger = idToBigInteger(currentPeer.getPeerID().getPeerIDBytes());
						// computes distance between current peer and given id
						BigInteger currentDistance = KBucket.getDistanceBetween(currentPeerAsBigInteger,
								closestAsBigInteger);

						// if (current peer is closer to given one than previous best)
						if (currentDistance.compareTo(shortestDistance) < 0) {
							closestPeer = currentPeer;
							closestAsBigInteger = currentPeerAsBigInteger;
							shortestDistance = currentDistance;
						}
					}
				}
			} // end of for
			if (closestPeer == null) {
                if (logger.isTraceEnabled()) {
				    logger.trace("This peer doesn't know of a peer that is closer to id="
						+ ByteUtils.byteArrayToHexString(id));
                }
			} else if (logger.isTraceEnabled()) {
				logger.trace("Peer=" + ByteUtils.byteArrayToHexString(closestPeer.getPeerID().getPeerIDBytes())
						+ " is the closest known to id=" + ByteUtils.byteArrayToHexString(id));
			}
			return closestPeer;
		} else {
            if (logger.isTraceEnabled()) {
			    logger.trace("Given id=" + ByteUtils.byteArrayToHexString(id) + " isn't compatible with used overlay.");
            }
			return null;
		}

	}

	@Override
    public void onTimeSlot() {

        Date nowDate = new Date();
        if ((nowDate.getTime() - this.lastPing.getTime()) / 1000 >= NodeTimers.ROUTING_TABLE_MAINTENANCE_TIMER_SECONDS) {

            for (final KBucket currentBucket : this.buckets) {
                int bucketSize = currentBucket.getSize();
                for (int i = 0; i < bucketSize; i++) {

                    final PeerInfo pPI = currentBucket.getEntryAtIndex(i);

                    TransactionListener lookupPeerTransactionListener = new TransactionListener() {

                        public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType, Request request, Response response,
                                                     TransactionTable transactionTable, P2PPEntity node) {

                            if (logger.isTraceEnabled()) {
                                logger.trace("transactionID=" + ByteUtils.byteArrayToHexString(transactionID) + "; transactionState=" + transactionState);
                            }

                            if (transactionState == Transaction.TERMINATED_STATE) {

                                if (logger.isTraceEnabled()) {
                                    logger.trace("Peer PONGs back " + pPI);
                                }

                                //currentBucket.addPeerInfo(transactionTable, localPeerInfo, pPI); // Basic 2.2
                                currentBucket.addPeerInfo(pPI); // Optimized 4.1

                            } else {

                                if (logger.isTraceEnabled()) {
                                    logger.trace("Removing unresponsive peer from the bucket: " + pPI);
                                }

                                currentBucket.removePeerInfo(pPI);

                            }

                        }

                    };

                    if ((nowDate.getTime() - currentBucket.getPingDate(pPI).getTime()) / 1000 >= KademliaConstants.KADEMLIA_PING_INTERVAL) {
                        if (logger.isTraceEnabled()) logger.trace("PINGing peer " + pPI);
                        LookupPeerRequest request = new LookupPeerRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null,
                                localPeerIDBytes, GlobalConstants.isOverReliable, false, null, localPeerInfo);

                        transactionTable.createTransactionAndFill(request, lookupPeerTransactionListener, pPI.getAddressInfos(), localPeerIDBytes,
                                pPI.getPeerID().getPeerIDBytes());
                    }

                }
            }

            this.lastPing = nowDate;
        }

    }

	@Override
	public String[] getEntriesDescription() {
		if (getNumOfEntries() == 0) {
			return null;
		} else {
			// TODO synchronize? will this method be invoked in other threads?
			String[] entriesDescriptions = new String[getNumOfEntries()];
			int bucketsSize = buckets.size();
			int entryIndex = 0;

			// goes bucket by bucket and gets an entry by entry inside it
			for (int i = 0; i < bucketsSize; i++) {
				KBucket bucket = buckets.get(i);
				for (int j = 0; j < bucket.getSize(); j++) {
					PeerInfo currentPeerInfo = bucket.getEntryAtIndex(j);
					if (currentPeerInfo != null) {
						StringBuilder entryBuilder = new StringBuilder("unhashedID=");
						entryBuilder.append(new String(currentPeerInfo.getUnhashedID().getUnhashedIDValue()));
                        entryBuilder.append("; peerID=");
						entryBuilder.append(ByteUtils.byteArrayToHexString(currentPeerInfo.getPeerID().getPeerIDBytes()));
						entryBuilder.append("; addresses=");
						Vector<AddressInfo> addresses = currentPeerInfo.getAddressInfos();
						for (int k = 0; k < addresses.size(); k++) {
							AddressInfo currentAddress = addresses.get(k);
							entryBuilder.append(currentAddress.getAddressAsString());
                            entryBuilder.append(":");
                            entryBuilder.append(currentAddress.getPort());
							if (k != addresses.size() - 1) {
								entryBuilder.append(",");
							}
						}
						entryBuilder.append("; ");
						entriesDescriptions[entryIndex] = entryBuilder.toString();
						entryIndex++;
					}
				}
			}

			return entriesDescriptions;
		}
	}

	@Override
	public void addPeerInfo(PeerInfo peerInfo) {
		byte[] givenPeerIDBytes = peerInfo.getPeerID().getPeerIDBytes();

		// prevents adding a PeerInfo object of (local peer) and (peer that has bad ID [doesn't conform to Kademlia's
		// rules for IDs])
		if (!Arrays.equals(givenPeerIDBytes, localPeerIDBytes) && givenPeerIDBytes.length * 8 == keyLength) {

			// handles no PeerID set yet (before bootstrap) and no peerID in given PeerInfo object
			if (localPeerIDBytes != null) {

                if (logger.isTraceEnabled()) {
				    logger.trace("Added PeerInfo of peer=" + ByteUtils.byteArrayToHexString(givenPeerIDBytes)
						+ " to routing table.");
                }

				// calculates a distance to given peer
				BigInteger distance = KBucket.getDistanceBetween(givenPeerIDBytes, localPeerIDBytes);

				// index of kbucket for given peerinfo is an index of first "1" in distance
				int bucketIndex = distance.bitLength();
				// adds given peerinfo to a proper bucket
				KBucket bucketForPeerInfo = buckets.get(bucketIndex);
				//bucketForPeerInfo.addPeerInfo(this.transactionTable, localPeerInfo, peerInfo); // Basic 2.2
                bucketForPeerInfo.addPeerInfo(peerInfo); // Optimized 4.1
			} else if (logger.isTraceEnabled()) {
				logger.trace("PeerInfo of peer=" + ByteUtils.byteArrayToHexString(givenPeerIDBytes)
						+ " wasn't added to routing table.");
			}
		} else if (logger.isTraceEnabled()) {
			logger.trace("PeerInfo of peer=" + ByteUtils.byteArrayToHexString(givenPeerIDBytes)
					+ " wasn't added because it contains local peerID or length of peerID is wrong.");
		}

	}

    /*
     * Probably very wrong.
     */
    public void removePeerInfo(PeerInfo peerInfo) {
        byte[] givenPeerIDBytes = peerInfo.getPeerID().getPeerIDBytes();
        BigInteger distance = KBucket.getDistanceBetween(givenPeerIDBytes, localPeerIDBytes);
        int bucketIndex = distance.bitLength();
        KBucket bucketForPeerInfo = buckets.get(bucketIndex);
        bucketForPeerInfo.removePeerInfo(peerInfo);
        if (logger.isDebugEnabled()) {
            logger.debug("Removed PeerInfo from the routing table: " + peerInfo);
        }
    }

	@Override
	public NeighborTable createNeighborTableForID(byte[] remotePeerID) {

		// TODO synchronize? will this method be invoked in other threads?
		if (getNumOfEntries() == 0) {
			return null;
		} else {

			NeighborTable neighborTable = new KademliaNeighborTable();

			// vector that will hold two best neighbors
			Vector<PeerInfo> bestNeighbors = new Vector<PeerInfo>();

			BigInteger distanceToWorstOfBest = null;
			BigInteger remotePeerIDAsBigInteger = new BigInteger(remotePeerID);

			// goes bucket by bucket and checks entry by entry inside it looking for two best neighbors
			int bucketsSize = buckets.size();
			for (int i = 0; i < bucketsSize; i++) {
				KBucket bucket = buckets.get(i);
				for (int j = 0; j < bucket.getSize(); j++) {
					PeerInfo currentPeerInfo = bucket.getEntryAtIndex(j);
					if (currentPeerInfo != null) {

						BigInteger currentPeerIDAsBigInteger = new BigInteger(currentPeerInfo.getPeerID()
								.getPeerIDBytes());
						BigInteger currentDistance = KBucket.getDistanceBetween(currentPeerIDAsBigInteger,
								remotePeerIDAsBigInteger);

						// if there's no already found neighbor
						if (bestNeighbors.size() == 0) {
							distanceToWorstOfBest = currentDistance;
							bestNeighbors.add(currentPeerInfo);
						} // if there's only one peer info in the list of best neighbors
						else if (bestNeighbors.size() == 1) {
							bestNeighbors.add(currentPeerInfo);
							// if peer added to best neighbors is farther then previously added
							if (currentDistance.compareTo(distanceToWorstOfBest) == 1) {
								distanceToWorstOfBest = currentDistance;
							}
						} // if there are already 2 peers in list of best ones
						else {
							// if current peer is closer to remote peer than worst of best
							if (currentDistance.compareTo(distanceToWorstOfBest) == -1) {
								PeerInfo oneOfBest = (PeerInfo) bestNeighbors.get(0);
								BigInteger oneOfBestAsBig = new BigInteger(oneOfBest.getPeerID().getPeerIDBytes());
								BigInteger distanceToOneOfBest = KBucket.getDistanceBetween(oneOfBestAsBig,
										remotePeerIDAsBigInteger);
								// if first of best is the worst of best
								if (distanceToOneOfBest.compareTo(distanceToWorstOfBest) == 0) {
									bestNeighbors.remove(0);
									bestNeighbors.add(currentPeerInfo);
								} else {
									bestNeighbors.remove(1);
									bestNeighbors.add(currentPeerInfo);
								}
								// computes distance for old neihbor and new one and saves longest as worst
								distanceToOneOfBest = new BigInteger(((PeerInfo) bestNeighbors.get(0)).getPeerID()
										.getPeerIDBytes());
								if (currentDistance.compareTo(distanceToOneOfBest) == -1) {
									distanceToWorstOfBest = distanceToOneOfBest;
								} else {
									distanceToWorstOfBest = currentDistance;
								}
							}
						}

					}
				}
			}

			try {
				// copies best neighbors to neighbor table and returns it
                for (PeerInfo neighbour : bestNeighbors) {
					neighborTable.addSubobject(neighbour);
				}
			} catch (UnsupportedGeneralObjectException e) {
				// ignore because PeerInfo is always acceptable in tables
			}
			return neighborTable;
		}

	}

	@Override
	public boolean isLocalPeerCloser(byte[] id, byte[] remoteId) {
		BigInteger distanceToLocal = KBucket.getDistanceBetween(id, localPeerIDBytes);
		BigInteger distanceToRemote = KBucket.getDistanceBetween(id, remoteId);
        return distanceToLocal.compareTo(distanceToRemote) < 0;
	}

	@Override
	public void leaveReset() {
		buckets.clear();
		buckets = null;
	}
}
