/*
 * AppSampleP2P.java
 *
 * Created on April 15, 2008, 11:14 AM
 * 
 * @author  Oliviu Ghica
 */
package sidnet.stack.users.sample_p2p.app;

//import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.sun.msv.driver.textui.Driver;

import jist.runtime.JistAPI;
import jist.swans.Constants;
//import jist.swans.app.io.BufferedWriter;
import jist.swans.mac.MacAddress;
import jist.swans.misc.Message;
import jist.swans.net.NetAddress;
import jist.swans.net.NetInterface;
import sidnet.colorprofiles.ColorProfileGeneric;
import sidnet.core.gui.TopologyGUI;
import sidnet.core.interfaces.AppInterface;
import sidnet.core.interfaces.CallbackInterface;
import sidnet.core.interfaces.ColorProfile;
// import sidnet.stack.users.sample_p2p.kmaxrs_centralized.Objects;
import sidnet.core.misc.HierarchicalNode;
import sidnet.core.misc.Location2D;
import sidnet.core.misc.Node;
import sidnet.core.query.Query;
import sidnet.core.simcontrol.SimManager;
import sidnet.stack.std.routing.heartbeat.MessageHeartbeat;
import sidnet.stack.std.routing.shortestgeopath.SGPWrapperMessage;
import sidnet.stack.users.sample_p2p.driver.Driver_SampleP2P;
import sidnet.stack.users.sample_p2p.driver.InputReading;
import sidnet.stack.users.sample_p2p.driver.Organization;
import sidnet.stack.users.sample_p2p.experiments.ExperimentData_SD;
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.DistSlabfile;
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.Stripline_kMaxRS;
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.Target;
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.Window;
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.centralized_kMaxRS;
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.distributed_kMaxRS;
import sidnet.utilityviews.statscollector.StatsCollector;

public class AppSampleP2P implements AppInterface, CallbackInterface {
  private final HierarchicalNode myNode; // The SIDnet handle to the node representation 
    
  public static TopologyGUI topologyGUI = null;

  private static final byte TTL = 40;
  
  //sink configuration
	private static NetAddress sinkIP;
	private static Location2D sinkLocation;
	
	private static String target_file = "target_file.txt";
	private static int time = 0;
	  
  /** network entity. */ 
  private NetInterface netEntity;
    
  /** self-referencing proxy entity. */
  private Object self;
    
  /** flag to mark if a heartbean protocol has been initialized */
  private boolean heartbeatInitiated = false;
    
  private static boolean flag = false;
    
  private boolean signaledUserRequest = false;
    
  private final short routingProtocolIndex;
    
  private StatsCollector stats = null;
    
  private boolean startedSensing = false;

  // normal motes will sampling every 30 seconds, while the principle nodes will
  // do so every 1 minutes.
  private static long SAMPLING_UNIT = Constants.SECOND;
  private static int SAMPLING_INTERVAL_DATAVALUES = 120;
  private static long SAMPLING_INTERVAL_KMAXRS = SAMPLING_INTERVAL_DATAVALUES * 4 * SAMPLING_UNIT;
  // new version = true, old version = false
  private static boolean VERSION_SAMPLING_DATAVALUES = true;
  private static int BYTE_PER_MSG = 128;
  private static Random ran = new Random();
  
  private static int THRESHOLD = 0;
    
  private ExperimentData_SD experimentData = null;
  // do not make this static
  private ColorProfileGeneric colorProfileGeneric = new ColorProfileGeneric(); 
    
  /** Creates a new instance of the AppP2P */
  public AppSampleP2P(Node myNode, 
                      short routingProtocolIndex,
                      StatsCollector stats,
                      ExperimentData_SD experimentData)
  {
    this.self = JistAPI.proxyMany(this, new Class[] { AppInterface.class });
    this.myNode = (HierarchicalNode) myNode;
        
    // To allow the upper layer (user's terminal) 
    // to signal any updates to this node */
    this.myNode.setAppCallback(this);
  
    this.routingProtocolIndex = routingProtocolIndex;
    // System.out.println("sampling interval: " + experimentData.getLong(experimentData.SAMPLING_INTERVAL, 10) + " " + Constants.SECOND); 
    this.stats = stats;
    this.experimentData = experimentData;
    this.sinkIP = this.experimentData.sinkIP;
    this.sinkLocation = this.experimentData.sinkLocation;
  }

  /* 
   * This is your main execution loop at the Application Level. Here you design the application functionality. It is simulation-time driven
   * The first call to this function is made automatically upon starting the simulation, from the Driver
   */
  public void run(String[] args) 
  {   
    // create an array of objects for the sink node in the centralized algorithm
    if (myNode.sink_node) myNode.setupObjects(Driver_SampleP2P.nodes);
    /* At time 0, set the simulation speed to x1000 to get over the heartbeat node identification phase fast */
    if (JistAPI.getTime() == 0)  // this is how to get the simulation time, by the way
      myNode.getSimControl().setSpeed(SimManager.X1000);
     
    //if (myNode.getID() != 2) return;  // ???
    /* This is a one-time phase. We'll allow a one-hour warm-up in which each node identifies its neighbors (The Heartbeat Protocol) */
    if (JistAPI.getTime() > 0 && !heartbeatInitiated)
    {
      // System.out.println("["+(myNode.getID() * 5 * Constants.MINUTE) +"] Node " + myNode.getID() + " broadcasts a heartbeat message");
      myNode.getNodeGUI().colorCode.mark(colorProfileGeneric, ColorProfileGeneric.TRANSMIT, 500); 
               
      /* To avoid all nodes to transmit in the same time */
      JistAPI.sleepBlock(myNode.getID() * 5 * Constants.SECOND); 
                
      MessageHeartbeat msg = new MessageHeartbeat();
      msg.setNCS_Location(myNode.getNCS_Location2D());
                               
      myNode.getNodeGUI().colorCode.mark(colorProfileGeneric, ColorProfileGeneric.TRANSMIT, 500); 
                
      /* Send the heartbeat message. The heartbeat protocol will handle these messages and continue according to the protocol*/
      netEntity.send(msg, NetAddress.ANY, Constants.NET_PROTOCOL_HEARTBEAT, Constants.NET_PRIORITY_NORMAL, (byte)100);  // TTL 100
                
      heartbeatInitiated = true;
    }
         
    /* Wait 1 hour for the heartbeat-bootstrap to finish, then slow down to allow users to interact in real-time*/
    // increase the speed of the simulation here
    if (JistAPI.getTime()/Constants.HOUR >= 1 && !flag) {
      myNode.getSimControl().setSpeed(SimManager.X1);
      flag = true;
    }
          
    if (JistAPI.getTime()/Constants.MINUTE < 60) {
      /*if (myNode.getID() == 0)
        {
        topologyGUI.addLink(new NCS_Location2D(0,0), new NCS_Location2D(1,1), 0 , Color.blue);
        topologyGUI.addLink(3, 5, 0, Color.green);
        }*/
              
      JistAPI.sleep(5000*Constants.MILLI_SECOND);  // 5000 milliseconds
              
      /* this is to schedule the next run(args) */
      ((AppInterface)self).run(null);  
      /* !!! Pay attention to the way we re-run the app-layer code. We don't use a while loop, 
       * but rather let JiST call this again and again */
              
      return;
    }

    // #Besim code
    LinkedList params = new LinkedList();
    params.add(sampling_interval_datavalues()); // sampling
    params.add(100 * Constants.HOUR); // duration
    params.add(1); // queryId
    params.add(2l); // sequence number
    params.add(this.experimentData.sinkIP); // some random sink address
    params.add(this.experimentData.sinkLocation);
    params.add(myNode.getID());
    //    sensing(params);
    ((AppInterface)self).sensing(params);
  }
    
  // #Besim same  
  public void run() {
    //Location currentLoc = field.getRadioData(new Integer(nodenum)).getLocation();
    JistAPI.sleep(2 + (long)((1000-2)*Constants.random.nextFloat())); 
    run(null);
  }

  /* Sensing the phenomena is most likely a periodic process. We wrote a procedure to do so.
   * Since the sensing() takes place at various simulation-time, this function should be called through a proxy reference, rather than directly to avoid
   * an infinite starvation loop */
  public void sensing(List params)
  {
    long samplingInterval  = (Long)params.get(0);
    long endTime           = (Long)params.get(1);
    int  queryId           = (Integer)params.get(2);
    long sequenceNumber    = (Long)params.get(3);
    NetAddress sinkAddress = (NetAddress) params.get(4);
    Location2D sinkLocation= (Location2D) params.get(5);
    int nodeid = (Integer) params.get(6);
    // MessageDataValue msgDataValue = (MessageDataValue) params.get(6);
    // expect that all motes in the cluster will send the data to the
    // principle node for two times
    myNode.getNodeGUI().colorCode.mark(colorProfileGeneric, ColorProfileGeneric.SENSE, 5);
    double sensedValue = 0;
    if (Driver_SampleP2P.inputreading == InputReading.RANDOM_EVERYTHING ||
        Driver_SampleP2P.inputreading == InputReading.FIX_LOCATION) {
      sensedValue = myNode.readAnalogSensorData(0);
    } else 
      sensedValue = myNode.fix_sv;
      
    if (sensedValue <= 0) // the sensed value must be higher than zero.
      sensedValue = 1;

    myNode.getNodeGUI().setUserDefinedData1((int)sensedValue);
    
    // +++++++++++++++++++++++++ data processing 
    if (Driver_SampleP2P.organization == Organization.CENTRALIZED) {
      if (myNode.sink_node) {
        // delay and process the received data
        myNode.setObjects(myNode.getID(), 
                          myNode.getLocation2D().getX(),
                          myNode.getLocation2D().getY(),
                          sensedValue);
        JistAPI.sleepBlock(SAMPLING_INTERVAL_KMAXRS);
        centralized_kMaxRS.centralized(myNode.getObjects(), Driver_SampleP2P.k);
      } else {
        //send_datavalue(sensedValue, myNode.sink_location, myNode.sink_address, myNode.getID());
        JistAPI.sleepBlock(sampling_interval_datavalues());
      }
      
    } else  if (Driver_SampleP2P.organization == Organization.CLUSTER) 
    {
      if (myNode.cluster_head &&
          (myNode.cluster_number == (Driver_SampleP2P.cluster_number_x * Driver_SampleP2P.cluster_number_y -1))) 
      {
        // if this is the last cluster 
        myNode.addObjectsv(myNode.getID(), 
                           myNode.getLocation2D().getX(),
                           myNode.getLocation2D().getY(),
                           sensedValue);
        
        JistAPI.sleepBlock(SAMPLING_INTERVAL_KMAXRS);
        System.out.println("clus number: " + myNode.cluster_number);
        // public static DistSlabfile[] distributed(Vector<Objects> obj, int k, DictWindow dic, int cl_id)
        DistSlabfile[] res = distributed_kMaxRS.distributed_last(myNode.dis_obj, 
                                                                 Driver_SampleP2P.k, 
                                                                 null, 
                                                                 myNode.cluster_number);
        if (Driver_SampleP2P.DEBUG) System.out.println("send the sf to x: " + myNode.upLocation.getX() + " y: " + myNode.upLocation.getY());
        // queryId = 0 = up
        send_dist_slapfile(res[0], myNode.upLocation, myNode.upAddress, myNode.getID());
        send_dist_slapfile(res[1], myNode.nextLocation, myNode.nextAddress, myNode.getID());
        System.out.println("send the sf to x: " + myNode.nextLocation.getX() + " y: " + myNode.nextLocation.getY());
      } else if (myNode.cluster_head) {
        myNode.addObjectsv(myNode.getID(), 
                           myNode.getLocation2D().getX(),
                           myNode.getLocation2D().getY(),
                           sensedValue);
        JistAPI.sleepBlock(sampling_interval_datavalues());
      } else {
        JistAPI.sleepBlock(sampling_interval_datavalues());
      }
    } else if (Driver_SampleP2P.organization == Organization.STRIP_LINE) 
    {
      if (myNode.cluster_head &&
          (myNode.cluster_number == (Driver_SampleP2P.cluster_number_x -1))) 
      {      
        myNode.addObjectsv(myNode.getID(), 
                           myNode.getLocation2D().getX(),
                           myNode.getLocation2D().getY(),
                           sensedValue);
        
        JistAPI.sleepBlock(SAMPLING_INTERVAL_KMAXRS);
        System.out.println("clus number: " + myNode.cluster_number);
        // public static DistSlabfile[] distributed(Vector<Objects> obj, int k, DictWindow dic, int cl_id)
        DistSlabfile res = Stripline_kMaxRS.stripline_last(myNode.dis_obj, 
                                                             Driver_SampleP2P.k, 
                                                             null, 
                                                             myNode.cluster_number);
        System.out.println("send the sf to x: " + myNode.nextLocation.getX() + " y: " + myNode.nextLocation.getY());
        // queryId = 0 = up
        send_dist_slapfile(res, myNode.nextLocation, myNode.nextAddress, myNode.getID());
      } else if (myNode.cluster_head) {
        myNode.addObjectsv(myNode.getID(), 
                           myNode.getLocation2D().getX(),
                           myNode.getLocation2D().getY(),
                           sensedValue);
        JistAPI.sleepBlock(sampling_interval_datavalues());
      } else {
        JistAPI.sleepBlock(sampling_interval_datavalues());
      }      
    }
    
    // Prepare the Aggregation Message containing the measurement
    // ++++++++++++++++++++++++++++ data transmission
    
    if (Driver_SampleP2P.organization == Organization.CENTRALIZED) {
      // send the sensed value from the nodes to the sink
      if (!myNode.sink_node) {
        send_datavalue(sensedValue, myNode.sink_location, myNode.sink_address, myNode.getID());
        myNode.getNodeGUI().colorCode.mark(colorProfileGeneric,
                                   ColorProfileGeneric.TRANSMIT, 
                                   5);
      }
    } else if (Driver_SampleP2P.organization == Organization.CLUSTER || 
               Driver_SampleP2P.organization == Organization.STRIP_LINE) 
    {
      // +++++++++++++++++++++ send the sensed value to the cluster head
      if (!myNode.sink_node && !myNode.cluster_head) {
        send_datavalue(sensedValue, myNode.ch_location, myNode.ch_address, myNode.getID());
        myNode.getNodeGUI().colorCode.mark(colorProfileGeneric,
                                           ColorProfileGeneric.TRANSMIT, 
                                           5);           
      } 
    }
    
    // #Besim same
    if (JistAPI.getTime() < endTime)
    {
      sequenceNumber++;
                
      params.set(0, samplingInterval);
      params.set(1, endTime);
      params.set(2, queryId);
      params.set(3, sequenceNumber);
      params.set(4, sinkAddress);
      params.set(5, sinkLocation);
      // params.set(6, msgDataValue);
      // System.out.println("Sink IP: " + sinkAddress);                
      // System.out.println("Sink Location: " + sinkLocation);
      // this is to schedule the next run(args). 
      // DO NOT use WHILE loops to do this, 
      // nor call the function directly. Let JiST handle it 
      ((AppInterface)self).sensing(params);
    }
  }
      
  /** Callback registered with the terminal,
   * The terminal will call this function whenever the user posts a new query or just closes the terminal window
   * <p>
   * You should inspect the myNode.localTerminalDataSet.getQueryList() to check for new posted queries that your node must act upon
   * Have a look at the TerminalDataSet.java for the available data that is exchanged between this node and the terminal
   */
  // #Besim same
  public void signalUserRequest()
  {
    /* We'll assume that the node through which the user has posted a query becomes a sink node */
    if (myNode.getQueryList().size() > 0 )
    {     
      Query query = ((LinkedList<Query>)myNode.getQueryList()).getLast();
      myNode.getNodeGUI().colorCode.mark(colorProfileGeneric, ColorProfileGeneric.SINK, ColorProfile.FOREVER); // to make easier to you to see the node you've posted the query through (the sink node)
          
      if (!query.isDispatched()) {        
        myNode.getNodeGUI().colorCode.mark(colorProfileGeneric, ColorProfileGeneric.SINK, ColorProfileGeneric.FOREVER);
                
        int[] rootIDArray = new int[1];
        rootIDArray[0] = myNode.getID();
                
        MessageQuery msgQuery = new MessageQuery(query);

        // wrap the MessageQuery as a SGP message
        SGPWrapperMessage msgSGP 
          = new SGPWrapperMessage(msgQuery, query.getRegion(),
                                  0, JistAPI.getTime());
                
        netEntity.send(msgSGP, 
                       null /*unknown Dest IP, only its approx location*/,
                       routingProtocolIndex /* (see Driver) */,
                       Constants.NET_PRIORITY_NORMAL, (byte)100);                  
                            
        query.dispatched(true);
      }
    }
  }
    
  /**
   * Message has been received. 
   * This node must be the either the sink or the source nodes 
   */
  public void receive(Message msg, NetAddress src, MacAddress lastHop, byte macId, NetAddress dst, byte priority, byte ttl) 
  {
    if (msg == null)
      return;
    
    if (myNode.getEnergyManagement().getBattery().getPercentageEnergyLevel() < 5)
      return;
    
    if (msg instanceof MessageTarget) {
      MessageTarget msgtar = (MessageTarget) msg;
      // this.stats.incrementValue("RECEIVE_PACKET", 1);
      stats_increase_value("RECEIVE_PACKET", 1, msgtar.size());
      // this.stats.incrementValue("MESSAGE_HOPS", TTL - ttl);
      stats_increase_value("MESSAGE_HOPS", TTL - ttl, msgtar.size());
    } else if (msg instanceof MessageDataValue) {
      MessageDataValue msgdv = (MessageDataValue) msg;
      // this.stats.incrementValue("RECEIVE_PACKET", 1);
      stats_increase_value("RECEIVE_PACKET", 1, 0);
      // this.stats.incrementValue("MESSAGE_HOPS", TTL - ttl);
      stats_increase_value("MESSAGE_HOPS", TTL - ttl, 0);      
    } else if (msg instanceof MessageSlabFile) {
      MessageSlabFile msgsl = (MessageSlabFile) msg;
      // this.stats.incrementValue("RECEIVE_PACKET", 1);
      stats_increase_value("RECEIVE_PACKET", 1, msgsl.size());
      // this.stats.incrementValue("MESSAGE_HOPS", TTL - ttl);
      stats_increase_value("MESSAGE_HOPS", TTL - ttl, msgsl.size());
    }

    if (myNode.sink_node && msg instanceof MessageTarget) {
      MessageTarget msgt = (MessageTarget) msg;
      // this.stats.incrementValue("SINK_RECEIVED_TARGET", 1);
      stats_increase_value("SINK_RECEIVED_TARGET", 1, msgt.size());
      System.out.println("\n\n -------------- sink received target ----------------------");
      System.out.println("target: " + msgt.target);
      write_target(msgt.target);
      System.out.println("-------------- sink received target ---------------------- \n\n");
    }
    
    MessageSlabFile msgsf_tx = null;
    SGPWrapperMessage msgSGP_tx = null;
    DistSlabfile dsf = null;
    if (myNode.cluster_head && msg instanceof MessageSlabFile && 
        Driver_SampleP2P.organization == Organization.CLUSTER) 
    { 
      /* This is a source node. It receives the query request, and not it prepares to do the periodic sensing/sampling */
      MessageSlabFile msgsf = (MessageSlabFile)msg;
      System.out.println(" $$$$$$$$$$$$ cluster number: " + myNode.cluster_number +
                         " msg source id: " + ((MessageSlabFile)msg).sourceNodeId + 
                         " node id: " + myNode.getID() + 
                         " node cluster: " + myNode.cluster_number);
      System.out.println("$$$$$$$$$$$$$ cluster sink: " + Driver_SampleP2P.cluster_sink);
      System.out.println("|||");
      if (msgsf.sourceNodeId == myNode.getID()) return;
      
      if (myNode.cluster_number == Driver_SampleP2P.cluster_sink) 
      { // the zeroth cluster
        save_message(msgsf.dist_sf, msgsf.sourceLocation);
        if (myNode.dis_sf_next != null && myNode.dis_sf_up != null) {
          if (Driver_SampleP2P.DEBUG) System.out.println("msg: src location: " + msgsf.sourceLocation);
          Target tar = distributed_kMaxRS.distributed_sink_pkt(myNode.dis_obj, 
                                                               Driver_SampleP2P.k, 
                                                               myNode.dis_sf_up,
                                                               myNode.dis_sf_next,
                                                               myNode.cluster_number);
          myNode.dis_sf_next = null;
          myNode.dis_sf_up = null;
          // send the message to the sink node
          // remember that the principle node in the same cluster as sink
          // will save the sink address in the upAddress
          System.out.println("send the sink uplocation: " + myNode.upLocation + " upaddress: " + myNode.upAddress);          
          MessageTarget msgtar_tx = new MessageTarget(tar,
                                                      0,
                                                      0,
                                                      myNode.getID(),
                                                      myNode.getID(),
                                                      myNode.getLocation2D());
  
          msgSGP_tx = new SGPWrapperMessage(msgtar_tx,
                                            myNode.upLocation,
                                            0,
                                            JistAPI.getTime());
  
          netEntity.send(msgSGP_tx, 
                         myNode.upAddress,
                         routingProtocolIndex,
                         Constants.NET_PRIORITY_NORMAL, 
                         (byte) TTL); 
  
          // this.stats.incrementValue("SEND_PACKET", 1);
          stats_increase_value("SEND_PACKET", 1, msgtar_tx.getSize());
        }
      } else if ((myNode.cluster_number == (Driver_SampleP2P.cluster_number_x - 1)) ||
                 (myNode.cluster_number == (Driver_SampleP2P.cluster_number_y - 1) * (Driver_SampleP2P.cluster_number_x))) 
      { // "6", "2"
        //  0  1 "2"  
        //  3  4  5
        // "6" 7  8 
        // handle the corner cases such as 2 and 6 in the 3 x 3 cluster
        dsf = distributed_kMaxRS.distributed_one_pkt(myNode.dis_obj, 
                                                     Driver_SampleP2P.k,
                                                     msgsf.dist_sf,
                                                     myNode.cluster_number);
        
        if (Driver_SampleP2P.DEBUG) System.out.println("node id: " + myNode.getID());

        NetAddress dst_add = null;
        Location2D dst_loc = null;
        if (myNode.cluster_number == Driver_SampleP2P.cluster_number_x - 1) {
          // handle the "2" case
          dst_add = myNode.nextAddress;
          dst_loc = myNode.nextLocation;
        } else {
          // handle the "6" case
          dst_add = myNode.upAddress;
          dst_loc = myNode.upLocation;
        }
        
        // public void send_dist_slapfile(DistSlabfile dsf, Location2D dst_loc, NetAddress dst_na, int net_id) {
        send_dist_slapfile(dsf, dst_loc, dst_add, myNode.getID());
        if (Driver_SampleP2P.DEBUG) System.out.println("send the sf to x: " + dst_loc.getX() + " y: " + dst_loc.getY());
      } else if (myNode.cluster_number < Driver_SampleP2P.cluster_number_x) 
      { // the first row
        // 0 "1" 2 <- 
        // 3  4  5
        // 6  7  8  
        save_message(msgsf.dist_sf, msgsf.sourceLocation);
        if (myNode.dis_sf_next != null && myNode.dis_sf_up != null) {
          dsf = distributed_kMaxRS.distributed_rx2_tx1(myNode.dis_obj, 
                                                       Driver_SampleP2P.k, 
                                                       myNode.dis_sf_up,
                                                       myNode.dis_sf_next,
                                                       myNode.cluster_number);
          // public void send_dist_slapfile(DistSlabfile dsf, Location2D dst_loc, NetAddress dst_na, int net_id) {
          send_dist_slapfile(dsf, myNode.nextLocation, myNode.nextAddress, myNode.getID());
        }       
      } else if ((myNode.cluster_number > (Driver_SampleP2P.cluster_number_y - 1) * Driver_SampleP2P.cluster_number_x) && 
                 (myNode.cluster_number < Driver_SampleP2P.cluster_number_x * Driver_SampleP2P.cluster_number_y))
      { // the last row
        // 0  1   2
        // 3  4   5
        // 6 "7"  8 <- the last row
        DistSlabfile[] dsf_two = distributed_kMaxRS.distributed_rx1_tx2(myNode.dis_obj, 
                                                                        Driver_SampleP2P.k,
                                                                        msgsf.dist_sf,                                                     
                                                                        myNode.cluster_number);
        myNode.dis_sf_next = null;
        myNode.dis_sf_up = null;
        // public void send_dist_slapfile(DistSlabfile dsf, Location2D dst_loc, NetAddress dst_na, int net_id) {
        send_dist_slapfile(dsf_two[0], myNode.upLocation, myNode.upAddress, myNode.getID());
        if (Driver_SampleP2P.DEBUG) System.out.println("send the sf to x: " + myNode.upLocation.getX() + " y: " + myNode.upLocation.getY());

        send_dist_slapfile(dsf_two[1], myNode.nextLocation, myNode.nextAddress, myNode.getID());
        if (Driver_SampleP2P.DEBUG) System.out.println("send the sf to x: " + myNode.nextLocation.getX() + " y: " + myNode.nextLocation.getY());
        
      } else if (myNode.cluster_number%Driver_SampleP2P.cluster_number_x == 0)
      { // "3"
        // 0   1 2 
        // "3" 4 5
        // 6   7 8 
        // ^  
        // cluster on the left most column
        save_message(msgsf.dist_sf, msgsf.sourceLocation);
        if (myNode.dis_sf_next != null & myNode.dis_sf_up != null) {
          dsf = distributed_kMaxRS.distributed_rx2_tx1(myNode.dis_obj, 
                                                       Driver_SampleP2P.k,
                                                       myNode.dis_sf_up,
                                                       myNode.dis_sf_next,
                                                       myNode.cluster_number);          
          myNode.dis_sf_next = null;
          myNode.dis_sf_up = null;
          if (Driver_SampleP2P.DEBUG) System.out.println("send the sf to x: " + myNode.upLocation.getX() + " y: " + myNode.upLocation.getY());
          send_dist_slapfile(dsf, myNode.upLocation, myNode.upAddress, myNode.getID());
        }
      } else if ((myNode.cluster_number + 1)%Driver_SampleP2P.cluster_number_x == 0) 
      { // "5"
        // 0 1  2  
        // 3 4 "5"
        // 6 7  8 
        //     ^  
        // the right column
        DistSlabfile[] dsf_two = distributed_kMaxRS.distributed_rx1_tx2(myNode.dis_obj, 
                                                                        Driver_SampleP2P.k,
                                                                        msgsf.dist_sf,
                                                                        myNode.cluster_number);
        
        System.out.println("node id: " + myNode.getID());
       
        send_dist_slapfile(dsf_two[0], myNode.upLocation, myNode.upAddress, myNode.getID());
        if (Driver_SampleP2P.DEBUG) System.out.println("send the sf to x: " + myNode.upLocation.getX() + " y: " + myNode.upLocation.getY());
        send_dist_slapfile(dsf_two[1], myNode.nextLocation, myNode.nextAddress, myNode.getID());
        if (Driver_SampleP2P.DEBUG)  System.out.println("send the sf to x: " + myNode.nextLocation.getX() + " y: " + myNode.nextLocation.getY());
      } else { // inner clusters such as "4"
        // 0 1 2
        // 3 4 5 
        // 6 7 8 
        // if other principle nodes are the inner clusters such as 4
        save_message(msgsf.dist_sf, msgsf.sourceLocation);
        if (myNode.dis_sf_next != null && myNode.dis_sf_up != null) {
          DistSlabfile[] dsf_two = distributed_kMaxRS.distributed_inner_two_pkt(myNode.dis_obj, 
                                                                                Driver_SampleP2P.k,
                                                                                myNode.dis_sf_up,
                                                                                myNode.dis_sf_next,                                                     
                                                                                myNode.cluster_number);
          myNode.dis_sf_next = null;
          myNode.dis_sf_up = null;
          send_dist_slapfile(dsf_two[0], myNode.upLocation, myNode.upAddress, myNode.getID());
          if (Driver_SampleP2P.DEBUG) System.out.println("send the sf to x: " + myNode.upLocation.getX() + " y: " + myNode.upLocation.getY());
          
          send_dist_slapfile(dsf_two[1], myNode.nextLocation, myNode.nextAddress, myNode.getID()); 
          if (Driver_SampleP2P.DEBUG) System.out.println("send the sf to x: " + myNode.nextLocation.getX() + " y: " + myNode.nextLocation.getY());       
        }      
      }
    }// if (msg instanceof MessageSlabFile)
        
    // III merge the code with the last if
    // III if (msg instanceof MessageSlabFile)    
    if (msg instanceof MessageSlabFile 
        && Driver_SampleP2P.organization == Organization.STRIP_LINE 
        && myNode.cluster_head) 
    {
      MessageSlabFile msgsf = (MessageSlabFile) msg;
      if (Driver_SampleP2P.DEBUG) System.out.println("node id: " + myNode.getID());
      System.out.println("cluster_sink: " + Driver_SampleP2P.cluster_sink + " node cluster: " + myNode.cluster_number);

      if (myNode.cluster_number == Driver_SampleP2P.cluster_sink) {
        Target tar = Stripline_kMaxRS.stripline_sink_pkt(myNode.dis_obj,
                                                         Driver_SampleP2P.k,
                                                         msgsf.dist_sf,
                                                         myNode.cluster_number);
        MessageTarget msgtar = new MessageTarget(tar, 
                                                 0, 
                                                 0, 
                                                 myNode.getID(), 
                                                 myNode.getID(), 
                                                 myNode.getLocation2D());
        
        msgSGP_tx = new SGPWrapperMessage(msgtar,
                                          myNode.nextLocation,
                                          0,
                                          JistAPI.getTime());
        netEntity.send(msgSGP_tx, 
                       myNode.nextAddress, 
                       routingProtocolIndex, 
                       Constants.NET_PRIORITY_NORMAL, 
                       (byte) TTL); 

        // this.stats.incrementValue("SEND_PACKET", 1);
        stats_increase_value("SEND_PACKET", 1, msgtar.size());
      } else {
        DistSlabfile sf = Stripline_kMaxRS.stripline_one_pkt(myNode.dis_obj,
                                                             Driver_SampleP2P.k,
                                                             msgsf.dist_sf,
                                                             myNode.cluster_number);
        send_dist_slapfile(sf, myNode.nextLocation, myNode.nextAddress, myNode.getID());
      } 
    }

    if (msg instanceof MessageDataValue) 
    {
      MessageDataValue msgData = (MessageDataValue)msg;
      
      if (Driver_SampleP2P.organization == Organization.CENTRALIZED) {

        if (myNode.sink_node) {          
          this.stats.incrementValue("SINK_RECEIVED_DATAVALUES", 1);
          /*if (DEBUG) {
            System.out.println("receive: Sink val: " + msgData.dataValue + " node id: " + msgData.sourceNodeId);
            System.out.println("receive: Sink source location: " + msgData.sourceLocation);
          }*/
          myNode.setupObjects(Driver_SampleP2P.nodes);
          myNode.setObjects(msgData.sourceNodeId, 
                            msgData.sourceLocation.getX(), 
                            msgData.sourceLocation.getY(),
                            msgData.dataValue);
        } 
      } else if (Driver_SampleP2P.organization == Organization.CLUSTER ||
                 Driver_SampleP2P.organization == Organization.STRIP_LINE) 
      {
        // update the transmitted data into the vector
        if (myNode.cluster_head) {
          myNode.addObjectsv(msgData.sourceNodeId, 
                             msgData.sourceLocation.getX(), 
                             msgData.sourceLocation.getY(), 
                             msgData.dataValue);
        } 
      }
    }
     // Connecting a terminal to this node, at run time,
     // allows the user to visualize the result of the posted query 
     // myNode.getNodeGUI()
     //   .getTerminal()
     //   .appendConsoleText(myNode.getNodeGUI().localTerminalDataSet,
     //                      "Sample #" +
     //                      msgData.sequenceNumber + " | val: " + msgData.dataValue);             
  }
 
  /* **************************************** *
   * Helper functions                         *
   * **************************************** */
  public long sampling_interval_datavalues() {
    // if version_sampling_datavalues == true --> new version
    // in the old version, the simulator will throw an concurrent exception 
    // when the number of nodes is more than 300.
    if (VERSION_SAMPLING_DATAVALUES)
      return randInt(SAMPLING_INTERVAL_DATAVALUES/2, SAMPLING_INTERVAL_DATAVALUES) * SAMPLING_UNIT;
    else
      return SAMPLING_INTERVAL_DATAVALUES * SAMPLING_UNIT;
  }

  public int randInt(int min, int max) {
    // copied from stack overflow 
    // NOTE: This will (intentionally) not run as written so that folks
    // copy-pasting have to think about how to initialize their
    // Random instance.  Initialization of the Random instance is outside
    // the main scope of the question, but some decent options are to have
    // a field that is initialized once and then re-used as needed or to
    // use ThreadLocalRandom (if using at least Java 1.7).
    Random rand = new Random();

    // nextInt is normally exclusive of the top value,
    // so add 1 to make it inclusive
    return rand.nextInt((max - min) + 1) + min;
  }

  public void send_dist_slapfile(DistSlabfile dsf, Location2D dst_loc, NetAddress dst_na, int net_id) {
    MessageSlabFile msgsf_tx = new MessageSlabFile(dsf,
                                                   0,
                                                   0,
                                                   net_id,
                                                   net_id,
                                                   myNode.getLocation2D());
    
    SGPWrapperMessage msgSGP_tx = new SGPWrapperMessage(msgsf_tx,
                                                        dst_loc,
                                                        0,
                                                        JistAPI.getTime());
    netEntity.send(msgSGP_tx,
                   dst_na,
                   routingProtocolIndex,
                   Constants.NET_PRIORITY_NORMAL,
                   (byte) TTL);  

    //this.stats.incrementValue("SEND_PACKET", 1);
    stats_increase_value("SEND_PACKET", 1, msgsf_tx.size());
  }

  public void send_datavalue(double sensedValue, Location2D dst_loc, NetAddress dst_na, int net_id) {
    MessageDataValue msgdv_tx = new MessageDataValue(sensedValue,
                                                     0,
                                                     0,
                                                     net_id,
                                                     net_id,
                                                     myNode.getLocation2D());
    
    SGPWrapperMessage msgSGP_tx = new SGPWrapperMessage(msgdv_tx,
                                                        dst_loc,
                                                        0,
                                                        JistAPI.getTime());
    netEntity.send(msgSGP_tx,
                   dst_na,
                   routingProtocolIndex,
                   Constants.NET_PRIORITY_NORMAL,
                   (byte) TTL);     

    this.stats.incrementValue("SEND_PACKET", 1);
  }
  
  private void stats_increase_value(String str_key, int value, int msg_sz) {
    if (msg_sz / BYTE_PER_MSG != 0) 
      value *= (int) Math.ceil(msg_sz / ((double) BYTE_PER_MSG)); 
    this.stats.incrementValue(str_key, value);
  }
  
  private int location2cluster(Location2D loc) {
    int cluster_size_x = Driver_SampleP2P.field_x/Driver_SampleP2P.cluster_number_x;
    int cluster_size_y = Driver_SampleP2P.field_y/Driver_SampleP2P.cluster_number_y;
    int x = (int) loc.getX()/cluster_size_x;
    if (x == Driver_SampleP2P.cluster_number_x) x = x - 1;
    int y = (int) loc.getY()/cluster_size_y;
    if (y == Driver_SampleP2P.cluster_number_y) y = y - 1;
    return y*Driver_SampleP2P.cluster_number_x + x;
  }
  
  private void save_message(DistSlabfile sf, Location2D src_loc) {
    // the cluster number of the sender
    int src_sn = location2cluster(src_loc);
    if (Driver_SampleP2P.DEBUG) 
      System.out.print("\n ---- node cluster # " + myNode.cluster_number + " ----- src cluster: " + src_sn + " x : " + src_loc.getX() + " y: " + src_loc.getY());
    
    if (myNode.cluster_number == src_sn - 1) {
      // last row 
      if (Driver_SampleP2P.DEBUG) System.out.println(" save next");
      myNode.dis_sf_next = sf;
      return;
    }
    
    if (src_sn == myNode.cluster_number + Driver_SampleP2P.cluster_number_x) {
      // from the below cluster to the upper cluster
      if (Driver_SampleP2P.DEBUG) System.out.println(" save up");
      myNode.dis_sf_up = sf;
    }
  }
  
  /* *****************************************
   * SWANS network's stack hook-up interfaces *
   * **************************************** */
    
  /**
   * Set network entity.
   *
   * @param netEntity network entity
   */
  // #Besim same
  public void setNetEntity(NetInterface netEntity) {
    this.netEntity = netEntity;
  } 
    
  /**
   * Return self-referencing APPLICATION proxy entity.
   *
   * @return self-referencing APPLICATION proxy entity
   */
  // #Besim same   
  public AppInterface getAppProxy() {
    return (AppInterface)self;
  } 
  
  private void write_target(Target tar) {
    FileWriter fw = null;
    try {
      fw = new FileWriter(target_file,true); //the true will append the new data
      for (int i = 0; i < tar.target.size(); i++) {
        Window win = (Window) tar.target.get(i);
        fw.write(JistAPI.getTime()/Constants.MINUTE + " " + i + " " + win.l + " " + win.r + " " + win.h + " " + win.score + "\n");//appends the string to the file
      }
      fw.close();
    } catch (IOException e) {
      //exception handling left as an exercise for the reader
      e.printStackTrace();
      System.err.println("IOException: " + e.getMessage());
    } finally {
      if (fw != null)
        try {
          fw.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
    }
    time++;
  }
}
