
package pl.edu.pjwstk.p2pp.debug.processor.subsystems.readers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

import pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystem;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystemFactoryFunctor;

public class RawTextReader extends SubSystem implements IReader, SubSystemFactoryFunctor {

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
    private static final Logger LOG = Logger.getLogger(RawTextReader.class);

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Data members
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private String sourceDir;

    private LinkedBlockingQueue<String> records;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Methods
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private LinkedList<String> readFileToList(File file) {
        LinkedList<String> result = new LinkedList<String>();

        FileInputStream in = null;
        String fileString = "";

        try {
            int size = (int) file.length();
            if (size > 0) {
                byte[] buffer = new byte[size];
                in = new FileInputStream(file);
                in.read(buffer, 0, size);
                fileString = new String(buffer, 0, size, "ISO8859_1");
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.error("This should not happen", e);
                }
            }

            if (!"".equals(fileString)) {
                StringTokenizer st = new StringTokenizer(fileString, "\n");
                while (st.hasMoreTokens()) {
                    result.add(st.nextToken());
                }
            }
        }
        return result;
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
            throw new IllegalArgumentException("Argument passed to RawTextReader#init() cannot be null");
        }
        if (settings.length != 1) {
            throw new IllegalArgumentException("Argument passed to RawTextReader#init() must be an" +
                    " Object array consisting of a path to source directory.");
        }
        if (null == settings[0] || "".equals(settings[0]) || !(settings[0] instanceof String)) {
            throw new IllegalArgumentException("First field in an argument passed to RawTextReader#init()" +
                    " must a non-empty String");
        }
        this.sourceDir = settings[0].toString();
        this.records = new LinkedBlockingQueue<String>();
    }

    public Object takeRecord() {
        if (null == records) {
            LOG.warn("Trying to get record from uninitialized RawTextReader. Won't happen.");
            return null;
        }
        try {
            return records.take();
        } catch (InterruptedException e) {
            LOG.error("Error while executing RawTextReader#takeRecord()");
            return null;
        }
    }

    public Object takeRecord(long timeout) {
        if (null == records) {
            LOG.warn("Trying to get record from uninitialized RawTextReader. Won't happen.");
            return null;
        }
        try {
            return records.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.error("Error while executing RawTextReader#takeRecord()");
            return null;
        }
    }

    public SubSystem makeInstance() {
        if (!(_instance instanceof RawTextReader)) {
            _instance = new RawTextReader();
        }
        return _instance;
    }

    @Override
    public void run() {
        if (LOG.isDebugEnabled()) LOG.debug("RawTextReader started");

        File sourceDir = new File(this.sourceDir);
        if (!(sourceDir.exists() || sourceDir.isDirectory())) {
            LOG.error("Source directory given at RawTextReader#init() doesn't exist. Stopping.");
            return;
        }

        File[] logList = sourceDir.listFiles(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return (name.endsWith(".log"));
            }
        });

        for (File f: logList) {
            if (LOG.isDebugEnabled()) LOG.debug("Reading: "+f.getName());
            LinkedList<String> records = readFileToList(f);
            for (String record: records) {
                if (LOG.isDebugEnabled()) LOG.debug("Read: "+record);
                this.records.offer(record);
            }
        }

    }
}
