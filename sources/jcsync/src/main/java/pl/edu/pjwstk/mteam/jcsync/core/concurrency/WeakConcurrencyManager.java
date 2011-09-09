package pl.edu.pjwstk.mteam.jcsync.core.concurrency;
import java.util.WeakHashMap;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;

/**
 * @author Piotr Bucior
 * @version 1.0
 */
public class WeakConcurrencyManager extends ConsistencyModel {

        private static final WeakHashMap<String, Semaphore> constructorLockers = new WeakHashMap<String, Semaphore>(50);
        
        private WeakConcurrencyManager(){

	}
        

	public void finalize() throws Throwable {
		super.finalize();
	}

//    @Override
//    public void lockRead(String collID, int transID) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    @Override
    public Object waitForWriteResults(String collID, int transID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


//    @Override
//    public void unlockRead(String collID, int transID, int respCode) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public int lockCreateCollection(String collID, int transID) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public void unlockCreateCollection(String collID, int transID, int respCode) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public int lockCreateTopic(String collID, int transID) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public void unlockCreateTopic(String collID, int transID, int respCode) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public void initCreateTopicLocker(String id, int transID) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public void initCreateCollectionLocker(String id, int transID) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    @Override
    public void preInitWaitForWriteResults(String id, int transID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean onDeliverWriteResults(String collID, int transID, int respCode, Object retVal) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void registerEventInvoker(String collectionID, EventInvoker invoker) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EventInvoker getEventInvoker(String collID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeEventInvoker(String collectionID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void pauseEventInvoker(String collID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void runEventInvoker(String collID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void acquireReadAccess(String collID, Object elementIdentifier) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void acquireWriteAccess(String collID, Object elementIdentifier) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void releaseReadAccess(String collID, Object elementIdentifier) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void releaseWriteAccess(String collID, Object elementIdentifier) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}//end WeakConcurrencyManager