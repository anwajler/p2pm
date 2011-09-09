package pl.edu.pjwstk.p2pp.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class ByteUtilsTests {

	@Test
	public void testBytesToLong() {
		byte b00 = 0;

		long a0 = ByteUtils.bytesToLong(b00, b00, b00, b00);

		long e0 = 0;

		assertEquals(e0, a0);
	}

	@Test
	public void testGoodAddByteToArrayAtBitIndex() {

		byte[] a0 = { 0x00 };
		byte[] a1 = { 0x00, 0x00 };
		byte[] a2 = { 0x00, 0x00, 0x00 };
		byte[] a3 = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
		byte[] a4 = { (byte) 0xF0, (byte) 0x0F, (byte) 0x00 };
		byte[] a5 = { (byte) 0xAC, (byte) 0x2F };
		byte[] a6 = { (byte) 0x30, (byte) 0xCA };
		byte[] a7 = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xAA,
				(byte) 0xAC, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

		byte b0 = (byte) 0xFF;
		byte b1 = (byte) 0xFF;
		byte b2 = (byte) 0xFF;
		byte b3 = (byte) 0x00;
		byte b4 = (byte) 0xFF;
		byte b5 = (byte) 0x99;
		byte b6 = (byte) 0xE0;
		byte b7 = (byte) 0xFF;

		int i0 = 0;
		int i1 = 4;
		int i2 = 12;
		int i3 = 4;
		int i4 = 4;
		int i5 = 3;
		int i6 = 7;
		int i7 = (38 * 8) + 2;

		byte[] e0 = { (byte) 0xFF };
		byte[] e1 = { 0x0F, (byte) 0xF0 };
		byte[] e2 = { 0x00, 0x0F, (byte) 0xF0 };
		byte[] e3 = { (byte) 0xF0, 0x0F, (byte) 0xFF };
		byte[] e4 = { (byte) 0xFF, (byte) 0xFF, 0x00 };
		byte[] e5 = { (byte) 0xB3, (byte) 0x2F };
		byte[] e6 = { (byte) 0x31, (byte) 0xC0 };
		byte[] e7 = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xBF,
				(byte) 0xEC, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

		ByteUtils.addByteToArrayAtBitIndex(b0, a0, i0);
		ByteUtils.addByteToArrayAtBitIndex(b1, a1, i1);
		ByteUtils.addByteToArrayAtBitIndex(b2, a2, i2);
		ByteUtils.addByteToArrayAtBitIndex(b3, a3, i3);
		ByteUtils.addByteToArrayAtBitIndex(b4, a4, i4);
		ByteUtils.addByteToArrayAtBitIndex(b5, a5, i5);
		ByteUtils.addByteToArrayAtBitIndex(b6, a6, i6);
		ByteUtils.addByteToArrayAtBitIndex(b7, a7, i7);

		assertArrayEquals(e0, a0);
		assertArrayEquals(e1, a1);
		assertArrayEquals(e2, a2);
		assertArrayEquals(e3, a3);
		assertArrayEquals(e4, a4);
		assertArrayEquals(e5, a5);
		assertArrayEquals(e6, a6);
		assertArrayEquals(e7, a7);

	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void testOutOfBoundsAddByteToArrayAtBitIndex() {

		byte[] a0 = { 0x00 };

		byte b0 = (byte) 0xFF;

		int i0 = 1;

		ByteUtils.addByteToArrayAtBitIndex(b0, a0, i0);

	}

	@Test
	public void testAddBooleanArrayToArrayAtIndex() {

		int i0 = 0;
		int i1 = 0;
		int i2 = 0;

		boolean[] s0 = new boolean[] { false, true, true, true, true, true, true, true };
		boolean[] s1 = new boolean[] { true, true, true, true, true, true, true, true };
		boolean[] s2 = new boolean[] { false, false, false, false, false, false, false, false };

		byte[] a0 = new byte[] { 0 };
		byte[] a1 = new byte[] { 0 };
		byte[] a2 = new byte[] { -1 };

		byte[] e0 = new byte[] { 127 };
		byte[] e1 = new byte[] { -1 };
		byte[] e2 = new byte[] { 0 };

		ByteUtils.addBooleanArrayToArrayAtIndex(s0, a0, i0);
		ByteUtils.addBooleanArrayToArrayAtIndex(s1, a1, i1);
		ByteUtils.addBooleanArrayToArrayAtIndex(s2, a2, i2);

		assertArrayEquals(e0, a0);
		assertArrayEquals(e1, a1);
		assertArrayEquals(e2, a2);

	}

	@Test(expected = NullPointerException.class)
	public void testNullAddByteToArrayAtBitIndex() {

		byte[] a3 = null;
		byte[] a4 = { 0x00, 0x00 };

		byte b3 = 0x7F;
		byte b4 = 0x7F;

		int i3 = 5;
		int i4 = 10;

		byte[] e3 = null;
		byte[] e4 = null;

		ByteUtils.addByteToArrayAtBitIndex(b3, a3, i3);
		ByteUtils.addByteToArrayAtBitIndex(b4, a4, i4);

		assertArrayEquals(e3, a3);
		assertArrayEquals(e4, a4);

	}

	@Test
	public void testCopyBitsToByte() {

		byte f0 = (byte) 0xAA; // 10101010
		byte f1 = (byte) 0xFF; // 11111111
		byte f2 = 0x01; // 00000001
		byte f3 = 0x77; // 01110111
		byte f4 = (byte) 0x88; // 10001000
		byte f5 = (byte) 0xE9; // 11101001
		byte f6 = (byte) 0xFF; // 11111111
		byte f7 = (byte) 0x00; // 00000000
		byte f8 = (byte) 0x00; // 00000000
		byte f9 = (byte) 0x3D; // 00111101
		byte f10 = (byte) 0x7C; // 01111100
		byte f11 = (byte) 0xFF; // 11111111

		byte s0 = 0x00; // 00000000
		byte s1 = 0x00; // 00000000
		byte s2 = (byte) 0xFF; // 11111111
		byte s3 = 0x15; // 00010101
		byte s4 = (byte) 0xAD; // 10101101
		byte s5 = (byte) 0x17; // 00010111
		byte s6 = 0x00; // 00000000
		byte s7 = (byte) 0xFF; // 11111111
		byte s8 = (byte) 0xFF; // 11111111
		byte s9 = (byte) 0x4F; // 01001111
		byte s10 = (byte) 0x52; // 01010010
		byte s11 = (byte) 0x00; // 00000000

		int fi0 = 0;
		int fi1 = 0;
		int fi2 = 2;
		int fi3 = 4;
		int fi4 = 3;
		int fi5 = 5;
		int fi6 = 0;
		int fi7 = 8;
		int fi8 = 8;
		int fi9 = 4;
		int fi10 = 1;
		int fi11 = 0;

		int si0 = 0;
		int si1 = 0;
		int si2 = 4;
		int si3 = 2;
		int si4 = 5;
		int si5 = 2;
		int si6 = 8;
		int si7 = 0;
		int si8 = 8;
		int si9 = 3;
		int si10 = 2;
		int si11 = 4;

		byte e0 = (byte) 0xAA; // 10101010, 0, 00000000, 0 -> 10101010
		byte e1 = (byte) 0xFF; // 11111111, 0, 00000000, 1 -> 11111111
		byte e2 = (byte) 0xF0; // 00000001, 2, 11111111, 4 -> 11110000
		byte e3 = (byte) 0x1D; // 01110111, 4, 00010101, 2 -> 00011101
		byte e4 = (byte) 0xAA; // 10001000, 3, 10101101, 5 -> 10101010
		byte e5 = (byte) 0x0F; // 11101001, 5, 00010111, 2 -> 00001111
		byte e6 = (byte) 0x00; // 11111111, 0, 00000000, 8 -> 00000000
		byte e7 = (byte) 0xFF; // 00000000, 8, 11111111, 0 -> 11111111
		byte e8 = (byte) 0xFF; // 00000000, 8, 11111111, 8 -> 11111111
		byte e9 = (byte) 0x5B; // 0011,1101, 4, 010,0111,1, 3 -> 01011011
		byte e10 = (byte) 0x7E; // 01111100, 1, 01010010, 1 -> 01111110
		byte e11 = (byte) 0x0F; // 11111111, 0, 00000000, 4 -> 00001111

		byte a0 = ByteUtils.copyBitsToByte(f0, s0, fi0, si0);
		byte a1 = ByteUtils.copyBitsToByte(f1, s1, fi1, si1);
		byte a2 = ByteUtils.copyBitsToByte(f2, s2, fi2, si2);
		byte a3 = ByteUtils.copyBitsToByte(f3, s3, fi3, si3);
		byte a4 = ByteUtils.copyBitsToByte(f4, s4, fi4, si4);
		byte a5 = ByteUtils.copyBitsToByte(f5, s5, fi5, si5);
		byte a6 = ByteUtils.copyBitsToByte(f6, s6, fi6, si6);
		byte a7 = ByteUtils.copyBitsToByte(f7, s7, fi7, si7);
		byte a8 = ByteUtils.copyBitsToByte(f8, s8, fi8, si8);
		byte a9 = ByteUtils.copyBitsToByte(f9, s9, fi9, si9);
		byte a10 = ByteUtils.copyBitsToByte(f10, s10, fi10, si10);
		byte a11 = ByteUtils.copyBitsToByte(f11, s11, fi11, si11);

		/*
		 * System.out.println(Integer.toBinaryString(a0 & 0xFF)); System.out.println(Integer.toBinaryString(a1 & 0xFF));
		 * System.out.println(Integer.toBinaryString(a2 & 0xFF)); System.out.println(Integer.toBinaryString(a3 & 0xFF));
		 */

		assertEquals(e0, a0);
		assertEquals(e1, a1);
		assertEquals(e2, a2);
		assertEquals(e3, a3);
		assertEquals(e4, a4);
		assertEquals(e5, a5);
		assertEquals(e6, a6);
		assertEquals(e7, a7);
		assertEquals(e8, a8);
		assertEquals(e9, a9);
		assertEquals(e10, a10);
		assertEquals(e11, a11);
	}

	@Test
	public void testAddIntToArrayOfBytes() {

		int a0 = 0;
		int a1 = 0;
		int a2 = 0;
		@SuppressWarnings("unused")
		int a3 = 293754;

		byte[] b0 = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
		byte[] b1 = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
		byte[] b2 = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
		@SuppressWarnings("unused")
		byte[] b3 = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		int i0 = 0;
		int i1 = 8;
		int i2 = 4;
		@SuppressWarnings("unused")
		int i3 = 20;

		byte[] e0 = { 0x00, 0x00, 0x00, 0x00 };
		byte[] e1 = { (byte) 0xFF, 0x00, 0x00, 0x00, 0x00 };
		byte[] e2 = { (byte) 0xF0, 0x00, 0x00, 0x00, 0x0F };
		// byte[] e3 = {0x00, 0x00, (byte)(a3)

		ByteUtils.addIntToArrayAtBitIndex(a0, b0, i0);
		ByteUtils.addIntToArrayAtBitIndex(a1, b1, i1);
		ByteUtils.addIntToArrayAtBitIndex(a2, b2, i2);

		assertArrayEquals(e0, b0);
		assertArrayEquals(e1, b1);
		assertArrayEquals(e2, b2);

	}

	@Test
	public void testAddShortToArrayAtBitIndex() {
		byte[] a0 = new byte[2];
		byte[] a1 = new byte[2];

		short s0 = 1;
		short s1 = 255;

		int i0 = 0;
		int i1 = 0;

		byte[] e0 = new byte[] { 0, 1 };
		byte[] e1 = new byte[] { 0, -1 };

		ByteUtils.addShortToArrayAtBitIndex(s0, a0, i0);
		ByteUtils.addShortToArrayAtBitIndex(s1, a1, i1);

		assertArrayEquals(e0, a0);
		assertArrayEquals(e1, a1);
	}

	@Test
	public void testStringIPAddressToBytes() {
		String a0 = "10.10.10.2";
		String a1 = "192.168.1.50";

		byte[] b0 = ByteUtils.stringIPAddressToBytes(a0);
		byte[] b1 = ByteUtils.stringIPAddressToBytes(a1);

		byte[] e0 = new byte[] { 10, 10, 10, 2 };
		byte[] e1 = new byte[] { (byte) 192, (byte) 168, 1, 50 };

		assertArrayEquals(e0, b0);
		assertArrayEquals(e1, b1);
	}

	@Test
	public void testByteArrayToHexString() {
		byte[] a0 = new byte[] { 0 };
		byte[] a1 = new byte[] { 0, 0 };
		byte[] a2 = new byte[] { 1, 2, 3 };
		byte[] a3 = new byte[] { (byte) 255, (byte) 254, (byte) 253 };
		byte[] a4 = null;

		String h0 = ByteUtils.byteArrayToHexString(a0);
		String h1 = ByteUtils.byteArrayToHexString(a1);
		String h2 = ByteUtils.byteArrayToHexString(a2);
		String h3 = ByteUtils.byteArrayToHexString(a3);
		String h4 = ByteUtils.byteArrayToHexString(a4);

		String e0 = "00";
		String e1 = "0000";
		String e2 = "010203";
		String e3 = "fffefd";
		String e4 = null;

		assertEquals(e0, h0);
		assertEquals(e1, h1);
		assertEquals(e2, h2);
		assertEquals(e3, h3);
		assertEquals(e4, h4);
	}

	@Test
	public void testBooleanArrayToInt() {
		boolean[] b0 = new boolean[] { false, false };
		boolean[] b1 = new boolean[] { false, true };
		boolean[] b2 = new boolean[] { true, false };
		boolean[] b3 = new boolean[] { true, true };
		boolean[] b4 = new boolean[] { true, true, true, true, true, true };
		boolean[] b5 = new boolean[] { true, true, false, false, true, false };
		boolean[] b6 = new boolean[] { false, true, true, false, false, true, false, false, false };
		boolean[] b7 = new boolean[] { true, false, false, true, false, true, true, true, false };

		int i0 = ByteUtils.booleanArrayToInt(b0);
		int i1 = ByteUtils.booleanArrayToInt(b1);
		int i2 = ByteUtils.booleanArrayToInt(b2);
		int i3 = ByteUtils.booleanArrayToInt(b3);
		int i4 = ByteUtils.booleanArrayToInt(b4);
		int i5 = ByteUtils.booleanArrayToInt(b5);
		int i6 = ByteUtils.booleanArrayToInt(b6);
		int i7 = ByteUtils.booleanArrayToInt(b7);

		int e0 = 0;
		int e1 = 1;
		int e2 = 2;
		int e3 = 3;
		int e4 = 63;
		int e5 = 50;
		int e6 = 200;
		int e7 = 302;

		assertEquals(e0, i0);
		assertEquals(e1, i1);
		assertEquals(e2, i2);
		assertEquals(e3, i3);
		assertEquals(e4, i4);
		assertEquals(e5, i5);
		assertEquals(e6, i6);
		assertEquals(e7, i7);
	}

	@Test
	public void testGetBitsFromByteArray() {

		byte[] b0 = new byte[] { 0 };
		byte[] b1 = new byte[] { 1 };
		byte[] b2 = new byte[] { (byte) 255 };
		byte[] b3 = new byte[] { (byte) 170, (byte) 170 };
		byte[] b4 = new byte[] { 1, (byte) 170 };
		byte[] b5 = new byte[] { 1, (byte) 170, 0 };
		byte[] b6 = new byte[] { (byte) 170, (byte) 170, 0 };

		boolean[] a0 = ByteUtils.getBitsFromByteArray(b0, 0, 8);
		boolean[] a1 = ByteUtils.getBitsFromByteArray(b1, 4, 4);
		boolean[] a2 = ByteUtils.getBitsFromByteArray(b2, 4, 4);
		boolean[] a3 = ByteUtils.getBitsFromByteArray(b3, 4, 8);
		boolean[] a4 = ByteUtils.getBitsFromByteArray(b4, 7, 9);
		boolean[] a5 = ByteUtils.getBitsFromByteArray(b5, 7, 10);
		boolean[] a6 = ByteUtils.getBitsFromByteArray(b6, 4, 16);

		boolean[] e0 = new boolean[] { false, false, false, false, false, false, false, false };
		boolean[] e1 = new boolean[] { false, false, false, true };
		boolean[] e2 = new boolean[] { true, true, true, true };
		boolean[] e3 = new boolean[] { true, false, true, false, true, false, true, false };
		boolean[] e4 = new boolean[] { true, true, false, true, false, true, false, true, false };
		boolean[] e5 = new boolean[] { true, true, false, true, false, true, false, true, false, false };
		boolean[] e6 = new boolean[] { true, false, true, false, true, false, true, false, true, false, true, false,
				false, false, false, false };

		assertEquals(true, Arrays.equals(e0, a0));
		assertEquals(true, Arrays.equals(e1, a1));
		assertEquals(true, Arrays.equals(e2, a2));
		assertEquals(true, Arrays.equals(e3, a3));
		assertEquals(true, Arrays.equals(e4, a4));
		assertEquals(true, Arrays.equals(e5, a5));
		assertEquals(true, Arrays.equals(e6, a6));
	}
}
