package pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCore;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.JCSyncHashMap;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.SharedCollectionObject;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.util.JCSyncObservable;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.util.SharedObservableObject;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.p2pm.tests.core.events.EventManager;
import pl.edu.pjwstk.mteam.p2pm.tests.core.events.IEventSubscriber;
import pl.edu.pjwstk.mteam.p2pm.tests.core.rules.FieldRule;
import pl.edu.pjwstk.mteam.p2pm.tests.core.tests.ITest;
import pl.edu.pjwstk.mteam.p2pm.tests.tests.psnode.PSNode;
import static pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic.JCSyncTestRules.*;
import pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic.node.NodeCallback;
import pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic.node.P2ppNode;
import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;

public class JCsyncBasicTest extends Thread implements ITest, IEventSubscriber {

    public static final Logger LOG = Logger.getLogger(JCsyncBasicTest.class);
    private static final JCSyncTestRules rules = new JCSyncTestRules();
    private static final String[] acceptedEvents = {PSNode.EVENT_ONJOIN, PSNode.EVENT_ONTOPICCREATE, PSNode.EVENT_ONTOPICSUBSCRIBE,
        PSNode.EVENT_ONPUBSUBERROR, PSNode.EVENT_ONTOPICNOTIFY, PSNode.EVENT_ONDELIVER};
    public static String EVENT_JCSYNC_ON_REMOTE_UPDATE = "JCSYNC_ON_REMOTE_UPDATE";
    public static String EVENT_JCSYNC_ON_LOCAL_UPDATE = "JCSYNC_ON_LOCAL_UPDATE";
    public static String EVENT_LAST_NODE_CONNECTED = "LAST_NODE_CONNECTED";
    public static String EVENT_WORKER_FINISHED = "WORKER_FINISHED";
    private Map<String, Object> kwargs;
    private P2ppNode p2pNode;
    private TestState testState = TestState.UNCONNECTED;
    private CoreAlgorithm pubsubmgr;
    private JCSyncCore jcsyncCore;
    public long currTimeMillis;
    public long nextTimeMillis;
    private JCSyncHashMap<String, OperationDetails> collection;
    private JCSyncObservable observable;
    private SharedCollectionObject collection_so;
    private SharedObservableObject observable_so;
    private CountDownLatch workersLatch;
    private CountDownLatch waitForLastNode;
    private CountDownLatch finish;
    private JCSyncBasicTestCollectionListener collectionListener;
    private static String[] mapValues = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};
    private static String collectionName = "collection";

    public JCsyncBasicTest(Map<String, Object> kwargs) {
        setName("JCsyncBasicTest");
        this.kwargs = kwargs;
        for (String acceptedEvent : acceptedEvents) {
            EventManager.getInstance().subscribe(acceptedEvent, this);
        }
        EventManager.getInstance().subscribe(EVENT_JCSYNC_ON_REMOTE_UPDATE, this);
        EventManager.getInstance().subscribe(EVENT_JCSYNC_ON_LOCAL_UPDATE, this);
        EventManager.getInstance().subscribe(EVENT_LAST_NODE_CONNECTED, this);
        EventManager.getInstance().subscribe(EVENT_WORKER_FINISHED, this);
    }

    static String getCollectionName() {
        return collectionName;
    }

    private boolean verifyKwargs() {

        Map<String, FieldRule> fieldRules = rules.getFieldsRules();
        for (FieldRule fr : fieldRules.values()) {
            String fieldName = fr.getFieldName();
            Object kwargValue = kwargs.get(fieldName);
            if (kwargValue == null) {
                StringBuilder strb = new StringBuilder(getName() + " must be given a \"");
                strb.append(fieldName).append("\" kwarg.");
                if (LOG.isDebugEnabled() || LOG.isTraceEnabled()) {
                    strb.append(" Required kwargs: ").append(fieldRules.values());
                }
                LOG.error(strb.toString());
                return false;
            }
            if (!rules.verifyField(fieldName, kwargValue)) {
                return false;
            }

        }

        return true;
    }

    public boolean test() {

        if (!verifyKwargs()) {
            LOG.error("JCSyncBasicTest cannot run with given arguments: " + this.kwargs);
            return false;
        }
        setCurrTimeMillis();

        this.p2pNode = initLayer(this.kwargs);
        if (this.p2pNode == null) {
            return false;
        }
        //join to the overlay
        this.p2pNode.networkJoin();
//        while (!this.p2pNode.isConnected()) {
//            snooze(100);
//        }
        while (this.testState != TestState.JOINED) {
            snooze(10);
        }
        try {
            logTestArguments(this.kwargs);
        } catch (SQLException ex) {
            LOG.error("An error occurred: ", ex);
        }
        setNextTimeMillis();
        logOperationTime("join to network layer");

        try {
            initTestEnviroinment(this.kwargs, this.p2pNode);
        } catch (Exception e) {
            LOG.error("An error occurred: ", e);
        }
        this.waitForLastNode = new CountDownLatch(1);
        Boolean lastNode = (Boolean) this.kwargs.get(FIELD_LAST_NODE);


        Integer workers_count = (Integer) this.kwargs.get(FIELD_WORKERS_COUNT);
        Boolean worker = (Boolean) this.kwargs.get(FIELD_WORKER);
        Integer operation_count = (Integer) this.kwargs.get(FIELD_WORKER_OPERATION_COUNT);
        Long deelay_between_operation = (Long) this.kwargs.get(FIELD_OPERATION_DELAY);
        this.finish = new CountDownLatch(workers_count * operation_count);
        this.workersLatch = new CountDownLatch(workers_count);
        if (lastNode) {
            this.observable.notifyObservers("last_node_connected");
        }
        try {
            this.waitForLastNode.await();
        } catch (InterruptedException ex) {
            LOG.error("An error occurred: ", ex);
        }

        if (worker) {
            OperationDetails op = null;
            long currTimeMillis = 0;
            String operationID;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < operation_count; i++) {
                sb = null;
                sb = new StringBuilder();
                sb.append(this.p2pNode.getUserName());
                sb.append("@");
                sb.append(i);
                op = null;
                currTimeMillis = System.currentTimeMillis();
                op = new OperationDetails(this.p2pNode.getUserName(), sb.toString(), currTimeMillis);
                this.collection.put(sb.toString(), op);
                snooze(deelay_between_operation);
            }
            this.observable.notifyObservers("worker_finished:" + this.p2pNode.getUserName());
        }
        try {
            this.workersLatch.await();
        } catch (InterruptedException ex) {
            LOG.error("An error occurred: ", ex);
        }

        try {
            this.finish.await();
        } catch (InterruptedException ex) {
            LOG.error("An error occurred: ", ex);
        }

        printCollectionData(this.collection);
        try {
            logDataToDatabase(this.collection);
        } catch (SQLException ex) {
            LOG.error("An error occurred: ", ex);
        }

        snooze(10000);
        this.p2pNode.networkLeave();
        return true;
    }

    public void initTestEnviroinment(Map<String, Object> args, P2PNode node) throws Exception {
        this.jcsyncCore = new JCSyncCore(node, ((Integer) args.get(FIELD_PORT) + 2));
        this.jcsyncCore.init();

        this.observable = new JCSyncObservable();
        //String collectionName = (String) args.get(FIELD_COLLECTION_NAME);
        try {

            this.observable_so = new SharedObservableObject(collectionName + "_obs", this.observable, this.jcsyncCore);
        } catch (ObjectExistsException e) {
            this.observable_so =
                    (SharedObservableObject) SharedObservableObject.getFromOverlay(collectionName + "_obs", this.jcsyncCore);
            this.observable = (JCSyncObservable) this.observable_so.getNucleusObject();
        }
        this.collection = new JCSyncHashMap<String, OperationDetails>();
        try {
            this.collection_so =
                    new SharedCollectionObject(collectionName, this.collection, this.jcsyncCore, ConsistencyManager.class);
        } catch (ObjectExistsException e) {
            this.collection_so =
                    (SharedCollectionObject) SharedCollectionObject.getFromOverlay(collectionName, this.jcsyncCore);
            this.collection = (JCSyncHashMap<String, OperationDetails>) this.collection_so.getNucleusObject();
        }
        this.collectionListener = new JCSyncBasicTestCollectionListener(this.observable);
        this.observable.addObserver(this.collectionListener);
    }

    public P2ppNode initLayer(Map<String, Object> args) {
        Integer nodeNumber = (Integer) this.kwargs.get(FIELD_NODE_NUMBER);
        Integer port = (Integer) this.kwargs.get(FIELD_PORT);
        String bootIP = (String) this.kwargs.get(FIELD_BOOT_IP);
        Integer bootPort = (Integer) this.kwargs.get(FIELD_BOOT_PORT);
        String transportProtocol = (String) this.kwargs.get(FIELD_TRANSPORT_PROTOCOL);
        // Creating PSNode
        P2ppNode node = new P2ppNode(new NodeCallback(), P2PNode.RoutingAlgorithm.SUPERPEER);
        node.setBootIP(bootIP);
        node.setBootPort(bootPort);
        node.setUserName("node_" + nodeNumber);
        if (transportProtocol.compareToIgnoreCase("udp") == 0) {
            node.setUdpPort(port);
        } else {
            node.setTcpPort(port);
        }
        return node;
    }

    public synchronized void handleEvent(String eventType, Object eventData) {

        if (LOG.isTraceEnabled()) {
            LOG.trace(this.p2pNode.getUserId() + ": Handling event type=" + eventType /*
                     * + " data=" + eventData
                     */);
        }

        if (PSNode.EVENT_ONJOIN.equals(eventType)) {

            this.testState = TestState.JOINED;

        } else if (PSNode.EVENT_ONTOPICCREATE.equals(eventType)) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(this.p2pNode.getUserId() + ": Created topic " + eventData);
            }

            this.testState = TestState.CREATEDTOPIC;

        } else if (PSNode.EVENT_ONTOPICSUBSCRIBE.equals(eventType)) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(this.p2pNode.getUserId() + ": Subscribed to topic " + eventData);
            }

            this.testState = TestState.SUBSCRIBED;

        } else if (PSNode.EVENT_ONPUBSUBERROR.equals(eventType)) {

            Object[] eventDataArr = (Object[]) eventData;
            Object topicID = eventDataArr[0];
            Byte operationType = (Byte) eventDataArr[1];
            Integer errorCode = (Integer) eventDataArr[2];

            if (LOG.isDebugEnabled()) {
                LOG.debug(this.p2pNode.getUserId() + ": Error " + errorCode + " while " + operationType + " for id=" + topicID);
            }

//            switch (this.testState) {
//                case JOINED:
//                    if (operationType == 2 && errorCode == 5) {
//                        this.p2pNode.createTopic("topicID", true);
//                    }
//                    break;
//            }

        } else if (PSNode.EVENT_ONTOPICNOTIFY.equals(eventType)) {

            Object[] eventDataArr = (Object[]) eventData;
            Object topicID = eventDataArr[0];
            byte[] message = (byte[]) eventDataArr[1];

            if (LOG.isDebugEnabled()) {
                LOG.info(this.p2pNode.getUserId() + ": New event in topic " + topicID + ": " + new String(message));
            }
        } else if (EVENT_JCSYNC_ON_LOCAL_UPDATE.equals(eventType)) {
            this.finish.countDown();
//            JCSyncWriteMethod wm = (JCSyncWriteMethod) eventData;
//            if (LOG.isDebugEnabled()) {
//                LOG.info(this.p2pNode.getUserId() + ": [JCSync] On local update event invoked " + wm.getCollectionID() + ": " + wm.getGenericMethodName());
//            }
        } else if (EVENT_JCSYNC_ON_REMOTE_UPDATE.equals(eventType)) {
            this.finish.countDown();
//            JCSyncWriteMethod wm = (JCSyncWriteMethod) eventData;
//            if (LOG.isDebugEnabled()) {
//                LOG.info(this.p2pNode.getUserId() + ": [JCSync] On remote update event invoked " + wm.getCollectionID() + ": " + wm.getGenericMethodName());
//            }
        } else if (EVENT_LAST_NODE_CONNECTED.equals(eventType)) {
            this.waitForLastNode.countDown();
        } else if (EVENT_WORKER_FINISHED.equals(eventType)) {
            this.workersLatch.countDown();
        }


        if (LOG.isTraceEnabled()) {
            LOG.trace(this.p2pNode.getUserId() + ": Test state after handling event: " + this.testState);
        }

    }

    private void snooze(long t) {
        if (t <= 0) {
            return;
        }
        try {
            sleep(t);
        } catch (Throwable e) {
            LOG.error("Error while sleeping...", e);
        }
    }

    public static boolean isTestFromName(String name) {
        if("JCsyncBasicTest".compareToIgnoreCase(name)==0)return true;
        else return false;
    }

    public static int getArgsCount() {
        return rules.getFieldsRulesCount();
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setCurrTimeMillis() {
        this.currTimeMillis = System.currentTimeMillis();
    }

    public void setNextTimeMillis() {
        this.nextTimeMillis = System.currentTimeMillis();
    }

    public long estimateOperationTime() {
        return this.nextTimeMillis - this.currTimeMillis;
    }

//    private JCSyncAbstractCollection createCollection(String collName, Class declaredCollectionClass, Parameter... param) throws CollectionExistException {
//        return AbstractCollectionsManager.getInstance().requestCreateCollection(declaredCollectionClass, collName, EventInvoker.InvokerType.QUEUED, param);
//    }
//
//    private boolean test_CreateCollection() {
//        setCurrTimeMillis();
//        Class declaredCollectionClass = JCSyncHashMap.class;
//        Parameter param = null;
//        String collName = "coll_1";
//
//        JCSyncAbstractCollection coll;
//        JCSyncHashMap coll_HS;
//        try {
//            coll = createCollection(collName, declaredCollectionClass, (Parameter[]) null);
//            setNextTimeMillis();
//            logOperationTime("create collection: " + collName);
//        } catch (CollectionExistException ex) {
//            LOG.error("Error: Collection: coll_1 already exists!.", ex);
//            return false;
//        }
//        //VERIFY
//        if (coll == null) {
//            LOG.error("[TestError] Collection: " + collName + " doesn't exists!");
//            return false;
//        }
//        try {
//            coll_HS = (JCSyncHashMap) coll;
//        } catch (Exception e) {
//            LOG.error("[TestError] Invalid collection type,found " + coll.getDeclaredClass().getName() + " , required: " + declaredCollectionClass.getName());
//            return false;
//        }
//        if (coll_HS.getcollectionID().getID().compareTo(collName) != 0) {
//            LOG.error("[TestError] Invalid collection name,found " + coll_HS.getcollectionID().getID() + " , required: " + collName);
//        }
//        this.collection = coll_HS;
//        this.collection.addStateListener(collectionListener);
//        this.testState = TestState.CREATEDCOLLECTION;
//        return true;
//    }
    private void logOperationTime(String operationName) {
        LOG.info("[" + operationName + "] takes [ms]: " + estimateOperationTime());
    }
//    private boolean doOperations(JCSyncHashMap collection) {
//        this.jcsyncCore.DEBUG_publish(collection.getcollectionID().getID(), "[event]doOperation starts".getBytes());
//        for (int i = 0; i < mapValues.length; i++) {
//            this.collection.put(i, mapValues[i]);
//        }
//        return true;
//    }
//    private boolean checkCollectionDataIntegrity(JCSyncHashMap collection) {
//        String val;
//        for (int i = 0; i < mapValues.length; i++) {
//            val = (String) this.collection.get(i);
//            if (val.compareTo(mapValues[i]) != 0) {
//                return false;
//            }
//        }
//        return true;
//    }

    private void printCollectionData(JCSyncHashMap<String, OperationDetails> collection) {
        Set<Entry<String, OperationDetails>> set = collection.entrySet();
        LOG.info("Collection data:");
        for (Entry e : set) {
            LOG.info(e.getKey() + ": " + e.getValue());
        }
    }

    private void logTestArguments(Map<String, Object> args) throws SQLException {
        String driver = "com.mysql.jdbc.Driver";
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("mysql.properties"));
        } catch (IOException e) {
        }
        String url = prop.getProperty("url");
        String userName = prop.getProperty("user");
        String pass = prop.getProperty("password");
        Connection con = (Connection) DriverManager.getConnection(url, userName, pass);
        if (!con.isClosed()) {
            Statement st = (Statement) con.createStatement();
            String table = "CREATE TABLE IF NOT EXISTS tests_arguments"
                    + "(testID           VARCHAR(258), "
                    + " nodeName         VARCHAR(258), "
                    + " nodeNumber       INT, "
                    + "port             INT, "
                    + "bootIP           VARCHAR(258),"
                    + "bootPort         INT, "
                    + "transportProtocol         VARCHAR(258), "
                    + "protocol         VARCHAR(258),"
                    + "workersCount     INT,"
                    + "worker           BOOL,"
                    + "workerOperationCount   INT,"
                    + "operationDeelay  BIGINT,"
                    + "lastNode         BOOL,"
                    + "collectionName   VARCHAR(258),"
                    + "logName          VARCHAR(258))";
            st.executeUpdate(table);

            String query = "insert into tests_arguments (testID,nodeName,nodeNumber,port,bootIP,bootPort, transportProtocol,"
                    + "protocol,workersCount,worker,workerOperationCount,operationDeelay, lastNode,collectionName"
                    + ",logName) values(?,?, ?, ?,?,?,?,?,?,?,?,?,?,?,?)";

            PreparedStatement ps = (PreparedStatement) con.prepareStatement(query); // create a statement

            ps.setString(1, (String) args.get(FIELD_TEST_IDENTIFIER));
            ps.setString(2, this.p2pNode.getUserName());
            ps.setInt(3, (Integer) args.get(FIELD_NODE_NUMBER));
            ps.setInt(4, (Integer) args.get(FIELD_PORT));
            ps.setString(5, (String) args.get(FIELD_BOOT_IP));
            ps.setInt(6, (Integer) args.get(FIELD_BOOT_PORT));
            ps.setString(7, (String) args.get(FIELD_TRANSPORT_PROTOCOL));
            ps.setString(8, (String) args.get(FIELD_PROTOCOL));
            ps.setInt(9, (Integer) args.get(FIELD_WORKERS_COUNT));
            ps.setBoolean(10, (Boolean) args.get(FIELD_WORKER));
            ps.setInt(11, (Integer) args.get(FIELD_WORKER_OPERATION_COUNT));
            ps.setLong(12, (Long) args.get(FIELD_OPERATION_DELAY));
            ps.setBoolean(13, (Boolean) args.get(FIELD_LAST_NODE));
            ps.setString(14, (String) args.get(FIELD_COLLECTION_NAME));
            ps.setString(15, (String) args.get(FIELD_LOG_NAME));
            ps.executeUpdate(); // execute insert statement
            //LOG.info(e.getKey()+": "+e.getValue());

            con.close();
        }
    }

    private void logDataToDatabase(JCSyncHashMap<String, OperationDetails> collection) throws SQLException {
        String driver = "com.mysql.jdbc.Driver";
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("mysql.properties"));
        } catch (IOException e) {
        }
        String url = prop.getProperty("url");
        String userName = prop.getProperty("user");
        String pass = prop.getProperty("password");

        Connection con = (Connection) DriverManager.getConnection(url, userName, pass);
        if (!con.isClosed()) {
            Statement st = (Statement) con.createStatement();
            String table = "CREATE TABLE IF NOT EXISTS tests"
                    + "(testID VARCHAR(258), "
                    + " nodeName VARCHAR(258), "
                    + " publisher VARCHAR(258), "
                    + "opID          VARCHAR(258), "
                    + "cT           BIGINT, rRT          BIGINT, "
                    + "iRT          BIGINT,"
                    + "iRT_cT   BIGINT,"
                    + "iRT_rRT   BIGINT)";
            st.executeUpdate(table);

            String query = "insert into tests (testID,nodeName, publisher, opID, cT, rRT, iRT,iRT_cT,iRT_rRT) values(?,?, ?, ?,?,?,?,?,?)";

            PreparedStatement pstmt = (PreparedStatement) con.prepareStatement(query); // create a statement

            Set<Entry<String, OperationDetails>> set = collection.entrySet();
            OperationDetails od;
            for (Entry e : set) {
                pstmt.setString(1, (String) this.kwargs.get(FIELD_TEST_IDENTIFIER));
                pstmt.setString(2, this.p2pNode.getUserName());
                od = (OperationDetails) e.getValue();
                pstmt.setString(3, od.publisher);
                pstmt.setString(4, od.operationID);
                pstmt.setLong(5, od.creationTime);
                pstmt.setLong(6, od.requestInvokingTime);
                pstmt.setLong(7, od.indicationReceivingTime);
                if (this.p2pNode.getUserName().compareToIgnoreCase(od.publisher) == 0) {
                    pstmt.setLong(8, od.indicationReceivingTime - od.creationTime);
                    pstmt.setLong(9, -1);
                } else {
                    pstmt.setLong(8, -1);
                    pstmt.setLong(9, od.indicationReceivingTime - od.requestInvokingTime);
                }
                pstmt.executeUpdate(); // execute insert statement
                //LOG.info(e.getKey()+": "+e.getValue());
            }
            con.close();
        }
    }
}

enum TestState {

    UNCONNECTED, JOINED, CREATEDTOPIC, SUBSCRIBED, CREATEDCOLLECTION
}