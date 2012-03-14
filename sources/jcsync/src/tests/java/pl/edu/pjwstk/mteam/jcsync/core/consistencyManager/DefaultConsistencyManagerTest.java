package pl.edu.pjwstk.mteam.jcsync.core.consistencyManager;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCore;
import pl.edu.pjwstk.mteam.jcsync.core.JCsyncAlgorithInterface;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.JCSyncHashMap;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.SharedCollectionObject;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.util.JCSyncObservable;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.util.SharedObservableObject;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectNotExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.OperationForbiddenException;
import pl.edu.pjwstk.mteam.jcsync.operation.JCsyncAbstractOperation;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.p2p.P2PNode.RoutingAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.message.request.PublishRequest;
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
public class DefaultConsistencyManagerTest {

    public DefaultConsistencyManagerTest() {
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
    public static ArrayList<Worker> workers = new ArrayList();

    @BeforeClass
    public static void setUpClass() throws Exception {
        Thread server = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    manager = new P2PPManager(7080, 0, 0, 0, 0, "", "", new P2PPMessageFactory(), "myOverlayID".getBytes("UTF-8"));
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
        node.setUserName("node_first");
        node.setTcpPort(9090);
        node.networkJoin();
        while (!node.isConnected()) {
            Thread.sleep(100);
        }
        core = new JCSyncCore(node, 5050);
        core.init();


    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        node.networkLeave();
        for (int i = 0; i < workers.size(); i++) {
            workers.get(i).node.networkLeave();
        }
        manager.stop();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of responseReceived method, of class DefaultConsistencyManager.
     */
    @Ignore
    @Test
    public void testResponseReceived() {
        System.out.println("responseReceived");
        JCsyncAbstractOperation op = null;
        short respCode = 0;
        DefaultConsistencyManager instance = new DefaultConsistencyManager();
        instance.responseReceived(op, respCode);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of requestReceived method, of class DefaultConsistencyManager.
     */
    @Ignore
    @Test
    public void testRequestReceived() {
        System.out.println("requestReceived");
        PublishRequest req = null;
        JCsyncAbstractOperation op = null;
        DefaultConsistencyManager instance = new DefaultConsistencyManager();
        instance.requestReceived(req, op);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of indicationReceived method, of class DefaultConsistencyManager.
     */
    @Ignore
    @Test
    public void testIndicationReceived() {
        System.out.println("indicationReceived");
        JCsyncAbstractOperation op = null;
        DefaultConsistencyManager instance = new DefaultConsistencyManager();
        instance.indicationReceived(op);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of beforeRequestSend method, of class DefaultConsistencyManager.
     */
    @Ignore
    @Test
    public void testBeforeRequestSend() throws Exception {
        System.out.println("beforeRequestSend");
        JCsyncAbstractOperation op = null;
        boolean blocking = false;
        DefaultConsistencyManager instance = new DefaultConsistencyManager();
        instance.beforeRequestSend(op, blocking);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of afterRequestSend method, of class DefaultConsistencyManager.
     */
    @Ignore
    @Test
    public void testAfterRequestSend() throws Exception {
        System.out.println("afterRequestSend");
        JCsyncAbstractOperation op = null;
        boolean blocking = false;
        DefaultConsistencyManager instance = new DefaultConsistencyManager();
        Object expResult = null;
        Object result = instance.afterRequestSend(op, blocking);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of registerObject method, of class DefaultConsistencyManager.
     */
    @Ignore
    @Test
    public void testRegisterObject() {
        System.out.println("registerObject");
        String id = "";
        DefaultConsistencyManager instance = new DefaultConsistencyManager();
        instance.registerObject(id);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCoreAlgorith method, of class DefaultConsistencyManager.
     */
    @Ignore
    @Test
    public void testSetCoreAlgorith() {
        System.out.println("setCoreAlgorith");
        JCsyncAlgorithInterface core = null;
        DefaultConsistencyManager instance = new DefaultConsistencyManager();
        instance.setCoreAlgorith(core);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Ignore
    @Test
    public void testMultiOperations() throws ObjectExistsException, Exception {
        JCSyncHashMap<String, String> map1;
        JCSyncHashMap<String, String> map2;
        String name = "map_multiOperations";
        HashMap sh = new HashMap();
        sh.put("key1", "value1");
        sh.put("key2", "value2");
        sh.put("key3", "value3");
        sh.put("key4", "value4");
        SharedCollectionObject so_1;
        SharedCollectionObject so_2;
        map1 = new JCSyncHashMap<String, String>(sh);
        so_1 = new SharedCollectionObject(name, map1, core);

        int nodesCount = 10;
        int operationCount = 100;
        CountDownLatch latch = new CountDownLatch(nodesCount);
        CountDownLatch endLatch = new CountDownLatch(nodesCount);
        for (int i = 0; i < nodesCount; i++) {
            Worker w = new Worker("node_" + i, name, latch, endLatch, "127.0.0.1", 7080, "127.0.0.1", 7080, 6080 + i, 5060 + i, operationCount);
            try {
                w.initAndJoin();
            } catch (InterruptedException ex) {
                Logger.getLogger(DefaultConsistencyManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(DefaultConsistencyManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            Thread t = new Thread(w);
            t.start();
            workers.add(w);
            latch.countDown();
        }
        endLatch.await();
        long sleepTime = 2000 * nodesCount;
        //if(sleepTime<10000) sleepTime = 10000;
        Thread.sleep(sleepTime);
        assertTrue(map1.keySet().size() == (4 + nodesCount * operationCount));
        System.out.println("map for node: " + core.getNodeInfo().getName());
        System.out.println(map1);
        for (int i = 0; i < nodesCount; i++) {
            System.out.println("map for node: " + workers.get(i).core.getNodeInfo().getName());
            System.out.println(workers.get(i).hashMap);
        }
        for (int i = 0; i < nodesCount; i++) {
            assertTrue(workers.get(i).hashMap.keySet().size() == (4 + nodesCount * operationCount));
        }
    }

    @Test
    public void testStressTest() throws Exception {

        ArrayList<Worker2> workers = new ArrayList();
        String name = "test_observer";
        int nodesCount = 5;
        int operationCount = 100;
        JCSyncObservable obs = new JCSyncObservable();
        SharedObservableObject soo = new SharedObservableObject(name, obs, core);
        CountDownLatch latch = new CountDownLatch(nodesCount);
        CountDownLatch endLatch = new CountDownLatch(nodesCount);
        for (int i = 0; i < nodesCount; i++) {
            Worker2 w = new Worker2("node_" + i, name, latch, endLatch, "127.0.0.1", 7080, "127.0.0.1", 7080, 6080 + i, 5060 + i, operationCount);
            try {
                w.initAndJoin();
            } catch (InterruptedException ex) {
                Logger.getLogger(DefaultConsistencyManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(DefaultConsistencyManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            Thread t = new Thread(w);
            t.start();
            workers.add(w);
            latch.countDown();
        }
        endLatch.await();
        long sleepTime = 2000 * nodesCount;
        //if(sleepTime<10000) sleepTime = 10000;
        Thread.sleep(sleepTime);

        for (int i = 0; i < nodesCount; i++) {
            ConcurrentHashMap<String, ArrayList<OperationDescriptor>> ops = workers.get(i).observer.getOperations();
            Set<String> set = ops.keySet();
            String[] s = new String[set.size()] ;
            s = set.toArray(s);
            assertTrue("operation publisher count must be the equal to nodesCount value", s.length == nodesCount);
            for (int j = 0; j < s.length; j++) {
                assertTrue("operations count for this publisher must be equal to operationsCount value", (ops.get(s[j]).size()) == operationCount);

            }
        }
//        for (int i = 0; i < nodesCount; i++) {
//            assertTrue(workers.get(i).hashMap.keySet().size() == (4 + nodesCount * operationCount));
//        }
    }
}

class Worker implements Runnable {

    String nodeName = "";
    String serverReflexiveAddress = "";
    int serverReflexivePort = -1;
    String bootIP = "";
    int bootPort = -1;
    int udpPort = -1;
    int pubsubPort = -1;
    P2PNode node;
    JCSyncCore core;
    CountDownLatch startLatch;
    JCSyncHashMap<String, String> hashMap = new JCSyncHashMap<String, String>();
    SharedCollectionObject so;
    String collectionName;
    int operationCount;
    CountDownLatch endLatch;

    public Worker(String nodeName, String collectionName, CountDownLatch latch, CountDownLatch endLatch,
            String sra, int srp, String bootIP, int bootPort, int udpPort, int pubsubPort, int operationCount) {
        this.nodeName = nodeName;
        this.serverReflexiveAddress = sra;
        this.serverReflexivePort = srp;
        this.bootIP = bootIP;
        this.bootPort = bootPort;
        this.udpPort = udpPort;
        this.pubsubPort = pubsubPort;
        this.startLatch = latch;
        this.collectionName = collectionName;
        this.operationCount = operationCount;
        this.endLatch = endLatch;
    }

    @Override
    public void run() {
        try {
            this.so = (SharedCollectionObject) SharedCollectionObject.getFromOverlay(this.collectionName, this.core);
        } catch (ObjectNotExistsException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OperationForbiddenException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            this.startLatch.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date;
        this.hashMap = (JCSyncHashMap<String, String>) this.so.getNucleusObject();
        for (int i = 0; i < this.operationCount; i++) {
            date = new Date();
            this.hashMap.put(this.nodeName + "_key_" + i, "" + i + ":" + dateFormat.format(date));
            date = null;
        }
        this.endLatch.countDown();
    }

    protected void initAndJoin() throws InterruptedException, Exception {
        this.node = new P2PNode(null, RoutingAlgorithm.SUPERPEER);
        this.node.setServerReflexiveAddress(this.serverReflexiveAddress);
        this.node.setServerReflexivePort(this.serverReflexivePort);
        this.node.setBootIP(this.bootIP);
        this.node.setBootPort(this.bootPort);
        this.node.setUserName(this.nodeName);
        this.node.setTcpPort(this.udpPort);
        this.node.networkJoin();
        while (!this.node.isConnected()) {
            Thread.sleep(100);
        }
        this.core = new JCSyncCore(this.node, this.pubsubPort);
        this.core.init();
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getBootIP() {
        return bootIP;
    }

    public int getBootPort() {
        return bootPort;
    }

    public int getPubsubPort() {
        return pubsubPort;
    }

    public String getServerReflexiveAddress() {
        return serverReflexiveAddress;
    }

    public int getServerReflexivePort() {
        return serverReflexivePort;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public JCSyncCore getCore() {
        return core;
    }

    public JCSyncHashMap<String, String> getHashMap() {
        return hashMap;
    }

    public CountDownLatch getLatch() {
        return startLatch;
    }

    public P2PNode getNode() {
        return node;
    }

    public SharedCollectionObject getSo() {
        return so;
    }
}

class Worker2 extends Worker {

    public SharedObservableObject so_;
    public JCSyncObservable observable = new JCSyncObservable();
    public Observer_ observer;

    public Observer_ getObserver() {
        return observer;
    }

    public Worker2(String nodeName, String collectionName, CountDownLatch latch, CountDownLatch endLatch,
            String sra, int srp, String bootIP, int bootPort, int udpPort, int pubsubPort, int operationCount) {
        super(nodeName, collectionName, latch, endLatch, sra, srp, bootIP,
                bootPort, udpPort, pubsubPort, operationCount);
    }

    @Override
    public void run() {
//        try {
//            this.so_ = (SharedObservableObject) SharedObservableObject.getFromOverlay(this.collectionName, this.core);
//        } catch (ObjectNotExistsException ex) {
//            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (OperationForbiddenException ex) {
//            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
        try {
            this.startLatch.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }

        //this.observable = (JCSyncObservable) this.so_.getNucleusObject();
        this.observer = new Observer_();
        this.observable.addObserver(this.observer);

        for (int i = 0; i < this.operationCount; i++) {
            this.observable.notifyObservers(new OperationDescriptor(this.nodeName, i));
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Worker2.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.endLatch.countDown();
    }

    @Override
    protected void initAndJoin() throws InterruptedException, Exception {
        super.initAndJoin();

        try {

            this.so_ = new SharedObservableObject(collectionName, this.observable, this.core);
        } catch (ObjectExistsException e) {
            this.so_ =
                    (SharedObservableObject) SharedObservableObject.getFromOverlay(collectionName, this.core);
            this.observable = (JCSyncObservable) this.so_.getNucleusObject();
        }

    }
}

class OperationDescriptor implements Serializable {

    private String nodeName;
    private int operationID;

    public OperationDescriptor(String nn, int oID) {
        this.nodeName = nn;
        this.operationID = oID;
    }

    public String getNodeName() {
        return nodeName;
    }

    public int getOperationID() {
        return operationID;
    }
}

class Observer_ implements Observer {

    private final ConcurrentHashMap<String, ArrayList<OperationDescriptor>> operations = new ConcurrentHashMap<String, ArrayList<OperationDescriptor>>();

    @Override
    public void update(Observable o, Object arg) {

        synchronized (this) {
            OperationDescriptor ods = (OperationDescriptor) arg;
            if (this.operations.containsKey(ods.getNodeName())) {
                this.operations.get(ods.getNodeName()).add(ods);
            } else {
                ArrayList<OperationDescriptor> arl = new ArrayList<OperationDescriptor>();
                arl.add(ods);
                this.operations.put(ods.getNodeName(), arl);
            }
        }
    }

    public ConcurrentHashMap<String, ArrayList<OperationDescriptor>> getOperations() {
        return operations;
    }
}
