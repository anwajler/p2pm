package pl.edu.pjwstk.p2pp.objects;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * RLookup (Resource lookup) object as defined in P2PP specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class RLookup extends GeneralObject {

	@Override
	public String toString() {
        // TODO StringBuilder
		String resourceIDAsString = "";
		if (resourceID != null) {
			resourceIDAsString = resourceID.toString();
		} else {
			resourceIDAsString += resourceID;
		}
		String ownerAsString = "";
		if (owner != null) {
			ownerAsString = owner.toString();
		} else {
			ownerAsString += owner;
		}
		return super.toString() + "RLookup=[contType=" + contentType + ", contSubtype=" + contentSubtype
				+ ", resourceID=[" + resourceIDAsString + "], owner=[" + ownerAsString + "]]";
	}

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(Owner owner) {
		this.owner = owner;
	}

	private static final int R_LOOKUP_SPECIFIC_STATIC_DATA_LENGTH = 16;

	private byte contentType;

	private byte contentSubtype;

	private ResourceID resourceID;

	private Owner owner;

	/**
	 * Creates RLookup object.
	 * 
	 * @param contentType
	 *            An IANA assigned identifier for the type of content contained in this resource-object.
	 * 
	 * 
	 * @param contentSubtype
	 *            An IANA assigned identifier which further classifies the content type as defined by cont-type.
	 * 
	 * @param resourceID
	 *            The Resource-ID TLV of the resource object. Can't be null.
	 * @param owner
	 *            Owner. May be null.
	 */
	public RLookup(byte contentType, byte contentSubtype, ResourceID resourceID, Owner owner) {
		super(GeneralObject.R_LOOKUP_OBJECT_TYPE);

		this.contentType = contentType;
		this.contentSubtype = contentSubtype;
		this.resourceID = resourceID;
		this.owner = owner;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();

		ByteUtils.addByteToArrayAtBitIndex(contentType, bytes, currentIndex);
		currentIndex += 8;
		ByteUtils.addByteToArrayAtBitIndex(contentSubtype, bytes, currentIndex);
		currentIndex += 8;
		ByteUtils.addByteArrayToArrayAtBitIndex(resourceID.asBytes(), bytes, currentIndex);
		currentIndex += resourceID.getBitsCount();
		if (owner != null) {
			ByteUtils.addByteArrayToArrayAtBitIndex(owner.asBytes(), bytes, currentIndex);
		}

		return bytes;
	}

	@Override
	public int getBitsCount() {
		int additionalLength = 0;
		if (owner != null)
			additionalLength += owner.getBitsCount();
		return super.getBitsCount() + R_LOOKUP_SPECIFIC_STATIC_DATA_LENGTH + resourceID.getBitsCount()
				+ additionalLength;
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		if (subobject instanceof ResourceID) {
			resourceID = (ResourceID) subobject;
		} else if (subobject instanceof Owner) {
			owner = (Owner) subobject;
		} else {
			throw new UnsupportedGeneralObjectException("RLookup can't " + "contain " + subobject.getClass().getName()
					+ " subobject.");
		}
	}

	/**
	 * Returns ResourceID object included in this object.
	 * 
	 * @return
	 */
	public ResourceID getResourceID() {
		return resourceID;
	}

	public void setResourceID(ResourceID resourceID) {
		this.resourceID = resourceID;
	}

	/**
	 * Returns content type included in this object.
	 * 
	 * @return
	 */
	public byte getContentType() {
		return contentType;
	}

	public void setContentType(byte contentType) {
		this.contentType = contentType;
	}

	public byte getContentSubtype() {
		return contentSubtype;
	}

	public void setContentSubtype(byte contentSubtype) {
		this.contentSubtype = contentSubtype;
	}

}
