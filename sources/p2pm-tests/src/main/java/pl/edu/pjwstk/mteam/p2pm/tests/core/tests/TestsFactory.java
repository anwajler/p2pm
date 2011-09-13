package pl.edu.pjwstk.mteam.p2pm.tests.core.tests;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic.JCsyncBasicTest;
import pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.p2ppbasic.P2ppBasicTest;
import pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.pubsubbasic.PubsubBasicTest;

import java.util.Hashtable;
import java.util.Map;

public class TestsFactory {

    public static final Logger LOG = Logger.getLogger(TestsFactory.class);

    public static ITest getTestForName(String testName, final String[] args) {
        ITest test = null;

        int argsLength = args.length;

        if (P2ppBasicTest.isTestFromName(testName)) {

            int testArgsCount = P2ppBasicTest.getArgsCount();

            if (argsLength < testArgsCount+1) {
                LOG.error("P2ppBasicTest takes exactly " + testArgsCount + " arguments. " + (argsLength-1) + " given");
                return null;
            }

            Map<String, Object> kwargs = new Hashtable<String, Object>() {{
                put("nodeNumber", Integer.parseInt(args[1]));
                put("port", Integer.parseInt(args[2]));
                put("bootIP", args[3]);
                put("bootPort", Integer.parseInt(args[4]));
                put("overlayId", args[5]);
                put("transport", args[6]);
                put("protocol", args[7]);
            }};

            test = new P2ppBasicTest(kwargs);


        } else if (PubsubBasicTest.isTestFromName(testName)) {

            int testArgsCount = PubsubBasicTest.getArgsCount();

            if (argsLength < testArgsCount+1) {
                LOG.error("PubsubBasicTest takes exactly " + testArgsCount + " arguments. " + (argsLength-1) + " given");
                return null;
            }

            Map<String,Object> kwargs = new Hashtable<String,Object>() {{
                put("nodeNumber", Integer.parseInt(args[1]));
                put("port", Integer.parseInt(args[2]));
                put("bootIP", args[3]);
                put("bootPort", Integer.parseInt(args[4]));
            }};

            test = new PubsubBasicTest(kwargs);

        }
        else if(JCsyncBasicTest.isTestFromName(testName)){
            int testArgsCount = JCsyncBasicTest.getArgsCount();

            if (argsLength < testArgsCount+1) {
                LOG.error(testName + " takes exactly " + testArgsCount + " arguments. " + (argsLength-1) + " given");
                return null;
            }

            Map<String,Object> kwargs = new Hashtable<String,Object>() {{
                put("nodeNumber", Integer.parseInt(args[1]));
                put("port", Integer.parseInt(args[2]));
                put("bootIP", args[3]);
                put("bootPort", Integer.parseInt(args[4]));
            }};

            test = new JCsyncBasicTest(kwargs);
        }

        return test;
    }

}
