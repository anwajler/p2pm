package pl.edu.pjwstk.mteam.jcsync.core;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectNotExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.OperationForbiddenException;
import pl.edu.pjwstk.mteam.jcsync.operation.RegisteredOperations;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.p2p.P2PNode.RoutingAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.operation.SubscribeOperation;
import pl.edu.pjwstk.p2pp.P2PPManager;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerBootstrapServer;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerConstants;
import pl.edu.pjwstk.p2pp.util.P2PPMessageFactory;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 *
 * @author Piotr Bucior
 */
public class JCSyncCoreTest {

    static P2PNode node;
    static JCSyncCore core;
    static P2PNode node2;
    static JCSyncCore core2;
    public static P2PPManager manager;
    
    public JCSyncCoreTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Thread server = new Thread(new Runnable() {
            

            @Override
            public void run() {
                try{
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
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            
        });
        server.start();
        node = new P2PNode(null, RoutingAlgorithm.SUPERPEER);
        node.setServerReflexiveAddress("127.0.0.1");
        node.setServerReflexivePort(7080);
        node.setBootIP("127.0.0.1");
        node.setBootPort(7080);
        node.setUserName("node1");
        node.setUdpPort(9090);
        node.networkJoin();
        while(!node.isConnected()){
            Thread.sleep(100);
        }
        core = new JCSyncCore(node, 5050);
        core.init();
        
        node2 = new P2PNode(null, RoutingAlgorithm.SUPERPEER);
        node2.setServerReflexiveAddress("127.0.0.1");
        node2.setServerReflexivePort(7080);
        node2.setBootIP("127.0.0.1");
        node2.setBootPort(7080);
        node2.setUserName("Node22");
        node2.setUdpPort(6060);
        node2.networkJoin();
        while(!node2.isConnected()){
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
    

    /**
     * Test of createSharedObject method, of class JCSyncCore.
     */
    @Test
    public void testCreateSharedObject() throws Exception {
        System.out.println("creatingSharedObject ...");
        String name = "Cobrowsing";
        boolean subscribeIfExist = false;     
        Topic result = core.createSharedObject(name,true);
        assertNotNull("(createSharedObject) returns null, which is not approved",result);
        assertEquals(result.getID(), name);
        Topic t2 = core.getAssignedTopic(name);        
        System.out.println("zzz");        
    }
    /**
     * Test of createSharedObject method, of class JCSyncCore, trying to create existed object.
     */
    @Test (expected=ObjectExistsException.class)
    public void testCreateSharedObject_existed() throws Exception {
        System.out.println("creatingExistedSharedObject ...");
        String name1 = "topic1";
        Topic t1 = core.createSharedObject(name1, true);
        boolean th = false;
        Topic res2 = null;        
        res2 = core2.createSharedObject(name1,true);        
    }
    
    @Test
    public void testCreateSharedObject_withACRules() throws Exception {
        System.out.println("creatingSharedObject with custom acRules ...");
        String name = "custom_ac_rules";
        Topic t = new Topic(name);
        Subscriber subscriber = new Subscriber("node1", t);
        t.setOwner(subscriber);
        
        AccessControlRules acRules = new AccessControlRules(t);
        
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).getOperation().addEvent(new Event(RegisteredOperations.OP_REQ_TRANSFER_OBJECT));
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(RegisteredOperations.OP_REQ_TRANSFER_OBJECT, null);
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).getOperation().addEvent(new Event(RegisteredOperations.OP_IND_TRANSFER_OBJECT));
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(RegisteredOperations.OP_IND_TRANSFER_OBJECT, null);
        
        boolean subscribeIfExist = false;     
        Topic result = core.createSharedObject(name,true,acRules);
        assertNotNull("(createSharedObject) returns null, which is not approved",result);
        assertEquals(result.getID(), name);
        Topic t2 = core.getAssignedTopic(name);    
        assertNotNull("AccessControlRules must be the same", 
                t2.getAccessControlRules().getRule(PubSubConstants.OPERATION_PUBLISH).getUsers(RegisteredOperations.OP_REQ_TRANSFER_OBJECT));
        assertNotNull("AccessControlRules must be the same", 
                t2.getAccessControlRules().getRule(PubSubConstants.OPERATION_NOTIFY).getUsers(RegisteredOperations.OP_IND_TRANSFER_OBJECT));
        Topic t3 = core2.subscribeSharedObject(name, true);
        assertNotNull("AccessControlRules must be the same", 
                t3.getAccessControlRules().getRule(PubSubConstants.OPERATION_PUBLISH).getUsers(RegisteredOperations.OP_REQ_TRANSFER_OBJECT));
        assertNotNull("AccessControlRules must be the same", 
                t3.getAccessControlRules().getRule(PubSubConstants.OPERATION_NOTIFY).getUsers(RegisteredOperations.OP_IND_TRANSFER_OBJECT));
    }

    /**
     * Test of subscribeSharedObject method, of class JCSyncCore.
     */
    @Test
    public void testSubscribeSharedObject() throws Exception{
        System.out.println("subscribingExistedSharedObject ...");
        String name = "topic_sso";
        Topic t1 = core.createSharedObject(name, true);
        core.subscribeSharedObject(name, true);
        boolean th = false;
        Topic res2 = null;
            res2 = core2.subscribeSharedObject(name,true);
        assertNotNull("(createSharedObject) returns null, which is not approved",res2);
        assertEquals(res2.getID(), name);   
        assertEquals(res2.getID(), t1.getID());
    }
    /**
     * try to subscribe to the nonexistent object, ObjectNotExistsEXception expected
     */
    
    @Test (expected=ObjectNotExistsException.class)
    public void testSubscribeSharedObject_unexisted() throws Exception{
        System.out.println("testSubscribeSharedObject_unexisted() ...");
        String name = "Cobrowsing2";
        boolean th = false;
        Topic res2 = null;
        res2 = core2.subscribeSharedObject(name,true);
    }
     /**
     * try to subscribe to to the forbidden object
     * @throws Exception 
     */
    @Test (expected=OperationForbiddenException.class)
    public void testSubscribeSharedObject_forbidden() throws Exception {
        System.out.println("testSubscribeSharedObject_forbidden()");
        String name = "not_for_subscribe";   
        Topic result = core.createSharedObject(name,true);
        assertNotNull("(createSharedObject) returns null, which is not approved",result);
        assertEquals(result.getID(), name);
        Subscriber user2 = new Subscriber("Node22",result);
        Event e = new Event(PubSubConstants.EVENT_ALL);		
        SubscribeOperation testOperation = new SubscribeOperation(result.getID(), user2, e);
        Subscriber user1 = new Subscriber("node1", result);
        SubscribeOperation testOperation1 = new SubscribeOperation(result.getID(),user1 , e);
        AccessControlRules ac = result.getAccessControlRules();
        //ac.removeRule(PubSubConstants.OPERATION_SUBSCRIBE);
        ac.getRule(PubSubConstants.OPERATION_SUBSCRIBE).addUser(PubSubConstants.EVENT_ALL, user1);
        Topic t2 = core2.subscribeSharedObject(name,true);
    }
    @Test
    public void testUnsubscribeSharedObject() throws Exception {
        System.out.println("testUnsubscribeSharedObject() ...");
        String name = "to_usubscribe";   
        Topic result = core.createSharedObject(name,true);
        assertNotNull("(createSharedObject) returns null, which is not approved",result);
        assertEquals(result.getID(), name);
        Topic t2 = core2.subscribeSharedObject(name,true);
        assertNotNull("(createSharedObject) returns null, which is not approved",t2);
        assertEquals("second topic name is different",result.getID(), t2.getID());        
        assertTrue("Must be only one subscriber now",core.getAssignedTopic(name).getChildren().size()==1);
        assertTrue("Must be only one subscriber now",core2.getAssignedTopic(name).getChildren().size()==1);
        core.subscribeSharedObject(name, true);
        int children1 = 0;
        if(core.getAssignedTopic(name).getGrandParent()!=null) children1++;
        if(core.getAssignedTopic(name).getParent()!=null) children1++;
        children1+=core.getAssignedTopic(name).getChildren().size();
        int children2 = 0;
        if(core2.getAssignedTopic(name).getGrandParent()!=null) children2++;
        if(core2.getAssignedTopic(name).getParent()!=null) children2++;
        children2+=core2.getAssignedTopic(name).getChildren().size();
        assertEquals("The count of subscribers must be the same (=2)",2,children1);
        assertEquals("The count of subscribers must be the same (=2)",children1, children2);   
        core2.unsubscribeSharedObject(name, true);
        assertTrue("Must be only one subscriber now",core.getAssignedTopic(name).getChildren().size()==1);
        Topic t3 = core2.getAssignedTopic(name);
        assertNull("Null topic expected after unsubscribe",t3);
        Topic t4 = core.getAssignedTopic(name);
        Subscriber s = t4.getChild("Node22");
        assertNull("Unsubscribed user should be removed from children list, NULL expected here",s);
    }
    
    /**
     * try to remove shared object
     */
    @Test
    public void testRemoveSharedObject() throws Exception{
        System.out.println("testRemoveSharedObject() ...");
        String name = "Cobrowsing";
        boolean th = false;
        Topic res2 = null;
            th = core.removeSharedObject(name,true);
        res2 = core.getAssignedTopic(name);
        assertNull(res2);   
    }
    @Ignore
    @Test (timeout=3000, expected=ObjectNotExistsException.class)
    public void testRemoveSharedObject2() throws Exception{
        System.out.println("testRemoveSharedObject() ...");
        String name = "toRemove";
        boolean th = false;
        Topic res2 = null;
            th = core.removeSharedObject(name,true);
        res2 = core.getAssignedTopic(name);
        assertNull(res2);   
    }
   
    
}
