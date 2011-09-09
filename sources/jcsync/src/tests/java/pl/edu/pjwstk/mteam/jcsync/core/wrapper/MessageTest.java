/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.pjwstk.mteam.jcsync.core.wrapper;
import pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper.DEBUG_MessageListener;
import pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper.PubSubWrapper;
import pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper.JCSyncMessageCarrier;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import pl.edu.pjwstk.mteam.core.NetworkObject;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.core.NodeCallback;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import static org.junit.Assert.*;
import org.junit.Test;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCoreAlgorithm;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncConstans;
import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncCreateCollectionRequest;
import pl.edu.pjwstk.mteam.pubsub.message.PubSubMessage;
import pl.edu.pjwstk.mteam.pubsub.transport.PubSubTransport;

/**
 *
 * @author Piotr Bucior
 */
public class MessageTest {

    private static PubSubWrapper pubsubwrapper;
    private static PubSubWrapper pubsubwrapper2;
    private static Logger logger = Logger.getLogger("jcsync.core.wrapper.MessageTest");
    public static P2PNode node = null;
    public static P2PNode node2 = null;
    static Boolean node1_started, node2_started = null;
    private static HashMap<String, PubSubMessage> delivered_responses = new HashMap<String, PubSubMessage>(3);
    private static HashMap<String, PubSubMessage> delivered_requests = new HashMap<String, PubSubMessage>(3);
    private static HashMap<String, PubSubMessage> delivered_indications = new HashMap<String, PubSubMessage>(3);
    private static HashMap<String, PubSubMessage> sent_responses = new HashMap<String, PubSubMessage>(3);
    private static HashMap<String, PubSubMessage> sent_requests = new HashMap<String, PubSubMessage>(3);
    private static HashMap<String, PubSubMessage> sent_indications = new HashMap<String, PubSubMessage>(3);

    public MessageTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Thread t1 = new Thread(new Runnable() {

            public void run() {
                node = new P2PNode(callback);
                node.setUserName("node1");
                node.setTcpPort(7000);
                node.setUdpPort(8000);
                ((P2PNode) node).setServerReflexiveAddress("127.0.0.1");
                ((P2PNode) node).setServerReflexivePort(7080);
                node.setBootIP("127.0.0.1");
                node.setBootPort(7080);
                pubsubwrapper = new PubSubWrapper(9000, node, new JCSyncCoreAlgorithm(node,9000));
                pubsubwrapper.setCustomizableAlgorithm();
                logger.info(" - initialising layer..... "+node);
                node.networkJoin();
            }
        });
        Thread t2 = new Thread(new Runnable() {

            public void run() {                
                node2 = new P2PNode(callback2);
                node2.setUserName("node2");
                node2.setTcpPort(7001);
                node2.setUdpPort(8001);
                ((P2PNode) node2).setServerReflexiveAddress("127.0.0.1");
                ((P2PNode) node2).setServerReflexivePort(7080);
                node2.setBootIP("127.0.0.1");
                node2.setBootPort(7080);
                pubsubwrapper2 = new PubSubWrapper(9001, node2, new JCSyncCoreAlgorithm(node2,9022));
                pubsubwrapper2.setCustomizableAlgorithm();
                logger.info(" - initialising layer..... "+node2);
                node2.networkJoin();
            }
        });
        t1.start();
        t2.start();

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test(timeout = 2500)
    public void n1_connected() {
        while (true) {
            if (node1_started != null) {
                break;
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }

        }
        assertTrue(node1_started.booleanValue());
    }
    @Test(timeout = 2500)
    public void n2_connected() {
        while (true) {
            if (node2_started != null) {
                break;
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }

        }
        assertTrue(node2_started.booleanValue());
    }
 
    @Test
    public void SendRequest(){
        JCSyncCreateCollectionRequest rq = new JCSyncCreateCollectionRequest(pubsubwrapper.getNodeInfo(), pubsubwrapper2.getNodeInfo(), "1", 1);
        JCSyncMessageCarrier req = new JCSyncMessageCarrier(rq);
        sent_requests.put(pubsubwrapper.getNodeInfo().getName(), req);
        logger.debug("Sending request:"+req.getInternalMessage().toString());
        assertTrue(pubsubwrapper.sendMessage(req, PubSubTransport.ROUTING_DIRECT, null));        
    }

    @Test (timeout = 2500)
    public void ReceiveRequest() {
        while (true) {
            if (delivered_requests.get(pubsubwrapper2.getNodeInfo().getName())!=null) {
                break;
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }
        JCSyncMessageCarrier req =(JCSyncMessageCarrier) delivered_requests.get(pubsubwrapper2.getNodeInfo().getName());
        JCSyncMessageCarrier sent_req = (JCSyncMessageCarrier) sent_requests.get(pubsubwrapper.getNodeInfo().getName());
        assertTrue((sent_req.getMessageType() & JCSyncConstans.JCSYNC_GENERIC_REQUEST) == JCSyncConstans.JCSYNC_GENERIC_REQUEST);
        
    }
    













    private static NodeCallback callback = new NodeCallback() {

        public void onDisconnect(Node node) {
            logger.info(" - OnDisconnect invoked.....");
        }

        public void onOverlayError(Node node, Object sourceID, int errorCode) {
            logger.info(" - OnOverlayError invoked.....");
            node1_started = Boolean.FALSE;
        }

        public void onJoin(Node node) {
            logger.info(" - OnJoin invoked..... "+node);
            node1_started = Boolean.TRUE;
        }

        public void onUserLookup(Node node, Object userInfo) {
            NodeInfo info = (NodeInfo) userInfo;
            logger.info(" - OnUserLookup result: " + info.getIP() + ":" + info.getPort());
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
        public void onTopicNotify(Node node, Object topicID, byte[] message) {
            logger.info(" - onTopicNotify for topic '" + topicID + "' callback invoked");
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
    private static NodeCallback callback2 = new NodeCallback() {

        public void onDisconnect(Node node) {
            logger.info(" - OnDisconnect invoked.....");
        }

        public void onOverlayError(Node node, Object sourceID, int errorCode) {
            logger.info(" - OnOverlayError invoked.....");
            node1_started = Boolean.FALSE;
        }

        public void onJoin(Node node) {
            logger.info(" - OnJoin invoked.....");
            node2_started = Boolean.TRUE;
        }

        public void onUserLookup(Node node, Object userInfo) {
            NodeInfo info = (NodeInfo) userInfo;
            logger.info(" - OnUserLookup result: " + info.getIP() + ":" + info.getPort());
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
        public void onTopicNotify(Node node, Object topicID, byte[] message) {
            logger.info(" - onTopicNotify for topic '" + topicID + "' callback invoked");
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
    private static DEBUG_MessageListener listener = new DEBUG_MessageListener() {
        public boolean onDeliverRequest(String ID, PubSubMessage msg) {
            logger.debug("ON DELIVER REQUEST ... :\n"+msg);
            delivered_requests.put(ID, msg);
            return true;
        }

        public boolean onDeliverResponse(String ID, PubSubMessage msg) {
            logger.debug("ON DELIVER RESPONSE ... :\n"+msg);
            delivered_responses.put(ID, msg);
            return true;
        }

        public boolean onDeliverIndication(String ID, PubSubMessage msg) {
            logger.debug("ON DELIVER INDICATION ... :\n"+msg);
            delivered_indications.put(ID, msg);
            return true;
        }
    };
}
