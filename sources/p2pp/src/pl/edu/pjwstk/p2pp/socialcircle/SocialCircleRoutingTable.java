package pl.edu.pjwstk.p2pp.socialcircle;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Vector;

import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.NeighborTable;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RoutingTable;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

public class SocialCircleRoutingTable extends RoutingTable {

    private Logger logger = org.apache.log4j.Logger.getLogger(SocialCircleRoutingTable.class);

    Vector<SocialPath> paths = new Vector<SocialPath>();


    public boolean addSocialPath(SocialPath path) {
        for (SocialPath savedPath : this.paths) {
            if (savedPath.compareTo(path) == 0) return false;
        }
        this.paths.add(path);
        return true;
    }

    public Vector<SocialPath> getPaths() {
        Vector<SocialPath> vset = new Vector<SocialPath>();
        for (SocialPath path : this.paths) {
        //    if (path.getNextA().getPeerID() == null && path.getNextB().getPeerID() == null) continue;
            vset.add(path);
        }
        return vset;
    }

    @Override
    public void addPeerInfo(PeerInfo peerInfo) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removePeerInfo(PeerInfo peerInfo) {}

    @Override
    public NeighborTable createNeighborTableForID(byte[] peerID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PeerInfo getClosestTo(byte[] id) {

        PathID lowestPathID = null;
        PeerInfo closest = null;
        //BigInteger closestBI = null;
        BigInteger shortestDistance = null;

        BigInteger idBI = new BigInteger(1, id);

        for (SocialPath path : this.paths) {
            PathID currentPathID = path.getPathId();

            if (null == closest) {
                lowestPathID = currentPathID;

                //checking distance from id to EndpointA
                PeerInfo current = path.getEndpointA();
                BigInteger currentBI = new BigInteger(1, current.getPeerID().getPeerIDBytes());
                BigInteger currentDistance = idBI.xor(currentBI);

                closest = current;
                //closestBI = currentBI;
                shortestDistance = currentDistance;

                //checking distance from id to EndpointB
                current = path.getEndpointB();
                currentBI = new BigInteger(1, current.getPeerID().getPeerIDBytes());
                currentDistance = idBI.xor(currentBI);

                // selecting shorter distance to id
                if (currentDistance.compareTo(shortestDistance) < 0) {
                    closest = current;
                    //closestBI = currentBI;
                    shortestDistance = currentDistance;
                }

            } else {
                //checking distance from id to EndpointA
                PeerInfo current = path.getEndpointA();
                BigInteger currentBI = new BigInteger(1, current.getPeerID().getPeerIDBytes());
                BigInteger currentDistance = idBI.xor(currentBI);

                // selecting shorter distance to id
                if (currentDistance.compareTo(shortestDistance) < 0) {
                    lowestPathID = currentPathID;
                    closest = current;
                    //closestBI = currentBI;
                    shortestDistance = currentDistance;
                    // when the distance to id is identical then select path with lower pathID
                } else if (currentDistance.compareTo(shortestDistance) == 0) {
                    if (currentPathID.compareTo(lowestPathID) < 0) {
                        lowestPathID = currentPathID;
                        closest = current;
                        //closestBI = currentBI;
                        shortestDistance = currentDistance;
                    }
                }

                //checking distance from id to EndpointB
                current = path.getEndpointB();
                currentBI = new BigInteger(1, current.getPeerID().getPeerIDBytes());
                currentDistance = idBI.xor(currentBI);

                // selecting shorter distance to id
                if (currentDistance.compareTo(shortestDistance) < 0) {
                    lowestPathID = currentPathID;
                    closest = current;
                    //closestBI = currentBI;
                    shortestDistance = currentDistance;
                    // when the distance to id is identical then select path with lower pathID
                } else if (currentDistance.compareTo(shortestDistance) == 0) {
                    if (currentPathID.compareTo(lowestPathID) < 0) {
                        lowestPathID = currentPathID;
                        closest = current;
                        //closestBI = currentBI;
                        shortestDistance = currentDistance;
                    }
                }

            }
        }

        if (closest == null) {
            if (logger.isTraceEnabled()) logger.trace("This peer doesn't know of a peer that is closer to id=" + ByteUtils.byteArrayToHexString(id));
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
        int index = 0;

        for (SocialPath path : this.paths) {

            PeerInfo endpointA = path.getEndpointA();
            StringBuilder entryBuilder = new StringBuilder("pathId=");
            entryBuilder.append(path.getPathId().toString());
            entryBuilder.append("; endpointA=[unhashedId=");
            entryBuilder.append(endpointA.getUnhashedID().getUnhashedIDValue());
            entryBuilder.append("; peerId=");
            entryBuilder.append(ByteUtils.byteArrayToHexString(endpointA.getPeerID().getPeerIDBytes()));
            entryBuilder.append("; addresses=");
            Vector<AddressInfo> addresses = endpointA.getAddressInfos();
            for (int k = 0; k < addresses.size(); k++) {
                AddressInfo currentAddress = (AddressInfo) addresses.get(k);
                entryBuilder.append(currentAddress.getAddressAsString());
                entryBuilder.append(":");
                entryBuilder.append(currentAddress.getPort());
                if (k != addresses.size() - 1) {
                    entryBuilder.append(",");
                }
            }

            PeerInfo endpointB = path.getEndpointB();
            entryBuilder.append("]; endpointB=[unhashedId=");
            entryBuilder.append(new String(endpointB.getUnhashedID().getUnhashedIDValue()));
            entryBuilder.append("; peerId=");
            entryBuilder.append(ByteUtils.byteArrayToHexString(endpointB.getPeerID().getPeerIDBytes()));
            entryBuilder.append("; addresses=");
            addresses = endpointB.getAddressInfos();
            for (int k = 0; k < addresses.size(); k++) {
                AddressInfo currentAddress = (AddressInfo) addresses.get(k);
                entryBuilder.append(currentAddress.getAddressAsString());
                entryBuilder.append(":");
                entryBuilder.append(currentAddress.getPort());
                if (k != addresses.size() - 1) {
                    entryBuilder.append(",");
                }
            }

            entryBuilder.append("]; nextA=");
            PeerInfo nextA = path.getNextA();
            if (nextA.getPeerID() != null) {
                entryBuilder.append("[unhashedId=");
                entryBuilder.append(new String(nextA.getUnhashedID().getUnhashedIDValue()));
                entryBuilder.append("; peerId=");
                entryBuilder.append(ByteUtils.byteArrayToHexString(nextA.getPeerID().getPeerIDBytes()));
                entryBuilder.append("; addresses=");
                addresses = nextA.getAddressInfos();
                for (int k = 0; k < addresses.size(); k++) {
                    AddressInfo currentAddress = (AddressInfo) addresses.get(k);
                    entryBuilder.append(currentAddress.getAddressAsString());
                    entryBuilder.append(":");
                    entryBuilder.append(currentAddress.getPort());
                    if (k != addresses.size() - 1) {
                        entryBuilder.append(",");
                    }
                }
            } else {
                entryBuilder.append("null");
            }

            entryBuilder.append("]; nextB=");
            PeerInfo nextB = path.getNextB();
            if (nextB.getPeerID() != null) {
                entryBuilder.append("[unhashedId=");
                entryBuilder.append(new String(nextB.getUnhashedID().getUnhashedIDValue()));
                entryBuilder.append("; peerId=");
                entryBuilder.append(ByteUtils.byteArrayToHexString(nextB.getPeerID().getPeerIDBytes()));
                entryBuilder.append("; addresses=");
                addresses = nextB.getAddressInfos();
                for (int k = 0; k < addresses.size(); k++) {
                    AddressInfo currentAddress = (AddressInfo) addresses.get(k);
                    entryBuilder.append(currentAddress.getAddressAsString());
                    entryBuilder.append(":");
                    entryBuilder.append(currentAddress.getPort());
                    if (k != addresses.size() - 1) {
                        entryBuilder.append(",");
                    }
                }
            } else {
                entryBuilder.append("null");
            }
            entryBuilder.append("]; ");

            entriesDescriptions[index++] = entryBuilder.toString();
        }

        return entriesDescriptions;
    }

    @Override
    public PeerInfo getNextHop(byte[] id) {
        if (logger.isTraceEnabled()) logger.trace("SocialCircleRoutingtable::getNextHop() invoked with id=" + ByteUtils.byteArrayToHexString(id));

        if (Arrays.equals(id, localPeerIDBytes)) {
            if (logger.isInfoEnabled()) logger.info("SocialCircleRoutingtable::getNextHop() returns null, because given id is the same as local peer's id.");
            return null;
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
                    logger.trace("SocialCircleRoutingtable::getNextHop() returns null, because this peer doesn't know of a peer that is closer to " +
                            "given id than local peer.");
                }
                return null;
            } // if closest-known peer is closer to given id
            else {
                if (logger.isTraceEnabled()) {
                    logger.trace("SocialCircleRoutingtable::getNextHop() returns closest="
                        + ByteUtils.byteArrayToHexString(closest.getPeerID().getPeerIDBytes()));
                }
                return closest;
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("SocialCircleRoutingtable::getNextHop() returns null, because this peer doesn't know of a peer that is closer to given"
                    + " id than local peer.");
            }
            return null;
        }
    }

    @Override
    protected int getNumOfEntries() {
        return this.paths.size();
    }

    @Override
    public boolean isLocalPeerCloser(byte[] id, byte[] remoteId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void leaveReset() {
        this.paths = new Vector<SocialPath>();
    }

    @Override
    public void onTimeSlot() {
        // TODO Auto-generated method stub
    }
}
