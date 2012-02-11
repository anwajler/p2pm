package pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic;

import pl.edu.pjwstk.mteam.p2pm.tests.core.rules.FieldRule;
import pl.edu.pjwstk.mteam.p2pm.tests.core.rules.TestRules;

import java.util.Hashtable;

public class JCSyncTestRules extends TestRules {
    public static String FIELD_NODE_NUMBER = "nodeNumber";
    public static String FIELD_PORT = "port";
    public static String FIELD_BOOT_IP = "bootIP";
    public static String FIELD_BOOT_PORT = "bootPort";
    public static String FIELD_TRANSPORT_PROTOCOL = "transportProtocol";
    public static String FIELD_PROTOCOL = "protocol";
    public static String FIELD_WORKER = "worker";
    public static String FIELD_WORKER_OPERATION_COUNT = "worker_operation_count";
    public static String FIELD_OPERATION_DELAY = "worker_operation_deelay";
    public static String FIELD_LAST_NODE = "lastNode";
    public static String FIELD_COLLECTION_NAME = "CollectionName";
    public static String FIELD_LOG_NAME = "logName";
    public static String FIELD_WORKERS_COUNT = "workers_count";
    public static String FIELD_TEST_IDENTIFIER = "testName";
    public static String FIELD_nodes_Count = "nodes_count";
    public JCSyncTestRules() {
        super(new Hashtable<String, FieldRule>() {{
            put(FIELD_NODE_NUMBER, new FieldRule(FIELD_NODE_NUMBER, "Number of the node launched", Integer.class));
            put(FIELD_PORT, new FieldRule(FIELD_PORT, "Node's port", Integer.class));
            put(FIELD_BOOT_IP, new FieldRule(FIELD_BOOT_IP, "Bootstrap server's IP address", String.class));
            put(FIELD_BOOT_PORT, new FieldRule(FIELD_BOOT_PORT, "Bootstrap server's port", Integer.class));
            put(FIELD_TRANSPORT_PROTOCOL, new FieldRule(FIELD_TRANSPORT_PROTOCOL, "{TCP/UDP}", String.class));
            put(FIELD_PROTOCOL, new FieldRule(FIELD_PROTOCOL, "P2P protocol: {kademlia|superpeer}", String.class));            put(FIELD_nodes_Count, new FieldRule(FIELD_nodes_Count, "Nodes count", Integer.class));           
            put(FIELD_WORKERS_COUNT,new FieldRule(FIELD_WORKERS_COUNT, "count of the workers nodes", Integer.class));
            put(FIELD_WORKER, new FieldRule(FIELD_WORKER, "determines, that current node will do some stuff on the collection", Boolean.class));
            put(FIELD_WORKER_OPERATION_COUNT, new FieldRule(FIELD_WORKER_OPERATION_COUNT, "how many operation will worker do", Integer.class));
            put(FIELD_OPERATION_DELAY, new FieldRule(FIELD_OPERATION_DELAY, "deelay between operation [ms]", Long.class));
            put(FIELD_LAST_NODE, new FieldRule(FIELD_LAST_NODE, "if true then test will be started", Boolean.class));
            put(FIELD_COLLECTION_NAME, new FieldRule(FIELD_COLLECTION_NAME, "collection identifier", String.class));
            put(FIELD_TEST_IDENTIFIER, new FieldRule(FIELD_TEST_IDENTIFIER, "test name", String.class));
            put(FIELD_LOG_NAME, new FieldRule(FIELD_LOG_NAME, "log file name", String.class));
        }});
    }

}
