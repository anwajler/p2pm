package pl.edu.pjwstk.mteam.pubsub.tests.psnode;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.core.NetworkObject;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.core.NodeCallback;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.tests.events.EventManager;

import java.util.List;

public class PSNodeCallback implements NodeCallback {

    public static final Logger LOG = Logger.getLogger(PSNodeCallback.class);

    public void onDisconnect(Node node) {
        LOG.info("OnDisconnect invoked.....");
    }

    public void onOverlayError(Node node, Object sourceID, int errorCode) {
        LOG.info("OnOverlayError invoked node=" + node + " sourceID=" + sourceID + " errorCode=" + errorCode);
    }

    public void onJoin(Node node) {
        LOG.info(node.getUserName() + ": OnJoin invoked.....");
        EventManager.getInstance().addEventToQueue(PSNode.EVENT_ONJOIN, null);
    }

    public void onUserLookup(Node node, Object userInfo) {
        NodeInfo info = (NodeInfo) userInfo;
        LOG.info("OnUserLookup result: " + info.getIP() + ":" + info.getPort());
    }

    public void onTopicRemove(Node node, Object topicID) {
        LOG.info("onTopicRemove for topic '" + topicID + "' callback invoked");
    }

    public void onTopicCreate(Node node, Object topicID) {
        LOG.info(node.getUserName() + ": onTopicCreate for topic '" + topicID + "' callback invoked");
        EventManager.getInstance().addEventToQueue(PSNode.EVENT_ONTOPICCREATE, topicID);
    }

    public void onTopicSubscribe(Node node, Object topicID) {
        LOG.info(node.getUserName() + ": onTopicSubscribe for topic '" + topicID + "' callback invoked");
        EventManager.getInstance().addEventToQueue(PSNode.EVENT_ONTOPICSUBSCRIBE, topicID);
    }

    public void onInsertObject(Node node, NetworkObject object) {
        LOG.info("onInsertObject for key '" + object.getKey() + "' callback invoked");
    }

    public void onPubSubError(Node node, Object topicID, byte operationType, int errorCode) {
        LOG.info(node.getUserName() + ": OnPubSubError for topic '" + topicID + "' callback invoked (" + topicID + ", " + operationType + ", " + errorCode + ")");
        EventManager.getInstance().addEventToQueue(PSNode.EVENT_ONPUBSUBERROR, new Object[]{topicID, operationType, errorCode});
    }

    public void onTopicNotify(Node node, Object topicID, byte[] message) {
        LOG.info("onTopicNotify for topic '" + topicID + "' callback invoked");
        EventManager.getInstance().addEventToQueue(PSNode.EVENT_ONTOPICNOTIFY, new Object[]{topicID,message});
    }

    public void onTopicUnsubscribe(Node node, Object topicID) {
        LOG.info("onTopicUnsubscribe for topic '" + topicID + "' callback invoked");
    }

    public void onObjectLookup(Node node, Object object) {
        // TODO Auto-generated method stub

    }

    public boolean onDeliverRequest(List<NetworkObject> objectList) {
        if (LOG.isDebugEnabled()) LOG.debug("onDeliverRequest invoked with: " + objectList);
        //EventManager.getInstance().addEventToQueue(PSNode.EVENT_ONDELIVER, objectList);
        boolean result = true;
        NetworkObject obj = objectList.get(0);
        if (obj.getType() == NetworkObject.TYPE_PROTOTRUST) {
            byte[] msgbytes = obj.getValue();
            if (LOG.isDebugEnabled()) LOG.debug("onDeliverRequest invoked !!!");
            //Protocol-specific parsing goes here...

            // Return 'false' to prevent the MESSAGE object from being inserted into DHT
            result = false;
        }
        return result;
    }

    public boolean onForwardingRequest(List<NetworkObject> objectList) {
        boolean result = true;
        NetworkObject obj = objectList.get(0);
        if (obj.getType() == NetworkObject.TYPE_PROTOTRUST) {
            byte[] msgbytes = obj.getValue();
            //Protocol-specific parsing goes here...

            // Return 'false' to discard P2PP insert request encapsulating ProtoTrust message
        }
        return result;
    }

    public void onOverlayError(Node arg0, Object arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub

    }

    public void onPubSubError(Node node, Object topicID, byte operationType, int errorCode, int arg4) {
        LOG.info("OnPubSubError for topic '" + topicID + "' callback invoked");
    }

    public void onTopicCreate(Node node, Object topicID, int arg2) {
        LOG.info("onTopicCreate for topic '" + topicID + "' callback invoked (" + arg2 + ")");
        EventManager.getInstance().addEventToQueue(PSNode.EVENT_ONTOPICCREATE, topicID);
    }

    public void onTopicNotify(Node node, Object topicID, byte[] message, boolean arg3) {
        LOG.info("onTopicNotify for topic '" + topicID + "' callback invoked (" + arg3 + ")");
        EventManager.getInstance().addEventToQueue(PSNode.EVENT_ONTOPICNOTIFY, new Object[]{topicID,message,arg3});
    }

    public void onTopicSubscribe(Node node, Object topicID, int arg2) {
        LOG.info("onTopicSubscribe for topic '" + topicID + "' callback invoked (" + arg2 + ")");
        EventManager.getInstance().addEventToQueue(PSNode.EVENT_ONTOPICSUBSCRIBE, topicID);
    }

}
