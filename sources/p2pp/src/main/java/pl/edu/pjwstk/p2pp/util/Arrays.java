package pl.edu.pjwstk.p2pp.util;

import java.util.List;

/**
 * Class with utility methods for comparing arrays. In J2SE and on Android this class exists, but J2ME doesn't have it,
 * so there's a need to create it.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public final class Arrays {

    /**
     * Compares two given arrays. They're equal if they have same length and if every corresponding pairs of elements
     * are equal. They're also equal if they're both null.
     *
     * @param arrayOne
     * @param arrayTwo
     * @return True if arrays are equal.
     */
    public static boolean equals(boolean[] arrayOne, boolean[] arrayTwo) {
        boolean result = true;
        if ((arrayOne != null && arrayTwo == null) || (arrayOne == null && arrayTwo != null)) {
            result = false;
        } else if (arrayOne != null) {
            if (arrayOne.length != arrayTwo.length) {
                result = false;
            } else {
                for (int i = 0; i < arrayOne.length; i++) {
                    if (arrayOne[i] != arrayTwo[i]) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Compares two given arrays. They're equal if they have same length and if every corresponding pairs of elements
     * are equal. They're also equal if they're both null.
     *
     * @param arrayOne
     * @param arrayTwo
     * @return True if arrays are equal.
     */
    public static boolean equals(byte[] arrayOne, byte[] arrayTwo) {
        boolean result = true;
        if ((arrayOne != null && arrayTwo == null) || (arrayOne == null && arrayTwo != null)) {
            result = false;
        } else if (arrayOne != null) {
            if (arrayOne.length != arrayTwo.length) {
                result = false;
            } else {
                for (int i = 0; i < arrayOne.length; i++) {
                    if (arrayOne[i] != arrayTwo[i]) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns true if given list contains at least one non-null element.
     *
     * @param list
     * @return
     */
    public static boolean containsNonNullElement(List<?> list) {

        for (Object current : list) {
            if (current != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert a byte[] array to readable string format. This makes the "hex"
     * readable!
     *
     * @param in byte[] buffer to convert to string format
     * @return result String buffer in String format
     */
    public static String byteArrayToHexString(byte in[]) {
        byte ch = 0x00;
        int i = 0;
        if (in == null || in.length <= 0)
            return null;

        String pseudo[] = {"0", "1", "2",
                "3", "4", "5", "6", "7", "8",
                "9", "A", "B", "C", "D", "E",
                "F"};
        StringBuffer out = new StringBuffer(in.length * 2);

        while (i < in.length) {
            ch = (byte) (in[i] & 0xF0);
            // Strip off high nibble
            ch = (byte) (ch >>> 4);
            // shift the bits down
            ch = (byte) (ch & 0x0F);
            // must do this is high order bit is on!
            out.append(pseudo[(int) ch]);
            // convert the nibble to a String Character
            ch = (byte) (in[i] & 0x0F);
            // Strip off low nibble
            out.append(pseudo[(int) ch]);
            // convert the nibble to a String Character
            i++;
	    }
	    return new String(out);
	}   
}
