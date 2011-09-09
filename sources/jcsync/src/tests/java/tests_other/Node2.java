package tests_other;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.core.NetworkObject;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.core.NodeCallback;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncCollectionStateListener;
import pl.edu.pjwstk.mteam.jcsync.collections.implementation.AbstractCollectionsManager;
import pl.edu.pjwstk.mteam.jcsync.collections.implementation.JCSyncHashMap;
import pl.edu.pjwstk.mteam.jcsync.collections.implementation.JCSyncVector;
import pl.edu.pjwstk.mteam.jcsync.core.JCSyncCoreAlgorithm;
import pl.edu.pjwstk.mteam.jcsync.core.concurrency.EventInvoker;
import pl.edu.pjwstk.mteam.jcsync.exception.CollectionExistException;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncWriteMethod;
import pl.edu.pjwstk.mteam.jcsync.lang.reflect.Parameter;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.p2p.P2PNode.RoutingAlgorithm;

/**
 *
 * @author Piotr Bucior
 */
public class Node2 {

    public static Logger log = Logger.getLogger("mainTest");
    public final JCSyncCoreAlgorithm core;
    public JCSyncHashMap coll1;
    public P2PNode p2pnode;
    public Node2(String[] args) {
        log.trace("initializing ...");
        p2pnode = new P2PNode(new NodeCallback() {

            public void onDisconnect(Node node) {
               // throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onUserLookup(Node node, Object userInfo) {
               // throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onObjectLookup(Node node, Object object) {
               // throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onTopicNotify(Node node, Object topicID, byte[] message, boolean historical) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onTopicCreate(Node node, Object topicID) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onTopicCreate(Node node, Object topicID, int transID) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onTopicRemove(Node node, Object topicID) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onTopicSubscribe(Node node, Object topicID) {
             //   throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onTopicSubscribe(Node node, Object topicID, int transID) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onTopicUnsubscribe(Node node, Object topicID) {
             //   throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onInsertObject(Node node, NetworkObject object) {
             //   throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onJoin(Node node) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onOverlayError(Node node, Object sourceID, int errorCode) {
             //   throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onOverlayError(Node node, Object sourceID, int errorCode, int transID) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onPubSubError(Node node, Object topicID, byte operationType, int errorCode) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onPubSubError(Node node, Object topicID, byte operationType, int errorCode, int transID) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean onDeliverRequest(List<NetworkObject> objectList) {
              //  throw new UnsupportedOperationException("Not supported yet.");
                return true;
            }

            public boolean onForwardingRequest(List<NetworkObject> objectList) {
              //  throw new UnsupportedOperationException("Not supported yet.");
               return true;
            }
        }, RoutingAlgorithm.SUPERPEER);
        p2pnode.setServerReflexiveAddress("127.0.0.1");
		p2pnode.setServerReflexivePort(7080);
		p2pnode.setBootIP("127.0.0.1");
		p2pnode.setBootPort(7080);
		p2pnode.setUserName("zzz");
		p2pnode.setTcpPort(6095);
		//p2pnode.setTcpPort(8090);
		//new CoreAlgorithm(p2pnode.getTcpPort()+1, p2pnode);
                JCSyncCoreAlgorithm.init(p2pnode, p2pnode.getTcpPort()+1);
                p2pnode.networkJoin();
        try {
            Thread.sleep(2500);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(Node2.class.getName()).log(Level.SEVERE, null, ex);
        }
		core = JCSyncCoreAlgorithm.getInstance();
//        this.core = new JCSyncCoreAlgorithm(args[0], Integer.parseInt(args[1]),
//                Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]),
//                args[5], Integer.parseInt(args[6]), Integer.parseInt(args[7]));
        //run();
        makeCollections();
    }

    public static void main(String[] args) {
//        if (args.length != 8) {
//            log.error("Invalid arguments count, please specify all argumenst:\n"
//                    + "[node_name],[tcp_port],[udp_port],[server_reflexive_address],[server_reflexive_port],[boot_ip],[boot_port],[pub-sub_port]");
//        } else {
            new Node2(args);
       // }
    }

    private void run() {
        try {
            log.trace("starting layer ...");
            this.core.joinToNetwork();
            Thread.sleep(2050);
        } catch (InterruptedException ex) {
        }
    }
public static String help = "?";
    public static String coll_toString = "dump";
    public static String demo = "demo";
    public static String remove = "remove";
    public void makeCollections() {
        final CollectionLogger logger = new CollectionLogger();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                logger.setVisible(true);
            }
            });
        try {
            try {
                /**
                 * below request new JCSyncHashMap with mapped constructor from HashMap(int initialCapacity, float loadFactor) - Parameter object marks these attributes
                 * HashMap m = new HashMap(10,100) ==>> JCSyncHashMap m = requestCreateCollection(JCSyncHashMap.class, "hashMap1", new Parameter(int.class, 10),new Parameter(float.class, 100));
                 * warning never use Integer.class instead int.class - its different
                 */
                this.coll1 = (JCSyncHashMap) AbstractCollectionsManager.getInstance().requestCreateCollection(JCSyncHashMap.class, "map/hashMap1",EventInvoker.InvokerType.QUEUED, null);
                this.coll1 = (JCSyncHashMap) AbstractCollectionsManager.getInstance().requestCreateCollection(JCSyncHashMap.class, "presence/hashMap1",EventInvoker.InvokerType.QUEUED, new Parameter(int.class, 10), new Parameter(float.class, 100));
                this.coll1 = (JCSyncHashMap) AbstractCollectionsManager.getInstance().requestCreateCollection(JCSyncHashMap.class, "chat/hashMap1",EventInvoker.InvokerType.QUEUED, new Parameter(int.class, 10), new Parameter(float.class, 100));
            } catch (CollectionExistException ex) {
                ex.printStackTrace();
            }
            this.coll1.addStateListener(new JCSyncCollectionStateListener() {

                public void onRemoteStateUpdated(JCSyncAbstractCollection collection, JCSyncMethod details) {
                    logger.addInfo("************ ON REMOTE UPDATE: \noperationID: "+coll1.getCurrentOperationID()+"\nDetails:"
                            +((JCSyncWriteMethod)details).toString()+"\n");

                }

                public void onLocalStateUpdated(JCSyncAbstractCollection collection, JCSyncMethod details) {
                    logger.addInfo("************ ON LOCAL  UPDATE: \noperationID: "+coll1.getCurrentOperationID()+"\nDetails:"
                            +((JCSyncWriteMethod)details).toString()+"\n");
                    if(details.getType()==JCSyncMethod.WRITE_OPERATION){
                        String methodName = ((JCSyncWriteMethod)details).getGenericMethodName();
                        Parameter[] params = ((JCSyncWriteMethod)details).getParameters();
                        String param = "params:";

                        for (int i = 0; i < params.length; i++) {
                            param = param+"\t\n-"+params[i].type.toString()+" : "+params[i].value.toString();

                        }
                        System.out.println("**** ON_LOCAL_UPDATE:\n methodName: "+methodName+"\n"+param);
                        logger.addInfo(help);
                    }

                }
            });
            //this.core.DEBUG_Subscribe("hashMap1");
            Thread.sleep(1500);

            this.core.DEBUG_publish("hashMap1", "test_message_node2".getBytes());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String msg = "";
            String k1 = "klucz1";
            while (true) {
                try {
                    msg = br.readLine();
                } catch (IOException ex) {
                }
                //this.core.DEBUG_publish("hashMap1", msg.getBytes());
                if (msg.compareTo(help) == 0) {
                    System.out.println("HELP: ?\nSHOW COLLECTION: show\nDEMO: demo");
                } else if (msg.compareTo(coll_toString) == 0) {
                    System.out.println("Collection size: "+this.coll1.keySet().size());
                    System.out.println(this.coll1.toString());
                } else if((msg.compareTo(demo) == 0)){
                    for (int i = 0; i < 100; i++) {
                        this.coll1.put(System.currentTimeMillis(), msg.toLowerCase()+""+i);
                    }
                } else if((msg.compareTo(remove) == 0)){
                        this.coll1.remove(k1);
                }else{
                    this.coll1.put(k1, msg);
                }
            }
            //Object z =  this.coll1.put(new Integer(10), 100);
            //        Object z2 = this.coll1.put(new Integer(101), 1200);
            //        for (int i = 0; i < 100000000; i++) {
            //            try {
            //                this.coll1.put(new Integer(10 + i), 1200 + i);
            //                Thread.sleep(1);
            //            } catch (Exception ex) {
            //                ex.printStackTrace();
            //            }
            //        }
            //        JCSyncVector v = (JCSyncVector) AbstractCollectionsManager.getInstance().requestCreateCollection(JCSyncVector.class, "hashMap33", null);
            //
            //
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(JCSyncMainTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

