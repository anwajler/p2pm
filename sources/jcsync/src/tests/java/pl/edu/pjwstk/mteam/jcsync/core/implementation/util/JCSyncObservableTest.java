
package pl.edu.pjwstk.mteam.jcsync.core.implementation.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
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
public class JCSyncObservableTest {
    static P2PNode node;
    static JCSyncCore core;
    static P2PNode node2;
    static JCSyncCore core2;
    public static P2PPManager manager;
    public JCSyncObservableTest() {
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
    @Test //(timeout=5000)
    public void testConstructor1() throws ObjectExistsException, Exception{
        JCSyncObservable  o1;
        JCSyncObservable  o2;
        String name = "observable1";
        
        SharedObservableObject so_1;
        SharedObservableObject so_2;
        o1 = new JCSyncObservable();
        o2 = new JCSyncObservable();
        so_1 = new SharedObservableObject(name, o1, core);
        so_2 =(SharedObservableObject) SharedObservableObject.getFromOverlay(name, core2);
    }
    @Test (timeout=5000, expected=ObjectExistsException.class)
    public void testConstructor2() throws ObjectExistsException, Exception{
        JCSyncObservable  o1;
        JCSyncObservable  o2;
        String name = "observable2";
        
        SharedObservableObject so_1;
        SharedObservableObject so_2;
        o1 = new JCSyncObservable();
        o2 = new JCSyncObservable();
        so_1 = new SharedObservableObject(name, o1, core);
        so_2 = new SharedObservableObject(name, o2, core);
    }
    @Test //(timeout=5000, expected=ObjectExistsException.class)
    public void testConstructor3() throws ObjectExistsException, Exception{
        JCSyncObservable  o1;
        JCSyncObservable  o2;
        String name = "observable3";
        
        SharedObservableObject so_1;
        SharedObservableObject so_2;
        o1 = new JCSyncObservable();
        o1.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        so_1 = new SharedObservableObject(name, o1, core);
        so_2 = (SharedObservableObject)SharedObservableObject.getFromOverlay(name, core2);
        o2 = (JCSyncObservable) so_2.getNucleusObject();
        Thread.sleep(500);
        assertTrue(o2!=null);
    }
    @Test (timeout=5000, expected=ObjectNotExistsException.class)
    public void testConstructor4() throws ObjectNotExistsException, Exception{
        JCSyncObservable  o1;
        JCSyncObservable  o2;
        String name = "observable4";
        
        SharedObservableObject so_1;
        SharedObservableObject so_2;
        o1 = new JCSyncObservable();
        o1.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //so_1 = new SharedObservableObject(name, o1, core);
        so_2 = (SharedObservableObject)SharedObservableObject.getFromOverlay(name, core2);
        o2 = (JCSyncObservable) so_2.getNucleusObject();
    }
    /**
     * Test of addObserver method, of class JCSyncObservable.
     */
    @Test
    public void testAddObserver() throws Exception {
        JCSyncObservable  o1;
        JCSyncObservable  o2;
        String name = "obs_addObserver";
        
        SharedObservableObject so_1;
        SharedObservableObject so_2;
        o1 = new JCSyncObservable();
        o1.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        so_1 = new SharedObservableObject(name, o1, core);
        so_2 = (SharedObservableObject)SharedObservableObject.getFromOverlay(name, core2);
        o2 = (JCSyncObservable) so_2.getNucleusObject();
        assertTrue(o1.countObservers()==1);
        assertTrue(o2.countObservers()==0);
    }


    /**
     * Test of notifyObservers method, of class JCSyncObservable.
     */
    @Test (expected=IllegalArgumentException.class)
    public void testNotifyObservers_0args() {
        JCSyncObservable o1 = new JCSyncObservable();
        o1.notifyObservers();
    }

    /**
     * Test of notifyObservers method, of class JCSyncObservable.
     */
    @Test
    public void testNotifyObservers_Object() throws Exception {
        JCSyncObservable  o1;
        JCSyncObservable  o2;
        String name = "obs_testNotify";        
        SharedObservableObject so_1;
        SharedObservableObject so_2;
        boolean r1 = false;
        boolean r2 = false;
        o1 = new JCSyncObservable();
        
        class Observer_ implements Observer{
            boolean retVal;
            Observer_(boolean rt){
                this.retVal = rt;
            }
            @Override
            public void update(Observable o, Object arg) {
                if(o instanceof JCSyncObservable && arg == null){
                    retVal = true;
                }
            }
            
        };
        Observer_ obs1 = new Observer_(r1);
        o1.addObserver(obs1);        
        so_1 = new SharedObservableObject(name, o1, core);
        so_2 = (SharedObservableObject)SharedObservableObject.getFromOverlay(name, core2);
        o2 = (JCSyncObservable) so_2.getNucleusObject();
        assertTrue(o1.countObservers()==1);
        assertTrue(o2.countObservers()==0);
        Observer_ obs2 = new Observer_(r2);
        o2.addObserver(obs2);
        o1.notifyObservers(null);
        Thread.sleep(500);
        assertTrue(obs1.retVal && obs2.retVal);
        
    }
    @Test
    public void testNotifyObservers_Object_2() throws Exception {
        JCSyncObservable  o1;
        JCSyncObservable  o2;
        String name = "obs_testNotify2";        
        SharedObservableObject so_1;
        SharedObservableObject so_2;
        boolean r1 = false;
        boolean r2 = false;
        final HashMap<String,Boolean> h1 = new HashMap<String,Boolean>();
        h1.put("z1",false);
        h1.put("z2",false);
        h1.put("z3",false);
        h1.put("z4",false);
        h1.put("z5",false);
        h1.put("z6",false);
        h1.put("z7",false);
        h1.put("z8",false);
        h1.put("z9",false);
        o1 = new JCSyncObservable();
        
        class Observer_ implements Observer{
            boolean retVal;
            Observer_(boolean rt){
                this.retVal = rt;
            }
            @Override
            public void update(Observable o, Object arg) {
                if(o instanceof JCSyncObservable){
                    String key = (String) arg;
                    if(h1.containsKey(key)){
                        h1.put(key, true);
                    }
                }
            }
            
        };
              
        so_1 = new SharedObservableObject(name, o1, core2);
        so_2 = (SharedObservableObject)SharedObservableObject.getFromOverlay(name, core);
        o2 = (JCSyncObservable) so_2.getNucleusObject();
        Thread.sleep(500);
        Observer_ obs2 = new Observer_(r2);
        o2.addObserver(obs2);
        for (int i = 1; i < 10; i++) {
            o1.notifyObservers("z"+i);
        }
        Thread.sleep(500);
        assertTrue(h1.size()==9);
        Set<String> s = h1.keySet();
        Iterator its = s.iterator();
        while(its.hasNext()){
            assertTrue(h1.get((String)its.next()));
        }
    }
}
