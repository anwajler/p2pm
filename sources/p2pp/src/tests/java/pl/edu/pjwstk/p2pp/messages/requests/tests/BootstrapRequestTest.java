package pl.edu.pjwstk.p2pp.messages.requests.tests;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.BootstrapRequest;
import pl.edu.pjwstk.p2pp.messages.responses.Response;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.objects.Uptime;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

public class BootstrapRequestTest {

	@Test
	public void testCreateResponse() throws UnsupportedGeneralObjectException {

		// creates peerinfo and subobjects for peer sending bootstrap request
		PeerID id0 = new PeerID(new byte[4]);
		Uptime u0 = new Uptime(0);
		AddressInfo ai0 = new AddressInfo((byte) 1, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 125,
				AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.HOST_ADDRESS_TYPE, 9876, new byte[] { 100, 120, 20, 2 });
		UnhashedID ui0 = new UnhashedID(new byte[] { 1, 2, 3, 4 });
		PeerInfo pi0 = new PeerInfo(id0, u0, ai0, ui0);

		// creates request
		BootstrapRequest r0 = new BootstrapRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255,
				new byte[] { 0, 1, 0, 1 }, false, false, pi0);

		// creates peerinfo and subojects for bootstrap peer
		PeerID bid0 = new PeerID(new byte[4]);
		Uptime bu0 = new Uptime(0);
		AddressInfo bai0 = new AddressInfo((byte) 1, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 125,
				AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.HOST_ADDRESS_TYPE, 9876, new byte[] { 100, 120, 20, 2 });
		UnhashedID bui0 = new UnhashedID(new byte[] { 1, 2, 3, 4 });
		PeerInfo bpi0 = new PeerInfo(bid0, bu0, bai0, bui0);

		// creates p2poptions
		P2POptions o0 = new P2POptions(P2PPUtils.SHA1_HASH_ALGORITHM, (byte) 20, P2PPUtils.KADEMLIA_P2P_ALGORITHM,
				(byte) 2, new byte[] { 5, 6, 7, 8 });

		// creates response for request
		Response res0 = r0.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, bpi0, o0, new byte[] { 9, 5, 7, 3 });

		// creates peerinfo and subobjects for bootstrapping candidates
		@SuppressWarnings("unused")
		PeerID cid0 = new PeerID(new byte[4]);
		@SuppressWarnings("unused")
		Uptime cu0 = new Uptime(0);
		@SuppressWarnings("unused")
		AddressInfo cai0 = new AddressInfo((byte) 1, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 125,
				AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.HOST_ADDRESS_TYPE, 9876, new byte[] { 100, 120, 20, 2 });
		@SuppressWarnings("unused")
		UnhashedID cui0 = new UnhashedID(new byte[] { 1, 2, 3, 4 });
		PeerInfo cpi0 = new PeerInfo(id0, u0, ai0, ui0);

		// adds bootstrapping candidate to response
		res0.addObject(cpi0);

		// creates byte representation of response
		byte[] b0 = res0.asBytes();

		// creates expected byte representation of message
		byte[] e0 = new byte[] { 116, (byte) 200, 2, (byte) 255,// header
				89, 106, (byte) 191, 13, // magic cookie
				0, 1, 0, 1, // transactionID
				0, 0, 0, (byte) 146, // TODO message length
				0, 0, 0, 0, // constant sourceID
				0, 0, 0, 1, // constant responseID
				0, 0, 0, 43, // TODO peerinfo length of bootstrap peer
				1, 0, 0, 4, // peerID header
				0, 0, 0, 0, // peerID value
				4, 0, 0, 4, // uptime header
				0, 0, 0, 0, // uptime value
				2, 0, 0, 15, // address info header
				1, 4, 0, 0, // address info first four byte
				0, 0, 0, 125, // address info priority
				0, 38, (byte) 148, // address info ht, tt, port
				100, 120, 20, 2, // address info IP address
				3, 0, 0, 4, // unhashedID header
				1, 2, 3, 4, // unhashedID value
				0, 0, 0, 35, // TODO peerinfo length of request originator
				1, 0, 0, 4, // peerID header
				9, 5, 7, 3, // peerID value
				2, 0, 0, 15, // address info header
				1, 4, 0, 0, // address info first four byte
				0, 0, 0, 125, // address info priority
				0, 38, (byte) 148, // address info ht, tt, port
				100, 120, 20, 2, // address info IP address
				3, 0, 0, 4, // unhashedID header
				1, 2, 3, 4, // unhashedID value
				5, 0, 0, 9, // p2poptions header
				1, 20, 2, 2, // first four bytes of p2poptions
				4, 5, 6, 7, 8, // overlayidlength and overlayid of p2poptions
				0, 0, 0, 43, // TODO peerinfo length
				1, 0, 0, 4, // peerID header
				0, 0, 0, 0, // peerID value
				4, 0, 0, 4, // uptime header
				0, 0, 0, 0, // uptime value
				2, 0, 0, 15, // address info header
				1, 4, 0, 0, // address info first four byte
				0, 0, 0, 125, // address info priority
				0, 38, (byte) 148, // address info ht, tt, port
				100, 120, 20, 2, // address info IP address
				3, 0, 0, 4, // unhashedID header
				1, 2, 3, 4, // unhashedID value
		};

		System.out.println(e0[31]);

		// tests
		assertArrayEquals(b0, e0);
	}

	@Test
	public void testRequest() {

		// creates peerinfo and subobjects
		PeerID id0 = new PeerID(new byte[4]);
		Uptime u0 = new Uptime(0);
		AddressInfo ai0 = new AddressInfo((byte) 1, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 125,
				AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.HOST_ADDRESS_TYPE, 9876, new byte[] { 100, 120, 20, 2 });
		UnhashedID ui0 = new UnhashedID(new byte[] { 1, 2, 3, 4 });
		PeerInfo pi0 = new PeerInfo(id0, u0, ai0, ui0);

		// adds peerinfo to request
		BootstrapRequest r0 = new BootstrapRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255,
				new byte[] { 0, 1, 0, 1 }, false, false, pi0);

		// gets byte representation of message
		byte[] b0 = r0.asBytes();

		// creates expected byte representation of message
		byte[] e0 = new byte[] { 68, 0, 2, (byte) 255,// first part of header
				89, 106, (byte) 191, 13, // magic cookie
				0, 1, 0, 1, // transactionID
				0, 0, 0, 47, // TODO message length
				0, 0, 0, 0, // constant sourceID
				0, 0, 0, 43, // TODO peerinfo length
				1, 0, 0, 4, // peerID header
				0, 0, 0, 0, // peerID value
				4, 0, 0, 4, // uptime header
				0, 0, 0, 0, // uptime value
				2, 0, 0, 15, // address info header
				1, 4, 0, 0, // address info first four byte
				0, 0, 0, 125, // address info priority
				0, 38, (byte) 148, // address info ht, tt, port
				100, 120, 20, 2, // address info IP address
				3, 0, 0, 4, // unhashedID header
				1, 2, 3, 4 // unhashedID value
		};

		// tests
		assertArrayEquals(b0, e0);

	}
}
