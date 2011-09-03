package pl.edu.pjwstk.mteam.pubsub.tests.tests.p2ppbasic;

import pl.edu.pjwstk.mteam.tests.rules.FieldRule;
import pl.edu.pjwstk.mteam.tests.rules.TestRules;

import java.util.Hashtable;

public class P2ppBasicTestRules extends TestRules {

    public P2ppBasicTestRules() {
        super(new Hashtable<String, FieldRule>() {{
            put("nodeNumber", new FieldRule("nodeNumber", "Number of the node launched", Integer.class));
            put("port", new FieldRule("port", "Node's port", Integer.class));
            put("bootIP", new FieldRule("bootIP", "Bootstrap server's IP address", String.class));
            put("bootPort", new FieldRule("bootPort", "Bootstrap server's port", Integer.class));
            put("overlayId", new FieldRule("overlayId", "Overlay ID", String.class));
            put("transport", new FieldRule("transport", "{tcp|udp}", String.class));
            put("protocol", new FieldRule("protocol", "P2P protocol: {kademlia|superpeer}", String.class));
        }});
    }

}
