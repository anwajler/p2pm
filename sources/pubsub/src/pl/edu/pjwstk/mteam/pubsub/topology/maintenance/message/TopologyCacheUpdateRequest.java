package pl.edu.pjwstk.mteam.pubsub.topology.maintenance.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.message.request.PubSubRequest;

/**
 * Class representing cache updating request for topology maintenance purposes.
 * 
 * @author Piotr Bucior, buciorp@gmail.com
 */
public class TopologyCacheUpdateRequest extends PubSubRequest {

    /**
     * [EVENT TYPE] - Updating topic's cache by adding new neighbour.Actually invoked only by topic's root when it subscribe new child.
     */
    public static final byte STORE_NEW_NEIGHBOUR_NODE_TO_CACHE = 0;
    /**
     * [EVENT TYPE] - Updating topic's cache by removing some node.
     */
    public static final byte REMOVE_NEIGHBOUR_FROM_CACHE = 1;
    /**
     * [EVENT TYPE] - Clearing topic's cache. Actually only if the topic root is outage and it's neighours choosing new root.
     */
    public static final byte CLEAR_CACHE = 2;
    /**
     * [EVENT TYPE] - Updating topic's cache's grandparent.
     */
    public static final byte UPDATE_GRANDPARENT_IN_CACHE = 3;

    public static final byte PARENT_LEAVE = 4;

    public static final byte CHILD_LEAVE = 5;

    private final String [] operations = {
        "STORE_NEW_NEIGHBOUR_NODE_TO_CACHE",
        "REMOVE_NODE_FROM_CACHE",
        "CLEAR_CACHE",
        "UPDATE_GRANDPARENT_IN_CACHE",
        "PARENT_LEAVE",
        "CHILD_LEAVE"
        };
    private byte eventType;
    private Vector<NodeInfo> involvedNodeIDs;
    private NodeInfo parentID;
    private NodeInfo grandParentID;

    /**
     * Creates new update cache request..
     * @param src Request sender.
     * @param dest Request destination.
     * @param topicId Id of the topic
     * @param operationType [EVENT TYPE] of the request, see list of the event types.
     * @param involved_node The node which will be added/removed from cache in selected topic.
     */
    public TopologyCacheUpdateRequest(NodeInfo src, NodeInfo dest, String topicId, byte operationType, int transID) {
        super(src, dest, topicId, PubSubConstants.MSG_MAINTENANCE_CACHE_UPDATE, transID);
        this.eventType = operationType;
        this.involvedNodeIDs = new Vector<NodeInfo>();
        this.parentID = null;
        this.grandParentID = null;
    }
    public TopologyCacheUpdateRequest(){
		super(new NodeInfo("127.0.0.1", 0), new NodeInfo("127.0.0.1", 0), "", PubSubConstants.MSG_MAINTENANCE_CACHE_UPDATE, 0);

    }

    public void addInvolvedNode(NodeInfo node) {
        this.involvedNodeIDs.add(node);
    }

    public void setParentID(NodeInfo parent) {
        this.parentID = parent;
    }

    public void setGrandParentID(NodeInfo gparent) {
        this.grandParentID = gparent;
    }

    public byte[] encode() {
        ByteArrayOutputStream ostr = new ByteArrayOutputStream();
        DataOutputStream dtstr = new DataOutputStream(ostr);
        ObjectOutputStream ostr_ = null;
        try {
            //writing header inherited from PubSubRequest object
            byte[] header = super.encode();
            dtstr.write(header);
            dtstr.writeByte(this.eventType);
            ostr_ = new ObjectOutputStream(ostr);
            ostr_.writeObject(this.involvedNodeIDs);
            ostr_.writeObject(this.grandParentID);
            ostr_.writeObject(this.parentID);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        finally{
//            try {
//                ostr_.close();
//            } catch (IOException ex) {
//               //ignore
//            }
//        }

        return ostr.toByteArray();
    }

    /**
     * Parses type-dependent message contents.
     * @param stream Received byte buffer.
     * @param offset Number of bytes reserved for headers (they will be skipped while
     * 				 parsing).
     */
    public void parse(byte[] stream, int offset) {
        ByteArrayInputStream istream = new ByteArrayInputStream(stream);
        DataInputStream dtstr = new DataInputStream(istream);
        ObjectInputStream ostr_ = null;

        try {
            istream.skip(offset);
            super.parse(stream, offset);
            istream.skip(super.getByteLength());
            this.eventType = dtstr.readByte();
            ostr_ = new ObjectInputStream(dtstr);
            this.involvedNodeIDs = (Vector<NodeInfo>) ostr_.readObject();
            this.grandParentID = (NodeInfo) ostr_.readObject();
            this.parentID = (NodeInfo) ostr_.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        finally {
//            try {
//                ostr_.close();
//            } catch (IOException ex) {
//                //ignore
//            }
//        }
    }
    public byte getEventType(){
        return this.eventType;
    }
    public NodeInfo getParentNodeInfo(){
        return this.parentID;
    }
    public NodeInfo getGrandParentNodeInfo(){
        return this.grandParentID;
    }
    public Vector<NodeInfo> getInvolvedNodeInfos(){
        return this.involvedNodeIDs;
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("TopicID: "+getTopicID()+
                ", Internal EventType: "+operations[getEventType()]+
                " , source: "+ getSourceInfo()+
                ", destination: "+getDestinationInfo()+
                ", g-parent: "+getGrandParentNodeInfo()+
                ", parent: "+getParentNodeInfo()+
                ", involved nodes: "+getInvolvedNodeInfos());
        return sb.toString();

    }
}
