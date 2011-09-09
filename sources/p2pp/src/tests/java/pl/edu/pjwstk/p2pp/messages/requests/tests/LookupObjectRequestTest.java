package pl.edu.pjwstk.p2pp.messages.requests.tests;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.LookupObjectRequest;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RLookup;
import pl.edu.pjwstk.p2pp.objects.ResourceID;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;

public class LookupObjectRequestTest {

	@Test
	public void testAsBytes() {

		// creates request with subobjects
		PeerID i0 = new PeerID(new byte[] { 99, 99, 99, 99 });
		AddressInfo a0 = new AddressInfo((byte) 0, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 0,
				AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.HOST_ADDRESS_TYPE, 9876, new byte[] { (byte) 192,
						(byte) 168, 1, 12 });
		UnhashedID u0 = new UnhashedID(new byte[] { 33, 33, 33, 33 });
		PeerInfo p0 = new PeerInfo(i0, null, a0, u0);
		ResourceID ri0 = new ResourceID(new byte[] { 7, 8, 9, 10 });
		RLookup r0 = new RLookup((byte) 3, (byte) 7, ri0, null);
		LookupObjectRequest l0 = new LookupObjectRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false,
				(byte) 255, new byte[] { 1, 1, 1, 1 }, new byte[] { 2, 2, 2, 2 }, false, false, null, p0, r0);

		// gets byte representation of request
		byte[] ac0 = l0.asBytes();

		// creates expected byte representation
		byte[] e0 = new byte[] { 68, 0, 10, (byte) 255, // header
				89, 106, (byte) 191, 13, // magic cookie
				1, 1, 1, 1, // transaction id
				0, 0, 0, 53, // TODO message length
				2, 2, 2, 2, // sourceID
				0, 0, 0, 35, // Peerinfo header
				1, 0, 0, 4, // PeerID header
				99, 99, 99, 99, // PeerID value
				2, 0, 0, 15, // AddressInfo header
				0, 4, 0, 0, // AddressInfo first four bytes
				0, 0, 0, 0, // AddressInfo priority
				0, 38, (byte) 148,// AddressInfo ht, tt, port
				(byte) 192, (byte) 168, 1, 12, // AddressInfo IPv4
				3, 0, 0, 4, // UnhashedID header
				33, 33, 33, 33, // UnhashedID value
				12, 0, 0, 10, // RLookup length
				3, 7, // cont type and subtype
				11, 0, 0, 4, // resourceid header
				7, 8, 9, 10, // resourceid value
		};

		assertArrayEquals(e0, ac0);
	}
}
