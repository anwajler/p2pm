package pl.edu.pjwstk.mteam.jcsync.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import pl.edu.pjwstk.mteam.jcsync.core.consistencyManager.DefaultConsistencyManager;
import static org.junit.Assert.*;
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
public class JCSyncAbstractSharedObjectTest {

    static P2PNode node;
    static JCSyncCore core;
    static P2PNode node2;
    static JCSyncCore core2;
    public static P2PPManager manager;

    public JCSyncAbstractSharedObjectTest() {
    }

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
        node = new P2PNode(null, RoutingAlgorithm.SUPERPEER);
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

        node2 = new P2PNode(null, RoutingAlgorithm.SUPERPEER);
        node2.setServerReflexiveAddress("127.0.0.1");
        node2.setServerReflexivePort(7080);
        node2.setBootIP("127.0.0.1");
        node2.setBootPort(7080);
        node2.setUserName("Node22");
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
    /**
     * test simple object creating method
     * @throws Exception 
     */
    @Test
    public void testCreateNewObject() throws Exception {
        System.out.println("(testCreateSharedObject)");
        String name = "s1";
        String simpleNucleaus = "SimpleNucleus";
        SimpleSharedObject s1 = null;
        s1 = new SimpleSharedObject(name,  core, DefaultConsistencyManager.class);
        assertNotNull("shared object are not initialized!", s1);
    }
    /**
     * attempt to create existing object
     * @throws ObjectExistsException
     * @throws Exception 
     */
    @Test(timeout=5000, expected = ObjectExistsException.class)
    public void testCreateNewObject_existed() throws ObjectExistsException, Exception {
        System.out.println("(testCreateSharedObject_existed)");
        String name = "s2";
        String simpleNucleaus = "SimpleNucleus";
        SimpleSharedObject s1 = null;
        s1 = new SimpleSharedObject(name,  core, DefaultConsistencyManager.class);
        assertNotNull("shared object are not initialized!", s1);
        SimpleSharedObject s2 = null;
        s2 = new SimpleSharedObject(name,  core2, DefaultConsistencyManager.class);
    }
    
    /**
     * validates an OP_REQ_OBJECT_TRANSFER operation
     * @throws Exception 
     */
    @Test (timeout=5000)
    public void testGetFromOverlay() throws Exception {
        System.out.println("(testGetFromOverlay)");
        String name = "ObjectFromOverlay";
        String simpleNucleaus = "SimpleNucleus";
        SimpleSharedObject s1 = null;
        Topic t = new Topic(name);
        Subscriber subscriber = new Subscriber("node1", t);
        t.setOwner(subscriber);
        AccessControlLists acRules = new AccessControlLists(t);
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).getOperation().addEvent(new Event(RegisteredOperations.OP_REQ_TRANSFER_OBJECT));
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(RegisteredOperations.OP_REQ_TRANSFER_OBJECT, null);
        
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).getOperation().addEvent(new Event(RegisteredOperations.OP_IND_TRANSFER_OBJECT));
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(RegisteredOperations.OP_IND_TRANSFER_OBJECT, null);
        
        s1 = new SimpleSharedObject(name,  core, DefaultConsistencyManager.class, acRules);
        assertNotNull("shared object are not initialized!", s1);
        JCSyncAbstractSharedObject s2 = null;
        s2 = JCSyncAbstractSharedObject.getFromOverlay(name, core2);
        assertTrue(s1.getID().compareTo(s2.getID()) == 0);
        assertEquals(s1.getClass(), s2.getClass());
        Thread.sleep(3000);
    }
    /**
     * validates write - locking mechanism 
     * @throws Exception 
     */
    @Test //(timeout=3000)
    public void beforeAfterPublishOperation() throws Exception {
        System.out.println("(beforeAfterPublishOperation)");
        String name = "testBeforeAfter";
        String simpleNucleaus = "SimpleNucleus";
        SimpleSharedObject s1 = null;
        Topic t = new Topic(name);
        Subscriber subscriber = new Subscriber("node1", t);
        t.setOwner(subscriber);
        AccessControlLists acRules = new AccessControlLists(t);
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).getOperation().addEvent(new Event(RegisteredOperations.OP_REQ_TRANSFER_OBJECT));
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(RegisteredOperations.OP_REQ_TRANSFER_OBJECT, null);
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).getOperation().addEvent(new Event(RegisteredOperations.OP_IND_TRANSFER_OBJECT));
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(RegisteredOperations.OP_IND_TRANSFER_OBJECT, null);
        
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).getOperation().addEvent(new Event(RegisteredOperations.OP_REQ_LOCK_APPLY));
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(RegisteredOperations.OP_REQ_LOCK_APPLY, null);
        
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).getOperation().addEvent(new Event(RegisteredOperations.OP_IND_LOCK_APPLY));
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(RegisteredOperations.OP_IND_LOCK_APPLY, null);
        
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).getOperation().addEvent(new Event(RegisteredOperations.OP_REQ_LOCK_RELEASE));
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(RegisteredOperations.OP_REQ_LOCK_RELEASE, null);
        
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).getOperation().addEvent(new Event(RegisteredOperations.OP_IND_LOCK_RELEASE));
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(RegisteredOperations.OP_IND_LOCK_RELEASE, null);
        s1 = new SimpleSharedObject(name,  core, DefaultConsistencyManager.class, acRules);
        assertNotNull("shared object are not initialized!", s1);
        SimpleSharedObject s2 = null;
        s2 = (SimpleSharedObject) JCSyncAbstractSharedObject.getFromOverlay(name, core2);
        assertTrue(s1.getID().compareTo(s2.getID()) == 0);
        assertEquals(s1.getClass(), s2.getClass());
        s2.lockObject();
        s2.unlockObject();
    }
    /**
     * validates simple non-blocking write operation
     * @throws Exception 
     */
    @Test (timeout=3000)
    public void publishNonBlockingWriteTest() throws Exception{
        System.out.println("(beforeAfterPublishOperation)");
        String name = "testPublishWrite";
        String simpleNucleaus = "SimpleNucleus";
        SimpleSharedObject s1 = null;
        Topic t = new Topic(name);
        Subscriber subscriber = new Subscriber("node1", t);
        t.setOwner(subscriber);
        AccessControlLists acRules = new AccessControlLists(t);
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).getOperation().addEvent(new Event(RegisteredOperations.OP_REQ_TRANSFER_OBJECT));
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(RegisteredOperations.OP_REQ_TRANSFER_OBJECT, null);
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).getOperation().addEvent(new Event(RegisteredOperations.OP_IND_TRANSFER_OBJECT));
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(RegisteredOperations.OP_IND_TRANSFER_OBJECT, null);
        
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).getOperation().addEvent(new Event(RegisteredOperations.OP_REQ_LOCK_APPLY));
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(RegisteredOperations.OP_REQ_LOCK_APPLY, null);
        
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).getOperation().addEvent(new Event(RegisteredOperations.OP_IND_LOCK_APPLY));
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(RegisteredOperations.OP_IND_LOCK_APPLY, null);
        
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).getOperation().addEvent(new Event(RegisteredOperations.OP_REQ_LOCK_RELEASE));
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(RegisteredOperations.OP_REQ_LOCK_RELEASE, null);
        
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).getOperation().addEvent(new Event(RegisteredOperations.OP_IND_LOCK_RELEASE));
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(RegisteredOperations.OP_IND_LOCK_RELEASE, null);
        
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).getOperation().addEvent(new Event(RegisteredOperations.OP_IND_WRITE_METHOD));
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(RegisteredOperations.OP_IND_WRITE_METHOD, null);
        
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).getOperation().addEvent(new Event(RegisteredOperations.OP_REQ_WRITE_METHOD));
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(RegisteredOperations.OP_REQ_WRITE_METHOD, null);
        
        
        s1 = new SimpleSharedObject(name, core, DefaultConsistencyManager.class, acRules);
        assertNotNull("shared object are not initialized!", s1);
        SimpleSharedObject s2 = null;
        s2 = (SimpleSharedObject) JCSyncAbstractSharedObject.getFromOverlay(name, core2);
        assertTrue(s1.getID().compareTo(s2.getID()) == 0);
        assertEquals(s1.getClass(), s2.getClass());
        s2.add("testObject");
        assertTrue(s2.size()==1);
        assertTrue(s1.size()==1);
    }
    /**
     * validates simple blocking write operation
     * @throws Exception 
     */
    @Test (timeout=7000)
    public void publishBlockedWriteTest() throws Exception{
        System.out.println("(beforeAfterPublishOperation)");
        String name = "testPublishBlockedWrite";
        String simpleNucleaus = "SimpleNucleus";
        final SimpleSharedObject s1;
        Topic t = new Topic(name);
        Subscriber subscriber = new Subscriber("node1", t);
        t.setOwner(subscriber);
        AccessControlLists acRules = new AccessControlLists(t);
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).getOperation().addEvent(new Event(RegisteredOperations.OP_REQ_TRANSFER_OBJECT));
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(RegisteredOperations.OP_REQ_TRANSFER_OBJECT, null);
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).getOperation().addEvent(new Event(RegisteredOperations.OP_IND_TRANSFER_OBJECT));
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(RegisteredOperations.OP_IND_TRANSFER_OBJECT, null);
        
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).getOperation().addEvent(new Event(RegisteredOperations.OP_REQ_LOCK_APPLY));
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(RegisteredOperations.OP_REQ_LOCK_APPLY, null);
        
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).getOperation().addEvent(new Event(RegisteredOperations.OP_IND_LOCK_APPLY));
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(RegisteredOperations.OP_IND_LOCK_APPLY, null);
        
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).getOperation().addEvent(new Event(RegisteredOperations.OP_REQ_LOCK_RELEASE));
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(RegisteredOperations.OP_REQ_LOCK_RELEASE, null);
        
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).getOperation().addEvent(new Event(RegisteredOperations.OP_IND_LOCK_RELEASE));
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(RegisteredOperations.OP_IND_LOCK_RELEASE, null);
        
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).getOperation().addEvent(new Event(RegisteredOperations.OP_IND_WRITE_METHOD));
        acRules.getRule(PubSubConstants.OPERATION_NOTIFY).addUser(RegisteredOperations.OP_IND_WRITE_METHOD, null);
        
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).getOperation().addEvent(new Event(RegisteredOperations.OP_REQ_WRITE_METHOD));
        acRules.getRule(PubSubConstants.OPERATION_PUBLISH).addUser(RegisteredOperations.OP_REQ_WRITE_METHOD, null);
        
        
        s1 = new SimpleSharedObject(name, core, DefaultConsistencyManager.class, acRules);
        assertNotNull("shared object are not initialized!", s1);
        SimpleSharedObject s2 = null;
        s2 = (SimpleSharedObject) JCSyncAbstractSharedObject.getFromOverlay(name, core2);
        assertTrue(s1.getID().compareTo(s2.getID()) == 0);
        assertEquals(s1.getClass(), s2.getClass());
        s2.lockObject();
        s2.add("testObject");
        assertTrue(s2.size()==1);
        assertTrue(s1.size()==1);
        
        final Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();
        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                s1.add("after_lock");
                // wait a while for notify propagation, 5 seconds will be enough 
                //  to receive and invoke notify indication on s2
                try {                    
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
                semaphore.release();
            }
        });
        //start the thread to send request
        t1.start();        
        //sent unlock request
        s2.unlockObject();
        //wait for the t1
        semaphore.acquire();
        assertTrue(s2.size()==2);
        assertTrue(s1.size()==2);
        assertTrue(((String)s1.get(1)).compareTo("after_lock")==0);
        
        
    }
    /**
     * checks validation of AClist - the event:OP_REQ_TRANSFER_OBJECT is not 
     * added to the AClist - operation forbidden expected
     * @throws Exception 
     */
    @Test(timeout = 3000, expected = OperationForbiddenException.class)
    public void testGetFromOverlay_forbidden_operation() throws Exception {
        System.out.println("(testGetFromOverlay_forbidden_operation)");
        String name = "OFO_forbidden";
        String simpleNucleaus = "SimpleNucleus2";
        SimpleSharedObject s1 = null;
        Topic t = new Topic(name);
        Subscriber subscriber = new Subscriber("node1", t);
        t.setOwner(subscriber);
        AccessControlLists acRules = new AccessControlLists(t);
        acRules.getRule(PubSubConstants.OPERATION_SUBSCRIBE).addUser(PubSubConstants.EVENT_ALL,subscriber);
        s1 = new SimpleSharedObject(name, core2, DefaultConsistencyManager.class,acRules);
        assertNotNull("shared object are not initialized!", s1);
        JCSyncAbstractSharedObject s2 = null;
        s2 = JCSyncAbstractSharedObject.getFromOverlay(name, core);
    }
    /**
     * attempt to get nonexistent object from overlay, object not exists expected 
     * @throws Exception 
     */
    @Test(timeout = 3000, expected = ObjectNotExistsException.class)
    public void testGetFromOverlay_nonexistent() throws Exception {
        System.out.println("(testGetFromOverlay_nonexistent)");
        String name = "OFO_nonexistent";
        String simpleNucleaus = "SimpleNucleus2";
        JCSyncAbstractSharedObject s2 = null;
        s2 = JCSyncAbstractSharedObject.getFromOverlay(name, core);
    }

    /**
     * Test of getID method, of class JCSyncAbstractSharedObject.
     */
    @Ignore
    @Test
    public void testGetID() {
        System.out.println("getID");
        JCSyncAbstractSharedObject instance = null;
        String expResult = "";
        String result = instance.getID();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encode method, of class JCSyncAbstractSharedObject.
     */
    @Ignore
    @Test
    public void testEncode() {
        System.out.println("encode");
        JCSyncAbstractSharedObject instance = null;
        byte[] expResult = null;
        byte[] result = instance.encode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIdentifier method, of class JCSyncAbstractSharedObject.
     */
    @Ignore
    @Test
    public void testGetIdentifier() {
        System.out.println("getIdentifier");
        JCSyncAbstractSharedObject instance = null;
        Topic expResult = null;
        Topic result = instance.getIdentifier();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCoreAlgorith method, of class JCSyncAbstractSharedObject.
     */
    @Ignore
    @Test
    public void testGetCoreAlgorith() {
        System.out.println("getCoreAlgorith");
        JCSyncAbstractSharedObject instance = null;
        JCSyncCore expResult = null;
        JCSyncCore result = null;//instance.getCoreAlgorith();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of invokeReadOperation method, of class JCSyncAbstractSharedObject.
     */
    @Ignore
    @Test
    public void testInvokeReadOperation() throws Exception {
        System.out.println("invokeReadOperation");
        String methodName = "";
        Class[] argTypes = null;
        Object[] argValues = null;
        JCSyncAbstractSharedObject instance = null;
        Object expResult = null;
        Object result = instance.invokeReadOperation(methodName, argTypes, argValues);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of invokeWriteOperation method, of class JCSyncAbstractSharedObject.
     */
    @Ignore
    @Test
    public void testInvokeWriteOperation() throws Exception {
        System.out.println("invokeWriteOperation");
        String methodName = "";
        Class[] argTypes = null;
        Object[] argValues = null;
        JCSyncAbstractSharedObject instance = null;
        Object expResult = null;
        Object result = instance.invokeWriteOperation(methodName, argTypes, argValues, false);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConstructorFromOverlay method, of class JCSyncAbstractSharedObject.
     */
    @Ignore
    @Test
    public void testGetConstructorFromOverlay() throws Exception {
        System.out.println("getConstructorFromOverlay");
        String name = "";
        JCsyncAlgorithInterface core = null;
        byte[] expResult = null;
        byte[] result = JCSyncAbstractSharedObject.getConstructorFromOverlay(name, core);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}

class SimpleSharedObject extends JCSyncAbstractSharedObject implements List {
    
    public SimpleSharedObject(String name, JCSyncCore core, Class consistencyManager, AccessControlLists acRules) throws ObjectExistsException, Exception {

        super(name, new ArrayList(), core, consistencyManager, SimpleSharedObject.class, acRules);
    }

    public SimpleSharedObject(String name, JCSyncCore core, Class consistencyManager) throws ObjectExistsException, Exception {
        super(name, new ArrayList(), core, consistencyManager, SimpleSharedObject.class);
    }

    public SimpleSharedObject() {
        super();
    }
    
    public void lockObject()throws Exception{
        super.beforePublishReadOperation() ;
    }
    public void unlockObject()throws Exception {
        super.afterPublishReadOperation();
    }
    
    @Override
    public int size() {
        return ((ArrayList)getNucleus()).size();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object[] toArray(Object[] a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean add(Object e) {
        Class argTypes [] = {Object.class};
        Serializable argValues[] = {(Serializable)e};
        try {
            super.publishWriteOperation(super.getNucleusClass().getMethod("add", Object.class).getName(), argTypes,argValues);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }return false;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsAll(Collection c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(int index, Collection c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object get(int index) {
        return ((ArrayList)getNucleus()).get(index);
    }

    @Override
    public Object set(int index, Object element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(int index, Object element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object remove(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
