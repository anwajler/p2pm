package pl.edu.pjwstk.p2pp.objects;

import pl.edu.pjwstk.p2pp.messages.MalformedP2PPMessageException;
import pl.edu.pjwstk.p2pp.resources.MessageResourceObject;
import pl.edu.pjwstk.p2pp.resources.STUNServiceResourceObject;
import pl.edu.pjwstk.p2pp.resources.StringValueResourceObject;
import pl.edu.pjwstk.p2pp.resources.UserInfoResourceObject;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * Class describing resource object as defined in P2PP specification (draft 01). One exception from specification is
 * that there's a ResourceObjectValue which wasn't defined there, but is used in reference implementation.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public abstract class ResourceObject extends GeneralObject {

    /**
     * Moment of last refresh of this resource. Used for determining if this resource has expired.
     */
    private long momentOfLastRefresh;

    /**
     * Type of resource.
     */
    private byte contentType;

    /**
     * Subtype of resource.
     */
    private byte contentSubtype;

    /**
     * HashedID for structured networks. Same as unhashedID for unstructured networks.
     */
    protected ResourceID resourceID;

    /**
     * ID that will be used for generating resourceID. If structured network is used, resourceID will be hashed basing
     * on this unhashedID. If unstructured network is used, resourceID will have the same value as unhashedID.
     */
    protected byte[] unhashedID;

    /**
     * ResourceObjectValue object containing bytes that represent a value of this resource object. This value is set by
     * ResourceObject subclasses.
     */
    protected ResourceObjectValue value = new ResourceObjectValue();

    /**
     * Owner of this resource-object.
     */
    protected Owner owner;

    /**
     * Time of expiration (seconds) of this object.
     */
    protected Expires expires;

    /**
     * The cryptographic signature of the resource-object.
     */
    protected Signature signature;

    /**
     * The X509 certificate of the peer publishing the resource-object.
     */
    protected Certificate certificate;

    /**
     * Creates empty ResourceObject.
     *
     * @param contentType
     * @param contentSubType
     */
    public ResourceObject(byte contentType, byte contentSubType) {
        super(GeneralObject.RESOURCE_OBJECT_OBJECT_TYPE);

        this.contentType = contentType;
        this.contentSubtype = contentSubType;

        // remembers a moment in which this resource was created TODO use this?
        this.momentOfLastRefresh = System.currentTimeMillis();
    }

    /**
     * Creates resource object of given content type and subtype.
     *
     * @param contentType
     * @param contentSubType
     * @param unhashedID     ID that will be used for generating resourceID. If structured network is used, resourceID will be
     *                       hashed basing on this unhashedID. If unstructured network is used, resourceID will have the same value
     *                       as unhashedID.
     * @param owner
     * @param expires
     * @param signature
     * @param certificate
     */
    public ResourceObject(byte contentType, byte contentSubType, byte[] unhashedID, Owner owner, Expires expires,
                          Signature signature, Certificate certificate) {
        super(GeneralObject.RESOURCE_OBJECT_OBJECT_TYPE);
        this.contentType = contentType;
        this.contentSubtype = contentSubType;
        this.owner = owner;
        this.expires = expires;
        this.signature = signature;
        this.certificate = certificate;

        // remembers a moment in which this resource was created TODO use this?
        this.momentOfLastRefresh = System.currentTimeMillis();
    }

    public void setUnhashedID(byte[] unhashedID) {
        this.unhashedID = unhashedID;
    }

    @Override
    public byte[] asBytes() {
        return asBytes(getBitsCount());
    }

    @Override
    protected byte[] asBytes(int bitsCount) {
        byte[] bytes = super.asBytes(bitsCount);

        int byteIndex = super.getBitsCount() / 8;

        ByteUtils.addByteToArrayAtBitIndex(contentType, bytes, byteIndex * 8);
        byteIndex += 1;
        ByteUtils.addByteToArrayAtBitIndex(contentSubtype, bytes, byteIndex * 8);
        byteIndex += 1;
        ByteUtils.addByteArrayToArrayAtByteIndex(resourceID.asBytes(), bytes, byteIndex);
        byteIndex += resourceID.getBitsCount() / 8;
        if (value != null) {
            ByteUtils.addByteArrayToArrayAtByteIndex(value.asBytes(), bytes, byteIndex);
            byteIndex += value.getBitsCount() / 8;
        }
        if (owner != null) {
            ByteUtils.addByteArrayToArrayAtByteIndex(owner.asBytes(), bytes, byteIndex);
            byteIndex += owner.getBitsCount() / 8;
        }
        if (expires != null) {
            ByteUtils.addByteArrayToArrayAtByteIndex(expires.asBytes(), bytes, byteIndex);
            byteIndex += expires.getBitsCount() / 8;
        }
        if (signature != null) {
            ByteUtils.addByteArrayToArrayAtByteIndex(signature.asBytes(), bytes, byteIndex);
            byteIndex += signature.getBitsCount() / 8;
        }
        if (certificate != null) {
            ByteUtils.addByteArrayToArrayAtByteIndex(certificate.asBytes(), bytes, byteIndex);
            //byteIndex += certificate.getBitsCount() / 8;
        }

        return bytes;
    }

    /**
     * ResourceObjects are considered equal if their content type, subtype and resourceID are equal. Otherwise they're
     * not equal. TODO what about value?
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResourceObject) {
            ResourceObject object = (ResourceObject) obj;
            if (contentType != object.getContentType() || contentSubtype != object.getContentSubtype()
                    || (!resourceID.equals(object.getResourceID()))) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    public int getBitsCount() {
        int additionalLength = 0;
        if (owner != null) {
            additionalLength += owner.getBitsCount();
        }
        if (expires != null) {
            additionalLength += expires.getBitsCount();
        }
        if (signature != null) {
            additionalLength += signature.getBitsCount();
        }
        if (certificate != null) {
            additionalLength += certificate.getBitsCount();
        }
        if (value != null) {
            additionalLength += value.getBitsCount();
        }

        return super.getBitsCount() + 16 + resourceID.getBitsCount() + additionalLength;
    }

    @Override
    public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
        if (subobject instanceof ResourceID) {
            resourceID = (ResourceID) subobject;
        } else if (subobject instanceof ResourceObjectValue) {
            value = (ResourceObjectValue) subobject;
        } else if (subobject instanceof Owner) {
            owner = (Owner) subobject;
        } else if (subobject instanceof Expires) {
            expires = (Expires) subobject;
        } else if (subobject instanceof Signature) {
            signature = (Signature) subobject;
        } else if (subobject instanceof Certificate) {
            certificate = (Certificate) subobject;
        } else {
            throw new UnsupportedGeneralObjectException("ResourceObject can't " + "contain "
                    + subobject.getClass().getName() + " as subobject.");
        }
    }

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

    public ResourceID getResourceID() {
        return resourceID;
    }

    public void setResourceID(ResourceID resourceID) {
        this.resourceID = resourceID;
    }

    public ResourceObjectValue getValue() {
        return value;
    }

    /**
     * Sets a {@link ResourceObjectValue} for this resource object.
     *
     * @param value
     */
    public void setValue(ResourceObjectValue value) {
        this.value = value;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Expires getExpires() {
        return expires;
    }

    public void setExpires(Expires expires) {
        this.expires = expires;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    /**
     * Refreshes this resource (i.e. changes the moment of last refreshment to this moment. It prevents resource from
     * expiration.
     */
    public void refresh() {
        this.momentOfLastRefresh = System.currentTimeMillis();
    }

    /**
     * Creates ResourceObject supported by current implementation.
     *
     * @param contentType
     * @param contentSubType
     * @return
     * @throws UnsupportedGeneralObjectException
     *                                       Thrown when given content type and subtype aren't supported by current implementation or there's a
     *                                       part of given resourceValue that isn't supported.
     * @throws MalformedP2PPMessageException Thrown when given resourceValue is malformed.
     */
    public static ResourceObject create(byte contentType, byte contentSubType)
            throws UnsupportedGeneralObjectException, MalformedP2PPMessageException {
        ResourceObject resource = null;
        switch (contentType) {
            case P2PPUtils.USER_INFO_CONTENT_TYPE: {
                resource = new UserInfoResourceObject();
            }
            break;

            case P2PPUtils.STUN_CONTENT_TYPE: {
                resource = new STUNServiceResourceObject();
            }
            break;

            // TODO implement
            case P2PPUtils.TURN_CONTENT_TYPE: {
                throw new UnsupportedGeneralObjectException(
                        "TODO ONLY NOW implementation can't support ResourceObject with contentType=" + contentType
                                + " and subtype=" + contentSubType + ".");
            }

            // TODO implement
            case P2PPUtils.STUN_TURN_ICE_CONTENT_TYPE: {
                throw new UnsupportedGeneralObjectException(
                        "TODO ONLY NOW implementation can't support ResourceObject with contentType=" + contentType
                                + " and subtype=" + contentSubType + ".");
            }

            case P2PPUtils.STRING_VALUE_CONTENT_TYPE: {
                resource = new StringValueResourceObject();
            }
            break;

            // TODO implement
            case P2PPUtils.SIP_CONTENT_TYPE: {
                throw new UnsupportedGeneralObjectException(
                        "TODO ONLY NOW implementation can't support ResourceObject with contentType=" + contentType
                                + " and subtype=" + contentSubType + ".");
            }

            case P2PPUtils.SUBSCRIPTION_INFO_CONTENT_TYPE: {
                throw new UnsupportedGeneralObjectException(
                        "TODO ONLY NOW implementation can't support ResourceObject with contentType=" + contentType
                                + " and subtype=" + contentSubType + ".");
            }

            case P2PPUtils.MESSAGE_CONTENT_TYPE: {
                resource = new MessageResourceObject();
            }
            break;

            default: {
                throw new UnsupportedGeneralObjectException(
                        "Current implementation can't support ResourceObject with contentType=" + contentType
                                + " and subtype=" + contentSubType + ".");
            }
        }

        resource.setContentSubtype(contentSubType);
        return resource;
    }

    /**
     * Returns ID that will be used for generating resourceID. If structured network is used, resourceID will be hashed
     * basing on this unhashedID. If unstructured network is used, resourceID will have the same value as unhashedID.
     *
     * @return
     */
    public byte[] getUnhashedID() {
        return unhashedID;
    }

    /**
     * Returns moment (milliseconds) of last refresh of ResourceObject. If object wasn't refreshed, it is a moment in
     * which it was first published.
     *
     * @return
     */
    public long getMomentOfLastRefresh() {
        return momentOfLastRefresh;
    }

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
        } else
            ownerAsString += owner;
        String expiresAsString = "";
        if (expires != null) {
            expiresAsString = expires.toString();
        } else {
            expiresAsString += expires;
        }
        String signatureAsString = "";
        if (signature != null) {
            signatureAsString = signature.toString();
        } else {
            signatureAsString += signature;
        }
        String certificateAsString = "";
        if (certificate != null) {
            certificateAsString = certificate.toString();
        } else {
            certificateAsString += certificate;
        }
        return super.toString() + "ResourceObject=[contType=" + contentType + ", contSubtype=" + contentSubtype
                + ", resourceID=[" + resourceIDAsString + "], unhashedID=["
                + ByteUtils.byteArrayToHexString(unhashedID) + "], owner=[" + ownerAsString + "], expires=["
                + expiresAsString + "], signature=[" + signatureAsString + "], certificate=[" + certificateAsString
                + "], resourceValue=[" + getValueAsString() + "]]";
    }

    /**
     * Returns string representation of value being part of this resource object. To be implemented in subclasses,
     * because they know how to interpret {@link ResourceObjectValue} being part of this resource.
     *
     * @return
     */
	public abstract String getValueAsString();

}
