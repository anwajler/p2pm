package pl.edu.pjwstk.mteam.pubsub.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Level;

import pl.edu.pjwstk.mteam.pubsub.logging.Logger;

import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;

/*
 * PubSubMessage header format:
 * 
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |    Type           |		Source user name length       //
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     //                  |   		Source user name               |
 *     +-+-+-+-+-+-+-+-+-+-+                                       +
 *     //                                                         //
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                   Source peer id length                   | 
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                    Source peer id                        //
 *     +                                                           + 
 *     //                                                          |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                Destination user name length               | 
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                     Destination user name                //
 *     +                                                           + 
 *     //                                                          |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                Destination peer id length                 | 
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                    Destination peer id                   //
 *     +                                                           + 
 *     //                                                          |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                      Topic id length                      | 
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                         Topic id                         //
 *     +                                                           + 
 *     //                                                          |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    
 *
 * 
 */
/**
 * Abstract class representing messages exchanged by publish-subscribe layer. 
 * Directly responsible for encoding/parsing standard pub-sub message header. 
 * Type-dependent part of the message is managed by subclasses.<p>
 * 
 * When creating new PubSubMessage subclass, register it with {@link #registerMessageType(String, byte)} 
 * method to enable proper parsing. There is no need for registering built-in publish-subscribe
 * messages when using default algorithm as they are registered automatically when it is loaded. Below example 
 * of PubSubMessage subclass registering.<p><p>
 * 
 * <code>public class CustomSubscribeMessage extends PubSubMessage{</code><p>
 * 		
 * <code>//other methods, etc.</code><p>
 * <code>}</code><p><p>
 * 
 * <code>//and when algorithm is loaded: </code><p>
 * <code>registerMessageType("pubsub.extension.message.CustomSubscribeMessage", PubSubConstants.MSG_SUBSCRIBE);</code><br>
 * <p>
 * 
 * Registering provides <code>PubSubMessage</code> subclasses with unique identifiers 
 * which are used as message types. When node receives publish-subscribe message and tries
 * to parse it, message type informs, which PubSubMessage subclass instance to create. 
 * Identifiers from 0 to 6 (inclusive) are reserved for built-in set of messages which can
 * be replaced by custom ones if custom algorithm requires some additional information.
 * Each custom message, overriding default message must derive from the standard
 * message of the specified type.
 * 
 * Subclass has to define parameterless constructor - it will be used by 
 * <code>PubSubMessage</code> class to create appropriate message object while parsing.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public abstract class PubSubMessage extends Message {

    static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage");
    /**
     * Messages types to enable creating various message instances dynamically.
     * When creating new PubSubMessage deriving class, register it with <code>
     * registerMessageType </code>.
     */
    private static Hashtable<Integer, String> messages;
    /**
     * Message type.
     */
    private Integer type;
    /**
     * ID of the topic, this message is associated with.
     */
    private String topicID;

    static {
        messages = new Hashtable<Integer, String>();
    }

    /**
     * Creates new publish-subscribe message.
     * @param src Message sender.
     * @param dest Message destination.
     */
    public PubSubMessage(NodeInfo src, NodeInfo dest, String topicId) {
        super(src, dest);
        /*
         * Setting NodeInfo specific values (not stored in GeneralNodeInfo
         * object by default)
         */
        ((NodeInfo) getSourceInfo()).setName(src.getName());
        ((NodeInfo) getSourceInfo()).setID(src.getID());
        ((NodeInfo) getDestinationInfo()).setName(dest.getName());
        ((NodeInfo) getDestinationInfo()).setID(dest.getID());
        topicID = topicId;
    }

    @Override
    protected void finalize() throws Throwable {
        topicID = null;
        type = null;
        super.finalize();
    }


    /**
     * Method overriding <code>getDestinationInfo</code> from <code>Message</code> class.
     * @return Information about the node this message is for, extended with publish-subscribe
     * 		   specific information, such as user name and peer ID.
     */
    public NodeInfo getDestinationInfo() {
        return (NodeInfo) super.getDestinationInfo();
    }

    /**
     * Method overriding <code>getSourceInfo</code> from <code>Message</code> class.
     * @return Information about the node this message is from, extended with publish-subscribe
     * 		   specific information, such as user name and peer ID.
     */
    public NodeInfo getSourceInfo() {
        return (NodeInfo) super.getSourceInfo();
    }

    /**
     * This value corresponds to registered type
     * ({@link PubSubConstants#MSG_CREATETOPIC}, etc.)
     * @return Message type.
     */
    public int getType() {
        return type;
    }

    /**
     * @return ID of the topic this message is associated with.
     */
    public String getTopicID() {
        return topicID;
    }

    /**
     * Sets type of publish-subscribe method. This value should be set automatically
     * from <code>PubSubMessage</code> subclass constructor as message type is associated
     * with specified subclass (assigned to them during registration process). User should
     * not be allowed to modify this value.
     * @param mType Message type assigned to <code>PubSubMessage</code> subclass during
     * 				registration process.
     */
    protected void setType(int mType) {
        type = mType;
    }

    /**
     * Sets ID of the topic, this message is associated with.
     * @param topicId topic ID.
     */
    protected void setTopicID(String topicId) {
        topicID = topicId;
    }

    /**
     * Prepares message for sending.
     * @return Bytes to send.
     */
    @Override
    public byte[] encode() {
        byte [] data = null;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream dtstr = new DataOutputStream(ostream);
        byte[] stdHeader = super.encode();
        logger.trace("Sent header length: " + stdHeader.length);
        try {
            //writing contents stored in Message class
            dtstr.write(stdHeader);
            //writing pub-sub specific message contents
            //writing message type
            dtstr.writeInt(this.type);
            NodeInfo ninfo = getSourceInfo();
            byte[] uname = ninfo.getName().getBytes();
            //writing source user name length
            dtstr.writeInt(uname.length);
            //writing source user name
            dtstr.write(uname, 0, uname.length);
            uname = ninfo.getID().getBytes();
            //writing source peer id length
            dtstr.writeInt(uname.length);
            //writing source peer id
            dtstr.write(uname, 0, uname.length);
            ninfo = getDestinationInfo();
            uname = ninfo.getName().getBytes();
            //writing destination user name length
            dtstr.writeInt(uname.length);
            //writing destination user name
            dtstr.write(uname, 0, uname.length);
            uname = ninfo.getID().getBytes();
            //writing destination peer id length
            dtstr.writeInt(uname.length);
            //writing destination peer id
            dtstr.write(uname, 0, uname.length);

            uname = topicID.getBytes();
            //writing topic id length
            dtstr.writeInt(uname.length);
            //writing topic id
            dtstr.write(uname, 0, uname.length);
            data = ostream.toByteArray();            
            logger.trace(getTopicID() + "-"+getType()+" encoded bytes: "+data.length);            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ostream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return data;
    }

    /**
     * Method used by node for parsing received publish-subscribe messages. It will
     * discover message type and dynamically create appropriate <code>PubSubMessage</code>
     * subclass instance (if it has been previously registered). There is no need to
     * register built-in messages - it is done automatically, when corresponding class is
     * loaded.
     * @param stream Received bytes.
     * @return Dynamically created <code>PubSubMessage</code> subclass (depending on
     * 		   message type.
     */
    public static PubSubMessage parseMessage(byte[] stream) {

        int headerLength = getHeaderLength(stream);
        logger.trace("Received header length: " + headerLength);
        /* reading message type to check, which Message class (from 'messages')
         * instance to create.
         */
        ByteArrayInputStream istream = new ByteArrayInputStream(stream);
        DataInputStream dtstr = new DataInputStream(istream);
        //skipping bytes used for encoding standard header (from Message class)
        istream.skip(headerLength);
        int msgType = -1;

        /*
         * Creating message object of specified type
         */
        Class messageClass = null;
        PubSubMessage parsedMessage = null;
        byte[] uname = null;
        try {
            msgType = dtstr.readInt();
            headerLength += 4;
            messageClass = Class.forName(getRegisteredMessageClassName(msgType));
            //creating object instance
            parsedMessage = (PubSubMessage) messageClass.newInstance();
            //parsing contents stored in Message class
            parsedMessage.getClass().getSuperclass().getMethod("parse", byte[].class).invoke(parsedMessage, stream);

            //parsing pub-sub specific message contents
            //reading message type
            parsedMessage.type = msgType;
            //reading source user name length and user name
            int unameLength = dtstr.readInt();
            uname = new byte[unameLength];
            headerLength += 4 + unameLength;
            dtstr.readFully(uname);
            parsedMessage.getSourceInfo().setName(new String(uname));
            //reading source peer id length and peer id
            unameLength = dtstr.readInt();
            uname = null;
            uname = new byte[unameLength];
            headerLength += 4 + unameLength;
            dtstr.readFully(uname);
            parsedMessage.getSourceInfo().setID(new String(uname));

            //reading destination user name length and user name
            unameLength = dtstr.readInt();
            uname = null;
            uname = new byte[unameLength];
            headerLength += 4 + unameLength;
            dtstr.readFully(uname);
            parsedMessage.getDestinationInfo().setName(new String(uname));

            //reading destination peer id length and peer id
            unameLength = dtstr.readInt();
            uname = null;
            uname = new byte[unameLength];
            headerLength += 4 + unameLength;
            dtstr.readFully(uname);
            parsedMessage.getDestinationInfo().setID(new String(uname));

            //reading topic id length and topic id
            unameLength = dtstr.readInt();
            uname = null;
            uname = new byte[unameLength];
            headerLength += 4 + unameLength;
            dtstr.readFully(uname);
            parsedMessage.setTopicID(new String(uname));

            //parsing message type dependent contents
            (messageClass.cast(parsedMessage)).getClass().getMethod("parse", byte[].class, int.class).invoke(messageClass.cast(parsedMessage), stream, headerLength);
            logger.trace(parsedMessage.getTopicID() + "-"+parsedMessage.getType()+" parsed bytes: "+stream.length);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }finally {
            try {
                istream.close();
                istream = null;
                dtstr = null;
                messageClass = null;
                uname = null;
                stream = null;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return parsedMessage;
    }

    /**
     * When creating new PubSubMessage subclass, register it with this method to
     * enable proper parsing. There is no need for registering built-in publish-subscribe
     * messages - they are registered when the class is loaded.
     * @param className Full name of the class representing message type.
     * @param mType Unique message type identifier.
     * @return Value indicating, whether registration was successful.
     */
    public static boolean registerMessageType(String className, short mType) {
        boolean result;
        if (!messages.contains(mType)) {
            logger.trace("Registering class: " + className + "(type:" + mType + ")");
            messages.put((int) mType, className);
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * @param type Type of the registered message.
     * @return Name of the class associated with specified type.
     */
    public static String getRegisteredMessageClassName(int type) {
        logger.trace("Looking for type: " + type);
        logger.trace("Found class: " + messages.get(type));
        return messages.get(type);
    }

    /**
     * @return List of registered PubSubMessage subclasses.
     */
    public static Hashtable<Integer, String> getRegisteredMessageTypes() {
        return messages;
    }
}
