package pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic;

import java.util.Observable;
import java.util.Observer;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.util.JCSyncObservable;
import pl.edu.pjwstk.mteam.p2pm.tests.core.events.EventManager;
import pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic.JCsyncBasicTest;

/**
 *
 * @author Piotr Bucior
 */
public class JCSyncBasicTestCollectionListener implements Observer {

    JCSyncObservable observable;
    public JCSyncBasicTestCollectionListener(JCSyncObservable obs){
        this.observable = obs;
    }
    @Override
    public void update(Observable o, Object arg) {
        if(o.equals(this.observable)){
            String args_ = (String)arg;
            if(args_.compareToIgnoreCase("last_node_connected")==0)
            EventManager.getInstance().addEventToQueue(JCsyncBasicTest.EVENT_LAST_NODE_CONNECTED, arg);
            else if(args_.startsWith("worker_finished")){
                EventManager.getInstance().addEventToQueue(JCsyncBasicTest.EVENT_WORKER_FINISHED, arg);
            }                
        }
    }
  
    
}