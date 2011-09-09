package pl.edu.pjwstk.p2pp.entities;

import java.util.Vector;

import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.messages.requests.Request;
import pl.edu.pjwstk.p2pp.messages.responses.AuthenticateResponse;
import pl.edu.pjwstk.p2pp.messages.responses.EnrollResponse;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;

/**
 * Class describing enrollment and authentication server as defined in Peer-to-Peer Protocol specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class EAndAServer extends P2PPEntity {

	/**
     * 
     */
	public EAndAServer() {

	}

	@Override
	protected boolean canConsume(Message message) {
        return message instanceof EnrollResponse || message instanceof AuthenticateResponse;
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
