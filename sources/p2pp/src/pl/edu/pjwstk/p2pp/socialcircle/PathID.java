/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.edu.pjwstk.p2pp.socialcircle;

import java.math.BigInteger;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 *
 * @author Luke
 */
public class PathID extends GeneralObject implements Comparable<PathID> {

    private byte[] pathID;

    public PathID(byte[] pathid){
        super(GeneralObject.PATH_ID_OBJECT_TYPE);

        this.pathID = pathid;
    }

    	@Override
	public byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int firstBitIndex = super.getBitsCount();

		ByteUtils.addByteArrayToArrayAtBitIndex(pathID, bytes, firstBitIndex);

		return bytes;
	}

    @Override
    public byte[] asBytes() {
        int bitsCount = getBitsCount();
	return asBytes(bitsCount);
    }

    @Override
    	public int getBitsCount() {
		return super.getBitsCount() + (pathID.length * 8);
	}

    @Override
    public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
	public String toString() {
		return super.toString() + "[PathID=" + ByteUtils.byteArrayToHexString(pathID) + "]";
	}
    public byte[] getPathIDValue()
    {
        return pathID;
    }

    public int compareTo(PathID path) {

        BigInteger thisPathId = new BigInteger(1, this.pathID);
        BigInteger pathId = new BigInteger(1, path.getPathIDValue());

        return thisPathId.compareTo(pathId);

    }
}
