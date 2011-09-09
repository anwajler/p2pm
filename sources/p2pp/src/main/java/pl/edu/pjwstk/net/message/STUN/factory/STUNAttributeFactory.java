package pl.edu.pjwstk.net.message.STUN.factory;

import java.util.Vector;

import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.net.factory.ProtocolAttributeFactory;
import pl.edu.pjwstk.net.message.STUN.STUNAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;
import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.types.ExtendedBitSet;

public class STUNAttributeFactory extends ProtocolAttributeFactory {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNAttributeFactory.class);

	public Vector<ProtocolObject> interpret(byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ExtendedBitSet ebs) {
		return this.interpret(ebs, 0, ebs.getFixedLength());
	}

	public Vector<ProtocolObject> interpret(byte[] bytes, int fromPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ExtendedBitSet ebs, int fromPosition) {
		return this.interpret(ebs, fromPosition, ebs.getFixedLength());
	}

	public Vector<ProtocolObject> interpret(byte[] bytes, int fromPosition,
			int toPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ExtendedBitSet ebs, int fromPosition,
			int toPosition) {


		
		return null;
	}

	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject,
			byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject,
			ExtendedBitSet ebs) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject,
			byte[] bytes, int fromPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject,
			ExtendedBitSet ebs, int fromPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject,
			byte[] bytes, int fromPosition, int toPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject,
			ExtendedBitSet ebs, int fromPosition, int toPosition) {
		// Validate parameters

		if (fromPosition < 0) {
			// FIXME Exception?
			if (logger.isDebugEnabled()) logger.debug("fromPosition must be greater or equal zero.");
			return null;
		}

		if (toPosition > ebs.getFixedLength()) {
			// FIXME Exception?
			if (logger.isDebugEnabled()) logger.debug("fromPosition must be lower or equal sizeOfHeader.");
			return null;
		}

		Vector<ProtocolObject> stunAttributes = new Vector<ProtocolObject>();
		int position = fromPosition;
		STUNAttribute stunAttribute;
		int tlvLengthCalc = 0;
		while (position < toPosition) {
			stunAttribute = null;
			for (STUNAttributeType stunAttributeType : STUNAttributeType
					.values()) {
				if (stunAttributeType.getType().equals(
						ebs.get(position, position + 16))) {
					int tlvLength = ebs.get(position + 16, position + 32).toInt();
					tlvLengthCalc = tlvLength * 8 + ((tlvLength % 4 == 0 ? 0 : 4) - tlvLength % 4) * 8;
                    //if (logger.isDebugEnabled()) {
					//    logger.debug("Found attribute " + stunAttributeType.toString() + " position = " + position + " length = " + tlvLength +
                    //            "(" + tlvLengthCalc + ")");
                    //}
					stunAttribute = STUNAttributeType.createAttribute((STUNMessage) referencedObject, stunAttributeType,
                            ebs.get(position + 32, position + 32 + tlvLength * 8),  position + 32 + tlvLengthCalc * 8);
					
					break;
				}
			}
			
			if (stunAttribute != null) {
				//if (logger.isDebugEnabled()) logger.debug(stunAttribute.getSTUNAttributeType().toString() + " "+ stunAttribute.toString());
				stunAttributes.add(stunAttribute);
			}
			else {
				if (logger.isDebugEnabled()) logger.debug("Problem when creating attribute" + Arrays.byteArrayToHexString(ebs.get(position, position + 16).toByteArray()));
			}		
			position += 32 + tlvLengthCalc;
		}
		return stunAttributes;
	}
}
