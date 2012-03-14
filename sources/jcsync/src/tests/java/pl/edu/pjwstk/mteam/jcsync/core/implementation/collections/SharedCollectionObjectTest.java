/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.pjwstk.mteam.jcsync.core.implementation.collections;

import java.io.Serializable;
import org.junit.*;
import static org.junit.Assert.*;
import pl.edu.pjwstk.mteam.jcsync.core.AccessControlLists;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCore;
import pl.edu.pjwstk.mteam.jcsync.exception.OperationForbiddenException;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.p2pp.P2PPManager;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerBootstrapServer;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerConstants;
import pl.edu.pjwstk.p2pp.util.P2PPMessageFactory;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;
import static pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations.*;

/**
 *
 * @author buti
 */
public class SharedCollectionObjectTest {

    public SharedCollectionObjectTest() {
    }
    static P2PNode node;
    static JCSyncCore core;
    static P2PNode node2;
    static JCSyncCore core2;
    public static P2PPManager manager;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Thread server = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    manager = new P2PPManager(0, 7080, 0, 0, 0, "", "", new P2PPMessageFactory(), "myOverlayID".getBytes("UTF-8"));
                    String hashAlgorithm = "SHA-1";
                    byte hashLength = 20;
                    byte hashBase = 2;
                    String overlayID = "myOverlayID";
                    manager.setOptions(new P2POptions(P2PPUtils.convertHashAlgorithmName(hashAlgorithm), hashLength,
                            P2PPUtils.convertP2PAlgorithmName(SuperPeerConstants.SUPERPEER_PROTOCOL_NAME), hashBase, overlayID.getBytes("UTF-8")));

                    SuperPeerBootstrapServer server = new SuperPeerBootstrapServer();
                    manager.addEntity(server);
                    manager.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        server.start();
        node = new P2PNode(null, P2PNode.RoutingAlgorithm.SUPERPEER);
        node.setServerReflexiveAddress("127.0.0.1");
        node.setServerReflexivePort(7080);
        node.setBootIP("127.0.0.1");
        node.setBootPort(7080);
        node.setUserName("node1");
        node.setUdpPort(9090);
        node.networkJoin();
        while (!node.isConnected()) {
            Thread.sleep(100);
        }
        core = new JCSyncCore(node, 5050);
        core.init();

        node2 = new P2PNode(null, P2PNode.RoutingAlgorithm.SUPERPEER);
        node2.setServerReflexiveAddress("127.0.0.1");
        node2.setServerReflexivePort(7080);
        node2.setBootIP("127.0.0.1");
        node2.setBootPort(7080);
        node2.setUserName("node2");
        node2.setUdpPort(6060);
        node2.networkJoin();
        while (!node2.isConnected()) {
            Thread.sleep(100);
        }
        core2 = new JCSyncCore(node2, 6065);
        core2.init();


    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        node.networkLeave();
        node2.networkLeave();
        manager.stop();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    @Test//(timeout = 3000)
    public void testGetFromOverlay_forbidden_operation() throws Exception {
        System.out.println("(testGetFromOverlay_forbidden_operation)");
        String name = "SCO_private";
        SharedCollectionObject s1 = null;
        JCSyncHashMap hs = new JCSyncHashMap();
        
        Topic t = new Topic(name);
        Subscriber subscriber = new Subscriber("node1", t);
        t.setOwner(subscriber);        
        
        AccessControlLists acRules = new AccessControlLists(t);        
        acRules.getRule(PubSubConstants.OPERATION_SUBSCRIBE).addUser(PubSubConstants.EVENT_ALL,subscriber);
        
        s1 = new SharedCollectionObject(name, hs, core, acRules);
        Thread.sleep(500);
        assertNotNull("shared object are not initialized!", s1);
        SharedCollectionObject s2 = null;
        
        Boolean b = false;
        try{
        s2 = (SharedCollectionObject) JCSyncAbstractSharedObject.getFromOverlay(name, core2);
        }catch(OperationForbiddenException ex){
            b = true;
        }
        assertTrue(b);
        
        Subscriber subscriber2 = new Subscriber("node2", t);    
        acRules.getRule(PubSubConstants.OPERATION_SUBSCRIBE).addUser(PubSubConstants.EVENT_ALL, subscriber2);
        core.modifyAccessControlLists(name, acRules);
        Thread.sleep(1000);
        b = true;
        try{
        s2 = (SharedCollectionObject) JCSyncAbstractSharedObject.getFromOverlay(name, core2);
        }catch(OperationForbiddenException ex){
            b = false;
        }
        assertTrue(b);
        assertNotNull("shared object are not initialized!", s2);
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(OP_REQ_WRITE_METHOD, subscriber);
        core.modifyAccessControlLists(name, acRules);
        Thread.sleep(1000);
        JCSyncHashMap hs_2 = (JCSyncHashMap) s2.getNucleusObject();        
        b = true;
        try{
        hs_2.put("key1", "value1");
        }catch(Exception ex){
            b = false;
        }
        assertFalse(b);
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(OP_REQ_WRITE_METHOD, subscriber2);
        core.modifyAccessControlLists(name, acRules);
        Thread.sleep(1000);
        hs_2.put("key1", "value1");
        b = true;
        try{
        hs_2.put("key1", "value1");
        }catch(Exception ex){
            b = false;
        }
        assertTrue(b);
    }

   
}
