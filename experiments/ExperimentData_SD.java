package sidnet.stack.users.sample_p2p.experiments;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;

import jist.swans.net.NetAddress;
import sidnet.core.misc.Location2D;
import sidnet.core.misc.Node;
import sidnet.utilityviews.statscollector.ExperimentData; 
/**
 *
 * @author Oliver
 */
public class ExperimentData_SD
  extends ExperimentData {  
  
  public NetAddress sinkIP;
  public Location2D sinkLocation;
  private Location2D[] nodeLocList;   //journal addition
  private NetAddress[] nodeAddressList = null;  //journal addition    
  public static final String SAMPLING_INTERVAL = "SAMPLING_INTERVAL",
    NUMBER_OF_NODES       = "NUMBER_OF_NODES",
    FIELD_WIDTH           = "FIELD_WIDTH",
    CLUSTER_COUNT         = "CLUSTER_COUNT",
    THRESHOLD             = "THRESHOLD",
    PHENOMENON_FREQ       = "PHENOMENON_FREQ";
  
  public ExperimentData_SD() {
    super();
    System.out.println("Experiment Data is initialized ...");
    extractData(SAMPLING_INTERVAL);
    extractData(CLUSTER_COUNT);
    extractData(THRESHOLD);
    extractData(PHENOMENON_FREQ);
    //extractData(FIELD_WIDTH);
  }
    
  public void setSinkAtCenter(Node[] nodes, int areaLength) {
    double minDist = Double.MAX_VALUE;
    Location2D center = new Location2D(areaLength/2, areaLength/2);
    
    for (Node node: nodes) {
      double distance = node.getLocation2D().distanceTo(center);
      if (distance < minDist) {
        minDist = distance;
        sinkIP = new NetAddress(node.getID());
        sinkLocation = node.getLocation2D();
      }
    }
  }
    
  //these functions are journal additions
  public void setLocations(Location2D[] locs){
    this.nodeLocList = locs;
  }
    
  public void setIPs(NetAddress [] ips){
    this.nodeAddressList = ips;
    System.out.println("ip addresses set "+nodeAddressList.length);
  }
    
  public Location2D getClosestNode(Node node[], Location2D loc){
    System.out.println(node.length);
    double minDist = Double.MAX_VALUE;
    NetAddress localIP = null;
    Location2D localLoc = null;
    for (int i = 0; i < node.length; i++) {
      double distance = node[i].getLocation2D().distanceTo(loc);
      if (distance < minDist) {
        minDist = distance;
        localIP = node[i].getIP();
        localLoc = node[i].getLocation2D();
      }
    }
    return localLoc;
  }
    
  public void setSinkAtLocation(Location2D loc) {
    sinkLocation = loc;
  }

  public void setSinkAtLocation(Node node[], Location2D loc){
    sinkLocation = getClosestNode(node, loc);
  }

  public void setSinkAddress(Node node[], int sink_index) {
    this.sinkIP = node[sink_index].getIP();
  }
  
  public Location2D getNodeLocation(NetAddress nodeAdd) {
    for(int i = 0; i < nodeAddressList.length; i ++) {
      if(nodeAddressList[i] == nodeAdd)
        return nodeLocList[i];
    }
    return null;
  }
}
