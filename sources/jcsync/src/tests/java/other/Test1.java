/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package other;

import java.util.List;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.core.NetworkObject;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.core.NodeCallback;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper.PubSubWrapper;
import pl.edu.pjwstk.mteam.p2p.P2PNode;

/**
 *
 * @author
 */
public class Test1 {
    private final PubSubWrapper pubsubmgr = null;
//    public Test1() {
////        logger.info(" - initialising layer.....");
////        this.node = new P2PNode(callback);
////        this.node.enableDebug();
////        this.node.setUserName("Test1");
////        this.node.setTcpPort(7000);
////        this.node.setUdpPort(8000);
////        ((P2PNode) this.node).setServerReflexiveAddress("127.0.0.1");
////        ((P2PNode) this.node).setServerReflexivePort(7080);
////        this.node.setBootIP("127.0.0.1");
////        this.node.setBootPort(7080);
////        pubsubmgr = new PubSubWrapper(this.node.getTcpPort() + 2000, this.node);
////        pubsubmgr.setCustomizableAlgorithm();
////        this.node.networkJoin();
//
//    }
    private static Logger logger = Logger.getLogger("Test");
    private P2PNode node = null;
    private NodeCallback callback = new NodeCallback() {

        public void onDisconnect(Node node) {
            logger.info(" - OnDisconnect invoked.....");
        }

        public void onOverlayError(Node node, Object sourceID, int errorCode) {
            logger.info(" - OnOverlayError invoked.....");
        }

        public void onOverlayError(Node node, Object sourceID, int errorCode, int code2) {
            logger.info(" - OnOverlayError invoked.....");
        }

        public void onJoin(Node node) {
            logger.info(" - OnJoin invoked.....");
        }

        public void onUserLookup(Node node, Object userInfo) {
            NodeInfo info = (NodeInfo) userInfo;
            logger.info(" - OnUserLookup result: " + info.getIP() + ":" + info.getPort());
        }

        public void onTopicCreate(Node node, Object topicID, int code) {
            logger.info(" - onTopicCreate for topic '" + topicID + "' callback invoked");
        }

        public void onTopicNotify(Node node, Object topicId, byte[] message, boolean code) {

        }

        public void onTopicRemove(Node node, Object topicID) {
            logger.info(" - onTopicRemove for topic '" + topicID + "' callback invoked");
        }

        public void onTopicCreate(Node node, Object topicID) {
            logger.info(" - onTopicCreate for topic '" + topicID + "' callback invoked");
        }

        public void onTopicSubscribe(Node node, Object topicID) {
            logger.info(" - onTopicSubscribe for topic '" + topicID + "' callback invoked");
        }

        @Override
        public void onInsertObject(Node node, NetworkObject object) {
            logger.info(" - onInsertObject for key '" + object.getKey() + "' callback invoked");
        }

        @Override
        public void onPubSubError(Node node, Object topicID, byte operationType, int errorCode) {
            logger.info(" - OnPubSubError for topic '" + topicID + "' callback invoked");
        }

        @Override
        public void onPubSubError(Node node, Object topicID, byte operationType, int errorCode, int code2) {
            logger.info(" - OnPubSubError for topic '" + topicID + "' callback invoked");
        }

        public void onTopicNotify(Node node, Object topicID, byte[] message) {
            logger.info(" - onTopicNotify for topic '" + topicID + "' callback invoked");
        }

        public void onTopicSubscribe(Node node, Object topicID, int code) {
            logger.info(" - onTopicSubscribe for topic '" + topicID + "' callback invoked with code=" + code);
        }

        @Override
        public void onTopicUnsubscribe(Node node, Object topicID) {
            logger.info(" - onTopicUnsubscribe for topic '" + topicID + "' callback invoked");
        }

        @Override
        public void onObjectLookup(Node node, Object object) {
            // TODO Auto-generated method stub
        }

        @Override
        public boolean onDeliverRequest(List<NetworkObject> objectList) {
            boolean result = true;
            NetworkObject obj = objectList.get(0);
            if (obj.getType() == NetworkObject.TYPE_PROTOTRUST) {
                byte[] msgbytes = obj.getValue();
                //Protocol-specific parsing goes here...

                /* Return 'false' to prevent the MESSAGE object
                 * from being inserted into DHT
                 */
                result = false;
            }
            return result;
        }

        @Override
        public boolean onForwardingRequest(List<NetworkObject> objectList) {
            boolean result = true;
            NetworkObject obj = objectList.get(0);
            if (obj.getType() == NetworkObject.TYPE_PROTOTRUST) {
                byte[] msgbytes = obj.getValue();
                //Protocol-specific parsing goes here...

                /* Return 'false' to discard P2PP insert request
                 * encapsulating ProtoTrust message
                 */
            }
            return result;
        }
    };

    public static void main(String[] args) {
        new Test1();
    }
}
