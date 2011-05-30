/**
 *
 */
package pl.edu.pjwstk.net.message.TURN.factory;

import java.util.Vector;

import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.net.message.STUN.STUNAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNMessageType;
import pl.edu.pjwstk.net.message.STUN.factory.STUNMessageFactory;
import pl.edu.pjwstk.net.message.TURN.TURNAttribute;
import pl.edu.pjwstk.net.message.TURN.TURNMessage;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 *         package pl.edu.pjwstk.net.message.TURN.factory
 */
public class TURNMessageFactory extends STUNMessageFactory {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TURNMessageFactory.class);

    /**
     *
     */
    public TURNMessageFactory() {
        // TODO Auto-generated constructor stub
    }

    public Vector<ProtocolObject> interpret(ExtendedBitSet ebs, int fromPosition,
                                            int toPosition) {

        // Validate parameters

        if (ebs.getFixedLength() < sizeOfHeader) {
            if (logger.isDebugEnabled()) logger.debug("Not valid size of header");
            return null;
        }

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
        //ExtendedBitSet ebs = new ExtendedBitSet(bytes.length * 8);
        //ebs.set(0,bytes);

        // Validate EBS

        if (logger.isDebugEnabled()) logger.debug("Checking EBS as a TURN Message");
        if (!protocolBits.equals(ebs.get(0, 2))) {
            if (logger.isDebugEnabled()) logger.debug("Message isn't TURN message.");
            return null;
        }

        int messsize = ebs.get(16, 32).toInt() * 8;
        if (messsize != (ebs.getFixedLength() - sizeOfHeader)) {
            //FIXME Exception?
            if (logger.isDebugEnabled()) {
                logger.debug("Message length problem " + (messsize) + " != " + (ebs.getFixedLength() - sizeOfHeader));
            }
            return null;
        }
        STUNMessageType protocolVersion;
        Vector<ProtocolObject> turnMessages = new Vector<ProtocolObject>();
        TURNMessage turnMessage = new TURNMessage();

        // Interpret data

        if (ebs.get(32, 64).equals(STUNMessageType.RFC5389.getProtocolMagicCookie())) {
            protocolVersion = STUNMessageType.RFC5389;
        } else {
            protocolVersion = STUNMessageType.RFC3489;
        }

        ExtendedBitSet transactionID = new ExtendedBitSet(protocolVersion.getLengthOfTransactionField(), false);
        transactionID.set(0, ebs.get(sizeOfHeader - protocolVersion.getLengthOfTransactionField(), sizeOfHeader));
        turnMessage.setTransactionID(transactionID);
        if (logger.isDebugEnabled()) logger.debug("TURN message received");

        try {
            turnMessage.setMessageClassandMethod(ebs.get(2, 16));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("TURN message Class = " + turnMessage.getTURNMessageClass().toString());
            logger.debug("TURN message Method = " + turnMessage.getTURNMessageMethod().toString());
        }

        Vector<ProtocolObject> turnAttrs = new TURNAttributeFactory().interpret(turnMessage, ebs, 160, ebs.getFixedLength());
        for (ProtocolObject turnAttr : turnAttrs) {
            if (turnAttr instanceof TURNAttribute) {
                turnMessage.add((TURNAttribute) turnAttr);
            } else if (turnAttr instanceof STUNAttribute) {
                turnMessage.add((STUNAttribute) turnAttr);
            }
        }

        turnMessages.add(turnMessage);
        return turnMessages;
    }
}
