package pl.edu.pjwstk.mteam.jcsync.core.implementation.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject;
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
public class JCSyncTreeMapTest {

    static P2PNode node;
    static JCSyncCore core;
    static P2PNode node2;
    static JCSyncCore core2;
    public static P2PPManager manager;
    public static JCSyncHashMap<String, String> h1;
    public static JCSyncHashMap<String, String> h2;
    public static String n1 = "map1";
    public static SharedCollectionObject s1;
    public static SharedCollectionObject s2;

    public JCSyncTreeMapTest() {
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
        manager.stop();
        node.networkLeave();
        node2.networkLeave();        
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor() throws Exception {
        JCSyncTreeMap<String,String> map1;
        JCSyncTreeMap<String,String> map2;
        String name = "map1_1";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncTreeMap<String, String>();        
        map1.put_("key1", "val1");
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncTreeMap<String, String>) so_2.getNucleusObject();
        assertEquals(map1,map2);
        //so_2 = new SharedCollectionObject(name, map2, core2);
    }
    @Test (expected=ObjectExistsException.class)
    public void testConstructor_existed() throws Exception {
        JCSyncTreeMap<String,String> map1;
        JCSyncTreeMap<String,String> map2;
        String name = "map1_1_1";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncTreeMap<String, String>();       
        map2 = new JCSyncTreeMap<String, String>();  
        so_1 = new SharedCollectionObject(name, map1, core);
        so_1 = new SharedCollectionObject(name, map2, core2);
        //so_2 = new SharedCollectionObject(name, map2, core2);
    }
    @Test (expected=ObjectNotExistsException.class)
    public void testConstructor_nonexistent() throws Exception {
        JCSyncTreeMap<String,String> map1;
        JCSyncTreeMap<String,String> map2;
        String name = "map1_1_2";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncTreeMap<String, String>();       
        map2 = new JCSyncTreeMap<String, String>();  
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
    }    
    @Test
    public void testConstructor2() throws Exception {
        JCSyncTreeMap<String,String> map1;
        JCSyncTreeMap<String,String> map2;
        String name = "map1_2";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        Comp c = new Comp();
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncTreeMap<String, String>(c);     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncTreeMap<String, String>) so_2.getNucleusObject();
        map1.putAll(sh);
        Thread.sleep(2000);
        assertEquals(map1,map2);
        assertTrue(map1.keySet().size()==4);
        assertTrue(map2.keySet().size()==4);
    }

    /**
     * Test of clear method, of class JCSyncTreeMap.
     */
    @Test
    public void testClear() throws Exception {
        JCSyncTreeMap<String,String> map1;
        JCSyncTreeMap<String,String> map2;
        String name = "TreeMap_testClear()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        Comp c = new Comp();
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncTreeMap<String, String>(c);     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncTreeMap<String, String>) so_2.getNucleusObject();
        assertTrue(map1.isEmpty()&& map2.isEmpty());
        map1.putAll(sh);
        Thread.sleep(500);
        assertEquals(map1,map2);
        assertTrue(map1.keySet().size()==4 &&map2.keySet().size()==4);
        assertFalse(map1.isEmpty()&& map2.isEmpty());
        map1.clear();
        Thread.sleep(500);
        assertTrue(map1.isEmpty()&& map2.isEmpty());
        map2.putAll(sh);
        assertTrue(map1.keySet().size()==4 &&map2.keySet().size()==4);
        assertFalse(map1.isEmpty()&& map2.isEmpty());
        map2.clear();
        Thread.sleep(500);
        assertTrue(map1.isEmpty()&& map2.isEmpty());
        
    }

    /**
     * Test of clone method, of class JCSyncTreeMap.
     */
    @Test
    public void testClone() throws Exception {
        JCSyncTreeMap<String,String> map1;
        JCSyncTreeMap<String,String> map2;
        String name = "TreeMap_testClone()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        Comp c = new Comp();
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncTreeMap<String, String>(c);     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncTreeMap<String, String>) so_2.getNucleusObject();
        assertTrue(map1.isEmpty()&& map2.isEmpty());
        map1.putAll(sh);
        Thread.sleep(2500);
        TreeMap t1 = (TreeMap) map1.clone();
        TreeMap t2 = (TreeMap) map2.clone();
        assertEquals(t1,t2);
        t1.put("zz", "zzz");
        assertEquals(map1, map2);
        assertTrue(map1.entrySet().size()==4);
        assertTrue(t1.entrySet().size()==5);
        assertNotSame(t1,t2);   
    }

    /**
     * Test of containsKey method, of class JCSyncTreeMap.
     */
    @Test
    public void testContainsKey() throws Exception {
        JCSyncTreeMap<String,String> map1;
        JCSyncTreeMap<String,String> map2;
        String name = "TreeMap_testContainsKey()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        Comp c = new Comp();
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncTreeMap<String, String>(c);     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncTreeMap<String, String>) so_2.getNucleusObject();
        assertTrue(map1.isEmpty()&& map2.isEmpty());
        map1.putAll(sh);
        Thread.sleep(1500);
        assertTrue(map1.containsKey("key1"));
        assertTrue(map2.containsKey("key1"));
        assertFalse(map1.containsKey("key5"));
        assertFalse(map2.containsKey("key5"));
        map2.put("key5", name);
        Thread.sleep(1500);
        assertTrue(map1.containsKey("key5"));
        assertTrue(map2.containsKey("key5"));
        
    }
    @Test (expected=IllegalArgumentException.class)
    public void testRemove_withNullArgument() throws Exception {
        JCSyncTreeMap<String,String> map1;
        JCSyncTreeMap<String,String> map2;
        String name = "TreeMap_testRemove_withNullArgument()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        Comp c = new Comp();
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncTreeMap<String, String>(c);     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncTreeMap<String, String>) so_2.getNucleusObject();
        assertTrue(map1.isEmpty()&& map2.isEmpty());
        map1.putAll(sh);
        Thread.sleep(500);
        map2.remove(null);
        
    }

    /**
     * Test of containsValue method, of class JCSyncTreeMap.
     */
    @Test
    public void testContainsValue() throws Exception {
        JCSyncTreeMap<String,String> map1;
        JCSyncTreeMap<String,String> map2;
        String name = "TreeMap_testContainsValue()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        Comp c = new Comp();
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncTreeMap<String, String>(c);     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncTreeMap<String, String>) so_2.getNucleusObject();
        assertTrue(map1.isEmpty()&& map2.isEmpty());
        map1.putAll(sh);
        Thread.sleep(1500);
        assertTrue(map1.containsValue("value1"));
        assertTrue(map2.containsValue("value1"));
        assertFalse(map1.containsValue("value5"));
        assertFalse(map2.containsValue("value5"));
        map2.put("key5", "value5");
        Thread.sleep(1500);
        assertTrue(map1.containsValue("value5"));
        assertTrue(map2.containsValue("value5"));
    }   

    /**
     * Test of remove method, of class JCSyncTreeMap.
     */
    @Test
    public void testRemove() throws Exception {
        JCSyncTreeMap<String,String> map1;
        JCSyncTreeMap<String,String> map2;
        String name = "TreeMap_testRemove()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        Comp c = new Comp();
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncTreeMap<String, String>(c);     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncTreeMap<String, String>) so_2.getNucleusObject();
        assertTrue(map1.isEmpty()&& map2.isEmpty());
        map1.putAll(sh);
        Thread.sleep(1500);
        assertTrue(map1.containsValue("value1"));
        assertTrue(map2.containsValue("value1"));
        String retVal = map2.remove("key5");
        assertTrue(retVal==null);
        Thread.sleep(1500);
        retVal = map1.remove("key1");
        Thread.sleep(1500);
        assertEquals("value1", retVal);
        assertTrue(map2.get("key1")==null);
    }
}
class Comp implements Serializable,Comparator<String>{
    
    public Comp(){
        
    }
    @Override
    public int compare(String o1, String o2) {
        return o1.compareTo(o2);
    }
    
}
