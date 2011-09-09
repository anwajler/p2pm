package pl.edu.pjwstk.util;

import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Class wrapping byte arrays, so that they can be used as keys in maps.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public class ByteArrayWrapper {

    private byte[] wrappedArray;

    /**
     * Constructor for a wrapper for given byte array.
     *
     * @param arrayToBeWrapped
     */
    public ByteArrayWrapper(byte[] arrayToBeWrapped) {
        this.wrappedArray = arrayToBeWrapped;
    }

    @Override
    public String toString() {
        return "ByteWrapper=[wrappedArray=" + ByteUtils.byteArrayToHexString(this.wrappedArray) + "]]";
    }

    @Override
    public int hashCode() {
        StringBuffer buffer = new StringBuffer("");
        for (byte wrappedByte : this.wrappedArray) {
            buffer.append(wrappedByte);
        }
        return buffer.toString().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ByteArrayWrapper) {
            ByteArrayWrapper anotherWrapper = (ByteArrayWrapper) object;
            return Arrays.equals(wrappedArray, anotherWrapper.getByteArray());
        } else {
            return false;
        }
    }

    /**
     * Returns array wrapped by this object.
     *
     * @return
     */
    public byte[] getByteArray() {
        return wrappedArray;
    }

    /**
     * Returns wrapped array as hex string.
     *
     * @return
     */
    public String getWrappedArrayAsHexString() {
        return ByteUtils.byteArrayToHexString(wrappedArray);
    }
}