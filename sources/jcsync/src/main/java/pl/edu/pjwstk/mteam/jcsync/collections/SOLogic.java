package pl.edu.pjwstk.mteam.jcsync.collections;

import java.io.Serializable;
import java.util.Observable;

/**
 * The "Shared-Object Logic" class provide features which are needed to
 * proper work in overlay notification about the changes inside all of them.
 * <b>All of the collection objects (keys, values) must inherit this class if that motion is required.</b>
 *
 * 
 * @author Piotr Bucior
 * @version 1.0
 */
public abstract class SOLogic extends Observable implements Serializable {
    /**
     *  This method <b>must be invoked mandatory every time</b>
     *  when the collection's key or value was modified.<p>
     *  See example below:<br>
     *  <code>
     *  <pre>
     *  public void increaseSalary(ArrayList employees, double percent){
     *    for ( Iterator<SOL> emps = employees.iterator(); emps.hasNext(); ){
     *       SOL e = emps.next();
     *       ((Employee)e).increaseSalary(percent);
     *       e.notifyChande(); //MANDATORY
     *    }
     *  }
     *  </pre>
     *  </code>
     */
    public void notifyChange(){
    // TODO finish and test it
        setChanged();
        notifyObservers(null);
    }
}
