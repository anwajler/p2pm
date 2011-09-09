package pl.edu.pjwstk.p2pp.ice;

import java.util.Vector;

import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.services.Service;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * STUN service as defined in P2PP specification (draft 01). Is always a client but may be also a server.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class STUNService extends Service {

	/**
	 * Server reflexive address in "xxx.xxx.xxx.xxx" form. TODO to be deleted when ICE implementation complete.
	 * Currently set by launcher.
	 */
	public static String serverReflexiveAddress;

	/**
	 * Server reflexive port. TODO to be deleted when ICE implementation complete. Currently set by launcher.
	 */
	public static int serverReflexivePort;

	/**
	 * True if this service is a server of STUN protocol. If false, it is only a client.
	 */
	private boolean isServer;

    private boolean[] transportType;

	// TODO some kind of a vector or so should be here to hold
	// STUNCommunicationListeners.

	/**
	 * Creates STUNService object.
	 * 
	 * @param isServer
	 *            True if this service has to be a server of STUN protocol. If false, it is only a client.
	 */
	public STUNService(boolean isServer, boolean[] transportType) {
		super(P2PPUtils.STUN_CONTENT_TYPE, (byte) 0);
		this.isServer = isServer;
        this.transportType = transportType;
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

	/**
	 * Method starting a process of server-reflexive address lookup. This method shouldn't take many time. Should give
	 * start use another thread for the process of determining an address.
	 * 
	 * @param listener
	 *            Listener that wants to know about a result of this process.
	 * @param addressInfos
	 *            Vector of AddressInfo objects containing information about known addresses of STUN server.
	 */
	public void determineServerReflexiveAddress(STUNCommunicationListener listener, Vector<AddressInfo> addressInfos) {

		// TODO has to be changed... now it's static... Has to be filled with STUN protocol implementation that informs
		// given listener about determined address.
		AddressInfo addressInfo = new AddressInfo((byte) 0, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 0,
				AddressInfo.SERVER_REFLEXIVE_ADDRESS_TYPE, transportType, serverReflexivePort,
				ByteUtils.stringIPAddressToBytes(serverReflexiveAddress));
		listener.onAddressDetermined(addressInfo);

	}

	@Override
	public ResourceObject getResourceObject() {
		// TODO Auto-generated method stub
		return null;
	}
}
