package pl.edu.pjwstk.p2pp.socialcircle;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Vector;

import pl.edu.pjwstk.p2pp.objects.NeighborTable;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;

public class SocialCircleNeighborTable extends NeighborTable {

    final static int NEIGHBOURHOOD_SIZE = 2;

    public Vector<PeerInfo> getLeftNeighbours(PeerInfo thisPeer) {
        Vector<PeerInfo> leftNeighbours = new Vector<PeerInfo>();
        BigInteger thisPeerBI = new BigInteger(1, thisPeer.getPeerID().getPeerIDBytes());
        for (PeerInfo neighbour : this.neighbors) {
            BigInteger neighbourBI = new BigInteger(1, neighbour.getPeerID().getPeerIDBytes());
            if (neighbourBI.compareTo(thisPeerBI) == -1) {
                leftNeighbours.add(neighbour);
            }
        }
        return leftNeighbours;
    }

    public Vector<PeerInfo> getRightNeighbours(PeerInfo thisPeer) {
        Vector<PeerInfo> rightNeighbours = new Vector<PeerInfo>();
        BigInteger thisPeerBI = new BigInteger(1, thisPeer.getPeerID().getPeerIDBytes());
        for (PeerInfo neighbour : this.neighbors) {
            BigInteger neighbourBI = new BigInteger(1, neighbour.getPeerID().getPeerIDBytes());
            if (neighbourBI.compareTo(thisPeerBI) == 1) {
                rightNeighbours.add(neighbour);
            }
        }
        return rightNeighbours;
    }

    private int getMostLeftIndex(PeerInfo thisPeer) {
        int index = 0;
        BigInteger thisPeerBI = new BigInteger(1, thisPeer.getPeerID().getPeerIDBytes());
        BigInteger currentBI = null;
        for (PeerInfo neighbour : this.neighbors) {
            BigInteger neighbourBI = new BigInteger(1, neighbour.getPeerID().getPeerIDBytes());
            if (currentBI == null) {
                switch (neighbourBI.compareTo(thisPeerBI)) {
                    case -1:
                        index = this.neighbors.indexOf(neighbour);
                    default:
                        currentBI = neighbourBI;
                }
            } else {
                switch (neighbourBI.compareTo(currentBI)) {
                    case -1:
                        index = this.neighbors.indexOf(neighbour);
                    default:
                        currentBI = neighbourBI;
                }
            }
        }
        return index;
    }

    private int getMostRightIndex(PeerInfo thisPeer) {
        int index = 0;
        BigInteger thisPeerBI = new BigInteger(1, thisPeer.getPeerID().getPeerIDBytes());
        BigInteger currentBI = null;
        for (PeerInfo neighbour : this.neighbors) {
            BigInteger neighbourBI = new BigInteger(1, neighbour.getPeerID().getPeerIDBytes());
            if (currentBI == null) {
                switch (neighbourBI.compareTo(thisPeerBI)) {
                    case 1:
                        index = this.neighbors.indexOf(neighbour);
                    default:
                        currentBI = neighbourBI;
                }
            } else {
                switch (neighbourBI.compareTo(currentBI)) {
                    case 1:
                        index = this.neighbors.indexOf(neighbour);
                    default:
                        currentBI = neighbourBI;
                }
            }
        }
        return index;
    }

    public void addNeighbour(PeerInfo newNeighbour, PeerInfo thisPeer) {

        // Not adding ownPeerInfo
        if (Arrays.equals(newNeighbour.getPeerID().getPeerIDBytes(), thisPeer.getPeerID().getPeerIDBytes()))
            return;
        // Not adding if is already here.
        if (hasNeighbour(newNeighbour)) {
            return;
        }

        // Not checking left/right if there is no neighbour hirtherto.
        if (this.neighbors.isEmpty()) {
            this.neighbors.add(newNeighbour);
            return;
        }

        BigInteger newNeighbourBI = new BigInteger(1, newNeighbour.getPeerID().getPeerIDBytes());
        BigInteger thisPeerBI = new BigInteger(1, thisPeer.getPeerID().getPeerIDBytes());

        switch (newNeighbourBI.compareTo(thisPeerBI)) {

            case -1:

                // Removing only if there is already full number of neighbours on the side.
                if (getLeftNeighbours(thisPeer).size() == NEIGHBOURHOOD_SIZE) {
                    PeerInfo mostLeft = this.neighbors.get(getMostLeftIndex(thisPeer));
                    BigInteger mostLeftBI = new BigInteger(1, mostLeft.getPeerID().getPeerIDBytes());
                    // If new is bigger than the one most on the left it means he is closer.
                    if (newNeighbourBI.compareTo(mostLeftBI) == 1) {
                        this.neighbors.remove(mostLeft);
                    }
                }
                this.neighbors.add(newNeighbour);
                break;

            case 1:

                // Removing only if there is already full number of neighbours on the side.
                if (getRightNeighbours(thisPeer).size() == NEIGHBOURHOOD_SIZE) {
                    PeerInfo mostRight = this.neighbors.get(getMostRightIndex(thisPeer));
                    BigInteger mostRightBI = new BigInteger(1, mostRight.getPeerID().getPeerIDBytes());
                    // If new is bigger than the one most on the right it means he is closer.
                    if (newNeighbourBI.compareTo(mostRightBI) == -1) {
                        this.neighbors.remove(mostRight);
                    }
                }
                this.neighbors.add(newNeighbour);
                break;

            default:

        }
    }

    public void replaceNeighbour(PeerInfo newNeighbour) {
        for (PeerInfo neighbour : this.neighbors) {
            if (Arrays.equals(neighbour.getPeerID().getPeerIDBytes(), newNeighbour.getPeerID().getPeerIDBytes())) {
                int index = this.neighbors.indexOf(neighbour);
                this.neighbors.setElementAt(newNeighbour, index);
            }
        }
    }

    public boolean hasNeighbour(PeerInfo neighbor) {
        for (PeerInfo neighbour : this.neighbors) {
            if (Arrays.equals(neighbour.getPeerID().getPeerIDBytes(), neighbor.getPeerID().getPeerIDBytes()))
                return true;
        }
        return false;
    }

    @Override
    public PeerInfo getNextHop(byte[] nodeID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected int getNumOfEntries() {
        return this.neighbors.size();
    }

    @Override
    public void leaveReset() {
        this.neighbors.clear();
    }

    @Override
    public void onTimeSlot() {
        // TODO Auto-generated method stub
    }

    @SuppressWarnings("unchecked")
    public Vector<PeerInfo> getVset(PeerInfo thisPeer) {
        Vector<PeerInfo> vset = (Vector<PeerInfo>) getNeighbors().clone();
        vset.add(thisPeer);

        return vset;
    }
}
