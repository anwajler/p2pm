package pl.edu.pjwstk.mteam.p2pm.tests.tests;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import pl.edu.pjwstk.mteam.p2pm.tests.core.tests.ITest;
import pl.edu.pjwstk.mteam.p2pm.tests.core.tests.TestsFactory;

public class Main {

    public static final Logger LOG = Logger.getLogger(Main.class);

    public static void main(final String[] args) {

        if (args.length == 1) {
            LOG.error("Name of the test must be specified as the first argument");
            System.exit(2);
        }

        PropertyConfigurator.configureAndWatch("./log4j.properties", 1000);

        String testName = args[0];

        ITest test = TestsFactory.getTestForName(testName, args);

        if (test != null) {

            LOG.info(test.test());

        } else {

            LOG.info("Unsupported test: " + testName);

        }

    }
}
