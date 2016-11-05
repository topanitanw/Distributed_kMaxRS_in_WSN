package sidnet.stack.users.sample_p2p.kmaxrs_centralized;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.sun.msv.driver.textui.Driver;

import sidnet.stack.users.sample_p2p.driver.Driver_SampleP2P;

public class distributed_kMaxRS {
  public static Area field;
  public static Area coverage;
  // public static int number_of_cluster;
  public static int cluster_number_x;
  public static int cluster_number_y;
  
  public static void setField(int sz) {
    field = new Area(sz, sz);
  }

  public static void setField(int field_x, int field_y) {
    field = new Area(field_y, field_x);
  }
    
  public static void setCoverage(int sz) {
    coverage = new Area(sz, sz);
  }
  
  public static void setCoverage(int coverage_x, int coverage_y) {
    coverage = new Area(coverage_y, coverage_x);
  }
      
//  public static void setNumberOfCluster(int n) {
//    number_of_cluster = n;
//  }

  public static void setNumberOfClusterxy(int cluster_x, int cluster_y) {
    cluster_number_y = cluster_y;
    cluster_number_x = cluster_x;
  }
  
  // Rx: 0
  // Tx: 2 up next
  public static DistSlabfile[] distributed_last(Vector<Objects> obj, 
                                                int k, 
                                                DictWindow dic, 
                                                int cl_id) 
  {

    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      System.out.println("obj: " + obj.size());
    }
 
    Meit meit = new Meit();
    meit.numberOfRectangle = k;
    meit.dic_window = new DictWindow(coverage, field);
    // TOP: modification

    Vector<Objects> myObjects_sorted = (Vector<Objects>) obj.clone();
    // sort the objects, add the rectangles
    Collections.sort(myObjects_sorted);   
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
//      for (Objects obj1: myObjects_sorted)
//        System.out.println("obj sorted obj x: " + obj1.x + " y: " + obj1.y + " w: " + obj1.weight);
//    }
    //System.out.println(" Object#: " + myObjects_sorted.size());

    Vector<Rectangle> myRectangles = new Vector<Rectangle>();
    object_to_rectangles(myRectangles, myObjects_sorted);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
//      for (Rectangle rec: myRectangles)
//        System.out.println("rec sorted: " + rec);
//    }
    
    Vector<Short> aListOfX1 = new Vector<Short>();
    for(int i = 0; i < myRectangles.size(); i++)
    {
      aListOfX1.add(myRectangles.get(i).x1);
      aListOfX1.add(myRectangles.get(i).x2);
    }

    Collections.sort(aListOfX1);
    Vector<Short> aListOfX = new Vector<Short>(); // xs in python code
    for(Short d : aListOfX1)
    {
      if(!aListOfX.contains(d))
        aListOfX.add(d);
    }

    IntervalTree root = meit.buildIntervalTree(0, aListOfX.size()-1, aListOfX, null);
    // System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    // System.out.println("root: " + root);
    // meit.preOrderTraverse(root, 0);
    meit.interval_tree_root = root;
    root.window = new Window(aListOfX.get(0), aListOfX.get(aListOfX.size() - 1),
                             (short) 0, (short) 0);

    Target cl2_target = meit.maxEnclosing_k(myRectangles, coverage, field, 
                                            root, meit.numberOfRectangle,
                                            null);
    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("-------------------------------------------------------");
    // System.out.println("cl2_target: " + cl2_target);
    // System.out.println("dic_window: " + meit.dic_window);
    // Vector<Window> hintervals = new Vector<Window>();
    /*
    for(short sfk : slabFile.keySet()) {
      Window sf = (Window) slabFile.get(sfk);
      hintervals.add(sf);
    }
    Collections.sort(hintervals); // unused
    for(Window h : hintervals){
      System.out.println(h.h+"-----"+h.l+ " "+h.r+" "+h.score);
    }*/
    //System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"); 

    // System.out.println("Clus2: opt interval ------------------------------------------");
    //    for(Window win : opt_interval)
    //      System.out.println("Opt " + win);
    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      meit.writeOutput("dis_cl" + cl_id + ".txt", field, coverage, myObjects_sorted, cl2_target.target);
      System.out.println("the resdis_" + cl_id + ".txt is written.");
    }
    
    Vector<Objects> values_up = new Vector<Objects>();
    Vector<Objects> values_next = new Vector<Objects>();
    int x_index = cl_id%cluster_number_x;
    int y_index = cl_id/cluster_number_y;
    int cluster_size_x = field.width/cluster_number_x;
    int cluster_size_y = field.height/cluster_number_y;
    // System.out.println("x_index: " + x_index + " y_index: " + y_index + " cluster_size: " + cluster_size + " coverage: " + coverage.width);
    for (Objects os : myObjects_sorted) {
      // System.out.print("obj: (" + os.y + ", " + os.x + ")");
      // System.out.print(" y condition: " + (y_index * cluster_size + coverage.height/2) + " | node y: " + os.y);
      if (y_index * cluster_size_y + coverage.height >= os.y) {
        values_up.add(os.clone());
      }
      // System.out.println(" x condition: " + (x_index * cluster_size + coverage.height/2) + " | node x: " + os.x);
      if (x_index * cluster_size_x + coverage.width >= os.x) {    
        values_next.add(os.clone());
      }
    }       
        
    DistSlabfile[] result = new DistSlabfile[2];
    // result[0] = new DistSlabfile(cl2_target.target, values);
    result[0] = new DistSlabfile(cl2_target.target, values_up, meit.dic_window.dic, meit.numberOfRectangle);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("result[0]: " + result[0]);
    // make a deep copy of the opt_interval
    // the clone method makes only the shallow copy of the vector and 
    // the objects inside.
    // Vector<Window> clu_3interval = new Vector<Window>();
    // for (Window win: cl2_target.target)
    //   clu_3interval.add(win.clone());
    // result[1] = new DistSlabfile(clu_3interval, values1);
    result[1] = new DistSlabfile(cl2_target.target, values_next, meit.dic_window.dic, meit.numberOfRectangle);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("result[1]: " + result[1]);        
    return result;
  }
  
  // Rx: 1
  // Tx: 1 cl_id
  public static DistSlabfile distributed_one_pkt(Vector<Objects> obj,
                                                 int k,
                                                 DistSlabfile sf,
                                                 int cl_id) 
  {

    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      System.out.println("obj: " + obj.size());
    }
 
    Meit meit = new Meit();
    meit.numberOfRectangle = k;
    meit.dic_window = new DictWindow(sf.dic, coverage, field);
    
    Target neightbor_target = new Target(k, coverage, field, true);
    for (Window win : sf.hintervals)
      neightbor_target.add_window(win, meit.dic_window);
    
    Vector<Objects> myObjects_sorted = (Vector<Objects>) obj.clone();
    // myObjects_sorted.addAll(sf.neededObj);
    for (Objects o : sf.neededObj)
      if (!myObjects_sorted.contains(o)) {
        myObjects_sorted.add(o);
      }
    // sort the objects, add the rectangles
    Collections.sort(myObjects_sorted);   
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
//      for (Objects obj1: myObjects_sorted)
//        System.out.println("obj sorted obj x: " + obj1.x + " y: " + obj1.y + " w: " + obj1.weight);
//    
//      System.out.println(" Object#: " + myObjects_sorted.size());
//      for (Objects o: myObjects_sorted) System.out.println("Objects sort: " + o);
//    }
    
    Vector<Rectangle> myRectangles = new Vector<Rectangle>();
    object_to_rectangles(myRectangles, myObjects_sorted);
    
    Vector<Short> aListOfX1 = new Vector<Short>();
    for(int i = 0; i < myRectangles.size(); i++)
    {
      aListOfX1.add(myRectangles.get(i).x1);
      aListOfX1.add(myRectangles.get(i).x2);
    }

    Collections.sort(aListOfX1);
    Vector<Short> aListOfX = new Vector<Short>(); // xs in python code
    for(Short d : aListOfX1)
    {
      if(!aListOfX.contains(d))
        aListOfX.add(d);
    }

    IntervalTree root = meit.buildIntervalTree(0, aListOfX.size()-1, aListOfX, null);
    //    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    //    System.out.println("root: " + root);
    // meit.preOrderTraverse(root, 0);
    meit.interval_tree_root = root;
    root.window = new Window(aListOfX.get(0), aListOfX.get(aListOfX.size() - 1),
                             (short) 0, (short) 0);

    Target cl2_target = meit.maxEnclosing_k(myRectangles, coverage, field, 
                                            root, meit.numberOfRectangle,
                                            neightbor_target);
    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("-------------------------------------------------------");
    // System.out.println("cl2_target: " + cl2_target);
    // System.out.println("dic_window: " + meit.dic_window);
    // Vector<Window> hintervals = new Vector<Window>();
    /*
    for(short sfk : slabFile.keySet()) {
      Window sf = (Window) slabFile.get(sfk);
      hintervals.add(sf);
    }
    Collections.sort(hintervals); // unused
    for(Window h : hintervals){
      System.out.println(h.h+"-----"+h.l+ " "+h.r+" "+h.score);
    }*/
    //System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"); 

    // System.out.println("Clus2: opt interval ------------------------------------------");
    //    for(Window win : opt_interval)
    //      System.out.println("Opt " + win);
    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      meit.writeOutput("dis_cl"+cl_id+".txt", field, coverage, myObjects_sorted, cl2_target.target);
      System.out.println("the resdis_one_pck"+cl_id+".txt is written.");
    }
    
    Vector<Objects> values_up = new Vector<Objects>();
    Vector<Objects> values_next = new Vector<Objects>();
    int x_index = cl_id%cluster_number_x;
    int y_index = cl_id/cluster_number_y;
    int cluster_size_x = field.width/cluster_number_x;
    int cluster_size_y = field.height/cluster_number_y;
    // System.out.println("x_index: " + x_index + " y_index: " + y_index + " cluster_size: " + cluster_size + " coverage: " + coverage.width);
    for (Objects os : myObjects_sorted) {
      // System.out.print("obj: (" + os.x + ", " + os.y + ")");
      // System.out.print(" y condition: " + (y_index * cluster_size + coverage.height/2) + " | node y: " + os.y);
      if (y_index * cluster_size_y + coverage.height >= os.y) {
        // System.out.print(" added up ");  
        values_up.add(os.clone());
      }
      // System.out.print(" x condition: " + (x_index * cluster_size + coverage.height/2) + " | node x: " + os.x);  
      if (x_index * cluster_size_x + coverage.width >= os.x) {
        // System.out.print(" added next ");
        values_next.add(os.clone());
      }
      // System.out.println(" | ");
    }        
        
    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      System.out.println("cluster number: " + cl_id);
      if (cl_id == 2) System.out.println("one_pkt dic: " + meit.dic_window);
    }
    
    DistSlabfile result = null;
    if (cl_id/Driver_SampleP2P.cluster_number_x >=1) {
      // cluster on the left most side
      result = new DistSlabfile(cl2_target.target, values_up, meit.dic_window.dic, meit.numberOfRectangle); 
    } else if ((cl_id < Driver_SampleP2P.cluster_number_x) || 
              ((cl_id >= (Driver_SampleP2P.cluster_number_y - 1) * Driver_SampleP2P.cluster_number_x) && 
               (cl_id < Driver_SampleP2P.cluster_number_y * Driver_SampleP2P.cluster_number_x))) {
      // cluster in the first row or the cluster in the last row
      result = new DistSlabfile(cl2_target.target, values_next, meit.dic_window.dic, meit.numberOfRectangle);
    }
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("result: " + result);
    // make a deep copy of the opt_interval
    // the clone method makes only the shallow copy of the vector and 
    // the objects inside.
    //    Vector<Window> clu_3interval = new Vector<Window>();
    //    for (Window win: cl2_target.target)
    //      clu_3interval.add(win.clone());
    // result[1] = new DistSlabfile(clu_3interval, values1);
    return result;
  }
  
  // Rx: 2
  // Tx: 1 target
  public static Target distributed_sink_pkt(Vector<Objects> obj, 
                                            int k, 
                                            DistSlabfile up_sf,
                                            DistSlabfile next_sf,
                                            int cl_id) 
  {

    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      System.out.println("sink +++++++++++++++++++++++++++++++++++++++++++++++++++++++");
      System.out.println("obj: " + obj.size());
    }
 
    Meit meit = new Meit();
    meit.numberOfRectangle = k;
    meit.dic_window = new DictWindow(up_sf.dic, coverage, field);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("up_sf " + up_sf);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("next_sf " + next_sf);    
    meit.dic_window.update_dic(next_sf.dic);
    Target neightbor_target = new Target(k, coverage, field, true);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("after update dic " + up_sf);
    int cluster_size_x = field.width/Driver_SampleP2P.cluster_number_x;
    for (Window win : next_sf.hintervals) {
//      if (cluster_size_x <= win.l) {
        neightbor_target.add_window(win, meit.dic_window);
//      }
    }
    
    int cluster_size_y = field.height/Driver_SampleP2P.cluster_number_y;
    for (Window win : up_sf.hintervals) {
//      if (cluster_size_y <= win.h) {
        neightbor_target.add_window(win, meit.dic_window);
//      }
    }
//    if (Driver_SampleP2P.DEBUG) System.out.println("Target: " + neightbor_target);
    
    // TOP: modification
    Vector<Objects> myObjects_sorted = (Vector<Objects>) obj.clone();
    if (false && Driver_SampleP2P.DEBUG_ALGORITHM)
      for (Objects o: myObjects_sorted) System.out.println("obj: " + o);
    // myObjects_sorted.addAll(up_sf.neededObj);
    // myObjects_sorted.addAll(next_sf.neededObj);
    for (Objects o : up_sf.neededObj)
      if (!myObjects_sorted.contains(o)) {
        myObjects_sorted.add(o);
      }
    
    for (Objects o : next_sf.neededObj)
      if (!myObjects_sorted.contains(o)) {
        myObjects_sorted.add(o);
      }    
    // sort the objects, add the rectangles
    Collections.sort(myObjects_sorted);   
//    if (Driver_SampleP2P.DEBUG) {
//      for (Objects obj1: myObjects_sorted)
//        System.out.println("obj sorted obj x: " + obj1.x + " y: " + obj1.y + " w: " + obj1.weight);
//    }
    //System.out.println(" Object#: " + myObjects_sorted.size());

    Vector<Rectangle> myRectangles = new Vector<Rectangle>();
    object_to_rectangles(myRectangles, myObjects_sorted);
//    if (Driver_SampleP2P.DEBUG) {
//      for (Rectangle rec: myRectangles)
//        System.out.println("rec sorted: " + rec);
//    }
    
    Vector<Short> aListOfX1 = new Vector<Short>();
    for(int i = 0; i < myRectangles.size(); i++)
    {
      aListOfX1.add(myRectangles.get(i).x1);
      aListOfX1.add(myRectangles.get(i).x2);
    }

    Collections.sort(aListOfX1);
    Vector<Short> aListOfX = new Vector<Short>(); // xs in python code
    for(Short d : aListOfX1)
    {
      if(!aListOfX.contains(d))
        aListOfX.add(d);
    }

    IntervalTree root = meit.buildIntervalTree(0, aListOfX.size()-1, aListOfX, null);
    //    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    //    System.out.println("root: " + root);
    // meit.preOrderTraverse(root, 0);
    meit.interval_tree_root = root;
    root.window = new Window(aListOfX.get(0), aListOfX.get(aListOfX.size() - 1),
                             (short) 0, (short) 0);
    
    Target cl2_target = meit.maxEnclosing_k(myRectangles, coverage, field, 
                                            root, meit.numberOfRectangle,
                                            neightbor_target);

    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      System.out.println("-------------------------------------------------------");
      System.out.println("sink before add windows from dic: " + cl2_target);
      //System.out.println("dic: " + meit.dic_window);
    }
    
    Enumeration<Window> enu = meit.dic_window.dic.keys();
    while (enu.hasMoreElements()) {
      Window key_win = enu.nextElement();
      cl2_target.add_window(key_win, null);
      Vector<Window> vwin = meit.dic_window.dic.get(key_win);
      for (Window win: vwin)
        cl2_target.add_window(win, null);
    }
    
    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      System.out.println("-------------------------------------------------------");
      meit.writeOutput("dis_cl" + cl_id + ".txt", field, coverage, myObjects_sorted, cl2_target.target);
      System.out.println("the dis_cl" + cl_id + ".txt is written.");
      System.out.println("sink: " + cl2_target);
    }
    
    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("sink add_dictwindow: " + cl2_target);
    return cl2_target;
  }
  
  // Rx: 2
  // Tx: 2
  public static DistSlabfile[] distributed_inner_two_pkt(Vector<Objects> obj, 
                                                         int k, 
                                                         DistSlabfile up_sf,
                                                         DistSlabfile next_sf,
                                                         int cl_id) 
  {

    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      System.out.println("obj: " + obj.size());
    }
 
    Meit meit = new Meit();
    meit.numberOfRectangle = k;
    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("up_sf " + up_sf);
    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("next_sf " + next_sf);   
    meit.dic_window = new DictWindow(up_sf.dic, coverage, field);
    meit.dic_window.update_dic(next_sf.dic);
    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("after update dic " + up_sf);
    
    Target neightbor_target = new Target(k, coverage, field, true);
    
    int cluster_size_x = field.width/Driver_SampleP2P.cluster_number_x;
    int x_index = cl_id%cluster_number_x;
    for (Window win : next_sf.hintervals) {
//      if (cluster_size_x * x_index <= win.l) {
        neightbor_target.add_window(win, meit.dic_window);
//      }
    }
    
    int cluster_size_y = field.height/cluster_number_y;
    int y_index = cl_id/cluster_number_y;
    for (Window win : up_sf.hintervals) {
//      if (cluster_size_y * y_index <= win.h) {
        neightbor_target.add_window(win, meit.dic_window);
//      }
    }
    
    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("Target: " + neightbor_target);
    
    // TOP: modification
    Vector<Objects> myObjects_sorted = (Vector<Objects>) obj.clone();
    // myObjects_sorted.addAll(up_sf.neededObj);
    // myObjects_sorted.addAll(next_sf.neededObj);
    for (Objects o : up_sf.neededObj)
      if (!myObjects_sorted.contains(o)) {
        myObjects_sorted.add(o);
      }
    
    for (Objects o : next_sf.neededObj)
      if (!myObjects_sorted.contains(o)) {
        myObjects_sorted.add(o);
      }
    // sort the objects, add the rectangles
    Collections.sort(myObjects_sorted);   
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
//      for (Objects obj1: myObjects_sorted)
//        System.out.println("obj sorted obj x: " + obj1.x + " y: " + obj1.y + " w: " + obj1.weight);
//    }
    //System.out.println(" Object#: " + myObjects_sorted.size());

    Vector<Rectangle> myRectangles = new Vector<Rectangle>();
    object_to_rectangles(myRectangles, myObjects_sorted);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
//      for (Rectangle rec: myRectangles)
//        System.out.println("rec sorted: " + rec);
//    }
    
    Vector<Short> aListOfX1 = new Vector<Short>();
    for(int i = 0; i < myRectangles.size(); i++)
    {
      aListOfX1.add(myRectangles.get(i).x1);
      aListOfX1.add(myRectangles.get(i).x2);
    }

    Collections.sort(aListOfX1);
    Vector<Short> aListOfX = new Vector<Short>(); // xs in python code
    for(Short d : aListOfX1)
    {
      if(!aListOfX.contains(d))
        aListOfX.add(d);
    }

    IntervalTree root = meit.buildIntervalTree(0, aListOfX.size()-1, aListOfX, null);
    //    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    //    System.out.println("root: " + root);
    // meit.preOrderTraverse(root, 0);
    meit.interval_tree_root = root;
    root.window = new Window(aListOfX.get(0), aListOfX.get(aListOfX.size() - 1),
                             (short) 0, (short) 0);
    
    Target cl2_target = meit.maxEnclosing_k(myRectangles, coverage, field, 
                                            root, meit.numberOfRectangle,
                                            neightbor_target);
    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("-------------------------------------------------------");
    // System.out.println("cl2_target: " + cl2_target);
    // System.out.println("dic_window: " + meit.dic_window);
    // Vector<Window> hintervals = new Vector<Window>();
    /*
    for(short sfk : slabFile.keySet()) {
      Window sf = (Window) slabFile.get(sfk);
      hintervals.add(sf);
    }
    Collections.sort(hintervals); // unused
    for(Window h : hintervals){
      System.out.println(h.h+"-----"+h.l+ " "+h.r+" "+h.score);
    }*/
    //System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"); 

    // System.out.println("Clus2: opt interval ------------------------------------------");
    //    for(Window win : opt_interval)
    //      System.out.println("Opt " + win);
    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      meit.writeOutput("dis_cl" + cl_id + ".txt", field, coverage, myObjects_sorted, cl2_target.target);
    
      System.out.println("the resdis_cl" + cl_id + ".txt is written.");
      System.out.println("sink: " + cl2_target);
    }
    
    Vector<Objects> values_up = new Vector<Objects>();
    Vector<Objects> values_next = new Vector<Objects>();

    for (Objects os : myObjects_sorted) {
      if (y_index * cluster_size_y + coverage.height >= os.y) {
        values_up.add(os.clone());
      }
      if (x_index * cluster_size_x + coverage.width >= os.x) {
        values_next.add(os.clone());
      }
    }       
        
    DistSlabfile[] result = new DistSlabfile[2];
    // result[0] = new DistSlabfile(cl2_target.target, values);
    result[0] = new DistSlabfile(cl2_target.target, values_up, meit.dic_window.dic, meit.numberOfRectangle);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("result[0]: " + result[0]);
    // make a deep copy of the opt_interval
    // the clone method makes only the shallow copy of the vector and 
    // the objects inside.
    // Vector<Window> clu_3interval = new Vector<Window>();
    // for (Window win: cl2_target.target)
    //   clu_3interval.add(win.clone());
    // result[1] = new DistSlabfile(clu_3interval, values1);
    result[1] = new DistSlabfile(cl2_target.target, values_next, meit.dic_window.dic, meit.numberOfRectangle);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("result[1]: " + result[1]);       
    return result;
  }

  // Rx: 2
  // Tx: 1 cl_id
  public static DistSlabfile distributed_rx2_tx1(Vector<Objects> obj,
                                                 int k,
                                                 DistSlabfile up_sf,
                                                 DistSlabfile next_sf,
                                                 int cl_id) 
  {
    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      System.out.println("obj: " + obj.size());
    }
 
    Meit meit = new Meit();
    meit.numberOfRectangle = k;
    meit.dic_window = new DictWindow(up_sf.dic, coverage, field);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("up_sf " + up_sf);
    meit.dic_window.update_dic(next_sf.dic);
    Target neightbor_target = new Target(k, coverage, field, true);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
//      System.out.println("next_sf " + next_sf);    
//      System.out.println("after update dic " + up_sf);
//    }
    
    int cluster_size_x = field.width/Driver_SampleP2P.cluster_number_x;
    int x_index = cl_id%cluster_number_x;
    for (Window win : next_sf.hintervals) {
//      if (cluster_size_x * x_index <= win.l) {
        neightbor_target.add_window(win, meit.dic_window);
//      }
    }
    
    int cluster_size_y = field.height/cluster_number_y;
    int y_index = cl_id/cluster_number_y;
    for (Window win : up_sf.hintervals) {
//      if (cluster_size_y * y_index <= win.h) {
        neightbor_target.add_window(win, meit.dic_window);
//      }
    }
    
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("Target: " + neightbor_target);
    
    // TOP: modification
    Vector<Objects> myObjects_sorted = (Vector<Objects>) obj.clone();
    // myObjects_sorted.addAll(up_sf.neededObj);
    // myObjects_sorted.addAll(next_sf.neededObj);
    for (Objects o : up_sf.neededObj)
      if (!myObjects_sorted.contains(o)) {
        myObjects_sorted.add(o);
      }
    
    for (Objects o : next_sf.neededObj)
      if (!myObjects_sorted.contains(o)) {
        myObjects_sorted.add(o);
      }
    // sort the objects, add the rectangles
    Collections.sort(myObjects_sorted);   
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
//      for (Objects obj1: myObjects_sorted)
//        System.out.println("obj sorted obj x: " + obj1.x + " y: " + obj1.y + " w: " + obj1.weight);
//    }
    //System.out.println(" Object#: " + myObjects_sorted.size());

    Vector<Rectangle> myRectangles = new Vector<Rectangle>();
    object_to_rectangles(myRectangles, myObjects_sorted);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
//      for (Rectangle rec: myRectangles)
//        System.out.println("rec sorted: " + rec);
//    }
    
    Vector<Short> aListOfX1 = new Vector<Short>();
    for(int i = 0; i < myRectangles.size(); i++)
    {
      aListOfX1.add(myRectangles.get(i).x1);
      aListOfX1.add(myRectangles.get(i).x2);
    }

    Collections.sort(aListOfX1);
    Vector<Short> aListOfX = new Vector<Short>(); // xs in python code
    for(Short d : aListOfX1)
    {
      if(!aListOfX.contains(d))
        aListOfX.add(d);
    }

    IntervalTree root = meit.buildIntervalTree(0, aListOfX.size()-1, aListOfX, null);
    //    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    //    System.out.println("root: " + root);
    // meit.preOrderTraverse(root, 0);
    meit.interval_tree_root = root;
    root.window = new Window(aListOfX.get(0), aListOfX.get(aListOfX.size() - 1),
                             (short) 0, (short) 0);
    
    Target cl2_target = meit.maxEnclosing_k(myRectangles, coverage, field, 
                                            root, meit.numberOfRectangle,
                                            neightbor_target);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("-------------------------------------------------------");
    // System.out.println("cl2_target: " + cl2_target);
//     System.out.println("cluster #: " + cl_id + " rx2 tx1 dic_window: " + meit.dic_window);
    // Vector<Window> hintervals = new Vector<Window>();
    /*
    for(short sfk : slabFile.keySet()) {
      Window sf = (Window) slabFile.get(sfk);
      hintervals.add(sf);
    }
    Collections.sort(hintervals); // unused
    for(Window h : hintervals){
      System.out.println(h.h+"-----"+h.l+ " "+h.r+" "+h.score);
    }*/
    //System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"); 

    // System.out.println("Clus2: opt interval ------------------------------------------");
    //    for(Window win : opt_interval)
    //      System.out.println("Opt " + win);
    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      meit.writeOutput("dis_cl" + cl_id + ".txt", field, coverage, myObjects_sorted, cl2_target.target);
    
      System.out.println("the dis_cl" + cl_id + ".txt is written.");
//      System.out.println("sink: " + cl2_target);
    }
    
    Vector<Objects> values_up = new Vector<Objects>();
    Vector<Objects> values_next = new Vector<Objects>();
    for (Objects os : myObjects_sorted) {
      if (y_index * cluster_size_y + coverage.height >= os.y) {
        values_up.add(os.clone());
      }
      if (x_index * cluster_size_x + coverage.width >= os.x) {
        values_next.add(os.clone());
      }
    }       
        
        
    DistSlabfile result = null;
    if (cl_id/Driver_SampleP2P.cluster_number_x >=1) {
      // cluster on the left most side
      result = new DistSlabfile(cl2_target.target, values_up, meit.dic_window.dic, meit.numberOfRectangle); 
    } else if ((cl_id < Driver_SampleP2P.cluster_number_x) || 
              ((cl_id >= (Driver_SampleP2P.cluster_number_y - 1) * Driver_SampleP2P.cluster_number_x) && 
               (cl_id < Driver_SampleP2P.cluster_number_y * Driver_SampleP2P.cluster_number_x))) {
      // cluster in the first row or the cluster in the last row
//      if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("next ");
      result = new DistSlabfile(cl2_target.target, values_next, meit.dic_window.dic, meit.numberOfRectangle);
      System.out.println("cluster #: " + cl_id + " | " + result.dic);
    }
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("result: " + result);
    // make a deep copy of the opt_interval
    // the clone method makes only the shallow copy of the vector and 
    // the objects inside.
    //    Vector<Window> clu_3interval = new Vector<Window>();
    //    for (Window win: cl2_target.target)
    //      clu_3interval.add(win.clone());
    // result[1] = new DistSlabfile(clu_3interval, values1);
    return result;
  }

  // Rx: 1
  // Tx: 2
  public static DistSlabfile[] distributed_rx1_tx2(Vector<Objects> obj,
                                                 int k,
                                                 DistSlabfile sf,
                                                 int cl_id) 
  {
    if (Driver_SampleP2P.DEBUG_ALGORITHM && obj != null) {
      System.out.println("obj: " + obj.size());
    }
 
    Meit meit = new Meit();
    meit.numberOfRectangle = k;
    meit.dic_window = new DictWindow(sf.dic, coverage, field);
    
    Target neightbor_target = new Target(k, coverage, field, true);
    for (Window win : sf.hintervals)
      neightbor_target.add_window(win, meit.dic_window);
    
    Vector<Objects> myObjects_sorted = (Vector<Objects>) obj.clone();
    
    for (Objects o : sf.neededObj)
      if (!myObjects_sorted.contains(o)) {
        myObjects_sorted.add(o);
      }
    // sort the objects, add the rectangles
    Collections.sort(myObjects_sorted);   
//    if (false && Driver_SampleP2P.DEBUG_ALGORITHM) {
//      for (Objects obj1: myObjects_sorted)
//        System.out.println("obj sorted obj x: " + obj1.x + " y: " + obj1.y + " w: " + obj1.weight);
//    }
    //System.out.println(" Object#: " + myObjects_sorted.size());

    Vector<Rectangle> myRectangles = new Vector<Rectangle>();
    object_to_rectangles(myRectangles, myObjects_sorted);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
//      for (Rectangle rec: myRectangles)
//        System.out.println("rec sorted: " + rec);
//    }
    
    Vector<Short> aListOfX1 = new Vector<Short>();
    for(int i = 0; i < myRectangles.size(); i++)
    {
      aListOfX1.add(myRectangles.get(i).x1);
      aListOfX1.add(myRectangles.get(i).x2);
    }

    Collections.sort(aListOfX1);
    Vector<Short> aListOfX = new Vector<Short>(); // xs in python code
    for(Short d : aListOfX1)
    {
      if(!aListOfX.contains(d))
        aListOfX.add(d);
    }

    IntervalTree root = meit.buildIntervalTree(0, aListOfX.size()-1, aListOfX, null);
    //    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    //    System.out.println("root: " + root);
    // meit.preOrderTraverse(root, 0);
    meit.interval_tree_root = root;
    root.window = new Window(aListOfX.get(0), aListOfX.get(aListOfX.size() - 1),
                             (short) 0, (short) 0);
    
    Target cl2_target = meit.maxEnclosing_k(myRectangles, coverage, field, 
                                            root, meit.numberOfRectangle,
                                            neightbor_target);
    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("-------------------------------------------------------");
    // System.out.println("cl2_target: " + cl2_target);
    // System.out.println("dic_window: " + meit.dic_window);
    // Vector<Window> hintervals = new Vector<Window>();
    /*
    for(short sfk : slabFile.keySet()) {
      Window sf = (Window) slabFile.get(sfk);
      hintervals.add(sf);
    }
    Collections.sort(hintervals); // unused
    for(Window h : hintervals){
      System.out.println(h.h+"-----"+h.l+ " "+h.r+" "+h.score);
    }*/
    //System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"); 

    // System.out.println("Clus2: opt interval ------------------------------------------");
    //    for(Window win : opt_interval)
    //      System.out.println("Opt " + win);
    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      meit.writeOutput("dis_cl" + cl_id + ".txt", field, coverage, myObjects_sorted, cl2_target.target);
    
      System.out.println("the resdis_" + cl_id + ".txt is written.");
      System.out.println("sink: " + cl2_target);
    }
    
    Vector<Objects> values_up = new Vector<Objects>();
    Vector<Objects> values_next = new Vector<Objects>();
    int x_index = cl_id%cluster_number_x;
    int y_index = cl_id/cluster_number_y;
    int cluster_size_x = field.width/cluster_number_x;
    int cluster_size_y = field.height/cluster_number_y;    
    for (Objects os : myObjects_sorted) {
      if (y_index * cluster_size_y + coverage.height >= os.y) {
        values_up.add(os.clone());
      }
      if (x_index * cluster_size_x + coverage.width >= os.x) {
        values_next.add(os.clone());
      }
    }       
        
    DistSlabfile[] result = new DistSlabfile[2];
    // result[0] = new DistSlabfile(cl2_target.target, values);
    result[0] = new DistSlabfile(cl2_target.target, values_up, meit.dic_window.dic, meit.numberOfRectangle);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("result[0]: " + result[0]);
    // make a deep copy of the opt_interval
    // the clone method makes only the shallow copy of the vector and 
    // the objects inside.
    // Vector<Window> clu_3interval = new Vector<Window>();
    // for (Window win: cl2_target.target)
    //   clu_3interval.add(win.clone());
    // result[1] = new DistSlabfile(clu_3interval, values1);
    result[1] = new DistSlabfile(cl2_target.target, values_next, meit.dic_window.dic, meit.numberOfRectangle);
//    if (Driver_SampleP2P.DEBUG_ALGORITHM) System.out.println("result[1]: " + result[1]);       
    return result;
  }

  static void object_to_rectangles(Vector<Rectangle> myRectangles, 
                                   Vector<Objects> myObjects_sorted) 
  {
    // assume that we have sorted the vector/list of objects before
    for(Objects obj : myObjects_sorted){
      myRectangles.add(new Rectangle((short)Math.max(0, obj.x - coverage.width/2),
                                     (short)Math.max(0, obj.y - coverage.height/2),
                                     (short)Math.min(field.width,
                                                     obj.x + coverage.width/2),
                                     (short)Math.min(field.height,
                                                     obj.y + coverage.height/2),
                                     obj.weight));
    }
  }
}
