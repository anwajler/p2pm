package pl.edu.pjwstk.mteam.pubsub.tests.tests.pubsubbasic;

import pl.edu.pjwstk.mteam.tests.rules.FieldRule;
import pl.edu.pjwstk.mteam.tests.rules.TestRules;

import java.util.Hashtable;

public class PubsubBasicTestRules extends TestRules {

    public PubsubBasicTestRules() {
        super(new Hashtable<String, FieldRule>() {{
            put("nodeNumber", new FieldRule("nodeNumber", "Number of the node launched", Integer.class));
            put("port", new FieldRule("port", "Node's port", Integer.class));
            put("bootIP", new FieldRule("bootIP", "Bootstrap server's IP address", String.class));
            put("bootPort", new FieldRule("bootPort", "Bootstrap server's port", Integer.class));
        }});
    }

}
