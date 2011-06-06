/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.pjwstk.mteam.pubsub.topology.maintenance.test;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Menu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;
import pl.edu.pjwstk.mteam.core.NetworkObject;
import pl.edu.pjwstk.mteam.core.Node;
import pl.edu.pjwstk.mteam.core.NodeCallback;
import pl.edu.pjwstk.mteam.core.NodeInfo;
import pl.edu.pjwstk.mteam.p2p.P2PNode;
import pl.edu.pjwstk.mteam.pubsub.accesscontrol.AccessControlRules;
import pl.edu.pjwstk.mteam.pubsub.core.CoreAlgorithm;
import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.Topic;
import pl.edu.pjwstk.mteam.pubsub.interestconditions.InterestConditions;
import pl.edu.pjwstk.mteam.pubsub.message.request.SubscribeRequest;
import pl.edu.pjwstk.mteam.pubsub.operation.SubscribeOperation;
import pl.edu.pjwstk.mteam.pubsub.core.Transaction;
import pl.edu.pjwstk.mteam.pubsub.transport.PubSubTransport;

/**
 *
 * @author buti
 */
public class NodePainter extends JComponent {

    String name;
    String id_dist;
    static final Dimension dim = new Dimension(120, 65);
    boolean entered_mouse = false;
    boolean entered_mouse_ = false;
    private TopologyPainter parent;
    boolean dragging_enabled = false;
    static final Vector<String> allTopics = new Vector<String>();
    static final Vector<NodePainter> allNodes = new Vector();
    static final HashMap<String, NodePainter> nodes = new HashMap<String, NodePainter>();
    NodePainter this_;
    private static Logger logger = Logger.getLogger("pl.edu.pjwstk.mteam.pubsub.topology.maintenance.test.NodePainter");
    int mouse_x, mouse_y;
    private JTextArea infoBox;
    private P2PNode node = null;
    private static int lastTCPPort = 7000;
    private static int lastUDPPort = 8000;
    private final int tcpPort, udpPort;
    private CoreAlgorithm pubsubmgr;
    private Color color = Color.blue;
    private HashMap<String, AccessControlRules> AClists = new HashMap<String, AccessControlRules>();
    private HashMap<String, InterestConditions> IClists = new HashMap<String, InterestConditions>();
    private static NodePainter poopedNode;
    JPopupMenu menu = new JPopupMenu("Choose action...");
    JMenuItem newTopic = new JMenuItem("Create topic...");
    JMenuItem networkLeave = new JMenuItem("Network leave");
    JMenuItem subscribeRex = new JMenuItem("Subscribe...");
    JMenuItem subscribeDirectReq = new JMenuItem("Subscribe direct...");
    JMenuItem outage = new JMenuItem("Force outage");
    JMenuItem joinNetwork = new JMenuItem("Join again...");
    static Topic tempTopic = new Topic("", new NodeInfo("zzz"));
    static AccessControlRules tempAC = new AccessControlRules(tempTopic);
    static SubscribeDirectFrame directRequestFrame;
    private ActionListener listener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == newTopic) {
                String str = JOptionPane.showInputDialog(null, "Insert topic ID: ", "Create new topic ...", 1);
                if (str != null && str.length() > 0) {
                    poopedNode.createTopic(str, true);
                }
            } else if (e.getSource() == networkLeave) {
                poopedNode.forceNetworkLeave();
            } else if (e.getSource() == outage) {
                poopedNode.forceOutage();
            } else if (e.getSource() == joinNetwork) {
                poopedNode.forceNetworkJoinAgain();
            } else if (e.getSource() == subscribeRex) {

                String str = (String) JOptionPane.showInputDialog(null,
                        "Select ID of the topic which you want to subscribe:",
                        "Subscribe", JOptionPane.QUESTION_MESSAGE,
                        null, allTopics.toArray(), "");
                if (str != null && str.length() > 0) {
                    poopedNode.node.getPubSubInterface().networkSubscribe(str);
                }
            } else if (e.getSource() == subscribeDirectReq) {
                directRequestFrame = null;
                directRequestFrame = new SubscribeDirectFrame(this_, allNodes,allTopics.toArray());

                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        directRequestFrame.setVisible(true);
                    }
                });
            }
        }
    };
    private boolean networkAvailable = true;
    private boolean subscription = false;

    ;
    private boolean notSubsribed = false;
    private String additionalInfo ="";
    private String additionalInfo2= "";
    private String distanceToTopic = "?";
    private String id_distance = "";

    public NodePainter() {
        super();
        setPreferredSize(dim);
        setSize(dim);
        this.tcpPort = -1;
        this.udpPort = -1;
    }

    public NodePainter(String name, String ID_DIST) {
        super();
        setPreferredSize(dim);
        setSize(dim);
        this.name = name;
        this.id_dist = ID_DIST;
        this.addMouseListener(ml);
        this.addMouseMotionListener(motionListener);
        this_ = this;
        allNodes.add(this);
        logger.debug("new node created :" + this.name + " , " + this.id_dist);
        this.tcpPort = lastTCPPort;
        this.udpPort = lastUDPPort;
        lastTCPPort++;
        lastUDPPort++;
        setToolTipText("Press left button to open menu...");
        menu.add(newTopic);
        newTopic.addActionListener(listener);
        menu.add(subscribeRex);
        subscribeDirectReq.addActionListener(listener);
        menu.add(subscribeDirectReq);
        subscribeRex.addActionListener(listener);
        menu.add(networkLeave);
        networkLeave.addActionListener(listener);
        menu.add(outage);
        outage.addActionListener(listener);
        menu.add(joinNetwork);
        joinNetwork.addActionListener(listener);
        joinNetwork.setEnabled(false);
        this.add(menu);
        joinNetwork.setEnabled(false);
        outage.setEnabled(false);
        newTopic.setEnabled(false);
        subscribeRex.setEnabled(false);
        networkLeave.setEnabled(false);
        subscribeDirectReq.setEnabled(false);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        color = Color.GRAY;
        networkAvailable = false;
        initP2PNode();
    }
    Font f;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(g2d.getClipBounds().x, g2d.getClipBounds().y, g2d.getClipBounds().width - 1, g2d.getClipBounds().height - 1, 5, 5);
        g2d.setColor(color);
        g2d.drawRoundRect(g2d.getClipBounds().x, g2d.getClipBounds().y, g2d.getClipBounds().width - 1, g2d.getClipBounds().height - 1, 5, 5);
        g2d.drawRoundRect(g2d.getClipBounds().x + 1, g2d.getClipBounds().y + 1, g2d.getClipBounds().width - 3, g2d.getClipBounds().height - 3, 5, 5);
        f = g2d.getFont();
        g2d.drawString(this.name, g2d.getClipBounds().x + 3, g2d.getClipBounds().y + 2 + f.getSize());
        g2d.drawString(this.id_distance, g2d.getClipBounds().x + 3, g2d.getClipBounds().y + 4 + f.getSize() * 2);
        g2d.drawString(this.additionalInfo, g2d.getClipBounds().x + 3, g2d.getClipBounds().y + 6 + f.getSize() * 3);
        g2d.drawString(this.additionalInfo2, g2d.getClipBounds().x + 3, g2d.getClipBounds().y + 8 + f.getSize() * 4);
    }

    private void updateGraphics() {
        if (this.networkAvailable == false) {
            this.color = Color.GRAY;
            return;
        }
        if (this.subscription) {
            this.color = Color.GREEN;
            if (this.dragging_enabled || this.entered_mouse_) {
                this.color = Color.red;
                showInfo();
            } else if (this.entered_mouse == true) {
                this.color = Color.PINK;
            }
            return;
        } else if (this.notSubsribed) {
            this.color = Color.ORANGE;
            if (this.dragging_enabled || this.entered_mouse_) {
                this.color = Color.red;
                showInfo();
            } else if (this.entered_mouse == true) {
                this.color = Color.PINK;
            }
            return;
        }
        if (this.dragging_enabled || this.entered_mouse_) {
            this.color = Color.red;
            showInfo();
        } else if (this.entered_mouse == true) {
            this.color = Color.PINK;
        } else {
            this.color = Color.blue;
        }
    }

    static NodePainter getNodeByUUID(String uuid) {
        return nodes.get(uuid);
    }
    MouseListener ml = new MouseAdapter() {

        @Override
        public void mousePressed(MouseEvent e) {

            if (e.getButton() == e.BUTTON1) {
                dragging_enabled = true;
                if (entered_mouse_) {
                    entered_mouse_ = false;
                } else {
                    entered_mouse_ = true;
                }
                mouse_x = e.getX();
                mouse_y = e.getY();
                updateGraphics();
                forceUnselectOtherNodes();
            } else if (e.isPopupTrigger()) {
                menu.show(this_, e.getX(), e.getY());
                forceUnselectOtherNodes();
                entered_mouse_ = true;
                poopedNode = this_;
            } else {
                super.mousePressed(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            dragging_enabled = false;
            updateGraphics();
        }
//

        @Override
        public void mouseEntered(MouseEvent e) {
            //forceUnselectOtherNodes();
            super.mouseEntered(e);
            entered_mouse = true;
            updateGraphics();
            repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            entered_mouse = false;
            updateGraphics();
            repaint();
        }
    };

    private void forceUnselectOtherNodes() {
        for (NodePainter a : allNodes) {
            if (a != this) {
                a.forceDeselect();
            }
            a.repaint();
        }

    }

    void setOwner(TopologyPainter jPanel2) {
        this.parent = jPanel2;
    }
    MouseMotionListener motionListener = new MouseMotionAdapter() {

        int oldX, oldY = 0;
        int new_x, new_y;

        public void mouseDragged(MouseEvent me) {
            oldX = getBounds().x;
            oldY = getBounds().y;
            new_x = oldX + (me.getX() - 30/*- mouse_x*/);
            new_y = oldY + (me.getY() - 15/*- mouse_y*/);
            setBounds(new_x, new_y, dim.width, dim.height);
            mouse_x = me.getX();
            mouse_y = me.getY();
        }
    };

    void setInfoBox(JTextArea jTextArea1) {
        this.infoBox = jTextArea1;
    }

    private void showInfo() {
        this.infoBox.setText("********************* NODE INFO *******************\n"
                + "NODE NAME:\t" + this.name + "\n"
                + "NODE ID:\t" + this.id_dist + "\n"
                + "NODE UUID:\t" + this.node.getID() + "\n"
                + "NODE IP:\t" + this.node.getIP() + "\n"
                + "NODE TCP Port:\t" + this.node.getTcpPort() + "\n"
                + "NODE UDP Port:\t" + this.node.getUdpPort() + "\n"
                + "DISTANCE TO SELECTED TOPIC: "+ this.distanceToTopic+"\n"
                + "********************* \\NODE INFO *******************\n"
                + "********************* TOPICS **********************\n" + this.node.getPubSubCoreAlgorithm().DEBUG_showTopics_() + "\n"
                + "********************* \\TOPICS **********************\n");
    }

    private void forceDeselect() {
        this.dragging_enabled = false;
        this.entered_mouse = false;
        this.entered_mouse_ = false;
        updateGraphics();
    }
    private NodeCallback callback = new NodeCallback() {

        public void onDisconnect(Node node) {
            logger.info(this_ + " - OnDisconnect invoked.....");
            repaint();
        }

        public void onOverlayError(Node node, Object sourceID, int errorCode) {
            logger.info(this_ + " - OnOverlayError invoked.....");
        }

        public void onJoin(Node node) {
            networkAvailable = true;
            joinNetwork.setEnabled(false);
            outage.setEnabled(true);
            newTopic.setEnabled(true);
            subscribeRex.setEnabled(true);
            networkLeave.setEnabled(true);
            subscribeDirectReq.setEnabled(true);
            updateGraphics();
            repaint();
            logger.info(this_ + " - OnJoin invoked.....");
            nodes.put(this_.node.getID(), this_);
            addUsertoAC(this_.node.getPubSubCoreAlgorithm().getNodeInfo().getName());
            forceACModify();
        }

        public void onUserLookup(Node node, Object userInfo) {
            NodeInfo info = (NodeInfo) userInfo;
            logger.info(this_ + " - OnUserLookup result: " + info.getIP() + ":" + info.getPort());
        }

        public void onTopicRemove(Node node, Object topicID) {
            logger.info(this_ + " - onTopicRemove for topic '" + topicID + "' callback invoked");
        }

        public void onTopicCreate(Node node, Object topicID) {
            logger.info(this_ + " - onTopicCreate for topic '" + topicID + "' callback invoked");
            parent.onTopicCreate(topicID);
            this_.node.getPubSubInterface().modifyAccessControlRules(topicID, tempAC);
            allTopics.add((String) topicID);
        }

        public void onTopicSubscribe(Node node, Object topicID) {
            logger.info(this_ + " - onTopicSubscribe for topic '" + topicID + "' callback invoked");
            this_.parent.refresh();
        }

        @Override
        public void onInsertObject(Node node, NetworkObject object) {
            logger.info(this_ + " - onInsertObject for key '" + object.getKey() + "' callback invoked");
        }

        @Override
        public void onPubSubError(Node node, Object topicID, byte operationType, int errorCode) {
            logger.info(this_ + " - OnPubSubError for topic '" + topicID + "' callback invoked");
        }

        @Override
        public void onTopicNotify(Node node, Object topicID, byte[] message, boolean b) {
            logger.info(this_ + " - onTopicNotify for topic '" + topicID + "' callback invoked");
        }

        @Override
        public void onTopicUnsubscribe(Node node, Object topicID) {
            logger.info(this_ + " - onTopicUnsubscribe for topic '" + topicID + "' callback invoked");
        }

        @Override
        public void onObjectLookup(Node node, Object object) {
            // TODO Auto-generated method stub
        }

        @Override
        public boolean onDeliverRequest(List<NetworkObject> objectList) {
            boolean result = true;
            NetworkObject obj = objectList.get(0);
            if (obj.getType() == NetworkObject.TYPE_PROTOTRUST) {
                byte[] msgbytes = obj.getValue();
                //Protocol-specific parsing goes here...

                /* Return 'false' to prevent the MESSAGE object
                 * from being inserted into DHT
                 */
                result = false;
            }
            return result;
        }

        @Override
        public boolean onForwardingRequest(List<NetworkObject> objectList) {
            boolean result = true;
            NetworkObject obj = objectList.get(0);
            if (obj.getType() == NetworkObject.TYPE_PROTOTRUST) {
                byte[] msgbytes = obj.getValue();
                //Protocol-specific parsing goes here...

                /* Return 'false' to discard P2PP insert request
                 * encapsulating ProtoTrust message
                 */
            }
            return result;
        }

        public void onTopicCreate(Node node, Object topicID, int transID) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void onTopicSubscribe(Node node, Object topicID, int transID) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void onOverlayError(Node node, Object sourceID, int errorCode, int transID) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void onPubSubError(Node node, Object topicID, byte operationType, int errorCode, int transID) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    static void forceACModify() {
        for (NodePainter np : allNodes) {
            np.forceACModify(tempAC);
        }

    }
    Topic t;

    private void forceACModify(AccessControlRules ac) {

        for (String topicID : allTopics) {
            t = null;
            t = this.node.getPubSubCoreAlgorithm().getTopic(topicID);
            try {
                if (t != null && (t.getOwner().getNodeInfo().getID() == null ? this.node.getID() == null : t.getOwner().getNodeInfo().getID().equals(this.node.getID()))) {
                    this.node.getPubSubInterface().modifyAccessControlRules(topicID, ac);
                }
            } catch (NullPointerException e) {
            }
        }
    }

    private void initP2PNode() {
        this.node = new P2PNode(callback);
        this.node.enableDebug();
        this.node.setUserName(this.name);
        this.node.setTcpPort(this.tcpPort);
        this.node.setUdpPort(this.udpPort);
        ((P2PNode) this.node).setServerReflexiveAddress("127.0.0.1");
        ((P2PNode) this.node).setServerReflexivePort(7080);
        this.node.setBootIP("127.0.0.1");
        this.node.setBootPort(7080);
        pubsubmgr = new CoreAlgorithm(this.node.getTcpPort() + 2000, this.node);
        pubsubmgr.setCustomizableAlgorithm();
        this.node.networkJoin();
    }

    @Override
    public String toString() {
        String res = "[" + this.name + ":" + this.id_dist + "]";
        return res;
    }

    public void createTopic(String topicID, boolean subscribe) {
        this.node.getPubSubInterface().createTopic(topicID, subscribe);
    }

    public void invokeACUpdateForAllNodes() {
    }

    public void forceOutage() {
        this.node.getPubSubCoreAlgorithm().getTransport().stop();
        this.node.getPubSubCoreAlgorithm().DEBUG_clearTopics();
        this.networkAvailable = false;
        this.joinNetwork.setEnabled(false);
        this.outage.setEnabled(false);
        this.newTopic.setEnabled(false);
        this.subscribeRex.setEnabled(false);
        this.networkLeave.setEnabled(false);
        subscribeDirectReq.setEnabled(false);
        updateGraphics();
        repaint();
    }

    private void forceNetworkLeave() {
        this.node.networkLeave();
        this.networkAvailable = false;
        this.joinNetwork.setEnabled(true);
        this.outage.setEnabled(false);
        this.newTopic.setEnabled(false);
        this.subscribeRex.setEnabled(false);
        this.networkLeave.setEnabled(false);
        subscribeDirectReq.setEnabled(false);
        updateGraphics();
    }

    private void forceNetworkJoinAgain() {
        this.node.networkJoin();
        //this.joinNetwork.setEnabled(false);
//        this.outage.setEnabled(true);
//        this.newTopic.setEnabled(true);
//        this.subscribeRex.setEnabled(true);
//        this.networkLeave.setEnabled(true);
        // this.networkAvailable = true;
//        subscribeDirectReq.setEnabled(false);
        updateGraphics();
    }

    boolean isLinkedWithTopic(String topicID) {
        Topic t = this_.node.getPubSubCoreAlgorithm().getTopic(topicID);
        if (t != null) {
            return true;
        } else {
            return false;
        }
    }

    public Vector getNodeChlidrenFromCache(String topicID) {
        Topic t = this.node.getPubSubCoreAlgorithm().getTopic(topicID);
        if(t!=null) return t.getChildren();
        else return null;
    }

    public String getUUID() {
        return this.name;
    }

    private void addUsertoAC(String username) {
        addUserToTempAC(PubSubConstants.OPERATION_SUBSCRIBE, PubSubConstants.EVENT_ALL, username);
        addUserToTempAC(PubSubConstants.OPERATION_SUBSCRIBE, PubSubConstants.EVENT_MODIFYAC, username);
        addUserToTempAC(PubSubConstants.OPERATION_PUBLISH, PubSubConstants.EVENT_ALL, username);
        addUserToTempAC(PubSubConstants.OPERATION_PUBLISH, PubSubConstants.EVENT_CUSTOM, username);
        addUserToTempAC(PubSubConstants.OPERATION_PUBLISH, PubSubConstants.EVENT_MODIFYAC, username);
        addUserToTempAC(PubSubConstants.OPERATION_PUBLISH, PubSubConstants.EVENT_REMOVETOPIC, username);
        addUserToTempAC(PubSubConstants.OPERATION_KEEPALIVE, PubSubConstants.EVENT_ALL, username);
    }

    void checkSubscription(String selectedTopic) {
        if (selectedTopic.length() > 0) {
            this.distanceToTopic = ""+this.node.getDistance(selectedTopic, this.node.getID());
            if (this.node.getPubSubCoreAlgorithm().getTopic(selectedTopic) == null) {
                this.subscription = false;
                this.notSubsribed = false;
                this.additionalInfo = "unsubscribed";
            } else if (this.node.getPubSubCoreAlgorithm().getTopic(selectedTopic).getChild(this.node.getID()) != null) {
                this.subscription = true;
                this.notSubsribed = false;
                if(this.node.getPubSubCoreAlgorithm().getTopic(selectedTopic).isTopicRoot()){
                    this.additionalInfo = "root";
                }
                else{
                    this.additionalInfo = "subscriber";
                }
            } else {
                this.subscription = false;
                this.notSubsribed = true;
                this.additionalInfo = "unsubscribed";
                if(this.node.getPubSubCoreAlgorithm().getTopic(selectedTopic).isTopicRoot()){
                    this.additionalInfo2 = "root";
                }
            }
        } else {
            this.subscription = false;
            this.notSubsribed = false;
            this.additionalInfo = "";
            this.additionalInfo2 = "";
            this.distanceToTopic = "?";
        }
        this.id_distance = this.id_dist+" ("+this.distanceToTopic+")";
        updateGraphics();
        repaint();
    }

    private void addUserToTempAC(byte operation, byte event, String username) {
        tempAC.getRule(operation).addUser(event, new Subscriber(username, tempTopic));
        logger.debug(tempAC);
    }
    public P2PNode getP2PNode(){
        return this.node;
    }

    void directRequest(String nodeName, String tid) {
           NodeInfo dst = null;
           Topic t;
           for(NodePainter np : allNodes){
               if(np.getP2PNode().getUserName().equals(nodeName)){
                   dst = np.getP2PNode().getPubSubCoreAlgorithm().getNodeInfo();
               }
           }
                        NodeInfo thisNode = this.node.getPubSubCoreAlgorithm().getNodeInfo();
			t = new Topic(tid);
			t.addSubscriber(thisNode);
			t.getChild(thisNode.getID()).getSubscription(t.getID()).setInterestConditions(new InterestConditions(t));
			int distance = this.node.getPubSubCoreAlgorithm().getNode().getDistance(tid, thisNode.getID());
			SubscribeOperation o = new SubscribeOperation(t.getID(),
					                                      new Subscriber(t, thisNode),
					                                      new Event(PubSubConstants.EVENT_ALL));
			Transaction trans = new Transaction(o, t);
			SubscribeRequest msg = new SubscribeRequest(trans.getID(), thisNode,
					                                    dst, tid,
					                                    100000000,
					                                    PubSubConstants.HISTORY_NONE,
					                                    distance);
			//Adding transaction and starting timer for it
			this.node.getPubSubCoreAlgorithm().addTransaction(trans);
			this.node.getPubSubCoreAlgorithm().sendMessage(msg, PubSubTransport.ROUTING_DIRECT, tid);

    }
}

