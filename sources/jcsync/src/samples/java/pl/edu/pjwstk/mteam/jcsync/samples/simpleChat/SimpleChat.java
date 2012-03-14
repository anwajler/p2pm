package pl.edu.pjwstk.mteam.jcsync.samples.simpleChat;

import java.util.logging.Level;
import java.util.logging.Logger;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCore;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.util.JCSyncObservable;
import pl.edu.pjwstk.mteam.jcsync.core.implementation.util.SharedObservableObject;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectNotExistsException;
import pl.edu.pjwstk.mteam.jcsync.exception.OperationForbiddenException;
import pl.edu.pjwstk.mteam.jcsync.samples.utils.BootstrapServerRunner;
import pl.edu.pjwstk.mteam.p2p.P2PNode;

/**
 * 
 * @author Piotr Bucior
 */
public class SimpleChat {
    private BootstrapServerRunner bs;
    private JCSyncCore core1;
    private JCSyncCore core2;
    private JCSyncObservable obs_core1;
    private JCSyncObservable obs_core2;
    
    public SimpleChat(){
        initBootstrapServer(6060);
        initNode1(5050, 6060);
        initNode2(5055, 6060);
        snooze(1000);
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ChatWindow(getObservable(core1),"user1").setVisible(true);
            }
        });
        snooze(1000);
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ChatWindow(getObservable(core2),"user2").setVisible(true);
            }
        });
    }
    public static void main(String []args){
        new SimpleChat();
    }
    
    public JCSyncObservable getObservable(JCSyncCore coreAlg){
        String obs_id = "observable";
        SharedObservableObject soo = null;
        try {
            soo= new SharedObservableObject(obs_id, new JCSyncObservable(), coreAlg);
        } catch (ObjectExistsException ex) {
            try {
                soo = (SharedObservableObject) SharedObservableObject.getFromOverlay(obs_id, coreAlg);
            } catch (ObjectNotExistsException ex1) {
                Logger.getLogger(SimpleChat.class.getName()).log(Level.SEVERE, null, ex1);
            } catch (OperationForbiddenException ex1) {
                Logger.getLogger(SimpleChat.class.getName()).log(Level.SEVERE, null, ex1);
            } catch (Exception ex1) {
                Logger.getLogger(SimpleChat.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (Exception ex) {
            Logger.getLogger(SimpleChat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (JCSyncObservable) soo.getNucleusObject();        
    }
    
    private void initBootstrapServer(int i) {
        //creates simple bootstrap server
        this.bs = new BootstrapServerRunner(i);
        // run bs
        this.bs.start();
    }
    private void initNode1(int i, int bootPort) {
        P2PNode node1;
        node1 = new P2PNode(null, P2PNode.RoutingAlgorithm.SUPERPEER);
        node1.setServerReflexiveAddress("127.0.0.1");
        node1.setServerReflexivePort(bootPort);
        node1.setBootIP("127.0.0.1");
        node1.setBootPort(bootPort);
        node1.setUserName("user1");
        node1.setUdpPort(i);
        node1.networkJoin();
        
        //wait for connection
        while(!node1.isConnected()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(SimpleChat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //creates new jcsync core instance
        core1 = new JCSyncCore(node1, i+2);
        
        try {
            core1.init();
        } catch (Exception ex) {
            Logger.getLogger(SimpleChat.class.getName()).log(Level.SEVERE, null, ex);
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
        node2.setUserName("user2");
        node2.setUdpPort(i);
        node2.networkJoin();
        
        //wait for connection
        while(!node2.isConnected()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(SimpleChat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //creates new jcsync core instance
        core2 = new JCSyncCore(node2, i+2);
        
        try {
            core2.init();
        } catch (Exception ex) {
            Logger.getLogger(SimpleChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void snooze(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            Logger.getLogger(SimpleChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
