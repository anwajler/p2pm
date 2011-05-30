package pl.edu.pjwstk.p2pp.ice;

import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.services.Service;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * ICE protocol service as defined in P2PP specification (draft 01). It is always a client of ICE protocol but may also
 * be a server (it depends on the constructor's argument).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class ICEService extends Service {

	/**
	 * True if this service is not only a client of ICE protocol, but also a server. If false, it is only a client.
	 */
	private boolean isServer;

	/**
	 * Constructor of ICE protocol service.
	 * 
	 * @param isServer
	 *            True if this service has to be a server of ICE protocol. If false, it is only a client.
	 */
	public ICEService(boolean isServer) {
		super(P2PPUtils.STUN_TURN_ICE_CONTENT_TYPE, (byte) 0);
		this.isServer = isServer;
	}

    public boolean isServer() {
        return this.isServer;
    }

	@Override
	public void onTimeSlot() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onReceive(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ResourceObject getResourceObject() {
		// TODO Auto-generated method stub
		return null;
	}

}
