/*
 * Driver_SampleP2P.java
 * @version 1.0
 *
 * Created on April 15, 2008, 1:00 PM
 *
 * @author Oliviu C. Ghica, Northwestern University
 */

package sidnet.stack.users.sample_p2p.driver;

import java.io.*;
import sidnet.core.misc.HierarchicalNode; // Hierarchical nodes
import sidnet.core.interfaces.ColorProfile; // set the sink node's color
// import sidnet.core.interfaces.ColorProfile.ColorBundle; 
import sidnet.stack.users.sample_p2p.experiments.ExperimentData_SD; // set the experiment data and constants 
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.Area;
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.Objects;
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.Stripline_kMaxRS;
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.centralized_kMaxRS;
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.distributed_kMaxRS;
import jist.swans.misc.Mapper; 
import jist.swans.misc.Location; 
import jist.swans.misc.Util; 
import jist.swans.net.PacketLoss; 
import jist.swans.net.NetIp; 
import jist.swans.net.NetAddress; 

import sidnet.core.gui.SimGUI;
import sidnet.colorprofiles.ColorProfileGeneric;
import sidnet.core.gui.PanelContext;
// import sidnet.stack.std.routing.HierarchicalRouting.HierarchicalRouting;

import java.awt.Color;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

import jist.runtime.JistAPI; 
import jist.swans.Constants;

import sidnet.core.simcontrol.SimManager;
import sidnet.core.interfaces.GPS;
import jist.swans.mac.MacAddress; 

import jist.swans.radio.RadioInfo; 
import jist.swans.field.Field; 
import jist.swans.field.Mobility; 
import jist.swans.field.Placement; 
import jist.swans.field.Spatial; 
import jist.swans.field.Fading; 
import jist.swans.field.PathLoss; 
import sidnet.stack.users.sample_p2p.app.AppSampleP2P;

import sidnet.models.deployment.models.discrepancy.DiscrepancyControlledPlacement;
import sidnet.models.energy.batteries.Battery;
import sidnet.models.energy.batteries.IdealBattery;
import sidnet.models.energy.energyconsumptionmodels.EnergyManagementImpl;
import sidnet.models.energy.energyconsumptionmodels.EnergyConsumptionModel;
import sidnet.models.energy.energyconsumptionmodels.EnergyConsumptionModelImpl;
import sidnet.core.gui.GroupSelectionTool;
import sidnet.core.gui.TopologyGUI;
import sidnet.stack.std.mac.ieee802_15_4.Mac802_15_4Impl;
import sidnet.stack.std.mac.ieee802_15_4.Phy802_15_4Impl;

import sidnet.core.misc.GPSimpl;
import sidnet.core.misc.Location2D;
import sidnet.core.misc.LocationContext;
import sidnet.core.misc.Node;
import sidnet.models.energy.batteries.BatteryUtils;
import sidnet.models.energy.energyconsumptionmodels.EnergyManagement;
import sidnet.models.energy.energyconsumptionparameters.ElectricParameters;
import sidnet.models.energy.energyconsumptionparameters.EnergyConsumptionParameters;
import sidnet.models.senseable.phenomena.GenericDynamicPhenomenon;
import sidnet.models.senseable.phenomena.PhenomenaLayerInterface;
import sidnet.utilityviews.statscollector.StatsCollector;
import sidnet.stack.std.routing.heartbeat.HeartbeatProtocol;
import sidnet.stack.std.routing.shortestgeopath.ShortestGeoPathRouting;
import sidnet.utilityviews.energymap.EnergyMap;
import sidnet.utilityviews.statscollector.ExperimentData;
import sidnet.utilityviews.statscollector.StatEntry_EnergyLeftPercentage;
import sidnet.utilityviews.statscollector.StatEntry_GeneralPurposeContor;
import sidnet.utilityviews.statscollector.StatEntry_PacketDeliveryLatency;
import sidnet.utilityviews.statscollector.StatEntry_PacketReceivedContor;
import sidnet.utilityviews.statscollector.StatEntry_PacketReceivedPercentage;
import sidnet.utilityviews.statscollector.StatEntry_PacketSentContor;
import sidnet.utilityviews.statscollector.StatEntry_Time;

public class Driver_SampleP2P {
  private static String file_name = "ran_obj";
  
  public static boolean DEBUG = false;
  public static boolean DEBUG_ALGORITHM = false;
  
  static Location2D[] cHeads = null;  
  public static TopologyGUI topologyGUI = new TopologyGUI();
  public static int nodes, field_x, field_y, coverage_x, coverage_y, cluster_number_x, cluster_number_y, time;
  // public static boolean centralized = false;
  public static Organization organization = Organization.UNKNOWN;
  public static ExperimentData experimentData = null;
  /** Define the battery-type for the nodes 75mAh should give enough juice for 24-48h */
  public static Battery battery = new IdealBattery(BatteryUtils.mAhToMJ(75, 3), 3);
  
  // @TOP modification
  public static int sink_index = -1;
  // public static int number_of_cluster = -1; 
  public static int cluster_sink = -1;
  public static Location2D sink_location = new Location2D(0, 0);
  public static Location2D[] headcluster_location = null;
  public static int coverage_size = -1;
  public static int k = -1;
  public static InputReading inputreading = InputReading.UNKNOWN;
  public static Vector<Objects> fixed_objects = null;
  // set the sink node at the center of the field
  
  /** Define the power-consumption characteristics of the nodes, based on Mica Mote MPR500CA */
  // @TOP
  // for Telosb Mote
  public static EnergyConsumptionParameters eCostParam = new EnergyConsumptionParameters(
    new ElectricParameters(   0.5,   // ProcessorCurrentDrawn_ActiveMode [mA],
                              0.0026,   // ProcessorCurrentDrawn_SleepMode [mA],
                              17.4,   // RadioCurrentDrawn_TransmitMode [mA],
                              19.7,   // RadioCurrentDrawn_ReceiveMode [mA],
                              0.365,   // RadioCurrentDrawn_ListenMode [mA],
                              0.001,   // RadioCurrentDrawn_SleepMode [mA],
                              0.909,   // SensorCurrentDrawn_ActiveMode [mA]
                              0.0006   // SensorCurrentDrawn_PassiveMode [mA]
      ),
    battery.getVoltage());

  /** This is the entry point in the program */
  public static void main(String[] args)
  {
    /** Command line arguments is the best way to configure run-time parameters, for now */        
    // @TOP
    System.out.println("now args.length " + args.length);
    int i = 0;
    for (String str: args) {
      System.out.println(i + ": " + str);
      i++;
    }
    
    if(args.length != 11)
    { 
      //                                                                  0                     1       2             3                        
      System.out.println("syntax: swans driver.Driver_SampleP2P_w802_15_4 <max-simulation time> <nodes> <field-x [m]> <field-y [m]> ");
      System.out.println("    eg: swans driver.Driver_SampleP2P_w802_15_4 50000                 500     100           100           ");
      //                          4            5            6                         7         8          9     10               
      System.out.println("con   : <coverage_x> <coverage_y> <centralized/distributed> <alpha_x> <alpha_y>  <k>   <random/fixed>");
      System.out.println("eg    : 75           75           c/d/s                     <1-10>    <1-10>     5     r/f");
      System.out.println("0. <max-simulation time>:");
      System.out.println("1. <nodes>: the number of nodes");
      System.out.println("2. <field-x [m]>: the length of the field -> field-size = field-x * field-y");
      System.out.println("3. <field-y [m]>: the length of the field -> field-size = field-x * field-y");
      System.out.println("4. <coverage_x>: the coverage size of the rectangle");
      System.out.println("5. <coverage_y>: the coverage size of the rectangle");
      System.out.println("6. <centralized/distributed/strip>: c/d/s");
      System.out.println("7. <alpha_x>: the number of clusters/strip -> the total number of cluster = alpha_x * alpha_y");
      System.out.println("8. <alpha_y>: the number of clusters/strip -> the total number of cluster = alpha_x * alpha_y");
      System.out.println("9. <k>: the number of 'k' MaxRS");
      System.out.println("10. <'r'andom/'f'ix/'rs'ensed value>: 'r'andom every thing/'f'ixed sensor location and sensed values/ random only the sensed values");
      return;
    }

    System.out.println("Debug? = " + Driver_SampleP2P.DEBUG);
    System.out.println("Driver initialization started ... ");
    experimentData = new ExperimentData_SD();
    
    /* Parse command line arguments */
    // @TOP
    if (args[0].matches("[0-9]+") && args[1].matches("[0-9]+") && args[2].matches("[0-9]+") && 
        args[3].matches("[0-9]+") && args[4].matches("[0-9]+") && args[5].matches("[0-9]+") && 
        args[7].matches("[0-9]+") && args[8].matches("[0-9]+") && args[9].matches("[0-9]+")) {
      time   = Integer.parseInt(args[0]);
      nodes  = Integer.parseInt(args[1]);
      field_x = Integer.parseInt(args[2]);
      field_y = Integer.parseInt(args[3]);
      coverage_x = Integer.parseInt(args[4]);
      coverage_y = Integer.parseInt(args[5]);
      cluster_number_x = Integer.parseInt(args[7]);
      cluster_number_y = Integer.parseInt(args[8]);
      k = Integer.parseInt(args[9]);
    } else 
      return;

    if (args[10].matches("f") || args[10].matches("rs")) {
      if (args[10].matches("f")) {
        inputreading = InputReading.FIX_EVERYTHING; 
      } else if (args[11].matches("rs")) {
        inputreading = InputReading.FIX_LOCATION;
      }
      
      file_name += String.valueOf(nodes) + ".txt"; 
      File inputFile = new File(file_name);
      if(!inputFile.isFile() || !inputFile.exists()) {
        System.out.println("The input file (" + file_name + ") does not exist.");
        System.exit(0);
      } 
      
      fixed_objects = readInput(inputFile, null, null);
    } else if (args[11].matches("r")) {
      inputreading = InputReading.RANDOM_EVERYTHING;
    }
    
    // @TOP
    if (args[6].matches("c")) {
      organization = Organization.CENTRALIZED; 
    } else if (args[6].matches("d")) {
      organization = Organization.CLUSTER;
    } else if (args[6].matches("s")) {
      organization = Organization.STRIP_LINE;
    }

    /** Computing some statistics basic */
    float density = nodes / (float)(field_x/1000.0 * field_y/1000.0);
    System.out.println("time        = "+time+" seconds");
    System.out.println("nodes       = "+nodes);
    System.out.println("size        = "+field_x+" x "+field_y);
    // @TOP
    System.out.println("organization = " + organization);
    System.out.println("cluster number x = " + cluster_number_x);
    System.out.println("cluster number y = " + cluster_number_y);
    System.out.println("coverage_x = " + coverage_x);
    System.out.println("coverage_y = " + coverage_y);
    System.out.println("k = " + k);
    System.out.print("Creating simulation nodes ... ");

    /** Create the simulation */
    System.out.println("create simulation");
    if (organization == Organization.CENTRALIZED) {
      centralized_kMaxRS.setField(field_x, field_y);
      centralized_kMaxRS.setCoverage(coverage_x, coverage_y);
    } else if (organization == Organization.CLUSTER){
      distributed_kMaxRS.setField(field_x, field_y);
      distributed_kMaxRS.setCoverage(coverage_x, coverage_y);
      // TODO: the number of cluster -> cluster_x, cluster_y 
      distributed_kMaxRS.setNumberOfClusterxy(cluster_number_x, cluster_number_y);
    } else if (organization == Organization.STRIP_LINE) {
      Stripline_kMaxRS.setField(field_x, field_y);
      Stripline_kMaxRS.setCoverage(coverage_x, coverage_y);
      // for the stripline organization, we need to use the number_of_cluster as the number of 
      // stripline 
      
      // for the stripline organization, we need to use the cluster_x as the number of stripline
      // Stripline_kMaxRS.setNumberOfStripline(number_of_cluster);
      Stripline_kMaxRS.setNumberOfStripline(cluster_number_x);
    } else {
      System.out.println("\n\n ------------- unknown organization -------------------- \n\n");
      System.exit(0);
    }
      
    Field f = createSim(nodes, field_x, field_y);
    
    System.out.println("Average density = "+f.computeDensity()*1000*1000+"/km^2");
    System.out.println("Average sensing = "+f.computeAvgConnectivity(true));
    System.out.println("Average receive = "+f.computeAvgConnectivity(false));

    /** Indicates WHEN the JiST simulation should self-terminate (automatically) */
    JistAPI.endAt(time * Constants.SECOND); /* so it will self-terminate after "time" seconds. Not the way we specify the unit of time */

    System.out.println("Driver initialization complete!");     
  } 

  /**
   * Initialize simulation environment and field
   *
   * @param nodes number of nodes
   * @param length length of field
   * @return simulation field
   */
  public static Field createSim(int nodes, int field_length_x, int field_length_y)
  {
    System.out.println("[Driver_SampleP2P_w802_15_4] : createSim()");  

    /** Launch the SIDnet main graphical interface and set-up the title */       
    SimGUI simGUI = new SimGUI();
    simGUI.appendTitle("Driver_SampleP2P_w802_15_4");
    
    /** Internal stuff: configure and start the simulation manager. Hook up control for GUI panels*/
    SimManager simManager = new SimManager(simGUI, null, SimManager.DEMO);
    
    /** Configure the SWANS: */
    
    /** Nodes deployment: random (but it can be XML-based, grid, manual place, air-dropped, etc */
    //    public Location2D(float x, float y)
    Location.Location2D bounds = new Location.Location2D(field_length_x, field_length_y);
    Placement placement = new Placement.Random(bounds);
    // #Besim
    // Placement placement = new DiscrepancyControlledPlacement(0.02, length, length, nodes, 1); 
    
    /** Nodes mobility: static (but nodes can move if you need to */
    Mobility mobility   = new Mobility.Static();
    
    /** Some other internals: Spatial configuration */
    Spatial spatial = new Spatial.HierGrid(bounds, 5);
    Fading fading = new Fading.None();
    PathLoss pathloss = new PathLoss.FreeSpace();
    Field field = new Field(spatial, fading, pathloss, mobility, Constants.PROPAGATION_LIMIT_DEFAULT);

    /** Configure the radio environment properties */
    RadioInfo.RadioInfoShared radioInfoShared = RadioInfo.createShared(
      Constants.FREQUENCY_DEFAULT, 40000 /* BANDWIDTH bps - it will be overloaded when using 802_15_4  */,
      0 /* dBm for Mica Z */, Constants.GAIN_DEFAULT,
      Util.fromDB(Constants.SENSITIVITY_DEFAULT) /* -94dBm */, Util.fromDB(Constants.THRESHOLD_DEFAULT),
      Constants.TEMPERATURE_DEFAULT, Constants.TEMPERATURE_FACTOR_DEFAULT, Constants.AMBIENT_NOISE_DEFAULT);

    /** Build up the networking stack: APP, NETWORK, MAC
     *  Technically, at the Network Layer you may have several "protocols". 
     *  We keep a mapping of these protocols (indexed) so that a packet may be forwarded to the proper protocol to be handled */
    // @TOP protocol
    Mapper protMap = new Mapper(Constants.NET_PROTOCOL_MAX);
    protMap.mapToNext(Constants.NET_PROTOCOL_HEARTBEAT); // Constants.NET_PROTOCOL_HEARTBEAT is just a numerical value to uniquely identify (index) one of the protocols (the node discovery one)
    protMap.mapToNext(Constants.NET_PROTOCOL_INDEX_1); // and this will be the other protocol, which is, in this case, a shortest-path routing protocol.
    
    /** We'll assume no packet loss due to "random" conditions. Packets may still be lost due to collisions though
     *  This should be the case when developing the first-time implementation, then you can remove this constraint if you want to test your rezilience 
     */
    PacketLoss pl = new PacketLoss.Zero();

    /* ******************************************
     * Create the SIDnet-specific simulation environment  *
     * ******************************************/
    
    /* Creating the SIDnet nodes */
    // HierarchicalNode[] myNode = new HierarchicalNode[nodes];
    HierarchicalNode[] myNode = new HierarchicalNode[nodes];
    LocationContext fieldContext = new LocationContext(field_length_x, field_length_y);
    
    /** 
     * StatsCollector Hook-up - to allow you to see a quick-stat including elapsed time, number of packet lost, and so on. 
     * Also used to perform run-time logging 
     **/
    StatsCollector statistics;
    if (experimentData != null)
      // create the text file with the data in it
      statistics = new StatsCollector(myNode, 0, experimentData);
    else {
      // length -> does not use
      statistics = new StatsCollector(myNode, field_length_x, 0, 30 * Constants.SECOND);
    }
    
    statistics.monitor(new StatEntry_Time());
    statistics.monitor(new StatEntry_PacketSentContor("DATA"));
    statistics.monitor(new StatEntry_PacketReceivedContor("DATA"));
    statistics.monitor(new StatEntry_PacketReceivedPercentage("DATA"));
    statistics.monitor(new StatEntry_PacketDeliveryLatency("DATA", StatEntry_PacketDeliveryLatency.MODE.MAX));
    statistics.monitor(new StatEntry_GeneralPurposeContor("SEND_PACKET"));
    statistics.monitor(new StatEntry_GeneralPurposeContor("RECEIVE_PACKET"));
    statistics.monitor(new StatEntry_GeneralPurposeContor("SINK_RECEIVED_TARGET"));
    statistics.monitor(new StatEntry_GeneralPurposeContor("SINK_RECEIVED_DATAVALUES"));
    statistics.monitor(new StatEntry_GeneralPurposeContor("MESSAGE_HOPS"));
    statistics.monitor(new StatEntry_EnergyLeftPercentage("A", StatEntry_EnergyLeftPercentage.MODE.AVG));
    
    /** Create the sensor nodes (each at a time). Initialize each node's data and network stack */
    for(int i=0; i<nodes; i++) {
      myNode[i] = createNode(i, field, placement, protMap, radioInfoShared, pl, pl, 
                             simGUI.getSensorsPanelContext(), fieldContext, simManager, 
                             statistics, topologyGUI);
      // #Besim
      // myNode[i] = (HierarchicalNode) createNode(i, field, placement, protMap, radioInfoShared, pl, pl, simGUI.getSensorsPanelContext(), fieldContext, simManager, statistics, topologyGUI, length);   
    }
    
    simManager.registerAndRun(statistics, simGUI.getUtilityPanelContext2()); // Indicate where do you want this to show up on the GUI
    simManager.registerAndRun(topologyGUI, simGUI.getSensorsPanelContext());
    topologyGUI.setNodeList(myNode);
    
    /** Configuring the sensorial layer - give the node something to sense, measure */
    PhenomenaLayerInterface phenomenaLayer = new GenericDynamicPhenomenon(); // but it can be something else, such as a moving-objects field
    // PhenomenaLayerInterface phenomenaLayer = new GenericDynamicPhenomenon(((ExperimentData_SD)experimentData).getLong("PHENOMENON_FREQ")*Constants.MILLI_SECOND); // but it can be something else, such as a moving-objects field
    simManager.registerAndRun(phenomenaLayer,simGUI.getSensorsPanelContext());     // needs to be done ... internals
    // PhenomenaLayerInterface phenomenaLayer2 = new GenericDynamicPhenomenon();   // but it can be something else, such as a moving-objects field
    // simManager.registerAndRun(phenomenaLayer2,simGUI.getSensorsPanelContext());     // needs to be done ... internals    
    /* #Besim        
       //Generic CLuster Generation Code
       int numberOfClusters = experimentData.getInt("CLUSTER_COUNT");
       int totalClusters = numberOfClusters*numberOfClusters;
       cHeads = getClusterHeadLocations(length, numberOfClusters);
       System.out.println("printin the heads 25");
       for(int i=0; i < totalClusters;i++)
       System.out.println(cHeads[i].getX()+" "+cHeads[i].getY());
    */

    /** All the nodes will measure the same environment in this case, but this is not a limitation. You can have them heterogeneous */
    for (int i = 0; i < nodes; i++)
      myNode[i].addSensor(phenomenaLayer);

    /** Allow simManager to handle nodes' GUI (internals)*/
    simManager.register(myNode);

    /** EnergyMap hookup - give an overall view of the energy levels in the networks */
    EnergyMap energyMap = new EnergyMap(myNode); 
    simManager.registerAndRun(energyMap, simGUI.getUtilityPanelContext1()); // Indicate where do you want this to show up on the GUI
    
    /** Add GroupInteraction capability - if you may want to be able to select a group of nodes */
    GroupSelectionTool gst = new GroupSelectionTool(myNode);
    simManager.registerAndRun(gst, simGUI.getSensorsPanelContext());
    myNode[0].getNodeGUI().setGroupSelectionTool(gst); // internals
    
   // if (experimentData != null) {
   //   ((ExperimentData_SD)experimentData).setSinkAtLocation( sink_location);
   //   ((ExperimentData_SD)experimentData).setSinkAddress(myNode, sink_index);
   // }

    // set the sink node
    ColorProfileGeneric colorProfileGeneric = new ColorProfileGeneric();
    Location2D sinkLoc = sink_location;    
    sink_index = getNodeIndex(sink_location, myNode);
    sink_location = myNode[sink_index].getLocation2D();
    myNode[sink_index].sink_node = true;
    sinkLoc = myNode[sink_index].getLocation2D();
    if (organization == Organization.CENTRALIZED) {
      myNode[sink_index].setupObjects(nodes);
    } 
    
    System.out.println("Driver: sink id: " + myNode[sink_index].getID());
    System.out.println("Driver: sink location: " + sink_location);    
    myNode[sink_index].getNodeGUI().colorCode.mark(colorProfileGeneric, ColorProfileGeneric.SINK, ColorProfile.FOREVER);
    if (experimentData != null)
      ((ExperimentData_SD)experimentData).setSinkAtLocation(myNode, sinkLoc);//Center(myNode, fieldLength);    

    if (organization == Organization.CENTRALIZED) {
      // set the node sink address and sink location of all nodes 
      System.out.println("setup the centralized network");
      for (int i = 0; i < nodes; i++) {
        myNode[i].sink_address = myNode[sink_index].getIP();
        myNode[i].sink_location = myNode[sink_index].getLocation2D();
      }    
      if (DEBUG) {
        for (int i = 0; i < nodes; i++) System.out.println(i + " " + myNode[i]);
      }
    } else if (organization == Organization.CLUSTER) {

      System.out.println("setup the distributed clusters");    
      //Generic CLuster Generation Code
      int totalClusters = cluster_number_x * cluster_number_y;
      //get the cluster heads approximate locations
      cHeads = getClusterHeadLocations(field_length_x, field_length_y, cluster_number_x, cluster_number_y);
      // if (DEBUG) System.out.println("printin the heads 25");
      for(int i=0; i < totalClusters; i++)
        System.out.println(cHeads[i].getX()+" "+cHeads[i].getY());
      
      double[] cHDistances = new double[totalClusters];
      int[] cHPositions = new int[totalClusters];
      
      for(int i=0; i < totalClusters; i++) {
        cHDistances[i] = Double.MAX_VALUE;
        cHPositions[i] = -1;
      }
  
      // save the index of all cluster heads
      for(int i = 0 ; i < nodes; i++) {
        Location2D nodeLoc = myNode[i].getLocation2D();
        for(int j = 0 ; j < totalClusters; j++) {
          if(cHeads[j].distanceTo(nodeLoc) < cHDistances[j]) {
            cHPositions[j] = i;
            cHDistances[j] = cHeads[j].distanceTo(nodeLoc);
          }
        }
      }
      
      // after finding cluster heads for each cluster, locate where sink is
      //      double sinkDist = Double.MAX_VALUE;
      //      int sinkPos = -1;
      //      for(int i = 0; i < nodes; i ++) {
      //        if(myNode[i].getLocation2D().distanceTo(sinkLoc) < sinkDist){
      //          sinkDist = myNode[i].getLocation2D().distanceTo(sinkLoc);
      //          sinkPos = i;
      //        }
      //      }
      //      
      //      System.out.println("sinkPos = " + sinkPos + " sink_index = " + sink_index);
      
      // print cluster number that has the sink
      int clusterThatHasSink = (int)(sinkLoc.getX()/(field_length_x/cluster_number_x))+
        (int)(sinkLoc.getY()/(field_length_y/cluster_number_y)) * cluster_number_y;
      if (DEBUG) System.out.println("sink cluster is "+clusterThatHasSink);
      
      // sink is located in the myNode array with sinkPos index, set its level
      int initialLevel = 0;
      myNode[sink_index].level = initialLevel;
      myNode[sink_index].sink_node = true;
      myNode[sink_index].cluster_number = clusterThatHasSink;
      
      if (DEBUG) {
        for (int i = 0; i < cHPositions.length; i++)
          System.out.println("i: " + i + " ch_position: " + cHPositions[i]);
      }
      
      // Now create CH tree by setting up their parents and levels
      // starting from the cluster head in the same cluster as the sink node
      myNode[cHPositions[clusterThatHasSink]].upLocation = sinkLoc;
      myNode[cHPositions[clusterThatHasSink]].upAddress =  myNode[sink_index].getIP();
      myNode[cHPositions[clusterThatHasSink]].level = 1;
      myNode[cHPositions[clusterThatHasSink]].cluster_head = true;
      myNode[cHPositions[clusterThatHasSink]].cluster_number = clusterThatHasSink;
      Driver_SampleP2P.cluster_sink = clusterThatHasSink;
      myNode[cHPositions[clusterThatHasSink]].getNodeGUI().colorCode.mark(new ColorProfileGeneric(), ColorProfileGeneric.CLUSTERHEAD, ColorProfile.FOREVER);
      LinkedList<Integer> chStack = new LinkedList<Integer>();
      chStack.add(clusterThatHasSink);
      System.out.println("test ");
      while(!chStack.isEmpty()) {
        int currentCluster = (Integer) chStack.removeFirst();
        int yLevel = currentCluster/cluster_number_x;
        
        if((int)((currentCluster - 1)/cluster_number_x) == yLevel &&
           currentCluster - 1 >= 0 &&
           myNode[cHPositions[currentCluster - 1]].level == -1) {
          // unnecessary 
          myNode[cHPositions[currentCluster - 1]].nextLocation = myNode[cHPositions[currentCluster]].getLocation2D();
          myNode[cHPositions[currentCluster - 1]].nextAddress = myNode[cHPositions[currentCluster]].getIP();
          myNode[cHPositions[currentCluster - 1]].level = myNode[cHPositions[currentCluster]].level+1;
          myNode[cHPositions[currentCluster - 1]].cluster_head = true;
          myNode[cHPositions[currentCluster - 1]].getNodeGUI().colorCode.mark(new ColorProfileGeneric(), ColorProfileGeneric.CLUSTERHEAD, ColorProfile.FOREVER);
          if (DEBUG) System.out.println("yLevel: " + yLevel + " cl:1 cur: " + currentCluster + " " + (currentCluster - 1));
          chStack.addLast(currentCluster - 1);
        }
        
        if((int)((currentCluster + 1)/cluster_number_x) == yLevel &&
           myNode[cHPositions[currentCluster + 1]].level == -1){
          // 0 <- 1 <- 2
          // 3 <- 4 <- 5
          // 6 <- 7 <- 8          
          myNode[cHPositions[currentCluster + 1]].nextLocation = myNode[cHPositions[currentCluster]].getLocation2D();
          myNode[cHPositions[currentCluster + 1]].nextAddress = myNode[cHPositions[currentCluster]].getIP();
          myNode[cHPositions[currentCluster + 1]].level = myNode[cHPositions[currentCluster]].level+1;
          myNode[cHPositions[currentCluster + 1]].cluster_head = true;
          myNode[cHPositions[currentCluster + 1]].getNodeGUI().colorCode.mark(new ColorProfileGeneric(), ColorProfileGeneric.CLUSTERHEAD, ColorProfile.FOREVER);
          if (DEBUG) System.out.println("yLevel: " + yLevel + " cl:2 cur: " + currentCluster + " " + (currentCluster + 1));
          chStack.addLast(currentCluster + 1);
        }
        
        if(currentCluster + cluster_number_x < totalClusters &&
           myNode[cHPositions[currentCluster + cluster_number_x]].level==-1){
          // except the last row
          // 
          // 0 1 2
          // ^ ^ ^
          // 3 4 5 
          // ^ ^ ^ 
          // 6 7 8
          myNode[cHPositions[currentCluster + cluster_number_x]].upLocation = myNode[cHPositions[currentCluster]].getLocation2D();
          myNode[cHPositions[currentCluster + cluster_number_x]].upAddress = myNode[cHPositions[currentCluster]].getIP(); 
          myNode[cHPositions[currentCluster + cluster_number_x]].level = myNode[cHPositions[currentCluster]].level+1;
          myNode[cHPositions[currentCluster + cluster_number_x]].cluster_head = true;
          myNode[cHPositions[currentCluster + cluster_number_x]].getNodeGUI().colorCode.mark(new ColorProfileGeneric(), ColorProfileGeneric.CLUSTERHEAD, ColorProfile.FOREVER);
          if ((currentCluster + cluster_number_x)%cluster_number_x != 0) {
            // the first column does not have to set the next location pointer
            myNode[cHPositions[currentCluster + cluster_number_x]].nextLocation = myNode[cHPositions[currentCluster + cluster_number_x - 1]].getLocation2D();   
          }
          if (DEBUG) System.out.println("yLevel: " + yLevel + " cl:3 cur: " + currentCluster + " " + (currentCluster + cluster_number_x));
          chStack.addLast(currentCluster + cluster_number_x);
        }
        
        if(currentCluster - cluster_number_x >= 0 &&
           myNode[cHPositions[currentCluster - cluster_number_x]].level==-1) {
          // unnecessary           
          myNode[cHPositions[currentCluster - cluster_number_x]].upLocation = myNode[cHPositions[currentCluster]].getLocation2D();
          myNode[cHPositions[currentCluster - cluster_number_x]].upAddress = myNode[cHPositions[currentCluster]].getIP();
          myNode[cHPositions[currentCluster - cluster_number_x]].level = myNode[cHPositions[currentCluster]].level+1;
          myNode[cHPositions[currentCluster - cluster_number_x]].cluster_head = true;
          myNode[cHPositions[currentCluster - cluster_number_x]].getNodeGUI().colorCode.mark(new ColorProfileGeneric(), ColorProfileGeneric.CLUSTERHEAD, ColorProfile.FOREVER);
          if (DEBUG) System.out.println("yLevel: " + yLevel + " cl:4 cur: " + currentCluster + " " + (currentCluster - cluster_number_x));
          chStack.addLast(currentCluster - cluster_number_x);
        }
        initialLevel++;
      }
      
      // determine the cluster head location and positions/indexes
      // csize = cluster_size
      int csize_x = field_length_x/cluster_number_x;
      int csize_y = field_length_y/cluster_number_y;
      System.out.println("cluster_size: " + csize_x + " x " + csize_y);
      for (int i = 0; i < nodes; i++) {
        int x_index = (int) myNode[i].getLocation2D().getX()/csize_x;
        if (x_index == cluster_number_x) x_index = cluster_number_x -1;
        int y_index = ((int) myNode[i].getLocation2D().getY()/csize_y);
        if (y_index == cluster_number_y) y_index = cluster_number_y -1;
        int index = x_index + y_index * cluster_number_y;
        if (!myNode[i].cluster_head && !myNode[i].sink_node) {
          // System.out.print("x: " + myNode[i].getLocation2D().getX() + " y: " + myNode[i].getLocation2D().getY());
          if (DEBUG) System.out.println(" i " + i + " index: " + index + " x_index: " + x_index + " y_index: " + y_index);
          myNode[i].ch_location = myNode[cHPositions[index]].getLocation2D();
          myNode[i].ch_address = myNode[cHPositions[index]].getIP();
          myNode[i].cluster_number = index;
        } else if (myNode[i].cluster_head && !myNode[i].sink_node) {
          myNode[i].cluster_number = index;
          myNode[i].setObjectsv();
        }
      }
      
      if(DEBUG) {
        for(int i =0; i < cluster_number_x * cluster_number_y; i ++) {
          System.out.println("cluster no: "+i+" "+myNode[cHPositions[i]].getLocation2D().getX()+", "+myNode[cHPositions[i]].getLocation2D().getY());
        }
        
        for(int i = 0; i < nodes; i++) 
          System.out.println(i + " nodes: " + myNode[i]);
      }
    } else if (organization == Organization.STRIP_LINE) { // if (organization == Organization.STRIP_LINE) {
      // ----- x
      // |
      // |
      // y
      System.out.println("setup the stripline network");
      int x_length = Driver_SampleP2P.field_x/Driver_SampleP2P.cluster_number_x;
      int x_half = x_length/2;
      int y_cor = Driver_SampleP2P.field_y/2;
      System.out.println("x_half: " + x_half + " x_length: " + x_length + " y_cor: " + y_cor);
      // setup the cluster head that are in the same cluster as the sink node
      int index_prev = getNodeIndex(new Location2D(x_half, y_cor), myNode);
      myNode[index_prev].cluster_head = true;
      myNode[index_prev].nextLocation = sink_location;
      myNode[index_prev].nextAddress = myNode[sink_index].getIP();
      Driver_SampleP2P.cluster_sink = (int) myNode[sink_index].getLocation2D().getX()/x_length;
      for (int i = 1; i < Driver_SampleP2P.cluster_number_x; i++) {
        // scan from left to right starting from 0 1 2 ... Driver_SampleP2P.cluster_number_x
        int x_cor = x_half + i * x_length;
        int index = getNodeIndex(new Location2D(x_cor, y_cor), myNode);
        myNode[index].cluster_head = true;
        myNode[index].nextLocation = myNode[index_prev].getLocation2D();
        myNode[index].nextAddress = myNode[index_prev].getIP();
        index_prev = index;
      }

      // set the cluster head address and location to every node
      for (int i = 0; i < nodes; i++) {
        int cl_num = (int) myNode[i].getLocation2D().getX()/x_length;
        if (cl_num == Driver_SampleP2P.cluster_number_x) cl_num -= 1;
        myNode[i].cluster_number = cl_num;
        if (!myNode[i].cluster_head && !myNode[i].sink_node) {
          int x_cor = x_half + cl_num * x_length;
          int ch_index = getNodeIndex(new Location2D(x_cor, y_cor), myNode);
          myNode[i].ch_location = myNode[ch_index].getLocation2D();
          myNode[i].ch_address = myNode[ch_index].getIP();
        } else if (myNode[i].cluster_head && !myNode[i].sink_node ) {
          myNode[i].getNodeGUI().colorCode.mark(new ColorProfileGeneric(), ColorProfileGeneric.CLUSTERHEAD, ColorProfile.FOREVER);
        }
      }
    }

    for (int i = 0; i < nodes; i++) 
      System.out.println(">>> node: " + myNode[i]);

    /** Starts the core (GUI) engine */
    simManager.getProxy().run();
    
    System.out.println("Simulation Started");
    
    return field;
  } 
  
  /**
   * Configures each node representation and network stack
   *
   * @param int id      a numerical value to represent the id of a node. Will correspond to the IP address representation
   * @param Field       the field properties
   * @param Placement   information regarding positions length of field
   * @param Mapper      network stack mapper
   * @param RadioInfo.RadioInfoShared   configuration of the radio
   * @param plIn        property of the PacketLoss for incoming data packet
   * @param plOut       property of the PacketLoss for outgoing data packet
   * @param hostPanelContext    the context of the panel this node will be drawn
   * @param fieldContext        the context of the actual field this node is in (for GPS)
   * @param simControl          handle to the simulation manager
   * @param Battery     indicate the battery that will power this particular node
   * @param StatsCollector the statistical collector tool
   */
  public static HierarchicalNode  createNode(int id,
                                             Field field,
                                             Placement placement,
                                             Mapper protMap,
                                             RadioInfo.RadioInfoShared radioInfoShared,
                                             PacketLoss plIn,
                                             PacketLoss plOut, 
                                             PanelContext hostPanelContext,
                                             LocationContext fieldContext,
                                             SimManager simControl,
                                             StatsCollector stats,
                                             TopologyGUI topologyGUI)
  {
    /** create entities (gives a physical location) **/
    Location nextLocation = null;
    if (inputreading == InputReading.RANDOM_EVERYTHING) {
      nextLocation = placement.getNextLocation(); 
    } else {
      Objects o = fixed_objects.get(id);
      nextLocation = new Location.Location2D(o.x, o.y);
    }

    /** Create an individual battery, since no two nodes can be powered by the same battery. The specs of the battery are the same though */
    Battery individualBattery = new IdealBattery(battery.getCapacity_mJ(), battery.getVoltage());

    /** Set the battery and the energy consumption profile */
    EnergyConsumptionModel energyConsumptionModel = new EnergyConsumptionModelImpl(eCostParam, individualBattery);
    energyConsumptionModel.setID(id);
    
    /** Create the energy management unit */
    EnergyManagement energyManagementUnit = new EnergyManagementImpl(energyConsumptionModel, individualBattery);

    /** Create the node and nodeGUI interface for this node */
    // Node node = new Node(id, energyManagementUnit, hostPanelContext, fieldContext, new ColorProfileGeneric(), simControl);
    HierarchicalNode node = new HierarchicalNode(id, energyManagementUnit, hostPanelContext, fieldContext, new ColorProfileGeneric(), simControl);
    node.enableRelocation(field); // if you want to be able to relocate, by mouse, the node in the field at run time.
    //RadioNoiseIndep radio = new RadioNoiseIndep(id, radioInfoShared); // uncomment this if you want noisy environments
    // set the node sensed value
    if (inputreading == InputReading.FIX_LOCATION || 
        inputreading == InputReading.FIX_EVERYTHING)
      node.fix_sv = fixed_objects.get(id).weight;
    
    /** Put a 'GPS' (must to) to obtain the location information (for this assignment, for geographical purposes only 
     *  Now, really, this is not a GPS per-se, just a 'logical' way of obtaining location information from the simulator
     */
    GPS gps = new GPSimpl(new Location2D((int)nextLocation.getX(), (int)nextLocation.getY()));
    gps.configure(new LocationContext(fieldContext));
    node.setGPS(gps);    
    
    /* *** Configuring the ISO layers - more or less self-explanatory *** */
    /* APP layer configuration */
    // change this node
    AppSampleP2P app = new AppSampleP2P(node, Constants.NET_PROTOCOL_INDEX_1, stats, (ExperimentData_SD) experimentData);

    if (app.topologyGUI == null)
      app.topologyGUI = topologyGUI;

    /* NET layer configuration - this is where the node gets its "ip" address */
    NetIp net = new NetIp(new NetAddress(id), protMap, plIn, plOut);

    /* ROUTING protocols configuration */
    // @TOP           
    HeartbeatProtocol heartbeatProtocol 
      = new HeartbeatProtocol(net.getAddress(), 
                              node,
                              hostPanelContext,
                              30 * Constants.MINUTE);

    ShortestGeoPathRouting shortestGeographicalPathRouting 
      = new ShortestGeoPathRouting(node);

    // #Besim HierarchicalRouting hr = new HierarchicalRouting(node);
    node.setIP(net.getAddress());

    /* MAC layer configuration */
    Mac802_15_4Impl mac = new Mac802_15_4Impl(new MacAddress(id), radioInfoShared, node.getEnergyManagement(), node);

    /* PHY layer configuration */
    Phy802_15_4Impl phy = new Phy802_15_4Impl(id, radioInfoShared, energyManagementUnit, node, 0 * Constants.SECOND);

    /* RADIO "layer configuration */
    field.addRadio(phy.getRadioInfo(), phy.getProxy(), nextLocation);
    field.startMobility(phy.getRadioInfo().getUnique().getID());
    
    /* *** Hooking up the ISO layers *** */
    /* APP <- Routing hookup */
    shortestGeographicalPathRouting.setAppInterface(app.getAppProxy());

    /* APP -> NET hookup */
    app.setNetEntity(net.getProxy());

    /* NET<->Routing hookup */
    // @TOP
    heartbeatProtocol.setNetEntity(net.getProxy());
    shortestGeographicalPathRouting.setNetEntity(net.getProxy());
    net.setProtocolHandler(Constants.NET_PROTOCOL_INDEX_1, shortestGeographicalPathRouting.getProxy());
    net.setProtocolHandler(Constants.NET_PROTOCOL_HEARTBEAT, heartbeatProtocol.getProxy());
    // net.setMacEntity(mac); // already commented at the first place

    /* net-MAC-phy hookup */
    byte intId = net.addInterface(mac.getProxy());
    mac.setNetEntity(net.getProxy(), intId);
    mac.setPhyEntity(phy.getProxy());

    /* PHY-RADIO hookup */
    phy.setFieldEntity(field.getProxy());
    phy.setMacEntity(mac.getProxy());
    
    /* Here we actually start this node's application layer execution. It is important to observe
       that we don't actually call the app's run() method directly, but through its proxy, which allows JiST engine to actually decide when this call will
       be actually made (based on the simulation time)*/
    app.getAppProxy().run(null);

    return node;
  }

  public static Location2D[] getClusterHeadLocations(int fieldLength_x, int fieldLength_y, 
                                                     int cnx, int cny) 
  {
    Location2D[] locArray = new Location2D[cnx * cny];
    for(int i =0; i < cnx; i++) {
      for(int j = 0; j < cny; j++) {
        locArray[i*cnx + j] = new Location2D((fieldLength_x/cnx) * j + (fieldLength_x/(cnx*2)),
                                              (fieldLength_y/cnx) * i + (fieldLength_y/(cnx*2)));
      }
    }
    return locArray;
  }
  
  public static Location2D clusterHeadLocation(int clusterNo, int fieldLength, int clusterSize){
    int edgeLength = fieldLength / clusterSize;
    return new Location2D((clusterNo%clusterSize)*edgeLength + (edgeLength/2), 
                          (int)(clusterNo/clusterSize) * edgeLength + (edgeLength/2));
  }

  // find the index of the node based on its location 
  public static int getNodeIndex(Location2D loc, Node[] node_array) { 
    double distance = Double.MAX_VALUE;
    int pos = -1;
    for (int i = 0; i < nodes; i ++) {
      if(node_array[i].getLocation2D().distanceTo(loc) < distance){
        distance = node_array[i].getLocation2D().distanceTo(loc);
        pos = i;
      }  
    }
    return pos;
  }

  // process the input file from the input_generator.java
  // the format of the input file should be 
  // --------------------------------------------------------------------------------
  // the number of objects
  // objects.x objects.y objects.weight
  private static Vector<Objects> readInput(File inputFile, Area area, Area rectangle)
  {
    Vector<Objects> aListOfObjects = new Vector<Objects>();
    try
    {
      Charset charset = Charset.forName("US-ASCII");
      FileReader inputReader = new FileReader(inputFile);
      String[] lineSplit = readline(inputReader);

      int field_length_x = Integer.parseInt(lineSplit[0]);
      int field_length_y = Integer.parseInt(lineSplit[1]);
      if (err_msg_unmatched_field_length(field_length_x, field_x) ||
          err_msg_unmatched_field_length(field_length_y, field_y)) {
        inputReader.close();
        System.exit(0);
      }              

      lineSplit = readline(inputReader);
      lineSplit = readline(inputReader);
      int obj_num = Integer.parseInt(lineSplit[0]);
      if (obj_num != nodes) {
        System.out.println("The number of objects does not match.");
        System.out.println("Text file obj_num " + obj_num + " vs Input Argument nodes: " + nodes);

        inputReader.close();
        System.exit(0);
      }
      
      lineSplit = readline(inputReader);
      while (lineSplit != null) {
        aListOfObjects.add(new Objects(Short.parseShort(lineSplit[0]),
          Short.parseShort(lineSplit[1]),
          Short.parseShort(lineSplit[2])));
        lineSplit = readline(inputReader);
      }

      Collections.sort(aListOfObjects);
      
    } catch(IOException ex)
    {
      ex.printStackTrace();
    }
    return aListOfObjects;
  }
  
  private static boolean err_msg_unmatched_field_length(int read_field, int args_field) {
    if (read_field != args_field) {
      System.out.println("The field_length does not match.");
      System.out.println("Text file field_length " + read_field + " vs Input Argument fieldLength: " + args_field);
      return true;
    }              
    return false;
  }
  
  private static String[] readline(FileReader f) throws IOException {
    int ch = f.read();
    String s = new String();
    while ((char) ch != '\n' && ch != -1) {
      s += (char) ch;
      ch = f.read();
      // System.out.println("readline test ch: " + String.valueOf((int)ch));
    }
    
    if (ch != -1) {
      // System.out.println("output readline: " + s.replace("\n", ""));
      return s.trim().replace("\n", "").split(" ");
    } 
    return null;
  }
}