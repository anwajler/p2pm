package pl.edu.pjwstk.p2pp.entities;

import java.util.Vector;

import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;

public class DiagnosticServer extends P2PPEntity {

	@Override
	protected boolean canConsume(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void consume(Message message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTimeSlot() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean isBestKnownPeer(Request request) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNodeAfterBootstrapping() {
		return false;
	}

	@Override
	public void updateTables(PeerInfo peerInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateTables(Vector<PeerInfo> peerInfos) {
		// TODO Auto-generated method stub

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
	protected void onForwardingRequest(Request request) {
		// TODO Auto-generated method stub

	}

}
