package tests;

import java.math.BigInteger;

/**
 * Tests of BigInteger class and how to use it.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class BigIntegerTest {
	public static void main(String[] args) {

		byte[] a1 = new byte[1];
		a1[0] = (byte) 1;
		print(new BigInteger(a1));

		byte[] a2 = new byte[1];
		a2[0] = (byte) 2;
		print(new BigInteger(a2));

		byte[] a3 = new byte[1];
		a3[0] = (byte) 3;
		print(new BigInteger(a3));

		byte[] a4 = new byte[1];
		a4[0] = (byte) 4;
		print(new BigInteger(a4));

		byte[] a5 = new byte[1];
		a5[0] = (byte) 5;
		print(new BigInteger(a5));

		byte[] a6 = new byte[1];
		a6[0] = (byte) 32;
		print(new BigInteger(a6));

		byte[] a7 = new byte[1];
		a7[0] = (byte) 33;
		print(new BigInteger(a7));

		byte[] a8 = new byte[2];
		a8[0] = (byte) 1;
		print(new BigInteger(a8));

		byte[] a9 = new byte[1];
		print(new BigInteger(a9));

		byte[] a10 = new byte[2];
		a10[1] = (byte) 1;
		print(new BigInteger(a10));

		byte[] a11 = new byte[2];
		a11[1] = (byte) 100;
		print(new BigInteger(a11));

		byte[] a12 = new byte[2];
		a12[1] = (byte) 255;
		print(new BigInteger(a12));

	}

	public static void print(BigInteger integer) {

		System.out.println("integer=" + integer);
		System.out.print("bits=");
		for (int i = integer.bitLength() - 1; i >= 0; i--) {
			int bit = 0;
			if (integer.testBit(i)) {
				bit = 1;
			}
			System.out.print(bit);
		}
		System.out.println();
	}
}
