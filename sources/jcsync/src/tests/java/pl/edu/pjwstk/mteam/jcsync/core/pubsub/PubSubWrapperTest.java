package pl.edu.pjwstk.mteam.jcsync.core.pubsub;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import pl.edu.pjwstk.mteam.core.NetworkObject;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.core.NodeCallback;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.p2p.P2PNode.RoutingAlgorithm;

/**
 *
 * @author Piotr Bucior
 */
public class PubSubWrapperTest {
    
    public static P2PNode node;
    public PubSubWrapperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        node = new P2PNode(new NodeCallback() {

            public void onDisconnect(Node node) {
               // throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onUserLookup(Node node, Object userInfo) {
               // throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onObjectLookup(Node node, Object object) {
               // throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onTopicNotify(Node node, Object topicID, byte[] message, boolean historical,short eventType) {
            }

            public void onTopicCreate(Node node, Object topicID) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onTopicCreate(Node node, Object topicID, int transID) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onTopicRemove(Node node, Object topicID) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onTopicSubscribe(Node node, Object topicID) {
             //   throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onTopicSubscribe(Node node, Object topicID, int transID) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onTopicUnsubscribe(Node node, Object topicID, int respCode) {
             //   throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onInsertObject(Node node, NetworkObject object) {
             //   throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onJoin(Node node) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onOverlayError(Node node, Object sourceID, int errorCode) {
             //   throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onOverlayError(Node node, Object sourceID, int errorCode, int transID) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onPubSubError(Node node, Object topicID, short operationType, int errorCode) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onPubSubError(Node node, Object topicID, short operationType, int errorCode, int transID) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean onDeliverRequest(List<NetworkObject> objectList) {
              //  throw new UnsupportedOperationException("Not supported yet.");
                return true;
            }

            public boolean onForwardingRequest(List<NetworkObject> objectList) {
              //  throw new UnsupportedOperationException("Not supported yet.");
               return true;
            }
        }, RoutingAlgorithm.SUPERPEER);
                node.setServerReflexiveAddress("192.168.5.103");
		node.setServerReflexivePort(7080);
		node.setBootIP("192.168.5.103");
		node.setBootPort(7080);
		node.setUserName("node1");
		node.setUdpPort(9090);
        
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

    /**
     * Test of init method, of class PubSubWrapper.
     */
    @Ignore("not implemented yet")
    @Test 
    public void testInit() {
        System.out.println("init");
        node.setUdpPort(9099);
        PubSubWrapper instance = null;
        instance.init();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
