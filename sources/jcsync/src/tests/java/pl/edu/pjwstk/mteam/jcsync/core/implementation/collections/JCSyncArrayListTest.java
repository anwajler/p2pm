package pl.edu.pjwstk.mteam.jcsync.core.implementation.collections;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCore;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectNotExistsException;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.p2p.P2PNode.RoutingAlgorithm;
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
public class JCSyncArrayListTest {
    static P2PNode node;
    static JCSyncCore core;
    static P2PNode node2;
    static JCSyncCore core2;
    public static P2PPManager manager;
    public static JCSyncHashMap<String,String> h1;
    public static JCSyncHashMap<String,String> h2;
    public static String n1 = "map1";
    public static SharedCollectionObject s1;
    public static SharedCollectionObject s2;
    public JCSyncArrayListTest() {
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
    @Test (timeout=5000)
    public void testConstructor1() throws ObjectExistsException, Exception{
        JCSyncArrayList<String> list1;
        JCSyncArrayList<String>  list2;
        String name = "list1";
        ArrayList<String> al = new ArrayList<String>();
        
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<String>(al);     
        list2 = new JCSyncArrayList<String>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 =(SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        assertEquals(list1,list2);
    }
    
    @Test (timeout=5000,expected=ObjectExistsException.class)
    public void testConstructor2() throws ObjectExistsException, Exception{
        JCSyncArrayList<String> list1;
        JCSyncArrayList<String>  list2;
        String name = "list1_2";
        ArrayList<String> al = new ArrayList<String>();
        
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<String>(al);     
        list2 = new JCSyncArrayList<String>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = new SharedCollectionObject(name, list2, core2);
    }
    @Test (timeout=5000)
    public void testConstructor3() throws ObjectExistsException, Exception{
        JCSyncArrayList<String> list1;
        JCSyncArrayList<String>  list2;
        String name = "list1_3";
        ArrayList<String> al = new ArrayList<String>();
        al.add("value1");
        al.add("value2");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<String>(al);     
        list2 = new JCSyncArrayList<String>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<String>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==2);
    }
    @Test (timeout=5000,expected=ObjectNotExistsException.class)
    public void testGetFromOverlay() throws Exception{
        JCSyncArrayList<String> list1;
        JCSyncArrayList<String>  list2;
        String name = "list_nonexistent";
        ArrayList<String> al = new ArrayList<String>();
        al.add("value1");
        al.add("value2");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<String>(al);     
        list2 = new JCSyncArrayList<String>(); 
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<String>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==2);
    }
    /**
     * Test of add method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testAdd_GenericType() throws Exception {
        String name = "testAdd_GenericType()";
        JCSyncArrayList<String> list1;
        JCSyncArrayList<String>  list2;        
        ArrayList<String> al = new ArrayList<String>();
        al.add("value1");
        al.add("value2");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<String>();     
        list2 = new JCSyncArrayList<String>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<String>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==0);
        list2.add("value3");
        Thread.sleep(500);
        assertTrue(list1.size()==1);    
    }
    @Test (timeout=5000)
    public void testAdd_GenericType_2() throws Exception {
        String name = "testAdd_GenericType_2()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>(al);     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==2);
        list2.add(200);
        Thread.sleep(500);
        assertTrue(list1.size()==3);    
    }

    /**
     * Test of add method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testAdd_int_GenericType() throws Exception {
        String name = "testAdd_int_GenericType()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>(al);     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==2);
        list2.add(0,200);
        Thread.sleep(500);
        assertTrue(list1.size()==3); 
        assertTrue(list1.get(0)==200);
        assertTrue(list2.get(0)==200);
    }

    /**
     * Test of addAll method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testAddAll_Collection() throws Exception{
        String name = "testAddAll_Collection()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>();     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==0);
        list2.addAll(al);
        Thread.sleep(500);
        assertTrue(list1.size()==2); 
        ArrayList<Integer> al2 = new ArrayList<Integer>();
        al2.add(200);
        al2.add(250);
        list1.addAll(al2);
        Thread.sleep(500);
        assertTrue(list2.size()==4); 
        assertTrue(list1.size()==4); 
    }

    /**
     * Test of addAll method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testAddAll_int_Collection() throws Exception {
        String name = "testAddAll_int_Collection()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>();     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==0);
        list2.addAll(al);
        Thread.sleep(500);
        assertTrue(list1.size()==2); 
        ArrayList<Integer> al2 = new ArrayList<Integer>();
        al2.add(200);
        al2.add(250);
        list1.addAll(1,al2);
        Thread.sleep(500);
        assertTrue(list2.size()==4); 
        assertTrue(list1.size()==4); 
        assertTrue(list2.get(1) == 200);
        assertTrue(list1.get(1) == 200);
        assertTrue(list2.get(2) == 250);
        assertTrue(list1.get(2) == 250);
        assertTrue(list2.get(0) == 100);
        assertTrue(list1.get(0) == 100);
        assertTrue(list2.get(3) == 150);
        assertTrue(list1.get(3) == 150);
    }

    /**
     * Test of clear method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testClear() throws Exception {
        String name = "testClear()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>();     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==0);
        list2.addAll(al);
        Thread.sleep(500);
        assertTrue(list1.size()==2); 
        list1.clear();
        Thread.sleep(500);
        assertTrue(list1.isEmpty());
        assertTrue(list2.isEmpty());        
    }

    /**
     * Test of clone method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testClone() throws Exception {
        String name = "testClone()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>();     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==0);
        list2.addAll(al);
        Thread.sleep(500);        
        ArrayList<Integer> cal = (ArrayList<Integer>) list1.clone();
        ArrayList<Integer> cal2 = (ArrayList<Integer>) list1.clone();
        assertEquals(cal,cal2);
        assertEquals(cal,list1);
        cal.add(111);
        assertNotSame(cal,cal2);
        assertNotSame(cal,list1);
        assertNotSame(cal,list2);
        list1.add(111);
        Thread.sleep(500);
        assertEquals(cal,list1);
        assertEquals(cal,list2);
        assertNotSame(cal2,list1);
        
    }

    /**
     * Test of contains method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testContains() throws Exception {
        String name = "testContains()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>();     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==0);
        list2.addAll(al);
        Thread.sleep(500); 
        assertTrue(list1.contains(100));
        assertTrue(list2.contains(100));
        assertFalse(list1.contains(200));
    }

    /**
     * Test of get method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testGet() throws Exception {
        String name = "testGet()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>();     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==0);
        list2.addAll(al);
        Thread.sleep(500); 
        assertTrue(list1.get(0)==100);
        assertTrue(list2.get(1)==150);
    }

    /**
     * Test of indexOf method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testIndexOf() throws Exception {
        String name = "testIndexOf()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>();     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==0);
        list2.addAll(al);
        Thread.sleep(500); 
        assertTrue(list1.indexOf(100)==0);
        assertTrue(list1.indexOf(100)==0);
    }

    /**
     * Test of isEmpty method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testIsEmpty() throws Exception {
        String name = "testIsEmpty()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>();     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==0);
        list2.addAll(al);
        Thread.sleep(500); 
        assertFalse(list1.isEmpty());
        assertFalse(list2.isEmpty());
        list1.clear();
        Thread.sleep(500);
        assertTrue(list1.isEmpty());
        assertTrue(list2.isEmpty());
        list1.addAll(al);
        Thread.sleep(500); 
        assertFalse(list1.isEmpty());
        assertFalse(list2.isEmpty());
        list2.clear();
        Thread.sleep(500);
        assertTrue(list1.isEmpty());
        assertTrue(list2.isEmpty());
    }

    /**
     * Test of remove method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testRemove_int() throws Exception {
        String name = "testRemove_int()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        al.add(250);
        al.add(350);
        al.add(450);
        al.add(550);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>();     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==0);
        list2.addAll(al);
        Thread.sleep(500); 
        Integer fr = list1.remove(2);
        assertTrue(fr.intValue()==250);
        Thread.sleep(500);
        assertTrue(list2.get(2)==350);
    }

   

    /**
     * Test of remove method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testRemove_Object() throws Exception {
        String name = "testRemove_Object()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        al.add(250);
        al.add(350);
        al.add(450);
        al.add(550);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>();     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==0);
        list2.addAll(al);
        Thread.sleep(500); 
        boolean fr = list1.remove(new Integer(250));
        assertTrue(fr);
        Thread.sleep(500);
        assertTrue(list2.get(2)==350);
        fr = list2.remove(new Integer(111));
        assertFalse(fr);
    }

    /**
     * Test of removeRange method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testRemoveRange() throws Exception {
        String name = "testRemoveRange()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        al.add(250);
        al.add(350);
        al.add(450);
        al.add(550);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>();     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==0);
        list2.addAll(al);
        Thread.sleep(500); 
        list1.removeRange(0, 3);
        Thread.sleep(500);
        assertTrue(list2.size()==3);
        assertEquals(list1,list2);
        assertTrue(list1.get(0)==350);
    }

    

    /**
     * Test of set method, of class JCSyncArrayList.
     */
    @Test (timeout=5000)
    public void testSet() throws Exception {
        String name = "testSet()";
        JCSyncArrayList<Integer> list1;
        JCSyncArrayList<Integer>  list2;        
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(100);
        al.add(150);
        al.add(250);
        al.add(350);
        al.add(450);
        al.add(550);
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        list1 = new JCSyncArrayList<Integer>();     
        list2 = new JCSyncArrayList<Integer>(); 
        so_1 = new SharedCollectionObject(name, list1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        list2 = (JCSyncArrayList<Integer>) so_2.getNucleusObject();
        assertEquals(list1, list2);
        assertTrue(list2.size()==0);
        list2.addAll(al);
        Thread.sleep(500);
        assertTrue(list2.get(2)==250);
        int r = list2.set(2, 555);
        assertTrue(r==250);
        Thread.sleep(500);
        assertEquals(list1, list2);
        assertTrue(list2.get(2)==555);
    }
    
}
