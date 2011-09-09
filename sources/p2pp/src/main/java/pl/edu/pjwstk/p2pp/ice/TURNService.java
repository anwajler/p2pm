package pl.edu.pjwstk.p2pp.ice;

import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.services.Service;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * TURN service as defined in P2PP specification (draft 01). This service is always a client of TURN protocol but may
 * also be a server (it depends on constructors argument).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class TURNService extends Service {

	/**
	 * True if this service is also a server of TURN protocol. If false, it is only a client.
	 */
	private boolean isServer;

	/**
	 * Creates TURNService object.
	 * 
	 * @param isServer
	 *            True if this service has to be a server of TURN protocol. If false, it is only a client.
	 */
	public TURNService(boolean isServer) {
		super(P2PPUtils.TURN_CONTENT_TYPE, (byte) 0);
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
