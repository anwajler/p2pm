package pl.edu.pjwstk.p2pp.messages.responses.unittests;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.responses.BootstrapResponse;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;
import pl.edu.pjwstk.p2pp.objects.Uptime;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

public class BootstrapResponseTest {

	@Test
	public void testAsBytes() {
		// bootstrap peerinfo
		PeerID bi0 = new PeerID(new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255 });
		Uptime bup0 = new Uptime(100);
		AddressInfo ba0 = new AddressInfo((byte) 1, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 0,
				AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.HOST_ADDRESS_TYPE, 7080, new byte[] { (byte) 192,
						(byte) 168, 1, 50 });
		// UnhashedID bu0 = new UnhashedID(new byte[] { 3, 3, 3, 3 });
		PeerInfo bp0 = new PeerInfo(bi0, bup0, ba0, null);

		// requesting peerinfo
		PeerID ri0 = new PeerID(new byte[] { (byte) 10, (byte) 10, (byte) 10, (byte) 10 });
		AddressInfo ra0 = new AddressInfo((byte) 1, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 0,
				AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.HOST_ADDRESS_TYPE, 7888, new byte[] { (byte) 192,
						(byte) 168, 1, 50 });
		UnhashedID ru0 = new UnhashedID(new byte[] { 6, 6, 6, 6 });
		PeerInfo rp0 = new PeerInfo(ri0, null, ra0, ru0);

		// creates p2poptions
		P2POptions p20 = new P2POptions(P2PPUtils.SHA1_HASH_ALGORITHM, (byte) 20, P2PPUtils.KADEMLIA_P2P_ALGORITHM,
				(byte) 2, new byte[] { 2, 2, 2, 2 });

		// creates bootstrap response
		BootstrapResponse r0 = new BootstrapResponse(P2PPMessage.P2PP_PROTOCOL_VERSION_1,
				P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE, false, true, false, Response.RESPONSE_CODE_OK_BITS_ARRAY,
				(byte) 255, new byte[4],// transactionID
				false, false, bp0, null, p20);
		r0.addObject(rp0);

		// creates byte representation of message
		byte[] b0 = r0.asBytes();

		// creates expected byte representation
		byte[] e0 = new byte[] { 116, (byte) 200, 2, (byte) 255,// first 4 bytes
				89, 106, (byte) 191, 13, // magic cookie
				0, 0, 0, 0, // transactionID
				0, 0, 0, 91, // message length
				0, 0, 0, 0, // sourceID
				0, 0, 0, 1, // responseID
				0, 0, 0, 35, // peerinfo header of bootstrap peer (check length)
				1, 0, 0, 4, // peerID header of bootstrap peer
				(byte) 255, (byte) 255, (byte) 255, (byte) 255, // peerIDValue
				4, 0, 0, 4, // uptime of bootstrap peer
				0, 0, 0, 100, // uptime value of bootstrap peer
				// resourceList isn't decided
				2, 0, 0, 15,// address info object header of bootstrap peer
				1, 4, 0, 0, // address info first four bytes of bootstrap peer
				0, 0, 0, 0, // address another four bytes of bootstrap peer
				0, 27, (byte) 168, // ht, tt, port of bootstrap peer
				(byte) 192, (byte) 168, 1, 50, // IP of bootstrap peer
				0, 0, 0, 35, // peerinfo header of request originator peer (TODO
				// check length)
				1, 0, 0, 4, // peerID header of request originator peer
				10, 10, 10, 10, // peerIDValue of request originator peer
				// resourceList isn't decided
				2, 0, 0, 15,// addressinfo header of reqeust originator peer
				1, 4, 0, 0, // address info first four bytes of request origpeer
				0, 0, 0, 0, // address another four bytes of request originapeer
				0, 30, (byte) 208, // ht, tt, port of request originator peer
				(byte) 192, (byte) 168, 1, 50, // IP of request originator peer
				3, 0, 0, 4, // unhashedID header of request originator peer
				6, 6, 6, 6, // unhashedID value of request originator peer
				5, 0, 0, 9,// p2poptions header of request originator peer
				1, 20, 2, 2,// first four bytes of request originator peer
				4, 32, 32, 32, 32 // overlayID and length of request orig peer
		};

		assertArrayEquals(e0, b0);
	}
}
