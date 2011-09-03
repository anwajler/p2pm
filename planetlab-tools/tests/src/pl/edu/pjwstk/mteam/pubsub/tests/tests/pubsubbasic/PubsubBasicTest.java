package pl.edu.pjwstk.mteam.pubsub.tests.tests.pubsubbasic;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.tests.events.EventManager;
import pl.edu.pjwstk.mteam.tests.events.IEventSubscriber;
import pl.edu.pjwstk.mteam.pubsub.tests.psnode.PSNode;
import pl.edu.pjwstk.mteam.tests.rules.FieldRule;
import pl.edu.pjwstk.mteam.pubsub.tests.tests.ITest;

import java.util.Map;
import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;

public class PubsubBasicTest extends Thread implements ITest, IEventSubscriber {

    public static final Logger LOG = Logger.getLogger(PubsubBasicTest.class);

    private static final PubsubBasicTestRules rules = new PubsubBasicTestRules();
    private static final String[] acceptedEvents = {PSNode.EVENT_ONJOIN,PSNode.EVENT_ONTOPICCREATE,PSNode.EVENT_ONTOPICSUBSCRIBE,
            PSNode.EVENT_ONPUBSUBERROR, PSNode.EVENT_ONTOPICNOTIFY, PSNode.EVENT_ONDELIVER};

    private Map<String,Object> kwargs;

    private PSNode psNode;
    private TestState testState = TestState.UNCONNECTED;
    private CoreAlgorithm pubsubmgr;

    public PubsubBasicTest(Map<String, Object> kwargs) {
        setName("PubsubBasicTest");
        this.kwargs = kwargs;
        for (String acceptedEvent : acceptedEvents) {
            EventManager.getInstance().subscribe(acceptedEvent, this);
        }
    }

    private boolean verifyKwargs() {

        Map<String,FieldRule> fieldRules = rules.getFieldsRules();
        for (FieldRule fr : fieldRules.values()) {
            String fieldName = fr.getFieldName();
            Object kwargValue = kwargs.get(fieldName);
            if (kwargValue == null) {
                StringBuilder strb = new StringBuilder("PubsubBasicTest must be given a \"");
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
            LOG.error("BasicTest cannot run with given arguments: " + this.kwargs);
            return false;
        }

        Integer nodeNumber = (Integer) this.kwargs.get("nodeNumber");
        Integer port = (Integer) this.kwargs.get("port");
        String bootIP = (String) this.kwargs.get("bootIP");
        Integer bootPort = (Integer) this.kwargs.get("bootPort");

        // Creating PSNode
        this.psNode = new PSNode("node" + nodeNumber, port, bootIP, bootPort, bootIP, bootPort);
        this.psNode.init();

        //snooze(30000*nodeNumber);
        
        
       
        this.psNode.networkJoin();
        // See handleEvent()

        /*for (int i=0; this.testState != TestState.SUBSCRIBED && i<30; i++) {
            if (i == 29) {
                LOG.error("node" + nodeNumber + ": PSNode haven't subscribe despite waiting. Quitting.");
                return false;
            }
            snooze(1000);
        }*/
        while (this.testState != TestState.SUBSCRIBED) snooze(1000);

        this.psNode.publish("topicID", ("Message from node #" + nodeNumber).getBytes());
        
//        while (true) {
//            try {
//                sleep(1000);
//            } catch (Throwable e) {
//                break;
//            }
//        }
        snooze(5000);

        return true;
    }

    public synchronized void handleEvent(String eventType, Object eventData) {

        if (LOG.isTraceEnabled()) {
            LOG.trace(this.psNode.getUserId() + ": Handling event type=" + eventType + " data=" + eventData);
        }

        if (PSNode.EVENT_ONJOIN.equals(eventType)) {

            this.testState = TestState.JOINED;
            this.psNode.subscribe("topicID", -1);

        } else if (PSNode.EVENT_ONTOPICCREATE.equals(eventType)) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(this.psNode.getUserId() + ": Created topic " + eventData);
            }

            this.testState = TestState.CREATEDTOPIC;

        } else if (PSNode.EVENT_ONTOPICSUBSCRIBE.equals(eventType)) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(this.psNode.getUserId() + ": Subscribed to topic " + eventData);
            }

            this.testState = TestState.SUBSCRIBED;

        } else if (PSNode.EVENT_ONPUBSUBERROR.equals(eventType)) {

            Object[] eventDataArr = (Object[]) eventData;
            Object topicID = eventDataArr[0];
            Byte operationType = (Byte) eventDataArr[1];
            Integer errorCode = (Integer) eventDataArr[2];

            if (LOG.isDebugEnabled()) {
                LOG.debug(this.psNode.getUserId() + ": Error " + errorCode + " while " + operationType + " for id=" + topicID);
            }

            switch (this.testState) {
                case JOINED:
                    if (operationType == 2 && errorCode == 5) {
                        this.psNode.createTopic("topicID", true);
                    }
                    break;
            }

        } else if (PSNode.EVENT_ONTOPICNOTIFY.equals(eventType)) {

            Object[] eventDataArr = (Object[]) eventData;
            Object topicID = eventDataArr[0];
            byte[] message = (byte[]) eventDataArr[1];

            if (LOG.isDebugEnabled()) {
                LOG.debug(this.psNode.getUserId() + ": New event in topic " + topicID + ": " + new String(message));
            }

        }

        if (LOG.isTraceEnabled()) {
            LOG.trace(this.psNode.getUserId() + ": Test state after handling event: " + this.testState);
        }

    }

    private void snooze(long t) {
        if (t <= 0) return;
        try{
            sleep(t);
        } catch (Throwable e) {
            LOG.error("Error while sleeping...", e);
        }
    }

    public static boolean isTestFromName(String name) {
        return "pubsubbasic".equals(name);
    }

    public static int getArgsCount() {
        return rules.getFieldsRulesCount();
    }

}

enum TestState {
    UNCONNECTED, JOINED, CREATEDTOPIC, SUBSCRIBED
}