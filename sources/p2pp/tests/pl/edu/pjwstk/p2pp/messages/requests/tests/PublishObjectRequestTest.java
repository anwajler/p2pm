package pl.edu.pjwstk.p2pp.messages.requests.tests;

import static org.junit.Assert.assertArrayEquals;

import java.util.Vector;

import org.junit.Test;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.requests.PublishObjectRequest;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.Certificate;
import pl.edu.pjwstk.p2pp.objects.Expires;
import pl.edu.pjwstk.p2pp.objects.Owner;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceID;
import pl.edu.pjwstk.p2pp.objects.Signature;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;
import pl.edu.pjwstk.p2pp.objects.Uptime;
import pl.edu.pjwstk.p2pp.resources.UserInfoResourceObject;

public class PublishObjectRequestTest {

	@Test
	public void testAsBytes() {

		// creates subobjects for request
		AddressInfo addrInfo0 = new AddressInfo((byte) 3, false, AddressInfo.IP_V4, (byte) 3, (byte) 5, 5225,
				AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.HOST_ADDRESS_TYPE, 891, new byte[] { 10, 11, 12, 13 });
		// RequestOptions reqOpt0 = new RequestOptions(true, false, true, false, true, false, true);
		PeerInfo peerInfo0 = new PeerInfo(new PeerID(new byte[] { 9, 8, 7, 6, 5, 1 }), new Uptime(389), addrInfo0,
				new UnhashedID(new byte[] { 90, 80, 70, 60 }));
		Vector<AddressInfo> addrVect0 = new Vector<AddressInfo>();
		addrVect0.add(addrInfo0);
		UserInfoResourceObject resObj0 = new UserInfoResourceObject(new Owner(new byte[] { 66, 55, 44 }), new Expires(
				1000), new Signature(new byte[] { 90, 12, 5, 55 }), new Certificate(true, new byte[] { 3, 1, 4 }),
				addrVect0);
		resObj0.setResourceID(new ResourceID(new byte[] { 1, 2, 2, 3 }));

		// creates publish object request
		PublishObjectRequest req0 = new PublishObjectRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false,
				(byte) 255, new byte[] { 4, 4, 4, 2 }, new byte[] { 1, 2, 3, 4, 5, 6 }, false, false, null, peerInfo0,
				resObj0);

		// expected
		byte[] exp0 = new byte[] { 68, 0, 9, (byte) 255, // header
				89, 106, (byte) 191, 13, // magic cookie
				4, 4, 4, 2, // transaction id
				0, 0, 0, 116, // TODO length
				1, 2, 3, 4, 5, 6, // sourceID
				0, 0, 0, 45, // peerinfo header TODO length
				1, 0, 0, 6, // peerid header
				9, 8, 7, 6, 5, 1, // peerid value
				4, 0, 0, 4, // uptime header
				0, 0, 1, (byte) 133, // uptime value
				2, 0, 0, 15, // AddressInfo header
				3, 4, 3, 5, // AddressInfo first four bytes
				0, 0, 20, 105, // AddressInfo priority
				0, 3, 123,// AddressInfo ht, tt, port
				10, 11, 12, 13, // AddressInfo IPv4
				3, 0, 0, 4, // UnhashedID header
				90, 80, 70, 60, // UnhashedID value
				13, 0, 0, 63, // resourceobject header TODO length
				0, 0, // resourceobject cont type and cont subtype
				11, 0, 0, 4, // resourceid header // 59
				1, 2, 2, 3,// resourceid value
				25, 0, 0, 19, // resourceobjectvalue header TODO length
				2, 0, 0, 15, // AddressInfo header
				3, 4, 3, 5, // AddressInfo first four bytes
				0, 0, 20, 105, // AddressInfo priority // 79
				0, 3, 123, // AddressInfo ht, tt, port
				10, 11, 12, 13, // AddressInfo IPv4
				15, 0, 0, 3, // owner header // 90
				66, 55, 44, // owner value
				14, 0, 0, 4, // expires header
				0, 0, 3, (byte) 232, // expires value
				18, 0, 0, 4, // signature header
				90, 12, 5, 55, // signature value
				18, 0, 0, 3, // certificate header
				3, 1, 4, // certificate value // 116
		};

		byte[] act0 = req0.asBytes();

		// compares expected value with generated one
		assertArrayEquals(exp0, act0);

	}
}
