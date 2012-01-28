package pl.edu.pjwstk.mteam.jcsync.core.implementation.collections;

import java.util.HashMap;
import java.util.Set;
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
public class JCSyncHashMapTest {
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
    
    public JCSyncHashMapTest() {
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
     * Test of clear method, of class JCSyncHashMap.
     */
    @Test (timeout=5000)
    public void testConstructor1() throws ObjectExistsException, Exception{
        h1 = new JCSyncHashMap<String, String>();  
        s1 = new SharedCollectionObject(n1, h1, core);
        s2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(n1, core2);
        h2 = (JCSyncHashMap<String, String>) s2.getNucleusObject();
        assertEquals(h1, h2);
    }
    @Test (timeout=5000,expected=ObjectExistsException.class)
    public void testConstructor2() throws ObjectExistsException, Exception{
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map1_1";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncHashMap<String, String>(sh);          
        map2 = new JCSyncHashMap<String, String>();
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = new SharedCollectionObject(name, map2, core2);
    }
    @Test //(timeout=5000)
    public void testConstructor3() throws ObjectExistsException, Exception{
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map1_2";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncHashMap<String, String>(sh);   
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        assertEquals(map1, map2);
        assertTrue(map1.entrySet().size()==4);
    }
    @Test //(timeout=5000)
    public void testConstructor3_1() throws ObjectExistsException, Exception{
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map1_3_1";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncHashMap<String, String>(sh);   
        so_2 = new SharedCollectionObject(name, map1, core2);
        so_1 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        assertEquals(map1, map2);
        assertTrue(map1.entrySet().size()==4);
    }
    @Test (timeout=5000,expected=ObjectNotExistsException.class)
    public void testGetFromOverlay() throws Exception{
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map1_3";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
    }
    
    @Test (timeout=5000)
    public void testClear() throws Exception {
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map_testClear()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncHashMap<String, String>(sh);   
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        assertEquals(map1, map2);
        assertTrue(map1.entrySet().size()==4);
        map2.clear();
        assertTrue(map1.entrySet().size()==0);
        assertTrue(map2.entrySet().size()==0);
        map1.clear();
    }

    /**
     * Test of clone method, of class JCSyncHashMap.
     */
    
    @Test (timeout=5000)
    public void testClone() throws Exception {
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map_testClone()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncHashMap<String, String>(sh);      
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        assertEquals(map1, map2);
        assertTrue(map1.entrySet().size()==4);
        HashMap cmap1 = (HashMap) map1.clone();
        HashMap cmap2 = (HashMap) map2.clone();
        assertEquals(cmap1, cmap2);
        cmap1.put("zz", "zzz");
        assertEquals(map1, map2);
        assertTrue(map1.entrySet().size()==4);
        assertTrue(cmap1.entrySet().size()==5);
        assertNotSame(cmap1, cmap2);        
    }

    /**
     * Test of containsKey method, of class JCSyncHashMap.
     */
    @Test (timeout=10000)
    public void testContainsKey() throws Exception {
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map_testContainsKey()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncHashMap<String, String>(sh);     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        assertEquals(map1, map2);
        assertTrue(map1.entrySet().size()==4);
        assertTrue(map1.containsKey("key1"));
        assertTrue(map2.containsKey("key1"));
        assertFalse(map1.containsKey("key5"));
        assertFalse(map2.containsKey("key5"));
        map1.put("key5","val5"); 
        //wait while for notify propagation
        Thread.sleep(2500);
        assertTrue(map1.containsKey("key5"));
        assertTrue(map2.containsKey("key5"));        
    }

    /**
     * Test of containsValue method, of class JCSyncHashMap.
     */
    @Test (timeout=5000)
    public void testContainsValue() throws Exception {
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map_testContainsValue()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncHashMap<String, String>(sh);     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        assertEquals(map1, map2);
        assertTrue(map1.entrySet().size()==4);
        assertTrue(map1.containsValue("value1"));
        assertTrue(map2.containsValue("value1"));
        assertFalse(map2.containsValue("value5"));
        assertFalse(map1.containsValue("value5"));
        map1.put("key5","val5"); 
        //wait while for notify propagation
        Thread.sleep(500);
        String oldValue = map2.put("key5", "value5");
        Thread.sleep(500);
        assertEquals("val5", oldValue);
        assertTrue(map1.containsValue("value5"));
        assertTrue(map2.containsValue("value5"));    
    }

    /**
     * Test of entrySet method, of class JCSyncHashMap.
     */
    
    @Test (timeout=5000)
    public void testEntrySet() throws Exception {
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map_testEntrySet()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncHashMap<String, String>(sh);     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        Set res1 = map1.entrySet();
        Set res2 = map2.entrySet();
        assertEquals(res1,res2);
    }

    /**
     * Test of get method, of class JCSyncHashMap.
     */
    @Test (timeout=5000)
    public void testGet() throws Exception{
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map_testGet()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncHashMap<String, String>(sh);     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        String res1 = map1.get("key2");
        String res2 = map2.get("key2");
        assertEquals("value2", res1);
        assertEquals(res2, res1);
        map2.put("key2", "new_value");
        Thread.sleep(500);
        res1 = map1.get("key2");
        res2 = map2.get("key2");
        assertEquals("new_value", res1);
        assertEquals(res2, res1);
    }

    /**
     * Test of isEmpty method, of class JCSyncHashMap.
     */
    @Test (timeout=5000) 
    public void testIsEmpty() throws Exception{
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map_testISEmpty()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncHashMap<String, String>();     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        assertTrue(map1.isEmpty());
        assertTrue(map2.isEmpty());
        map2.putAll(sh);
        Thread.sleep(500);
        assertFalse(map1.isEmpty());
        assertFalse(map2.isEmpty());
        for (int i = 1; i < 5; i++) {
            map1.remove("key"+i);            
        }
        Thread.sleep(1500);
        assertTrue(map1.isEmpty());
        assertTrue(map2.isEmpty());
    }

    /**
     * Test of keySet method, of class JCSyncHashMap.
     */
    @Test (timeout=5000)
    public void testKeySet() throws Exception {
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map_testKeySet()";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncHashMap<String, String>();     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        Set keySet1 = map1.keySet();
        Set keySet2 = map2.keySet();
        assertTrue(keySet1.isEmpty());        
        assertTrue(keySet2.isEmpty());
        map2.putAll(sh);
        Thread.sleep(500);
        keySet1 = map1.keySet();
        keySet2 = map2.keySet();
        assertTrue(keySet1.size()==4);
        assertTrue(keySet2.size()==4);
    }

    /**
     * Test of put method, of class JCSyncHashMap.
     */
    @Test (timeout=20000)
    public void testPut() throws Exception{
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map_testPut()";
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncHashMap<String, String>();     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        for (int i = 0; i < 100; i++) {
            map1.put("key"+i, "value"+i);            
        }
        Thread.sleep(4500);
        assertTrue(map2.size()==100);
        for (int i = 0; i < 100; i++) {
            assertEquals("value"+i, map2.get("key"+i));            
        }
    }

    /**
     * Test of putAll method, of class JCSyncHashMap.
     */
    @Test (timeout=15000)
    public void testPutAll() throws Exception{
        
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map_testPutAll()";
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        HashMap hs = new HashMap(10);
        map1 = new JCSyncHashMap<String, String>();     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        for (int i = 1; i < 11; i++) {
            hs.put("key"+i, "value"+i);            
        }
        map1.putAll(hs);
        HashMap hs2 = new HashMap(10);
        for (int i = 1; i < 11; i++) {
            if(i%2==0){
                hs2.put("key_"+i,"value__"+i);                
            }
            else {
                hs2.put("key"+i,"value_"+i);
            }            
        }
        map2.putAll(hs2);
        Thread.sleep(500);
        assertTrue(map1.size()==15);
        assertTrue(map2.size()==15);
        
        int z1,z2;
        for (int i = 1; i < 11; i++) {            
            if(i%2==0){
                z1 = map1.get("key_"+i).compareTo(map2.get("key_"+i));
                z2 = map1.get("key_"+i).compareTo("value__"+i);
                assertTrue(z1==0 && z2==0);
                z1 = map1.get("key"+i).compareTo(map2.get("key"+i));
                z2 = map1.get("key"+i).compareTo("value"+i);
                assertTrue(z1==0 && z2==0);
            }else{
                z1 = map1.get("key"+i).compareTo(map2.get("key"+i));
                z2 = map1.get("key"+i).compareTo("value_"+i);
                assertTrue(z1==0 && z2==0);
            }
            
        }
        
    }

    /**
     * Test of remove method, of class JCSyncHashMap.
     */
    @Test (timeout=5000)
    public void testRemove() throws Exception{
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map_testRemove()";
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        HashMap hs = new HashMap(10);
        map1 = new JCSyncHashMap<String, String>();     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        for (int i = 1; i < 11; i++) {
            hs.put("key"+i, "value"+i);            
        }
        map1.putAll(hs);
        Thread.sleep(500);        
        String res = map2.remove("key1");
        assertEquals("value1", res);
        Thread.sleep(500);
        String res2 = map1.remove("key1");        
        assertNull(res2);
        
    }

    /**
     * Test of size method, of class JCSyncHashMap.
     */
    @Test (timeout=5000)
    public void testSize() throws Exception{
        JCSyncHashMap<String,String> map1;
        JCSyncHashMap<String,String> map2;
        String name = "map_testSize()";
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        HashMap hs = new HashMap(10);
        map1 = new JCSyncHashMap<String, String>();     
        so_1 = new SharedCollectionObject(name, map1, core);
        so_2 = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(name, core2);
        map2 = (JCSyncHashMap<String, String>) so_2.getNucleusObject();
        assertTrue(map2.size()==0 && map1.size()==0);        
        map2.put("", null);
        Thread.sleep(500);
        String res = map1.get("");
        assertTrue(map2.size()==1 && map1.size()==1);
        assertNull(res);
    }

}
