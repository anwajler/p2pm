/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.pjwstk.mteam.jcsync.samples.Exchanger;

import java.io.Serializable;
import java.util.concurrent.Exchanger;
import org.junit.*;
import static org.junit.Assert.*;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncAbstractSharedObject;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCore;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.JCSyncHashMap;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.SharedCollectionObject;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.p2pp.P2PPManager;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerBootstrapServer;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerConstants;
import pl.edu.pjwstk.p2pp.util.P2PPMessageFactory;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 *
 * @author buti
 */
public class SharedExchangerTest {

    public SharedExchangerTest() {
    }
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

    @Test
    public void testCreate() throws Exception {
        SharedExchanger<String> exch1;
        SharedExchanger<String> exch2;
        String name = "test_exch";
        SharedExchangerObject so_1;
        SharedExchangerObject so_2;
        exch1 = new SharedExchanger<String>();
        so_1 = new SharedExchangerObject(name, exch1, core);
        so_2 = (SharedExchangerObject) SharedCollectionObject.getFromOverlay(name, core2);
        exch2 = (SharedExchanger<String>) so_2.getNucleusObject();
        FillingLoop fl = new FillingLoop((SharedExchanger) so_1.getNucleusObject());
        EmptyingLoop el = new EmptyingLoop((SharedExchanger) so_2.getNucleusObject());
        new Thread(fl).start();
        new Thread(el).start();




    }

    class FillingLoop implements Runnable {

        private Exchanger<String> exchanger;

        public FillingLoop(Exchanger e) {
            this.exchanger = e;
        }

        public void run() {
            StringBuilder currentBuffer = new StringBuilder();
            try {
                while (currentBuffer != null) {
                    currentBuffer.append("test");
                    currentBuffer = new StringBuilder(exchanger.exchange(currentBuffer.toString()));
                    System.out.println("received data "+currentBuffer);
                    assertTrue("currentBuffer lenght must be ==0", currentBuffer.length() == 0);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    class EmptyingLoop implements Runnable {

        private Exchanger<String> exchanger;

        public EmptyingLoop(Exchanger e) {
            this.exchanger = e;
        }

        public void run() {
            StringBuilder currentBuffer = new StringBuilder();
            try {
                while (currentBuffer != null) {
                    currentBuffer = new StringBuilder(exchanger.exchange(""));
                    assertTrue("current buffer lenght must be > 0", currentBuffer.length() > 0);
                    Thread.sleep(3000);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
