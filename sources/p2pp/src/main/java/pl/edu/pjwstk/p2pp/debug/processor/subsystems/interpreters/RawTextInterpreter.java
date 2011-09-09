package pl.edu.pjwstk.p2pp.debug.processor.subsystems.interpreters;

import java.sql.Timestamp;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.debug.DebugFields;

import pl.edu.pjwstk.p2pp.debug.DebugInformation;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystemFactoryFunctor;

public class RawTextInterpreter extends SubSystem implements IInterpreter, SubSystemFactoryFunctor {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Constants
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Instance of org.apache.log4j.Logger
     */
    private static final Logger LOG = Logger.getLogger(RawTextInterpreter.class);

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Data members
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    @SuppressWarnings("unused")
	private String sourceDir;

    private LinkedBlockingQueue<String> requests;
    private LinkedBlockingQueue<DebugInformation> results;

    private boolean running = true;

    private static Object castField(Byte debugFieldIndex, String debugFieldValue) {
        Object result = null;

        String className = DebugFields.FIELD_TYPES.get(debugFieldIndex).getName();

        try {
            if ("java.lang.Boolean".equals(className)) {
                result = Boolean.parseBoolean(debugFieldValue);
            } else if ("java.lang.Byte".equals(className)) {
                result = Byte.parseByte(debugFieldValue);
            } else if ("java.lang.Integer".equals(className)) {
                result = Integer.parseInt(debugFieldValue);
            } else if ("java.lang.String".equals(className)) {
                result = debugFieldValue;
            } else if ("java.lang.Long".equals(className)) {
                result = Long.parseLong(debugFieldValue);
            } else if ("java.sql.Timestamp".equals(className)) {
                result = Timestamp.valueOf(debugFieldValue);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return result;
    }

    private static Object interpretRawField(String rawField) {
        Object result = "";
        int index3 = rawField.indexOf(":");
        byte debugFieldIndex = Byte.parseByte(rawField.substring(0, index3));
        String debugFieldValue_raw = rawField.substring(index3 + 1);
        if (!(debugFieldValue_raw.startsWith("") || debugFieldValue_raw.endsWith(""))) {
            return "";
        }
        if (debugFieldValue_raw.length() > 2) {
            result = castField(debugFieldIndex, debugFieldValue_raw.substring(1,
                    debugFieldValue_raw.length() - 1));
        }
        return result;
    }

    public static DebugInformation interpret(String request) {
        if (LOG.isDebugEnabled()) LOG.debug("Intepretting: "+request);
        if (null == request || "".equals(request)) {
            throw new IllegalArgumentException("Argument passed to RawTextInterpreter#interpret()" +
                    "cannot be null nor empty.");
        }
        if (!(request.startsWith("[") || request.endsWith("]"))) {
            throw new IllegalArgumentException("Improper request format: \""+request+"\"");
        }
        DebugInformation result = new DebugInformation();
        int index1 = 1;
        int index2 = request.indexOf(",");
        while (index2 >= 0) {
            String debugField = request.substring(index1, index2);
            int index3 = debugField.indexOf(":");
            byte debugFieldIndex = Byte.parseByte(debugField.substring(0, index3));
            Object debugFieldValue = interpretRawField(debugField);
            result.put(debugFieldIndex, debugFieldValue);
            index1 = index2 + 1;
            index2 = request.indexOf(",", index1);
        }
        if (index1 < request.length()-1) {
            String debugField = request.substring(index1, request.length()-1);
            int index3 = debugField.indexOf(":");
            byte debugFieldIndex = Byte.parseByte(debugField.substring(0, index3));
            Object debugFieldValue = interpretRawField(debugField);
            result.put(debugFieldIndex, debugFieldValue);
        }
        return result;
    }


    public SubSystem makeInstance() {
        if (!(_instance instanceof RawTextInterpreter)) {
            _instance = new RawTextInterpreter();
        }
        return _instance;
    }

    public void init(Object[] settings) {
        requests = new LinkedBlockingQueue<String>();
        results = new LinkedBlockingQueue<DebugInformation>();
    }

    public void halt() {
        this.running = false;
    }

    public void offerRequest(Object request) {
        if (!this.isAlive()) {
            LOG.warn("Trying to make stopped RawTextInterpreter interpret. Won't happen.");
            return;
        }
        if (!(request instanceof String)) {
            throw new IllegalArgumentException("First argument passed to RawTextInterpreter#offerRequest()" +
                    " must be an instance of String.");
        }
        requests.offer(request.toString());
    }

    public DebugInformation takeResult() {
        if (!this.isAlive()) {
            LOG.warn("Trying to get result from a stopped RawTextInterpreter. Won't happen.");
            return null;
        }
        try {
            return results.take();
        } catch (InterruptedException e) {
            LOG.error("Error while executing RawTextInterpreter#takeResult()");
            return null;
        }
    }

    public DebugInformation takeResult(long timeout) {
        if (!this.isAlive()) {
            LOG.warn("Trying to get result from a stopped RawTextInterpreter. Won't happen.");
            return null;
        }
        try {
            return results.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.error("Error while executing RawTextInterpreter#takeResult()");
            return null;
        }
    }

    @Override
    public void run() {
        LOG.debug("RawTextInterpreter started");

        if (requests == null) {
            LOG.error("Trying to run uninitialized RawTextInterpreter. Stopping interpreter.");
            return;
        }

        while (this.running) {
            try {
                String request = requests.poll(100, TimeUnit.MILLISECONDS);
                if (null == request) continue;
                DebugInformation result = interpret(request);
                results.offer(result);
            } catch (InterruptedException e) {
                LOG.error("This should not happen", e);
            }
        }

    }

}
