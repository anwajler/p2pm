package pl.edu.pjwstk.p2pp.socialcircle;

import pl.edu.pjwstk.p2pp.objects.NeighborTable;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;

import java.util.Arrays;

public class SocialCirclePhysicalNeighborTable extends NeighborTable {

    public void replaceNeighbour(PeerInfo newNeighbour) {
        for (PeerInfo neighbour : this.neighbors) {
            if (Arrays.equals(neighbour.getPeerID().getPeerIDBytes(), newNeighbour.getPeerID().getPeerIDBytes())) {
                int index = this.neighbors.indexOf(neighbour);
                this.neighbors.setElementAt(newNeighbour, index);
            }
        }
    }

    public boolean hasNeighbor(PeerInfo neighbor) {
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
}