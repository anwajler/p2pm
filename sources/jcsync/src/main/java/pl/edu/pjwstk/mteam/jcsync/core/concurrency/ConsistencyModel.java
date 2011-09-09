package pl.edu.pjwstk.mteam.jcsync.core.concurrency;

import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

/**
 * @author Piotr Bucior
 * @version 1.0
 */
public abstract class ConsistencyModel {
        protected static final Logger log = Logger.getLogger("pl.edu.pjwstk.mteam.jcsync.core.EventLocker");
        public static final boolean TRUE = true;
        public static final boolean FALSE = false;        

        protected ConsistencyModel(){
        
	}
	public void finalize() throws Throwable {

	}
	/**
	 * 
	 * @param operation
	 */
	//public abstract void lockRead(String collID, int transID);

	/**
	 * 
	 * @param operation
	 */
	public abstract Object waitForWriteResults(String collID, int transID);
        public abstract boolean onDeliverWriteResults(String collID, int transID, int respCode, Object retVal);
        public abstract void registerEventInvoker(String collectionID, EventInvoker invoker);
        public abstract void removeEventInvoker(String collectionID);
        public abstract void pauseEventInvoker(String collID);
        public abstract void runEventInvoker(String collID);
        public abstract EventInvoker getEventInvoker(String collID);
        public abstract void acquireReadAccess(String collID, Object elementIdentifier);
        public abstract void acquireWriteAccess(String collID, Object elementIdentifier);
        public abstract void releaseReadAccess(String collID, Object elementIdentifier);
        public abstract void releaseWriteAccess(String collID, Object elementIdentifier);
        public abstract void preInitWaitForWriteResults(String id, int transID);
        //public abstract ConsistencyModel newInstance();
        //public abstract void unlockRead(String collID, int transID, int respCode);
        //public abstract int lockCreateCollection(String collID,int transID);
        //public abstract void unlockCreateCollection(String collID,int transID,int respCode);
        //public abstract int lockCreateTopic(String collID, int transID);
        //public abstract void unlockCreateTopic(String collID,int transID, int respCode);
        //public abstract void initCreateTopicLocker(String id,int transID);
       // public abstract void initCreateCollectionLocker(String id,int transID);
        

    
        protected class Key{
            public String id;
            public int transID;
            public Key(String id, int transID){
                this.id = id;
                this.transID = transID;
            }

        @Override
        public boolean equals(Object obj) {
            Key b = (Key) obj;
            if(this.id.equals(b.id)){
                if(this.transID== b.transID) return true;
            }
            return false;
        }

        @Override
        protected void finalize() throws Throwable {
            this.id = null;
            super.finalize();
        }


        @Override
        public int hashCode() {
            return ((String)(id+transID)).hashCode();
        }

        @Override
        public String toString() {
            return "collID: "+this.id+", transID: "+this.transID;
        }


        }
        protected class Locker{
            public Semaphore semaphore;
            public int respCode;
            public Object retVal = null;
            public Locker(Semaphore sem, int resp){
                this.semaphore = sem;
                this.respCode = resp;
            }
            public void setRespCode(int resp){
                this.respCode = resp;
            }
            public void setRetVal(Object ret){
                this.retVal = ret;
            }

        @Override
        protected void finalize() throws Throwable {
            this.semaphore = null;
            this.retVal = null;
            super.finalize();
        }

        }

}//end EventLocker