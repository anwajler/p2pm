package pl.edu.pjwstk.mteam.p2pm.tests.core.tests;

import pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic.JCsyncBasicTest;
import pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.p2ppbasic.P2ppBasicTest;
import pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.pubsubbasic.PubsubBasicTest;
import org.apache.log4j.Logger;


import java.util.Hashtable;
import java.util.Map;
import org.apache.log4j.Priority;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.net.SMTPAppender;
import static pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic.JCSyncTestRules.*;

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
            Logger logger = Logger.getRootLogger();
            RollingFileAppender rfa = (RollingFileAppender) logger.getAppender("R");
            rfa.setFile(args[14]);   
            rfa.activateOptions();
            System.setProperty("mail.smtp.port", "465");
            System.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            System.setProperty("mail.smtp.socketFactory.port", "465");
            System.setProperty("mail.smtp.socketFactory.fallback", "false");            
            SMTPAppender sa = (SMTPAppender) logger.getAppender("SMTP");
            //sa.setBufferSize(100);
            sa.setSubject(testName+":node"+args[1]);
            //sa.setThreshold(Priority.INFO);
            sa.activateOptions();
            LOG.error("Here i am (test message)");
            int testArgsCount = JCsyncBasicTest.getArgsCount();
            
            
            if (argsLength < testArgsCount+1) {
                LOG.error(testName + " takes exactly " + testArgsCount + " arguments. " + (argsLength-1) + " given");
                return null;
            }
            
            Map<String,Object> kwargs = new Hashtable<String,Object>() {{
                put(FIELD_NODE_NUMBER, Integer.parseInt(args[1]));
                put(FIELD_PORT, Integer.parseInt(args[2]));
                put(FIELD_BOOT_IP, args[3]);
                put(FIELD_BOOT_PORT, Integer.parseInt(args[4]));
                put(FIELD_TRANSPORT_PROTOCOL, args[5]);
                put(FIELD_PROTOCOL, args[6]);
                put(FIELD_WORKERS_COUNT,Integer.parseInt(args[7]));
                put(FIELD_WORKER, Boolean.parseBoolean(args[8]));
                put(FIELD_WORKER_OPERATION_COUNT,Integer.parseInt(args[9]));
                put(FIELD_OPERATION_DELAY,Long.parseLong(args[10]));
                put(FIELD_LAST_NODE,Boolean.parseBoolean(args[11]));
                put(FIELD_COLLECTION_NAME,args[12]);
                put(FIELD_TEST_IDENTIFIER,args[13]);
                put(FIELD_LOG_NAME,args[14]);
            }};

            test = new JCsyncBasicTest(kwargs);
        }

        return test;
    }

}
