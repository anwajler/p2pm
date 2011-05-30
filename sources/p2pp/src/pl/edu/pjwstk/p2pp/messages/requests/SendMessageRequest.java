package pl.edu.pjwstk.p2pp.messages.requests;

import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
import pl.edu.pjwstk.p2pp.messages.responses.SendMessageResponse;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.resources.MessageResourceObject;
import pl.edu.pjwstk.p2pp.resources.StringValueResourceObject;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

import java.util.Vector;

public class SendMessageRequest extends Request {

	/**
	 * RequestOptions object describing this request.
	 */
	private RequestOptions requestOptions;

	private PeerInfo publisherPeerInfo;

    private PeerID targetPeerID;

	/**
	 * ResourceObject which is part of this message.
	 */
	private MessageResourceObject messageResourceObject;

    private StringValueResourceObject protocolObject;

	/**
	 * Creates empty object. To be filled later. Convenient when creating object basing on received data.
	 */
	public SendMessageRequest() {
	}

	/**
	 *
	 * @param protocolVersion
	 * @param isAcknowledgment
	 * @param isSentByPeer
	 * @param isRecursive
	 * @param ttl
	 * @param transactionID
	 * @param sourceID
	 * @param isOverReliable
	 * @param isEncrypted
	 * @param requestOptions
	 *            May be null.
	 * @param publisherPeerInfo
	 * @param messageResourceObject
	 */
	public SendMessageRequest(boolean[] protocolVersion, boolean isAcknowledgment, boolean isSentByPeer, boolean isRecursive, byte ttl,
                              byte[] transactionID, byte[] sourceID, boolean isOverReliable, boolean isEncrypted, RequestOptions requestOptions,
                              PeerInfo publisherPeerInfo, PeerID targetPeerID, MessageResourceObject messageResourceObject, String protocol) {
		super(protocolVersion, isAcknowledgment, isSentByPeer, isRecursive, P2PPMessage.SEND_MESSAGE_MESSAGE_TYPE, ttl,
				transactionID, sourceID, isOverReliable, isEncrypted);
		this.requestOptions = requestOptions;
		this.publisherPeerInfo = publisherPeerInfo;
        this.targetPeerID = targetPeerID;
		this.messageResourceObject = messageResourceObject;
        this.protocolObject = new StringValueResourceObject(publisherPeerInfo.getUnhashedID().getUnhashedIDString(), protocol);
        this.protocolObject.setResourceID(new ResourceID(publisherPeerInfo.getUnhashedID().getUnhashedIDValue()));
	}

	@Override
	public RequestOptions getRequestOptions() {
		return requestOptions;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		if (object instanceof PeerInfo) {
			if (publisherPeerInfo == null) {
				publisherPeerInfo = (PeerInfo) object;
			} else {
				throw new UnsupportedGeneralObjectException("SendMessageRequest can't contain more than one PeerInfo object.");
			}
		} else if (object instanceof RequestOptions) {
			if (requestOptions == null) {
				requestOptions = (RequestOptions) object;
			} else {
				throw new UnsupportedGeneralObjectException("SendMessageRequest can't contain more than one RequestOptions object.");
			}
        } else if (object instanceof StringValueResourceObject) {
            if (this.protocolObject == null) {
                this.protocolObject = (StringValueResourceObject) object;
            } else {
                throw new UnsupportedGeneralObjectException("SendMessageRequest can't contain more than one StringValueResourceObject.");
            }
		} else if (object instanceof MessageResourceObject) {
            if (this.messageResourceObject == null) {
				this.messageResourceObject = (MessageResourceObject) object;
			} else {
				throw new UnsupportedGeneralObjectException("SendMessageRequest can't contain more than one MessageResourceObject.");
			}
        } else if (object instanceof PeerID) {
            if (this.targetPeerID == null) {
                this.targetPeerID = (PeerID) object;
            } else {
                throw new UnsupportedGeneralObjectException("SendMessageRequest can't contain more than one PeerID.");
            }
		} else {
			throw new UnsupportedGeneralObjectException("SendMessageRequest can't contain " + object.getClass().getName() + " object.");
		}
	}

	@Override
	public PeerInfo getPeerInfo() {
		return publisherPeerInfo;
	}

    public PeerID getTargPeerID() {
        return this.targetPeerID;
    }

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	/**
	 * Returns ResourceObject being published using this request.
	 *
	 * @return
	 */
	public MessageResourceObject getMessageResourceObject() {
		return this.messageResourceObject;
	}

	public void setMessageResourceObject(MessageResourceObject messageResourceObject) {
		this.messageResourceObject = messageResourceObject;
	}

    public StringValueResourceObject getProtocolObject() {
        return this.protocolObject;
    }

    public void setProtocolObject(String protocol) {
        this.protocolObject = new StringValueResourceObject(publisherPeerInfo.getUnhashedID().getUnhashedIDString(), protocol);
        this.protocolObject.setResourceID(new ResourceID(publisherPeerInfo.getUnhashedID().getUnhashedIDValue()));
    }

	public void setRequestOptions(RequestOptions requestOptions) {
		this.requestOptions = requestOptions;
	}

	@Override
	public boolean verify() {
		boolean result = true;

		if (publisherPeerInfo == null || this.messageResourceObject == null || this.targetPeerID == null || this.protocolVersion == null) {
			result = false;
		} else {
			// must contain peerID
			PeerID peerID = publisherPeerInfo.getPeerID();
			if (peerID == null) {
				result = false;
			} else {
				if (peerID.getPeerIDBytes() == null) {
					result = false;
				}
			}
			// not checked if peerID isn't here
			if (result) {
				// must contain unshadedID
				UnhashedID unhashedID = publisherPeerInfo.getUnhashedID();
				if (unhashedID == null) {
					result = false;
				} else {
					if (unhashedID.getUnhashedIDValue() == null) {
						result = false;
					}
				}
			}
			// not checked if PeerID or UnhashedID isn't here
			if (result) {
				// has to contain at least one AddressInfo
				Vector<AddressInfo> addressInfos = publisherPeerInfo.getAddressInfos();
				if (addressInfos == null) {
					result = false;
				} else if (addressInfos.size() <= 0) {
					result = false;
				}
			}
			// not checked if PeerID, UnhashedID or address info isn't here
			if (result) {
				// TODO probably signature and something more
				if (this.messageResourceObject.getResourceID() == null || this.messageResourceObject.getOwner() == null) {
					result = false;
				} else if (this.messageResourceObject.getResourceID().getResourceID() == null
						|| this.messageResourceObject.getOwner().getPeerIDValue() == null) {
					result = false;

				}
			}
		}

		return result;
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int byteIndex = super.getBitsCount() / 8;

		if (requestOptions != null) {
			ByteUtils.addByteArrayToArrayAtByteIndex(requestOptions.asBytes(), bytes, byteIndex);
			byteIndex += requestOptions.getBitsCount() / 8;
		}

		ByteUtils.addByteArrayToArrayAtByteIndex(publisherPeerInfo.asBytes(), bytes, byteIndex);
		byteIndex += publisherPeerInfo.getBitsCount() / 8;

		ByteUtils.addByteArrayToArrayAtByteIndex(this.messageResourceObject.asBytes(), bytes, byteIndex);
		byteIndex += this.messageResourceObject.getBitsCount()/8;

        ByteUtils.addByteArrayToArrayAtByteIndex(this.targetPeerID.asBytes(), bytes, byteIndex);
		byteIndex += this.targetPeerID.getBitsCount()/8;

        ByteUtils.addByteArrayToArrayAtByteIndex(this.protocolObject.asBytes(), bytes, byteIndex);

		return bytes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("SendMessageRequest=[message=[" + super.toString() + ", header=[" + getHeader() + "], publisherPeerInfo=[");
		if (publisherPeerInfo != null) {
			builder.append(publisherPeerInfo.toString());
		} else {
			builder.append(publisherPeerInfo);
		}
		builder.append("], requestOptions=[");
		if (requestOptions != null) {
			builder.append(requestOptions.toString());
		} else {
			builder.append(requestOptions);
		}
        builder.append("] protocol=[");
        if (this.protocolObject != null) {
            builder.append(this.protocolObject.getValueAsString());
        } else {
            builder.append("null");
        }
		builder.append("], messageResourceObject=[");
		if (this.messageResourceObject != null) {
			builder.append(this.messageResourceObject.toString());
		} else {
			builder.append("null");
		}
		builder.append("]]");
		return builder.toString();
	}

	@Override
	public int getBitsCount() {
		int additionalBits = 0;
		if (requestOptions != null) {
			additionalBits += requestOptions.getBitsCount();
		}

        int ppi = publisherPeerInfo.getBitsCount();
        int mro = this.messageResourceObject.getBitsCount();
        int tpi = this.targetPeerID.getBitsCount();
        int po = this.protocolObject.getBitsCount();

		return super.getBitsCount() + additionalBits + ppi + mro + tpi + po;
	}

	/**
	 * <p>
	 * Creates response for this message. Response is responseACK (not acknowledgment). Response contains given response
	 * code. PeerInfo given as argument is added to response as PeerInfo of peer that consumed this request. Response is
	 * for iterative routing.
	 * </p>
	 * <p>
	 * Response contains some copies of this request: protocolVersion, transactionID, sourceID, overReliable, encrypted.
	 * </p>
	 *
	 * @param responseCode
	 * @param ownPeerInfo
	 * @return
	 */
	public SendMessageResponse createResponse(boolean[] responseCode, PeerInfo ownPeerInfo) {
		return new SendMessageResponse(protocolVersion, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE, false, true, false,
                responseCode, (byte) 255, transactionID, sourceID, ownPeerInfo.getPeerID().getPeerIDBytes(), overReliable, encrypted, ownPeerInfo);
	}
}