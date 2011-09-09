package pl.edu.pjwstk.p2pp.debug.processor.subsystems.writers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.debug.DebugInformation;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystemFactoryFunctor;

/**
 * Writer subsystem saving given debug information in a specified text file.
 *
 * @see pl.edu.pjwstk.p2pp.debug.processor.DebugWriter
 *
 * @author Konrad Adamczyk conrad.adamczyk@gmail.com
 */

public class RawTextWriter extends SubSystem implements IWriter, SubSystemFactoryFunctor {

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
    private static final Logger LOG = Logger.getLogger(RawTextWriter.class);

    /**
     * Option indicating RawTextWriter's append write mode.
     */
    public static final byte APPEND = 0;

    /**
     * Option indicating RawTextWriter's overwrite write mode.
     */
    public static final byte OVERWRITE = 1;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Data members
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Path to a file where debug information should be written.
     */
    private String target;

    /**
     * RawTextWriter write mode.
     */
    private byte mode;

    /**
     * Buffer where debug information to be written are stored. The size of the buffer is defined in
     * settings passed to the {@link #init(java.lang.Object[])} method.
     *
     * @see pl.edu.pjwstk.p2pp.debug.processor.DebugWriter#options
     */
    private LinkedBlockingQueue<DebugInformation> buffer;

    private boolean running = true;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Constructors
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Constructs a new RawTextWriter.
     */
    public RawTextWriter() {}

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Methods
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     *
     * @return printStream
     */
    private PrintStream createPrintStream() {
        PrintStream printStream = null;
        try {
            File output = new File(target);
            if (!output.exists()) {
                if (!output.createNewFile()) LOG.error("Error while creating new file: " + output);
            } else if (mode == OVERWRITE) {
                if (!output.delete()) LOG.error("Error while deleting file: " + output);
                if (!output.createNewFile()) LOG.error("Error while creating new file: " + output);
            }
            FileOutputStream fos = new FileOutputStream(target, true);
            printStream = new PrintStream(fos);
        } catch (IOException e) {
            LOG.error("This should not happen", e);
            return null;
        }
        return printStream;
    }

    /**
     * 
     * @param ps
     * @param debugInfo
     * @throws IOException
     */
    private void writeDebugInformationActual(PrintStream ps, DebugInformation debugInfo) throws IOException {
        ps.println(debugInfo.toString());
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Interface
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public void init(Object[] settings) {
        if (null == settings) {
            throw new IllegalArgumentException("Argument passed to "+this.getClass().getSimpleName()+
                    "#init() cannot be null");
        }
        if (settings.length != 3) {
            throw new IllegalArgumentException("Argument passed to "+this.getClass().getSimpleName()+
                    "#init() must be an Object array consisting of a target file path, a write mode" +
                    " and a buffer size");
        }
        if (null == settings[0] || "".equals(settings[0])) {
            throw new IllegalArgumentException("First field in an argument passed to "
                    +this.getClass().getSimpleName()+"#init() cannot be null nor empty");
        }
        if (!(APPEND == (Byte)settings[1] || OVERWRITE == (Byte)settings[1])) {
            throw new IllegalArgumentException("Improper write mode passed in a second field of " +
                    "RawTextWriter#init() argument");
        }
        if (null == settings[2]) {
            throw new IllegalArgumentException("Third field in an argument passed to "
                    +this.getClass().getSimpleName()+" cannot be null");
        }
        target = (String) settings[0];
        mode = (Byte) settings[1];
        buffer = new LinkedBlockingQueue<DebugInformation>((Integer)settings[2]);
        if (LOG.isDebugEnabled()) LOG.debug(this.getClass().getSimpleName()+" initialized");
    }

    public void halt() {
        this.running = false;
    }

    public boolean hasRequests() {
        Object tmp = null;
        try {
            tmp = this.buffer.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("This should not happen", e);
        }
        return null != tmp;
    }
    
    public void writeDebugInformation(DebugInformation debugInfo) {
        if (!this.isAlive()) {
            LOG.warn("Trying to make stopped RawTextWriter write. Won't happen.");
            return;
        }
        try {
            buffer.offer(debugInfo, 200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.warn("Buffer in "+this.getClass().getSimpleName()+" is full. Failed to write debug information.");
        }
    }


    public SubSystem makeInstance() {
        if (!(_instance instanceof RawTextWriter)) {
            _instance = new RawTextWriter();
        }
        return _instance;
    }

    @Override
    public void run() {
        if (LOG.isDebugEnabled()) LOG.debug("RawTextWriter started");
        
        if (buffer == null) {
            LOG.error("Trying to run an uninitialized RawTextWriter");
            return;
        }

        PrintStream printStream = createPrintStream();
        if (printStream == null) {
            LOG.error("RawTextWriter could not write to the given " + target + " file. Stopping writer.");
            return;
        }

        while (this.running) {
            /* 
             * Synchronizing in order to obtain a bulk of data  representing whole buffer instead of
             * polling (taking) one record every loop run. The idea is to reduce output operations.
             */
            synchronized (buffer) {

            	Object[] bufferArray = buffer.toArray();
                for (Object debugInfo: bufferArray) {
                    try {
                        writeDebugInformationActual(printStream, (DebugInformation) debugInfo);
                    } catch (IOException e) {
                        LOG.error("RawTextWriter could not write to the given " + target + "file. " +
                                " Stopping writer.");
                        return;
                    }
                }
                buffer.clear();
            }

            synchronized (this) {
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    LOG.error("This should not happen", e);
                }
            }
        }
    }

}
