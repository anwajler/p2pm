package pl.edu.pjwstk.mteam.p2pm.tests.tests.psnode;

import java.util.Vector;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.core.NetworkObject;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.core.PubSubInterface;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.p2p.P2PNode.RoutingAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.core.User;
import pl.edu.pjwstk.mteam.pubsub.interestconditions.InterestConditions;

public class PSNode {

    public static final Logger LOG = Logger.getLogger(PSNode.class);

    public static final String EVENT_ONJOIN = "psnode.onjoin";
    public static final String EVENT_ONTOPICCREATE = "psnode.ontopiccreate";
    public static final String EVENT_ONTOPICSUBSCRIBE = "psnode.ontopicsubscribe";
    public static final String EVENT_ONPUBSUBERROR = "psnode.onpubsuberror";
    public static final String EVENT_ONTOPICNOTIFY = "psnode.ontopicnotify";
    public static final String EVENT_ONDELIVER = "psnode.ondeliver";

    private String userId;
    private int port;
    private String bootIP;
    private int bootPort;
    private String serverReflexiveIP;
    private int serverReflexivePort;

    private Node p2pNode;

    private Topic topic;
    private AccessControlRules accessControlRules;
    private InterestConditions interestConditions;
    private CoreAlgorithm coreAlgorithm;

    public PSNode(String userId, int port, String bootIP, int bootPort, String serverReflexiveIP, int serverReflexivePort) {
        this.userId = userId;
        this.port = port;
        this.bootIP = bootIP;
        this.bootPort = bootPort;
        this.serverReflexiveIP = serverReflexiveIP;
        this.serverReflexivePort = serverReflexivePort;
    }

    public String getUserId() {
        return this.userId;
    }

    public void init() {

        this.topic = new Topic("", new NodeInfo(this.userId));
        this.accessControlRules = new AccessControlRules(this.topic);
        this.interestConditions = new InterestConditions(this.topic);

        this.p2pNode = new P2PNode(new PSNodeCallback(), /*RoutingAlgorithm.KADEMLIA*/RoutingAlgorithm.SUPERPEER);

        ((P2PNode) this.p2pNode).setServerReflexiveAddress(this.serverReflexiveIP);
        ((P2PNode) this.p2pNode).setServerReflexivePort(this.serverReflexivePort);

        this.p2pNode.setUserName(this.userId);
        this.p2pNode.setTcpPort(this.port);

        this.p2pNode.enableDebug();

        //p.setTcpPort(P2PPTest.port);
        //The line below is only needed by pub-sub
        this.coreAlgorithm = new CoreAlgorithm(this.p2pNode.getTcpPort() + 1, this.p2pNode);
        this.p2pNode.setBootIP(this.bootIP);
        this.p2pNode.setBootPort(this.bootPort);
        this.coreAlgorithm.init();

        if (LOG.isTraceEnabled()) {
            LOG.trace("Creating peer " + this.p2pNode.getUserName() + ":" + this.p2pNode.getTcpPort());
        }

    }

    public boolean isInitiaded() {
        return this.p2pNode != null;
    }

    public void insertObject(String key, byte[] value) {
        NetworkObject obj = new NetworkObject(NetworkObject.TYPE_BYTEARRAY, key, value);
        this.p2pNode.insert(obj);
    }

    public void lookupObject(String key) {
        if (LOG.isDebugEnabled()) LOG.debug("Looking up object of key=\"" + key + "\"");
        this.p2pNode.networkLookupObject(key);
    }

    public void lookupUser(String name) {
        this.p2pNode.networkLookupUser(name);
    }

    public void networkJoin() {
        if (LOG.isDebugEnabled()) LOG.debug("Joining network");
        if (!isInitiaded()) {
            LOG.warn("Could not join network: PSNode is not initiated");
            return;
        }
        this.p2pNode.networkJoin();
    }

    public void networkLeave() {
        this.p2pNode.networkLeave();
    }

    public void setUserName(String newUserName) {
        this.p2pNode.setUserName(newUserName);
        this.userId = this.p2pNode.getUserName();
    }

    public void createTopic(String topicId, boolean subscribeFlag) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.userId + ": Creating topic '" + topicId + "' (subscribe=" + subscribeFlag + ")");
        }
        if (!isInitiaded()) {
            LOG.warn(this.userId + ": Could not create topic: PSNode is not initiated");
            return;
        }
        this.p2pNode.getPubSubInterface().createTopic(topicId, subscribeFlag, this.accessControlRules);
    }

    public void publish(String topicId, byte[] message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.userId + ": Publishing event for '" + topicId + "': " + new String(message));
        }
        if (!isInitiaded()) {
            LOG.warn(this.userId + ": Could not publish resource: PSNode is not initiated");
            return;
        }
        this.p2pNode.getPubSubInterface().networkPublish(topicId, message);
    }

    public void subscribe(String topicId, int eventIndex) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.userId + ": Subscribing for '" + topicId + "' (eventIndex=" + eventIndex + ")");
        }

        if (!isInitiaded()) {
            LOG.warn(this.userId + ": Could not subscribe: PSNode is not initiated");
            return;
        }

        PubSubInterface psInterface = this.p2pNode.getPubSubInterface();
        if (psInterface == null) {
            LOG.warn(this.userId + ": Could not subscribe: P2PNode returned null PubSubInterface");
            return;
        }

        psInterface.networkSubscribe(topicId, this.interestConditions, eventIndex);
    }

    public void unsubscribe(String topicId) {
        if (LOG.isDebugEnabled()) LOG.debug("Unsubscribing from '" + topicId + "'");
        this.p2pNode.getPubSubInterface().networkUnsubscribe(topicId);
    }

    public void removeTopic(String topicId) {
        if (LOG.isDebugEnabled()) LOG.debug("Removing topic '" + topicId + "'");
        this.p2pNode.getPubSubInterface().removeTopic(topicId);
    }

    public void modifyAC(String topicId) {
        if (LOG.isDebugEnabled()) LOG.debug("Modifying AC for '" + topicId + "'");
        this.p2pNode.getPubSubInterface().modifyAccessControlRules(topicId, this.accessControlRules);
    }

    public void modifyIC(String topicId) {
        if (LOG.isDebugEnabled()) LOG.debug("Modifying IC for '" + topicId + "'");
        this.p2pNode.getPubSubInterface().modifyInterestConditions(topicId, this.interestConditions);
    }

    public void addUserToTempIC(byte operation, byte event, String username) {
        this.interestConditions.getRule(operation).addUser(event, new User(username));
        if (LOG.isDebugEnabled()) LOG.debug(this.interestConditions);
    }

    public void addUserToTempAC(byte operation, byte event, String username) {
        this.accessControlRules.getRule(operation).addUser(event, new Subscriber(username, this.topic));
        if (LOG.isDebugEnabled()) LOG.debug(this.accessControlRules);
    }

    public void restoreDefaultAC() {
        if (LOG.isDebugEnabled()) LOG.debug("Restoring default AC");
        this.accessControlRules = new AccessControlRules(this.topic);
    }

    public void removeUsersFromTempAC(byte operation, byte event, Vector<String> users) {
        for (String user : users) {
            this.accessControlRules.getRule(operation).removeUser(event, new Subscriber(user, topic));
        }
        if (LOG.isDebugEnabled()) LOG.debug(this.accessControlRules);
    }

    public void removeUsersFromTempIC(byte operation, byte event, Vector<String> users) {
        for (String user : users) {
            this.interestConditions.getRule(operation).removeUser(event, new User(user));
        }
        if (LOG.isDebugEnabled()) LOG.debug(this.interestConditions);
    }

    public void showIC(String topicId) {
        if (LOG.isDebugEnabled()) LOG.debug("Showing IC for '" + topicId + "'");
        //TODO: AAA replace this with getting real IC!!!
        this.interestConditions = new InterestConditions(this.topic);
        //tempIC = Node.getTopicIC();
    }

    public void showAC(String topicId) {
        if (LOG.isDebugEnabled()) LOG.debug("Showing AC for '" + topicId + "'");
        //TODO: AAA replace this with getting real AC!!!
        this.accessControlRules = new AccessControlRules(topic);
        if (LOG.isDebugEnabled()) LOG.debug(this.accessControlRules);
    }

    public boolean isConnected() {
        return this.p2pNode.isConnected();
    }

}
