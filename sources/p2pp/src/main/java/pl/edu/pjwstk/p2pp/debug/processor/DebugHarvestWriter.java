package pl.edu.pjwstk.p2pp.debug.processor;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.debug.DebugInformation;

import pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystemFactoryFunctor;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.SubSystemWrapper;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.interpreters.IInterpreter;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.interpreters.RawTextInterpreter;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.readers.IReader;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.readers.RawTextReader;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.writers.BDSWriter;
import pl.edu.pjwstk.p2pp.debug.processor.subsystems.writers.IWriter;

/**
 * Provides an application writing debug information collected from P2PP entities. Used for development
 * testing discipline only.
 *
 * Run with:
 * java -classpath lib/commons-cli-1.1.jar:lib/commons-dbcp-1.2.2.jar:lib/commons-pool-1.5.4.jar:\
 * lib/log4j-1.2.15.jar:lib/mysql-connector-java-5.1.10-bin.jar:.:jar/p2pp.jar\
 * pl.edu.pjwstk.p2pp.debug.processor.DebugHarvestWriter [here_goes_dir_with_logs]
 *
 * Work in progress.
 *
 * @author Konrad Adamczyk conrad.adamczyk@gmail.com
 */

public final class DebugHarvestWriter extends Thread {

    private static final Logger LOG = Logger.getLogger(DebugHarvestWriter.class);

    private IInterpreter interpreter;
    private IReader reader;
    private IWriter writer;

    private final Thread proxyReaderToInterpreter = new Thread() {
        @Override public void run() {
            while (true) {
                Object record = reader.takeRecord(1000);
                if (null == record) return;
                interpreter.offerRequest(record);
            }
        }
    };
    private final Thread proxyInterpreterToWriter = new Thread() {
        @Override public void run() {
            while (true) {
                DebugInformation result = interpreter.takeResult(1000);
                if (null == result) {
                    interpreter.halt();
                    if (!writer.hasRequests()) {
                        writer.halt();
                        return;
                    }
                } else {
                    writer.writeDebugInformation(result);
                }
            }
        }
    };

    private String sourceDir;


    public DebugHarvestWriter(String sourceDir) {

        if (null == sourceDir || "".equals(sourceDir)) {
            throw new IllegalArgumentException("Argument passed to DebugHarvestWriter constructor cannot be null nor empty.");
        }

        this.sourceDir = sourceDir;
        
        SubSystemFactoryFunctor subSystemFactory = new RawTextInterpreter();
        SubSystemWrapper.setFactory(subSystemFactory);
        interpreter = (IInterpreter) SubSystemWrapper.instance();

        subSystemFactory = new RawTextReader();
        SubSystemWrapper.setFactory(subSystemFactory);
        SubSystemWrapper.setInstance(null);
        reader = (IReader) SubSystemWrapper.instance();

        subSystemFactory = new BDSWriter();
        SubSystemWrapper.setFactory(subSystemFactory);
        SubSystemWrapper.setInstance(null);
        writer = (IWriter) SubSystemWrapper.instance();
        
    }

    public void init() {
        interpreter.init(null);
        reader.init(new Object[]{this.sourceDir});
        // TODO properties
        writer.init(new Object[]{
                    "root",
                    "test123",
                    "jdbc:mysql://localhost:3306/debug?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8",
                    128});
    }
    
    @Override
    public void run() {
        LOG.info("DebugHarvestWriter started");

        interpreter.start();
        reader.start();
        writer.start();

        proxyReaderToInterpreter.start();
        proxyInterpreterToWriter.start();

    }

     public static void main(String[] args) {

         if (args.length != 1) {
             LOG.error("Usage: DebugHarvestWriter [ SOURCE_DIR ]");
             return;
         }

         DebugHarvestWriter dhw = new DebugHarvestWriter(args[0]);
         dhw.init();
         dhw.start();

     }

}