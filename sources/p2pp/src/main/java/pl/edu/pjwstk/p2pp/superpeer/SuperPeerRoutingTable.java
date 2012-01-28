package pl.edu.pjwstk.p2pp.superpeer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.objects.NeighborTable;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RoutingTable;
import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

public class SuperPeerRoutingTable extends RoutingTable {

    private Logger logger = org.apache.log4j.Logger.getLogger(SuperPeerRoutingTable.class);
    
    List<PeerInfo> peers = new ArrayList<PeerInfo>();

    
    @Override
    public void addPeerInfo(PeerInfo peerInfo) {
        if (peerInfo != null) {
            this.peers.add(peerInfo);
        }
    }

    public void removePeerInfo(PeerInfo peerInfo) {
        this.peers.remove(peerInfo);
    }

    @Override
    public NeighborTable createNeighborTableForID(byte[] peerID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PeerInfo getClosestTo(byte[] id) {

        PeerInfo closest = null;
        BigInteger shortestDistance = null;

        BigInteger idBI = new BigInteger(1, id);

        for (PeerInfo current : this.peers) {

            if (null == closest) {

                BigInteger currentBI = new BigInteger(1, current.getPeerID().getPeerIDBytes());
                BigInteger currentDistance = idBI.xor(currentBI);

                closest = current;
                shortestDistance = currentDistance;

                currentBI = new BigInteger(1, current.getPeerID().getPeerIDBytes());
                currentDistance = idBI.xor(currentBI);

                if (currentDistance.compareTo(shortestDistance) < 0) {
                    closest = current;
                    shortestDistance = currentDistance;
                }

            } else {

                BigInteger currentBI = new BigInteger(1, current.getPeerID().getPeerIDBytes());
                BigInteger currentDistance = idBI.xor(currentBI);

                if (currentDistance.compareTo(shortestDistance) < 0) {
                    closest = current;
                    shortestDistance = currentDistance;
                }

            }
        }

        if (closest == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("This peer doesn't know of a peer that is closer to id=" + ByteUtils.byteArrayToHexString(id));
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Peer=" + ByteUtils.byteArrayToHexString(closest.getPeerID().getPeerIDBytes())
                        + " is the closest known to id=" + ByteUtils.byteArrayToHexString(id));
            }
        }
        return closest;
    }

    @Override
    public String[] getEntriesDescription() {
        if (getNumOfEntries() == 0) {
            return null;
        }

        String[] entriesDescriptions = new String[getNumOfEntries()];
        @SuppressWarnings("unused")
		int index = 0;

        return entriesDescriptions;
    }

    @Override
    public PeerInfo getNextHop(byte[] id) {
        //logger.setLevel(Level.TRACE);
        if (logger.isTraceEnabled()) {
            logger.trace("getNextHop() invoked with id=" + ByteUtils.byteArrayToHexString(id));
        }

        if (Arrays.equals(id, localPeerIDBytes)) {
            if (logger.isDebugEnabled()) {
                logger.debug("getNextHop() returns null, because given id is the same as local peer's id.");
            }
            return null;
        }

        if (this.peers.size() == 1) {
            PeerInfo pi = this.peers.get(0);
            if (logger.isTraceEnabled()) {
                logger.trace("getNextHop() returns superPeerID=" + ByteUtils.byteArrayToHexString(pi.getPeerID().getPeerIDBytes()));
            }
            return pi;
        }

        PeerInfo closest = getClosestTo(id);

        if (closest != null) {
            BigInteger localAsBigInteger = new BigInteger(1, localPeerIDBytes);
            BigInteger closestAsBigInteger = new BigInteger(1, closest.getPeerID().getPeerIDBytes());
            BigInteger givenAsBigInteger = new BigInteger(1, id);

            // computes distance localPeer-givenID and closestPeer-givenID
            BigInteger distanceFromLocal = localAsBigInteger.xor(givenAsBigInteger);
            BigInteger distanceFromClosest = closestAsBigInteger.xor(givenAsBigInteger);

            if (logger.isTraceEnabled()) {
                logger.trace("Distance from closest = " + distanceFromClosest);
                logger.trace("Distance from local = " + distanceFromLocal);
            }

            // if local peer's ID is closest to given id
            if (distanceFromLocal.compareTo(distanceFromClosest) < 1) {
                if (logger.isTraceEnabled()) {
                    logger.trace("getNextHop() returns null, because this peer doesn't know of a peer that is closer to given"
                            + " id than local peer.");
                }
                return null;
            } // if closest-known peer is closer to given id
            else {
                if (logger.isTraceEnabled()) {
                    logger.trace("getNextHop() returns closest=" + ByteUtils.byteArrayToHexString(closest.getPeerID().getPeerIDBytes()));
                }
                return closest;
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("getNextHop() returns null, because this peer doesn't know of a peer that is closer to given id than local peer.");
            }
            return null;
        }
    }

    @Override
    protected int getNumOfEntries() {
        return this.peers.size();
    }

    @Override
    public boolean isLocalPeerCloser(byte[] id, byte[] remoteId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void leaveReset() {
        this.peers.clear();
    }

    @Override
    public void onTimeSlot() {
    }
}
