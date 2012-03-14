package pl.edu.pjwstk.mteam.jcsync.samples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCore;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.JCSyncArrayList;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.JCSyncHashMap;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.JCSyncTreeMap;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.SharedCollectionObject;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectNotExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.OperationForbiddenException;
import pl.edu.pjwstk.mteam.jcsync.samples.utils.BootstrapServerRunner;
import pl.edu.pjwstk.mteam.p2p.P2PNode;

/**
 * Sample class, that shows how to use implemented collections.
 * @author Piotr Bucior
 */
public class BasicCollectionUsage {
    
    private BootstrapServerRunner bs;
    private JCSyncCore core1;
    private JCSyncCore core2;
    
    
    public BasicCollectionUsage(){
        // first of all - run bootstrap and creates two jcsync instances
        initBootstrapServer(7080);
        // first node that will be used in this sample
        initNode1(6060,7080);
        // 2nd node
        initNode2(6070,7080);
        
        //creates (blank) new hashmap with identifier "testMap"
        String collID = "testMap";
        try {
            JCSyncHashMap hs_core1 = createHashMap(collID,this.core1);
        } catch (ObjectExistsException ex) {
            System.out.println("Ooops, collection with given name already exists.");
        } catch (Exception ex) {
            System.out.println("Ooops, an error occurred.");
        }
        // wait for data propagation
        snooze(500);
        
        JCSyncHashMap hs_core2;
        //try to create the same collection on node 2
        try {            
            hs_core2 = createHashMap(collID,this.core2);
            // it will throw ObjectExistsException (code below)
        } catch (ObjectExistsException ex) {
            try {
                // in this case, try to subscribe with this collection
                hs_core2 = (JCSyncHashMap) subscribeCollection(collID, core2).getNucleusObject();
            } catch (ObjectNotExistsException ex1) {
                System.out.println("Ooops, collection not exists.");
            } catch (OperationForbiddenException ex1) {
                System.out.println("Ooops, you cannot subscribe to this collection.");
            } catch (Exception ex1) {
                System.out.println("Ooops, an error occurred.");
            }
        } catch (Exception ex) {
            System.out.println("Ooops, an error occurred.");
        }
        // wait for data propagation
        snooze(500);
        
        // now we do own stuff on the collection, e.g. adding, removing, editing elements
        
        
    }
    
    /**
     * Method body shows how to create new blank collection instance
     * @param testMap collection identifier in overlay.
     * @return created collection
     */
    private JCSyncHashMap createHashMap(String testMap, JCSyncCore coreAlg) throws ObjectExistsException, Exception {
        // create collection instance
        JCSyncHashMap map = new JCSyncHashMap();
        // we need create an SharedCollectionObject, that will be assigned with our collection.
        SharedCollectionObject so_1 = new SharedCollectionObject(testMap, map, coreAlg);
        return map;        
    }
    
    private JCSyncArrayList<String> createArrayList(String testMap, JCSyncCore coreAlg, ArrayList<String> initValues) throws ObjectExistsException, Exception {
        // create collection instance
        JCSyncArrayList<String> arr = new JCSyncArrayList<String>(initValues);
        // we need create an SharedCollectionObject, that will be assigned with our collection.
        SharedCollectionObject so_1 = new SharedCollectionObject(testMap, arr, coreAlg);
        return (JCSyncArrayList<String>) so_1.getNucleusObject();       
    }
    
    private JCSyncTreeMap createTreeMap(String testMap, JCSyncCore coreAlg) throws ObjectExistsException, Exception {
        // create collection instance
        JCSyncTreeMap map = new JCSyncTreeMap();
        // we need create an SharedCollectionObject, that will be assigned with our collection.
        SharedCollectionObject so_1 = new SharedCollectionObject(testMap, map, coreAlg);
        return (JCSyncTreeMap) so_1.getNucleusObject();       
    }
    
    /**
     * Method body shows how to create new collection instance with already stored some data
     * @param testMap collection identifier in overlay.
     * @return created collection
     */
    private JCSyncHashMap createHashMap(String testMap, JCSyncCore coreAlg, HashMap coreMap) throws ObjectExistsException, Exception {
        // create collection instance
        JCSyncHashMap map = new JCSyncHashMap(coreMap);
        // we need create an SharedCollectionObject, that will be assigned with our collection.
        SharedCollectionObject so_1 = new SharedCollectionObject(testMap, map, coreAlg);
        return map;        
    }
    
    
    private SharedCollectionObject subscribeCollection(String collectionName, JCSyncCore coreAlg) 
            throws ObjectNotExistsException, OperationForbiddenException, Exception {
                
        SharedCollectionObject so = 
                (SharedCollectionObject) SharedCollectionObject.getFromOverlay(collectionName, coreAlg);
        return so;      
    }
    /**
     * initialise bootstrap server
     * @param i port
     */
    private void initBootstrapServer(int i) {
        //creates simple bootstrap server
        this.bs = new BootstrapServerRunner(i);
        // run bs
        this.bs.start();
    }
    /**
     * init first node and jcsync instance 
     * @param i port
     * @param bootPort bootstrap server port
     */
    private void initNode1(int i, int bootPort) {
        P2PNode node1;
        node1 = new P2PNode(null, P2PNode.RoutingAlgorithm.SUPERPEER);
        node1.setServerReflexiveAddress("127.0.0.1");
        node1.setServerReflexivePort(bootPort);
        node1.setBootIP("127.0.0.1");
        node1.setBootPort(bootPort);
        node1.setUserName("node1");
        node1.setUdpPort(i);
        node1.networkJoin();
        
        //wait for connection
        while(!node1.isConnected()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(BasicCollectionUsage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //creates new jcsync core instance
        core1 = new JCSyncCore(node1, i+2);
        
        try {
            core1.init();
        } catch (Exception ex) {
            Logger.getLogger(BasicCollectionUsage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * init first node and jcsync instance 
     * @param i port
     */
    private void initNode2(int i, int bootPort) {
        P2PNode node2;
        node2 = new P2PNode(null, P2PNode.RoutingAlgorithm.SUPERPEER);
        node2.setServerReflexiveAddress("127.0.0.1");
        node2.setServerReflexivePort(bootPort);
        node2.setBootIP("127.0.0.1");
        node2.setBootPort(bootPort);
        node2.setUserName("node1");
        node2.setUdpPort(i);
        node2.networkJoin();
        
        //wait for connection
        while(!node2.isConnected()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(BasicCollectionUsage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //creates new jcsync core instance
        core2 = new JCSyncCore(node2, i+2);
        
        try {
            core2.init();
        } catch (Exception ex) {
            Logger.getLogger(BasicCollectionUsage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void snooze(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            Logger.getLogger(BasicCollectionUsage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
