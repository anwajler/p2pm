package pl.edu.pjwstk.p2pp.messages.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;

public class MessageTests {

	@Test
	public void testMessage() {
		fail("Not yet implemented");
	}

	@Test
	public void testAsBytes() {

	}

	@Test
	public void testGetBitsCount() {
		fail("Not yet implemented");
	}

	@Test
	public void testByteToBitString() {
		fail("Not yet implemented");
	}

	@Test
	public void testCalculateBytesToStoreBits() {
		int b0 = 8;
		int b1 = 16;
		int b2 = 20;
		int b3 = 0;
		int b4 = 3;
		int b5 = -2;
		int b6 = 15;
		int b7 = 10001;
		@SuppressWarnings("unused")
		int b8 = 1000001;

		int r0 = 1;
		int r1 = 2;
		int r2 = 3;
		int r3 = 0;
		int r4 = 1;
		int r5 = 0;
		int r6 = 2;
		int r7 = 1251;
		@SuppressWarnings("unused")
		int r8 = 125001;

		assertEquals("" + b0, r0, P2PPMessage.calculateBytesToStoreBits(b0));
		assertEquals("" + b1, r1, P2PPMessage.calculateBytesToStoreBits(b1));
		assertEquals("" + b2, r2, P2PPMessage.calculateBytesToStoreBits(b2));
		assertEquals("" + b3, r3, P2PPMessage.calculateBytesToStoreBits(b3));
		assertEquals("" + b4, r4, P2PPMessage.calculateBytesToStoreBits(b4));
		assertEquals("" + b5, r5, P2PPMessage.calculateBytesToStoreBits(b5));
		assertEquals("" + b6, r6, P2PPMessage.calculateBytesToStoreBits(b6));
		assertEquals("" + b7, r7, P2PPMessage.calculateBytesToStoreBits(b7));
	}

}
