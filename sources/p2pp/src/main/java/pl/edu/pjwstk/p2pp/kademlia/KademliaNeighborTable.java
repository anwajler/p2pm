package pl.edu.pjwstk.p2pp.kademlia;

import pl.edu.pjwstk.p2pp.objects.NeighborTable;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;

/**
 * Neighbor table using Kademlia protocol.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class KademliaNeighborTable extends NeighborTable {

	@Override
	protected int getNumOfEntries() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public PeerInfo getNextHop(byte[] nodeID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onTimeSlot() {
		// TODO Auto-generated method stub

	}

	@Override
	public void leaveReset() {
		// TODO Auto-generated method stub

	}

}
