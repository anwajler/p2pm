package pl.edu.pjwstk.mteam.pubsub.tests.tests.p2ppbasic;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.tests.events.EventManager;
import pl.edu.pjwstk.mteam.tests.events.IEventSubscriber;
import pl.edu.pjwstk.mteam.pubsub.tests.p2ppnode.P2ppNode;
import pl.edu.pjwstk.mteam.pubsub.tests.p2ppnode.P2ppNodeCallback;
import pl.edu.pjwstk.mteam.tests.rules.FieldRule;
import pl.edu.pjwstk.mteam.tests.tests.ITest;
import pl.edu.pjwstk.p2pp.ErrorInterface;
import pl.edu.pjwstk.p2pp.P2PPManager;
import pl.edu.pjwstk.p2pp.entities.Peer;
import pl.edu.pjwstk.p2pp.ice.STUNService;
import pl.edu.pjwstk.p2pp.kademlia.KademliaPeer;
import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.P2POptions;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.resources.StringValueResourceObject;
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerPeer;
import pl.edu.pjwstk.p2pp.util.P2PPMessageFactory;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

import java.io.IOException;
import java.net.BindException;
import java.util.List;
import java.util.Map;

public class P2ppBasicTest extends Thread implements ITest, IEventSubscriber {

    public static final Logger LOG = Logger.getLogger(P2ppBasicTest.class);

    private static final P2ppBasicTestRules rules = new P2ppBasicTestRules();
    private static final String[] acceptedEvents = {P2ppNode.EVENT_ONERROR,P2ppNode.EVENT_ONJOIN,P2ppNode.EVENT_ONLOOKUP,P2ppNode.EVENT_ONPUBLISH};

    private Map<String,Object> kwargs;

    Integer nodeNumber;
    private P2PPManager p2ppManager;
    private Peer peer;

    private TestState testState = TestState.UNCONNECTED;

    public P2ppBasicTest(Map<String, Object> kwargs) {
        setName("P2ppBasicTest");
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
                StringBuilder strb = new StringBuilder("P2ppBasicTest must be given a \"");
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
            LOG.error("P2ppBasicTest cannot run with given arguments: " + this.kwargs);
            return false;
        }

        this.nodeNumber = (Integer) this.kwargs.get("nodeNumber");
        Integer port = (Integer) this.kwargs.get("port");
        String bootIP = (String) this.kwargs.get("bootIP");
        Integer bootPort = (Integer) this.kwargs.get("bootPort");
        String overlayId = (String) this.kwargs.get("overlayId");
        byte[] overlayIdBytes = overlayId.getBytes();
        String transport = (String) this.kwargs.get("transport");
        String protocol = (String) this.kwargs.get("protocol");

        StringBuilder strb = new StringBuilder("Starting P2ppBasicTest port=");
        strb.append(port).append(" bootIP=").append(bootIP).append(" bootPort=").append(bootPort).append(" overlayID=").append(overlayId);
        strb.append(" transport=").append(transport).append(" protocol=").append(protocol);
        LOG.info(strb.toString());

        boolean[] transportBools;
        if ("udp".equals(transport)) {
            transportBools = AddressInfo.UDP_TRANSPORT_TYPE;
            this.p2ppManager = new P2PPManager(-1, port, -1, -1, -1, "", "", new P2PPMessageFactory(), ("node"+this.nodeNumber).getBytes());
        } else {
            transportBools = AddressInfo.TCP_TRANSPORT_TYPE;
            this.p2ppManager = new P2PPManager(port, -1, -1, -1, -1, "", "", new P2PPMessageFactory(), ("node"+this.nodeNumber).getBytes());
        }

        byte protocolByte;
        if ("kademlia".equals(protocol)) {
            protocolByte = P2PPUtils.KADEMLIA_P2P_ALGORITHM;
        } else {
            protocolByte = P2PPUtils.SUPERPEER_P2P_ALGORITHM;
        }

        STUNService.serverReflexiveAddress = bootIP;
        STUNService.serverReflexivePort = bootPort;

        this.p2ppManager.setOptions(new P2POptions(P2PPUtils.SHA1_HASH_ALGORITHM, (byte)20, protocolByte, (byte)2, overlayIdBytes));
        this.peer = (protocolByte==P2PPUtils.KADEMLIA_P2P_ALGORITHM) ? new KademliaPeer() : new SuperPeerPeer();
        this.peer.addService(new STUNService(true, transportBools));
        this.peer.setCallback(new P2ppNodeCallback());
        this.p2ppManager.addEntity(peer);

        try {
            this.p2ppManager.start();
        } catch (BindException be) {
            LOG.error("Address already in use", be);
        } catch (IOException ioe) {
            LOG.error(ioe);
        }

        this.peer.join(overlayIdBytes, bootIP,  bootPort);

        while (true) {
            try {
                sleep(1000);
            } catch (Throwable e) {
                break;
            }
        }

        return true;
    }

    public synchronized void handleEvent(String eventType, Object eventData) {
        try {

            if (LOG.isTraceEnabled()) {
                LOG.trace(this.nodeNumber + ": Handling event type=" + eventType + " data=" + eventData);
            }

            if (P2ppNode.EVENT_ONERROR.equals(eventType)) {

                Object[] eventDataArr = (Object[]) eventData;
                ErrorInterface errorInterface = (ErrorInterface) eventDataArr[0];
                Integer errorCode = (Integer) eventDataArr[1];

                if (LOG.isDebugEnabled()) {
                    StringBuilder strb = new StringBuilder("onerror code=");
                    strb.append(errorCode).append(" value=").append(errorInterface.getValue());
                    LOG.debug(strb.toString());
                }

            } else if (P2ppNode.EVENT_ONJOIN.equals(eventType)) {

                this.testState = TestState.JOINED;

                String key = "node"+this.nodeNumber+"_key";
                String value = "node"+this.nodeNumber+"_value";
                this.peer.publish(key.getBytes(), new StringValueResourceObject(key, value));

            } else if (P2ppNode.EVENT_ONPUBLISH.equals(eventType)) {

                this.testState = TestState.PUBLISHED;

                Object[] eventDataArr = (Object[]) eventData;
                Byte contentType = (Byte) eventDataArr[0];
                Byte contentSubType = (Byte) eventDataArr[1];
                String resourceKey = new String((byte[])eventDataArr[2]);
                if (LOG.isTraceEnabled()) {
                    LOG.trace("onpublish: contentType=" + contentType + " contentSubType=" + contentSubType + " key=" + resourceKey);
                }

                String key = "node"+this.nodeNumber+"_key";
                this.peer.lookup(P2PPUtils.STRING_VALUE_CONTENT_TYPE, (byte)0, key.getBytes(), null);


            } else if (P2ppNode.EVENT_ONLOOKUP.equals(eventType)) {

                Object[] eventDataArr = (Object[]) eventData;
                List<ResourceObject> resourceObjects = (List<ResourceObject>) eventDataArr[0];

                boolean ok = false;

                if (!resourceObjects.isEmpty()) {
                    ResourceObject publishedResource = resourceObjects.get(0);
                    if (publishedResource instanceof StringValueResourceObject) {
                        StringValueResourceObject svro = (StringValueResourceObject) publishedResource;
                        String valueOk = "node"+this.nodeNumber+"_value";
                        String valueSvro = svro.getValueAsString();
                        if (LOG.isTraceEnabled()) LOG.trace(valueOk + " ?= " + valueSvro);
                        if (valueOk.equals(valueSvro)) ok = true;
                    }
                }

                LOG.info(this.nodeNumber + " returned correct resource: " + ok);

                this.testState = TestState.LOOKEDUP;

            }


            if (LOG.isTraceEnabled()) {
                LOG.trace(this.nodeNumber + ": Test state after handling event: " + this.testState);
            }

        } catch (Throwable e) {
            LOG.error("Error while handling event type=" + eventType, e);
        }
    }

    public static boolean isTestFromName(String name) {
        return "p2ppbasic".equals(name);
    }

    public static int getArgsCount() {
        return rules.getFieldsRulesCount();
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

enum TestState {
    UNCONNECTED, JOINED, PUBLISHED, LOOKEDUP
}