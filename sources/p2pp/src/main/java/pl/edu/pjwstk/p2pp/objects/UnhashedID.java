package pl.edu.pjwstk.p2pp.objects;

import java.util.Arrays;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Unhashed ID as defined in P2PP (draft 01). Unhashed-ID of a user, resource, or peer identifier. this peer. This is
 * only included in a DHT-based overlay.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public class UnhashedID extends GeneralObject {

    private byte[] unhashedID;

    /**
     * Constructs UnhashedID object for given ID.
     * @param unhashedID byte[]
     */
    public UnhashedID(byte[] unhashedID) {
        super(GeneralObject.UNHASHED_ID_OBJECT_TYPE);

        this.unhashedID = unhashedID;
    }

    /**
     * Returns unhashedID value.
     *
     * @return byte[]
     */
    public byte[] getUnhashedIDValue() {
        return unhashedID;
    }

    public String getUnhashedIDString() {
        return ByteUtils.byteArrayToHexString(this.unhashedID);
    }

    @Override
    public byte[] asBytes(int bitsCount) {
        byte[] bytes = super.asBytes(bitsCount);

        int firstBitIndex = super.getBitsCount();

        ByteUtils.addByteArrayToArrayAtBitIndex(unhashedID, bytes, firstBitIndex);

        return bytes;
    }

    @Override
    public int getBitsCount() {
        return super.getBitsCount() + (unhashedID.length * 8);
    }

    @Override
    public byte[] asBytes() {
        int bitsCount = getBitsCount();
        return asBytes(bitsCount);
    }

    @Override
    public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
        throw new UnsupportedGeneralObjectException("UnhashedID can't contain " + subobject.getClass().getName()
                + " as subobject.");
    }

    @Override
    public String toString() {
        return super.toString() + "[UnhashedID=" + ByteUtils.byteArrayToHexString(unhashedID) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UnhashedID) {
            UnhashedID otherUnhashedID = (UnhashedID) o;
            byte[] otherUnhashedIDBytes = otherUnhashedID.getUnhashedIDValue();
            return Arrays.equals(this.unhashedID, otherUnhashedIDBytes);
        } else {
            return false;
        }
    }

}
