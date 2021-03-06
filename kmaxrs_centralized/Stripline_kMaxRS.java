package sidnet.stack.users.sample_p2p.kmaxrs_centralized;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import sidnet.stack.users.sample_p2p.driver.Driver_SampleP2P;

public class Stripline_kMaxRS {
  public static Area field;
  public static Area coverage;
  public static int number_of_cluster;
  
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
      
  public static void setNumberOfStripline(int n) {
    number_of_cluster = n;
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
  
  public static DistSlabfile stripline_last(Vector<Objects> obj, 
                                            int k, 
                                            DictWindow dic, 
                                            int cl_id) 
  {
    if (Driver_SampleP2P.DEBUG) {
      System.out.println("obj: " + obj.size());
    }

    Meit meit = new Meit();
    meit.numberOfRectangle = k;
    meit.dic_window = new DictWindow(coverage, field);
    // TOP: modification

    Vector<Objects> myObjects_sorted = (Vector<Objects>) obj.clone();
    // sort the objects, add the rectangles
    Collections.sort(myObjects_sorted);   
    if (Driver_SampleP2P.DEBUG) {
      for (Objects obj1: myObjects_sorted)
        System.out.println("obj sorted obj x: " + obj1.x + " y: " + obj1.y + " w: " + obj1.weight);
    }
    //System.out.println(" Object#: " + myObjects_sorted.size());

    Vector<Rectangle> myRectangles = new Vector<Rectangle>();
    object_to_rectangles(myRectangles, myObjects_sorted);
    if (Driver_SampleP2P.DEBUG) {
      for (Rectangle rec: myRectangles)
        System.out.println("rec sorted: " + rec);
    }

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
    if (Driver_SampleP2P.DEBUG) System.out.println("-------------------------------------------------------");
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
    if (Driver_SampleP2P.DEBUG) {
      meit.writeOutput("sl_cl" + cl_id + ".txt", field, coverage, myObjects_sorted, cl2_target.target);
      System.out.println("the ressl_" + cl_id + ".txt is written.");
    }

    // Vector<Objects> values_up = new Vector<Objects>();
    Vector<Objects> values_next = new Vector<Objects>();
    int x_index = cl_id%number_of_cluster;
    int y_index = cl_id/number_of_cluster;
    int cluster_size = field.width/number_of_cluster;
    // System.out.println("x_index: " + x_index + " y_index: " + y_index + " cluster_size: " + cluster_size + " coverage: " + coverage.width);
    for (Objects os : myObjects_sorted) {
      // System.out.print("obj: (" + os.y + ", " + os.x + ")");
      // System.out.print(" y condition: " + (y_index * cluster_size + coverage.height/2) + " | node y: " + os.y);
      // if (y_index * cluster_size + coverage.height >= os.y) {
      //   values_up.add(os.clone());
      // }
      // System.out.println(" x condition: " + (x_index * cluster_size + coverage.height/2) + " | node x: " + os.x);
      if (x_index * cluster_size + coverage.width >= os.x) {    
        values_next.add(os.clone());
      }
    }       

    DistSlabfile result = null;
    // result[0] = new DistSlabfile(cl2_target.target, values);
    result = new DistSlabfile(cl2_target.target, values_next, meit.dic_window.dic, meit.numberOfRectangle);
    if (Driver_SampleP2P.DEBUG) System.out.println("result: " + result);
    // make a deep copy of the opt_interval
    // the clone method makes only the shallow copy of the vector and 
    // the objects inside.
    // Vector<Window> clu_3interval = new Vector<Window>();
    // for (Window win: cl2_target.target)
    //   clu_3interval.add(win.clone());
    // result[1] = new DistSlabfile(clu_3interval, values1);
    // result[1] = new DistSlabfile(cl2_target.target, values_next, meit.dic_window.dic, meit.numberOfRectangle);
    // if (Driver_SampleP2P.DEBUG) System.out.println("result[1]: " + result[1]);        
    return result;
  }

  // Rx: 1
  // Tx: 1 cl_id
  public static DistSlabfile stripline_one_pkt(Vector<Objects> obj,
                                               int k,
                                               DistSlabfile sf,
                                               int cl_id) 
  {

    if (Driver_SampleP2P.DEBUG) {
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
    if (Driver_SampleP2P.DEBUG) {
      for (Objects obj1: myObjects_sorted)
        System.out.println("obj sorted obj x: " + obj1.x + " y: " + obj1.y + " w: " + obj1.weight);

      System.out.println(" Object#: " + myObjects_sorted.size());
      for (Objects o: myObjects_sorted) System.out.println("Objects sort: " + o);
    }

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
    if (Driver_SampleP2P.DEBUG) System.out.println("-------------------------------------------------------");
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
    if (Driver_SampleP2P.DEBUG) {
      meit.writeOutput("sl_cl"+cl_id+".txt", field, coverage, myObjects_sorted, cl2_target.target);
      System.out.println("the ressl_one_pck"+cl_id+".txt is written.");
    }

    // Vector<Objects> values_up = new Vector<Objects>();
    Vector<Objects> values_next = new Vector<Objects>();
    int x_index = cl_id%number_of_cluster;
    int y_index = cl_id/number_of_cluster;
    int cluster_size = field.width/number_of_cluster;
    // System.out.println("x_index: " + x_index + " y_index: " + y_index + " cluster_size: " + cluster_size + " coverage: " + coverage.width);
    for (Objects os : myObjects_sorted) {
      // System.out.print("obj: (" + os.x + ", " + os.y + ")");
      // System.out.print(" y condition: " + (y_index * cluster_size + coverage.height/2) + " | node y: " + os.y);
      // if (y_index * cluster_size + coverage.height >= os.y) {
      // System.out.print(" added up ");  
      // values_up.add(os.clone());
      // }
      // System.out.print(" x condition: " + (x_index * cluster_size + coverage.height/2) + " | node x: " + os.x);  
      if (x_index * cluster_size + coverage.width >= os.x) {
        // System.out.print(" added next ");
        values_next.add(os.clone());
      }
      // System.out.println(" | ");
    }        

    DistSlabfile result = null;
    result = new DistSlabfile(cl2_target.target, values_next, meit.dic_window.dic, meit.numberOfRectangle);
    if (Driver_SampleP2P.DEBUG) System.out.println("result: " + result);
    // make a deep copy of the opt_interval
    // the clone method makes only the shallow copy of the vector and 
    // the objects inside.
    //    Vector<Window> clu_3interval = new Vector<Window>();
    //    for (Window win: cl2_target.target)
    //      clu_3interval.add(win.clone());
    // result[1] = new DistSlabfile(clu_3interval, values1);
    return result;
  }

  public static Target stripline_sink_pkt(Vector<Objects> obj, 
                                          int k, 
                                          DistSlabfile next_sf,
                                          int cl_id) 
  {

    if (Driver_SampleP2P.DEBUG) {
      System.out.println("obj: " + obj.size());
    }

    Meit meit = new Meit();
    meit.numberOfRectangle = k;
    meit.dic_window = new DictWindow(next_sf.dic, coverage, field);
    if (Driver_SampleP2P.DEBUG) System.out.println("next_sf " + next_sf);
    meit.dic_window.update_dic(next_sf.dic);
    Target neightbor_target = new Target(k, coverage, field, true);
    int cluster_size_width = field.width/Driver_SampleP2P.cluster_number_x;
    for (Window win : next_sf.hintervals) {
      if (cluster_size_width < win.l) {
        neightbor_target.add_window(win, meit.dic_window);
      }
    }

    if (Driver_SampleP2P.DEBUG) System.out.println("Target: " + neightbor_target);

    // TOP: modification
    Vector<Objects> myObjects_sorted = (Vector<Objects>) obj.clone();
    // myObjects_sorted.addAll(next_sf.neededObj);
    for (Objects o : next_sf.neededObj)
      if (!myObjects_sorted.contains(o)) {
        myObjects_sorted.add(o);
      }    

    // sort the objects, add the rectangles
    Collections.sort(myObjects_sorted);   
    System.out.println("Object size: " + myObjects_sorted.size());
    if (Driver_SampleP2P.DEBUG) {
      for (Objects obj1: myObjects_sorted)
        System.out.println("obj sorted obj x: " + obj1.x + " y: " + obj1.y + " w: " + obj1.weight);
    }
    //System.out.println(" Object#: " + myObjects_sorted.size());

    Vector<Rectangle> myRectangles = new Vector<Rectangle>();
    object_to_rectangles(myRectangles, myObjects_sorted);
    if (Driver_SampleP2P.DEBUG) {
      for (Rectangle rec: myRectangles)
        System.out.println("rec sorted: " + rec);
    }

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

    if (Driver_SampleP2P.DEBUG) System.out.println("-------------------------------------------------------");
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
    if (Driver_SampleP2P.DEBUG) {
      meit.writeOutput("sl_cl" + cl_id + ".txt", field, coverage, myObjects_sorted, cl2_target.target);
      System.out.println("the sl_cl" + cl_id + ".txt is written.");
      System.out.println("sink: " + cl2_target);
    }
    // Vector<Objects> values_up = new Vector<Objects>();
    // Vector<Objects> values_next = new Vector<Objects>();
    // int x_index = cl_id%number_of_cluster;
    // int y_index = cl_id/number_of_cluster;
    // int cluster_size = field.width;
    // for (Objects os : myObjects_sorted) {
    //   if (y_index * cluster_size + coverage.height/2 > os.y) {
    //     values_up.add(os.clone());
    //   }
    //   if (x_index * cluster_size + coverage.width/2 > os.x) {
    //     values_next.add(os.clone());
    //   }
    // }       

    // DistSlabfile[] result = new DistSlabfile[2];
    // result[0] = new DistSlabfile(cl2_target.target, values);
    // result[0] = new DistSlabfile(cl2_target.target, values_up, meit.dic_window.dic, meit.numberOfRectangle);
    // System.out.println("result[0]: " + result[0]);
    // make a deep copy of the opt_interval
    // the clone method makes only the shallow copy of the vector and 
    // the objects inside.
    //    Vector<Window> clu_3interval = new Vector<Window>();
    //    for (Window win: cl2_target.target)
    //      clu_3interval.add(win.clone());
    // result[1] = new DistSlabfile(clu_3interval, values1);
    // result[1] = new DistSlabfile(cl2_target.target, values_next, meit.dic_window.dic, meit.numberOfRectangle);
    // System.out.println("result[1]: " + result[1]);       

    Enumeration<Window> enu = meit.dic_window.dic.keys();
    while (enu.hasMoreElements()) {
      Window key_win = enu.nextElement();
      cl2_target.add_window(key_win, null);
      Vector<Window> vwin = meit.dic_window.dic.get(key_win);
      for (Window win: vwin)
        cl2_target.add_window(win, null);
    }    
    if (Driver_SampleP2P.DEBUG) System.out.println("sink add_dictwindow: " + cl2_target);
    
    int total_score = 0;
    int obj_within = 0;
    Window win = cl2_target.target.get(0);
    for (Objects o : myObjects_sorted) {
      if (within_window(win, o)) {
        total_score += o.weight;
        obj_within++;
        System.out.println("obj within: " + o);
      }      
    }
    
    System.out.println("total_score within target:" + total_score + " object within: " + obj_within);
    return cl2_target;
  }
  
  private static boolean within_window(Window win, Objects obj) {
    int half_width = coverage.width/2;
    int half_height = coverage.height/2;
    return ((win.l - half_width <= obj.x && obj.x <= win.r + half_width) && 
            (win.h - half_height <= obj.y && obj.y <= win.h + half_height));
  }
}
