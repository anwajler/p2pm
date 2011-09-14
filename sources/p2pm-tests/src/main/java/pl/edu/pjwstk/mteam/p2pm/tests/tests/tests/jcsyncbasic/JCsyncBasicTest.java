package pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.jcsync.exception.CollectionExistException;

import java.util.Map;

import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.collections.implementation.AbstractCollectionsManager;
import pl.edu.pjwstk.mteam.jcsync.collections.implementation.JCSyncHashMap;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCoreAlgorithm;
import pl.edu.pjwstk.mteam.jcsync.core.concurrency.EventInvoker;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncWriteMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.Parameter;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.p2pm.tests.core.events.EventManager;
import pl.edu.pjwstk.mteam.p2pm.tests.core.events.IEventSubscriber;
import pl.edu.pjwstk.mteam.p2pm.tests.core.rules.FieldRule;
import pl.edu.pjwstk.mteam.p2pm.tests.core.tests.ITest;
import pl.edu.pjwstk.mteam.p2pm.tests.tests.psnode.PSNode;
import pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic.node.NodeCallback;
import pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic.node.P2ppNode;
import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;

public class JCsyncBasicTest extends Thread implements ITest, IEventSubscriber {


    public static final Logger LOG = Logger.getLogger(JCsyncBasicTest.class);
    private static final JCSyncBasicTestRules rules = new JCSyncBasicTestRules();
    private static final String[] acceptedEvents = {PSNode.EVENT_ONJOIN, PSNode.EVENT_ONTOPICCREATE, PSNode.EVENT_ONTOPICSUBSCRIBE,
            PSNode.EVENT_ONPUBSUBERROR, PSNode.EVENT_ONTOPICNOTIFY, PSNode.EVENT_ONDELIVER};

    public static String EVENT_JCSYNC_ON_REMOTE_UPDATE = "JCSYNC_ON_REMOTE_UPDATE";
    public static String EVENT_JCSYNC_ON_LOCAL_UPDATE = "JCSYNC_ON_LOCAL_UPDATE";

    private Map<String, Object> kwargs;

    private P2ppNode p2pNode;
    private TestState testState = TestState.UNCONNECTED;
    private CoreAlgorithm pubsubmgr;
    private JCSyncCoreAlgorithm jcsyncCore;
    public long currTimeMillis;
    public long nextTimeMillis;
    private JCSyncHashMap collection;
    private final JCSyncBasicTestCollectionListener collectionListener = new JCSyncBasicTestCollectionListener();

    private static String[] mapValues = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};


    public JCsyncBasicTest(Map<String, Object> kwargs) {
        setName("JCsyncBasicTest");
        this.kwargs = kwargs;
        for (String acceptedEvent : acceptedEvents) {
            EventManager.getInstance().subscribe(acceptedEvent, this);
        }
        EventManager.getInstance().subscribe(EVENT_JCSYNC_ON_REMOTE_UPDATE, this);
        EventManager.getInstance().subscribe(EVENT_JCSYNC_ON_LOCAL_UPDATE, this);
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
        if (this.p2pNode == null) return false;

        JCSyncCoreAlgorithm.init(this.p2pNode, this.p2pNode.getTcpPort() + 1);
        this.p2pNode.networkJoin();
        // See handleEvent()

        /*for (int i=0; this.testState != TestState.SUBSCRIBED && i<30; i++) {
            if (i == 29) {
                LOG.error("node" + nodeNumber + ": PSNode haven't subscribe despite waiting. Quitting.");
                return false;
            }
            snooze(1000);
        }*/
        while (this.testState != TestState.JOINED) snooze(10);
        setNextTimeMillis();
        logOperationTime("join to network layer");

        this.jcsyncCore = JCSyncCoreAlgorithm.getInstance();

        if (!test_CreateCollection()) {
            return false;
        }
        if (this.collection.getcollectionID().isTopicRoot()) {
            if (!doOperations(this.collection)) {
                return false;
            }
        } else {
            if (!checkCollectionDataIntegrity(this.collection)) {
                return false;
            }
        }

        while (true) {
            try {
                sleep(1000);
            } catch (Throwable e) {
                break;
            }
        }
        //snooze(5000);

        return true;
    }

    public P2ppNode initLayer(Map<String, Object> args) {
        Integer nodeNumber = (Integer) this.kwargs.get("nodeNumber");
        Integer port = (Integer) this.kwargs.get("port");
        String bootIP = (String) this.kwargs.get("bootIP");
        Integer bootPort = (Integer) this.kwargs.get("bootPort");

        // Creating PSNode
        P2ppNode node = new P2ppNode(new NodeCallback(), P2PNode.RoutingAlgorithm.SUPERPEER);
        node.setBootIP(bootIP);
        node.setBootPort(bootPort);
        node.setUserName("node_" + nodeNumber);
        node.setTcpPort(port);
        return node;
    }

    public synchronized void handleEvent(String eventType, Object eventData) {

        if (LOG.isTraceEnabled()) {
            LOG.trace(this.p2pNode.getUserId() + ": Handling event type=" + eventType /*+ " data=" + eventData*/);
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
            JCSyncWriteMethod wm = (JCSyncWriteMethod) eventData;
            if (LOG.isDebugEnabled()) {
                LOG.info(this.p2pNode.getUserId() + ": [JCSync] On local update event invoked " + wm.getCollectionID() + ": " + wm.getGenericMethodName());
            }
        } else if (EVENT_JCSYNC_ON_REMOTE_UPDATE.equals(eventType)) {
            JCSyncWriteMethod wm = (JCSyncWriteMethod) eventData;
            if (LOG.isDebugEnabled()) {
                LOG.info(this.p2pNode.getUserId() + ": [JCSync] On remote update event invoked " + wm.getCollectionID() + ": " + wm.getGenericMethodName());
            }
        }


        if (LOG.isTraceEnabled()) {
            LOG.trace(this.p2pNode.getUserId() + ": Test state after handling event: " + this.testState);
        }

    }

    private void snooze(long t) {
        if (t <= 0) return;
        try {
            sleep(t);
        } catch (Throwable e) {
            LOG.error("Error while sleeping...", e);
        }
    }

    public static boolean isTestFromName(String name) {
        return "jcsyncbasic".equalsIgnoreCase(name);
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

    private JCSyncAbstractCollection createCollection(String collName, Class declaredCollectionClass, Parameter... param) throws CollectionExistException {
        return AbstractCollectionsManager.getInstance().requestCreateCollection(declaredCollectionClass, collName, EventInvoker.InvokerType.QUEUED, param);
    }

    private boolean test_CreateCollection() {
        setCurrTimeMillis();
        Class declaredCollectionClass = JCSyncHashMap.class;
        Parameter param = null;
        String collName = "coll_1";

        JCSyncAbstractCollection coll;
        JCSyncHashMap coll_HS;
        try {
            coll = createCollection(collName, declaredCollectionClass, (Parameter[]) null);
            setNextTimeMillis();
            logOperationTime("create collection: " + collName);
        } catch (CollectionExistException ex) {
            LOG.error("Error: Collection: coll_1 already exists!.", ex);
            return false;
        }
        //VERIFY
        if (coll == null) {
            LOG.error("[TestError] Collection: " + collName + " doesn't exists!");
            return false;
        }
        try {
            coll_HS = (JCSyncHashMap) coll;
        } catch (Exception e) {
            LOG.error("[TestError] Invalid collection type,found " + coll.getDeclaredClass().getName() + " , required: " + declaredCollectionClass.getName());
            return false;
        }
        if (coll_HS.getcollectionID().getID().compareTo(collName) != 0) {
            LOG.error("[TestError] Invalid collection name,found " + coll_HS.getcollectionID().getID() + " , required: " + collName);
        }
        this.collection = coll_HS;
        this.collection.addStateListener(collectionListener);
        this.testState = TestState.CREATEDCOLLECTION;
        return true;
    }

    private void logOperationTime(String operationName) {
        LOG.info("[" + operationName + "] takes [ms]: " + estimateOperationTime());
    }

    private boolean doOperations(JCSyncHashMap collection) {
        this.jcsyncCore.DEBUG_publish(collection.getcollectionID().getID(), "[event]doOperation starts".getBytes());
        for (int i = 0; i < mapValues.length; i++) {
            this.collection.put(i, mapValues[i]);
        }
        return true;
    }

    private boolean checkCollectionDataIntegrity(JCSyncHashMap collection) {
        String val;
        for (int i = 0; i < mapValues.length; i++) {
            val = (String) this.collection.get(i);
            if (val.compareTo(mapValues[i]) != 0) {
                return false;
            }
        }
        return true;
    }

}

enum TestState {
    UNCONNECTED, JOINED, CREATEDTOPIC, SUBSCRIBED, CREATEDCOLLECTION
}