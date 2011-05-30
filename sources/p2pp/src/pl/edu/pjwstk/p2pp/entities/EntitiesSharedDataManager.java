package pl.edu.pjwstk.p2pp.entities;

import java.util.*;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.OutgoingMessagesListener;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.NeighborTable;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RoutingTable;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;
import pl.edu.pjwstk.p2pp.objects.Uptime;
import pl.edu.pjwstk.p2pp.transactions.TransactionTable;
import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * <p>
 * Manager of everything that is shared by all managed entities. Those things are:
 * <ul>
 * <li>transaction table</li>
 * <li>routing table</li>
 * <li>neighbor table</li>
 * <li>resources manager</li>
 * <li>PeerInfo object that is unusual, because it is created once more every time {@link #getPeerInfo(boolean,boolean)} is
 * invoked. It is made this way because we don't want any object to modify PeerInfo information without using this
 * manager's methods.</li>
 * <li>P2P object describing overlay</li>
 * <li>ports</li>
 * <li>services manager</li>
 * <li>TODO probably some more</li>
 * </ul>
 * </p>
 * TODO probably all the things and methods should be moved from here to node, peer or entity
 *
 * @see TransactionTable
 * @see RoutingTable
 * @see NeighborTable
 * @see PeerInfo
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public class EntitiesSharedDataManager {

	private static Logger LOG = Logger.getLogger(EntitiesSharedDataManager.class);

	/**
	 * Default source ID used before bootstrap happened. 4 bytes long and filled with zeroes.
	 */
	public static final byte[] SOURCE_ID_BEFORE_BOOTSTRAP = new byte[4];

	/**
	 * Options of an overlay this entity is part of.
	 */
	private P2POptions options;

	/**
	 * Map of UDP addresses of entities. This map contains type of address (Integer) as key (as defined by ICE) and
	 * address as value (in String representation).
	 */
	private Hashtable<Integer, String> udpAddressesMap = new Hashtable<Integer, String>();

	/**
	 * Map of TCP addresses of entities.This map contains type of address (Integer) as key (as defined by ICE) and
	 * address as value (in String representation).
	 */
	private Hashtable<Integer, String> tcpAddressesMap = new Hashtable<Integer, String>();

	/**
	 * Map of UDP ports of entities. This map contains type of address (Integer) as key (as defined by ICE) and port as
	 * value (as Integer object).
	 */
	private Hashtable<Integer, Integer> udpPortsMap = new Hashtable<Integer, Integer>();

	/**
	 * Map of TCP ports of entities. This map contains type of address (Integer) as key (as defined by ICE) and port as
	 * value (as Integer object).
	 */
	private Hashtable<Integer, Integer> tcpPortsMap = new Hashtable<Integer, Integer>();

	/**
	 * Listener of entities. Used for sending messages. Set in setter.
	 */
	private OutgoingMessagesListener outgoingListener;

	/**
	 * PeerID value.
	 */
	private byte[] peerIDAsBytes;

	/**
	 * Moment in time when an entity started working. (initialized in constructor).
	 */
	private long momentOfUptimeStart;

	private byte[] unhashedIDBytes;

	/**
	 * Constructor of manager of data shared between local entities. Has to contain at least one non-zero port. Zero
	 * ports are omitted. Ports are used as host-reflexive address ports.
	 *
	 * @param udpPort
	 * @param tcpPort
	 * @param tlsPort
	 * @param dtlsPort
	 */
	public EntitiesSharedDataManager(int udpPort, int tcpPort, int sslPort, int tlsPort, int dtlsPort) {

		// adds UDP port to a map (if non-zero)
		if (udpPort != 0) {
			udpPortsMap.put(new Integer(0), new Integer(udpPort));
		}
		// adds TCP port to a map (if non-zero)
		if (tcpPort != 0) {
			tcpPortsMap.put(new Integer(0), new Integer(tcpPort));
		}
		if (sslPort > 0) {
			tcpPortsMap.put(0, sslPort);
		}
		if (tlsPort > 0) {
			tcpPortsMap.put(0, tlsPort);
		}
		// TODO what about tls and dtls ports? new maps?

		// creates sourceID for a state before bootstrap (has to be changed during bootstrapping process)
		peerIDAsBytes = SOURCE_ID_BEFORE_BOOTSTRAP;

		// uptime start initialized when this manager is created TODO is this OK?
		momentOfUptimeStart = System.currentTimeMillis();
	}

	/**
	 * Sets a listener of messages to be send. This object is passed to interested subobjects.
	 *
	 * @param outgoingListener
	 */
	public void setOutgoingListener(OutgoingMessagesListener outgoingListener) {
		this.outgoingListener = outgoingListener;
	}

	/**
	 * Method invoked when P2PP manager gives time for this object to handle all the timers, retransmissions etc.
	 *
	 * @param listener
	 *            Listener that may be used for sending messages by this object.
	 */
	public void onTimeSlot(OutgoingMessagesListener listener) {

	}

	/**
	 * Returns peerID as an array of bytes.
	 *
	 * @return
	 */
	public byte[] getPeerIDAsBytes() {
		return peerIDAsBytes;
	}

	/**
	 * Returns byte array being unhashed ID given on startup.
	 *
	 * @return
	 */
	public byte[] getUnhashedID() {
		return unhashedIDBytes;
	}

	public void setUnhashedIDBytes(byte[] unhashedIDBytes) {
		this.unhashedIDBytes = unhashedIDBytes;
	}

	/**
	 * Sets addresses of this peer. Map has to contain pairs of "address as string" and
	 * "type of address as Integer (as defined by ICE)". Type of address is using types defined in ICEConstants.
	 *
	 * @param map
	 *            Map that contains addresses.
	 */
	public void setAddresses(Hashtable<String,Integer> map, boolean[] transportType) {

        if (Arrays.equals(transportType, AddressInfo.UDP_TRANSPORT_TYPE)) {
            for (Enumeration<String> e = map.keys(); e.hasMoreElements();) {
                String address = e.nextElement();
                Integer addressType = map.get(address);
                if (LOG.isTraceEnabled()) LOG.trace("Adding UDP address " + address);
                udpAddressesMap.put(addressType, address);
            }
        } else /*if (Arrays.equals(transportType, AddressInfo.TCP_TRANSPORT_TYPE))*/ {
            for (Map.Entry<String,Integer> entry : map.entrySet()) {
                String address = entry.getKey();
                if (LOG.isTraceEnabled()) LOG.trace("Adding TCP address " + address);
                tcpAddressesMap.put(entry.getValue(), address);
            }
        }

	}

	/**
	 * Returns PeerInfo object describing entities. TODO Now always "host type" address is returned. Maybe should be
	 * done more wisely.
	 *
	 * @param isByNodeAfterBootstrapping
	 *            True if this method is invoked by a node after bootstrapping process. False otherwise.
	 * @param isByNode
	 *            True if this method is invoked by node. False otherwise.
	 * @return
	 */
	public PeerInfo getPeerInfo(boolean isByNodeAfterBootstrapping, boolean isByNode) {

		// creates subobjects for uptime, peerID
		Uptime uptime = new Uptime((int) (System.currentTimeMillis() - momentOfUptimeStart));
		PeerID peerID;
		UnhashedID unhashedID = null;

		// creates PeerInfo's subobjects depending on arguments
		if (isByNode) {
			if (LOG.isTraceEnabled()) LOG.trace("unhashedID=" + ByteUtils.byteArrayToHexString(unhashedIDBytes));
			unhashedID = new UnhashedID(unhashedIDBytes);
			if (isByNodeAfterBootstrapping) {
				peerID = new PeerID(peerIDAsBytes);
			} else {
				peerID = new PeerID(new byte[4]);
			}
		} else {
			peerID = new PeerID(new byte[4]);
		}

		// creates PeerInfo object that will be returned
		PeerInfo peerInfo = new PeerInfo(peerID, uptime, new Vector<AddressInfo>(), unhashedID);

		// goes through the UDP addresses
        for (Map.Entry<Integer,String> udpAddressEntry : udpAddressesMap.entrySet()) {
			// gets current address
			Integer currentAddressType = udpAddressEntry.getKey();
			String currentAddress = udpAddressEntry.getValue();

			// gets current port
			int currentPort = udpPortsMap.get(currentAddressType).intValue();

			// creates AddressInfo basing on current address
			byte[] currentAddressAsBytes = ByteUtils.stringIPAddressToBytes(currentAddress);
			boolean[] ipVersion = null;
			if (currentAddressAsBytes.length == 4) {
				ipVersion = AddressInfo.IP_V4;
			} else if (currentAddressAsBytes.length == 16) {
				ipVersion = AddressInfo.IP_V6;
			}
			// TODO is the rFlag OK?
			AddressInfo currentAddressInfo = new AddressInfo((byte) 0, false, ipVersion, (byte) 0, (byte) 0, 0,
					AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.integerToAddressType(currentAddressType.intValue()),
					currentPort, currentAddressAsBytes);
			peerInfo.addAddressInfo(currentAddressInfo);
		}
		// goes through the TCP addresses
        for (Map.Entry<Integer,String> tcpAddressEntry : tcpAddressesMap.entrySet()) {
			// gets current address
			Integer currentAddressType = tcpAddressEntry.getKey();
			String currentAddress = tcpAddressEntry.getValue();

			// gets current port
			Integer currentPortAsInteger = tcpPortsMap.get(currentAddressType);
			int currentPort = currentPortAsInteger.intValue();

			// creates AddressInfo basing on current address
			byte[] currentAddressAsBytes = ByteUtils.stringIPAddressToBytes(currentAddress);
			boolean[] ipVersion = null;
			if (currentAddressAsBytes.length == 4) {
				ipVersion = AddressInfo.IP_V4;
			} else if (currentAddressAsBytes.length == 16) {
				ipVersion = AddressInfo.IP_V6;
			}
			// TODO is the rFlag OK?
			AddressInfo currentAddressInfo = new AddressInfo((byte) 0, false, ipVersion, (byte) 0, (byte) 0, 0,
					AddressInfo.TCP_TRANSPORT_TYPE, AddressInfo.integerToAddressType(currentAddressType.intValue()),
					currentPort, currentAddressAsBytes);

            if (LOG.isTraceEnabled()) LOG.trace("Filling PeerInfo with TCP AddressInfo " + currentAddressInfo);

			peerInfo.addAddressInfo(currentAddressInfo);
		}

		return peerInfo;
	}

	/**
	 * Returns P2POptions object describing a peer-to-peer protocol being used.
	 *
	 * @return
	 */
	public P2POptions getOptions() {
		return options;
	}

	public void setOptions(P2POptions options) {
		this.options = options;
	}

	/**
	 * Sets value of UnhashedID object.
	 *
	 * @param bytes
	 */
	public void setUnhashedID(byte[] bytes) {
		if (LOG.isTraceEnabled()) LOG.trace("unhashedID=" + ByteUtils.byteArrayToHexString(bytes));
		unhashedIDBytes = bytes;
	}

	/**
	 * Informs entities listener about outgoing message.
	 */
	public void fireOnSend(Message message) {
		outgoingListener.onSend(message);
	}

	/**
	 * Adds given address to this manager. TODO probably shouldn't contain hardcoded integers.
	 *
	 * @param addressAsString
	 * @param port
	 * @param ht
	 * @param tt
	 */
	public void addAddress(String addressAsString, int port, boolean[] ht, boolean[] tt) {
		if (Arrays.equals(tt, AddressInfo.UDP_TRANSPORT_TYPE)) {
             LOG.info("UDP");
			Integer addressTypeAsInteger = new Integer(AddressInfo.addressTypeToInteger(ht));
			udpAddressesMap.put(addressTypeAsInteger, addressAsString);
			udpPortsMap.put(addressTypeAsInteger, new Integer(port));
		} else /*if (Arrays.equals(tt, AddressInfo.TCP_TRANSPORT_TYPE))*/ {
             LOG.info("TCP");
			Integer addressTypeAsInteger = new Integer(AddressInfo.addressTypeToInteger(ht));
			tcpAddressesMap.put(addressTypeAsInteger, addressAsString);
			tcpPortsMap.put(addressTypeAsInteger, new Integer(port));
		}
	}

	/**
	 * Sets peerID value.
	 *
	 * @param peerIDAsBytes
	 */
	public void setPeerIDAsBytes(byte[] peerIDAsBytes) {
		this.peerIDAsBytes = peerIDAsBytes;
	}

	/**
	 * Resets information stored here, so that this manager is in the same state as when it was created. To be used when
	 * node leaves an overlay.
	 */
	public void leaveReset() {
		// creates sourceID for a state before bootstrap (has to be changed during bootstrapping process)
		peerIDAsBytes = SOURCE_ID_BEFORE_BOOTSTRAP;

		options = null;

	}

}
