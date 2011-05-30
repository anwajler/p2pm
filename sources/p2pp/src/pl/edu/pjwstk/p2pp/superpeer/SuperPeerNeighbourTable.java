package pl.edu.pjwstk.p2pp.superpeer;

import pl.edu.pjwstk.p2pp.objects.NeighborTable;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;

public class SuperPeerNeighbourTable extends NeighborTable {

	@Override
	protected int getNumOfEntries() {
		return 0;
	}

	@Override
	public PeerInfo getNextHop(byte[] nodeID) {
		return null;
	}

	@Override
	public void onTimeSlot() {}

	@Override
	public void leaveReset() {}

}
