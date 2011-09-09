/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.edu.pjwstk.mteam.pubsub.topology.maintenance.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author buti
 */
public class TopologyPainter extends JComponent {
    Viewer mainController;
    JComponent parent;
    int mouse_x,mouse_y;
    NodePainter draggedNode = null;
    private boolean showingAllNodes = true;
    private String selectedTopic = "";
    private HashMap<String,NodePainter> nodes = new HashMap<String, NodePainter>();
    private Vector<NodePainter> nodes_ = new Vector<NodePainter>();
    private Vector<NodePainter> nodesToDrawConnections = new Vector<NodePainter>();
    public TopologyPainter(){
        super();
    }

    @Override
    protected void paintComponent(Graphics g) {
       // try{
        updateDimension();
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.white);
        g2d.fillRect(g2d.getClipBounds().x, g2d.getClipBounds().y, g2d.getClipBounds().width,g2d.getClipBounds().height);
       // drawTopologyConnections(g2d);
        if(this.selectedTopic.length()> 0){
            g2d.setColor(Color.black);
            for(NodePainter np : nodesToDrawConnections){
                this.startX = np.getBounds().x;
                this.startY = np.getBounds().y;
                Vector<String> children = np.getNodeChlidrenFromCache(this.selectedTopic);
                if(children==null) continue;
                for(String uuid : children){
                    this.endX = NodePainter.getNodeByUUID(uuid).getBounds().x;
                    this.endY = NodePainter.getNodeByUUID(uuid).getBounds().y;
                    g2d.drawLine(startX, startY, endX, endY);
                    g2d.drawLine(startX+1, startY+1, endX+1, endY+1);
                }
            }
        }
//        }
//        //System.out.println("Clipping area: "+g2d.getClipBounds().x+" "+ g2d.getClipBounds().y+" "+ g2d.getClipBounds().width+" "+ g2d.getClipBounds().height);
//        catch(Exception e){
//            //ignore
//        }
    }

    void setOwner(JPanel jPanel2) {
        this.parent = jPanel2;
    }
    void setMainController(Viewer v){
        this.mainController = v;
    }

    private void updateDimension() {
        setPreferredSize(new Dimension(this.parent.getWidth()-10, this.parent.getHeight()-10));
        setSize(getPreferredSize());
    }

    void onTopicCreate(Object topicID) {
        this.mainController.onTopicCreate(topicID);
    }

    void showAllNodes(String topicID,boolean selected) {
        this.showingAllNodes = selected;
        this.selectedTopic = topicID;
        this.nodesToDrawConnections = new Vector<NodePainter>();
        if(topicID.length() == 0){
        for (NodePainter np : nodes_){
           np.setVisible(selected);
           np.checkSubscription(selectedTopic);
        }}
        else{
            for (NodePainter np : nodes_){
                np.checkSubscription(selectedTopic);
                if(np.isLinkedWithTopic(topicID)){
                    np.setVisible(true);
                    this.nodesToDrawConnections.add(np);
                }
                else{
                    np.setVisible(selected);
                }
            }
        }
    }

    void add_(JComponent np) {
        super.add(np);
        this.nodes.put(((NodePainter)np).getName(),(NodePainter)np);
        this.nodes_.add((NodePainter)np);
    }

    int startX,startY,endX,endY = 0;
    private void drawTopologyConnections(Graphics2D g2d) {
        if(this.selectedTopic.length()> 0){
            g2d.setColor(Color.green);
            for(NodePainter np : nodesToDrawConnections){
                this.startX = np.getBounds().x;
                this.startY = np.getBounds().y;
                Vector<String> children = np.getNodeChlidrenFromCache(this.selectedTopic);
                for(String uuid : children){
                    this.endX = NodePainter.getNodeByUUID(uuid).getBounds().x;
                    this.endY = NodePainter.getNodeByUUID(uuid).getBounds().y;
                    g2d.drawLine(startX, startY, endX, endY);
                }
            }
        }
    }

    void refresh() {
       showAllNodes(selectedTopic, showingAllNodes);
       repaint();
    }
    

    
    

    


}
