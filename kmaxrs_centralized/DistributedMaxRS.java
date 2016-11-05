/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sidnet.stack.users.sample_p2p.kmaxrs_centralized;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

/**
 *
 * @author Muhammed
 */
public class DistributedMaxRS {

  public static Vector<Short> currentValues = new Vector<Short>();
  public static Vector<Objects> currentObjects2 = new Vector<Objects>();
  public static short current_phenomena = Constants.LIGHT_PHENOMENA;
  static Area area;
  static Area coverage;
    
  /////////////////////////////////////*********** Testing Purposes ************************////////////////////////////////////
  public static Vector<Rectangle> currentRectangle = new Vector<Rectangle>();
  public static Vector<Objects> currentObjects = new Vector<Objects>();

  /////////////////////////////////////*********** Testing Purposes ************************////////////////////////////////////
    
  /**
   * @param args the command line arguments
   */
//  public static void main(String[] args) {
//    System.out.println("---------------------------------------------------------------------");
//    Random rand = new Random();
//    Meit meit = new Meit();
//    boolean ov = false;
//    area = new Area(Constants.AREA_WIDTH, Constants.AREA_HEIGHT);
//    //coverage <=50, nothing to send
//    //coverage between 51 and 150, send only 1 layer
//    //coverage between 151 and 250, send 2 layers
//    //coverage between 251 and 350, send all values
//    coverage = computeCoverage(10);    
//    Vector<Window> opt_window_cen = null;
//    Vector<Window> opt_window_dist = null;
////    int round = 0;
////    boolean match = true;
////    predefine val    
////    fail due to the overlap rectangles between the highest maxRS and the second highest maxRS
////    overlab: ov
//// ov   short[] val = {52, 41, 52, 22, 92, 0, 64, 78, 12, 25, 5, 59, 33, 96, 81, 65, 83, 41, 11, 83, 51, 72, 47, 54, 82, 17, 55, 58, 48, 12, 92, 24, 63, 59, 5, 55 };
//// ov   short[] val = {70, 65, 7, 72, 67, 67, 46, 69, 4, 27, 28, 85, 0, 64, 96, 59, 93, 52, 8, 27, 74, 46, 24, 83, 67, 34, 91, 39, 43, 42, 12, 64, 23, 29, 62, 52};     
////    pass the highest and the second highest rec are in the same cluster -> cluster 1
//// ov    short[] val = {16, 8, 2, 9, 65, 27, 7, 95, 42, 61, 5, 42, 37, 50, 41, 89, 27, 67, 64, 76, 18, 4, 76, 79, 70, 34, 69, 29, 81, 15, 96, 4, 16, 79, 29, 62};
////    short[] val ={74, 98, 2, 1, 56, 50, 93, 79, 9, 99, 77, 10, 1, 31, 35, 72, 12, 56, 97, 28, 38, 9, 34, 77, 0, 6, 30, 5, 38, 27, 23, 77, 68, 70, 5, 31};
////     short[] val = {77, 24, 50, 43, 81, 16, 33, 51, 58, 42, 80, 11, 86, 48, 24, 32, 33, 38, 37, 37, 49, 70, 2, 62, 53, 58, 85, 38, 32, 55, 84, 40, 9, 60, 52, 59};
//     // this testcase fells due to the win[l: 250 r: 250 h: 250 s: 430].
//     // It is put into the dic_window in the 0th cluster, but it is not passed to the 1st cluster.
//     // this is because the key windows of this win[l: 250 r: 250 h: 250 s: 430] are not one of the kth MaxRS.
//     // fixed it by changing the temp_target to optimal_target
////    short[] val = {22, 29, 47, 11, 34, 30, 8, 70, 79, 10, 13, 81, 14, 41, 35, 43, 93, 67, 2, 69, 95, 80, 57, 9, 42, 92, 17, 41, 45, 2, 16, 85, 31, 48, 54, 50};
//// ov    short[] val = {62, 56, 9, 1, 54, 44, 27, 69, 50, 90, 96, 6, 51, 25, 89, 41, 38, 39, 28, 38, 99, 38, 92, 92, 61, 24, 91, 46, 26, 34, 24, 52, 67, 21, 89, 20};
//// ov    short[] val = {75, 27, 18, 45, 52, 24, 97, 82, 35, 80, 63, 13, 98, 7, 99, 16, 98, 58, 8, 40, 81, 30, 89, 93, 8, 93, 21, 50, 72, 53, 89, 81, 37, 35, 56, 42};
////    short[] val = {86, 43, 44, 84, 11, 12, 45, 75, 25, 82, 36, 74, 33, 19, 91, 72, 41, 42, 21, 17, 72, 66, 78, 85, 55, 91, 32, 20, 62, 47, 71, 64, 66, 74, 64, 43};
//    short[] val = {10, 52, 21, 16, 10, 9, 72, 98, 41, 36, 16, 92, 17, 75, 90, 10, 30, 66, 17, 43, 37, 50, 41, 14, 75, 67, 58, 61, 15, 26, 31, 81, 36, 66, 44, 12};
//    // fixed by adding the values from the dic_window into the target
////    short[] val = {31, 93, 63, 13, 45, 59, 30, 38, 8, 68, 64, 0, 27, 5, 22, 92, 57, 17, 12, 18, 82, 78, 7, 83, 42, 78, 14, 26, 59, 66, 10, 56, 43, 32, 14, 97};
////    short[] val = {77, 79, 59, 26, 47, 3, 5, 45, 1, 62, 21, 94, 5, 84, 39, 21, 14, 97, 0, 28, 35, 74, 86, 49, 7, 21, 97, 86, 53, 79, 51, 58, 93, 87, 0, 6};
////
//    for(int i=0; i<Constants.TOTAL_MOTES; i++){
//      //short val=(short)rand.nextInt(100);
//      //currentValues.add(val);
//      currentValues.add(val[i]);
//      Point p = Constants.getNodeLocation((short)i);
//      //currentObjects2.add(new Objects(p.x, p.y, val));
//      currentObjects2.add(new Objects(p.x, p.y, val[i]));
//    }
//
//    // System.out.println("-----------------------------" + round + "---------------------------------------");
//    // random val
////    for(int i=0; i<Constants.TOTAL_MOTES; i++) {
////      short val=(short)rand.nextInt(100);
////      //currentValues.add(val);
////      currentValues.add(val);
////      Point p = Constants.getNodeLocation((short)i);
////      //currentObjects2.add(new Objects(p.x, p.y, val));
////      currentObjects2.add(new Objects(p.x, p.y, val));
////    }
//
//    System.out.println("--------------------------------------------------------------------");
//    for (int k=0; k < currentValues.size(); k++)
//      System.out.print(currentValues.get(k)+", ");
//    System.out.println("\n--------------------------------------------------------------------");
//    //printing values to compare
//    for(int i=5; i>=0; i--){
//      for(int j=0; j<6;j++){
//        short cval = currentValues.get((i*6)+j);
//        System.out.print(cval+" ");
//      }
//      System.out.println(""); 
//    }
//
//    opt_window_cen = centralised();
//    ov = opt_window_cen.get(1).is_overlap(opt_window_cen.get(0), coverage, area);
//
//    DistSlabfile[] first_res = processingC_2ttk();
//    DistSlabfile zeroth_res = processingC_0ttk(first_res[0]);
//
//    //    for (Window whi: first_res[1].hintervals)
//    //      System.out.println("hinterval from cl:2 to cl:3 " + whi);
//
//    DistSlabfile third_res=processingC_3ttk(first_res[1]);
//    opt_window_dist =processingC_1ttk(zeroth_res, third_res);
//    //System.out.println(opt_window.score);
//    meit.writeOutput("dist-output.txt", area, coverage, currentObjects2, opt_window_dist);
//    System.out.println("ov " + ov);
//    System.out.println("Centralized approach:");
//    for (Window win: opt_window_cen)
//      System.out.println(win);
//    System.out.println("Distributed approach:");
//    for (Window win: opt_window_dist)
//      System.out.println(win);    
//  }
//    
  static void object_to_rectangles(Vector<Rectangle> myRectangles, Vector<Objects> myObjects_sorted) {
    // assume that we have sorted the vector/list of objects before
    for(Objects obj : myObjects_sorted){
      myRectangles.add(new Rectangle((short)Math.max(0, obj.x - coverage.width/2),
                                     (short)Math.max(0, obj.y - coverage.height/2),
                                     (short)Math.min(area.width,
                                                     obj.x + coverage.width/2),
                                     (short)Math.min(area.height,
                                                     obj.y + coverage.height/2),
                                     obj.weight));
    }      
  }
    
  static boolean within_window(Objects objects, Window optimal_win, Area area, Area coverage) {
    if ((Math.max(0, optimal_win.h - coverage.height/2) <= objects.y) && 
        (objects.y <= Math.min( area.height, optimal_win.h + coverage.height/2)) &&
        (Math.max(0, optimal_win.l - coverage.width/2) <= objects.x) && 
        (objects.x <= Math.min( area.width, optimal_win.l + coverage.width/2)))
      return true;
    return false;
  }
    
  static boolean remove_objects_within_window(Vector<Objects> object_vec, Window optimal_window, Area area, Area coverage) {
    boolean remove_obj = false;
    if (optimal_window == null)
      return remove_obj;
    
    for (int i = object_vec.size()-1; i >= 0; i--) {
      if (within_window(object_vec.elementAt(i), optimal_window, area, coverage)) {
        object_vec.remove(i);
        remove_obj = true;
      }
    }
    
    return remove_obj;
  }
    
  static void initializeC_2(Vector<Short> myObjectIds, Vector<Objects> myObjects){
    //hard-coded ids for each cluster
    myObjectIds.add((short)18);
    myObjectIds.add((short)19);
    myObjectIds.add((short)20);
    myObjectIds.add((short)24);
    myObjectIds.add((short)25);  //myid
    myObjectIds.add((short)26);
    myObjectIds.add((short)30);
    myObjectIds.add((short)31);
    myObjectIds.add((short)32);
        
    //setting up the objects for once
    for(int i=0; i<9; i++){
      short id = myObjectIds.get(i);
      Point p = Constants.getNodeLocation(id);
      myObjects.add(new Objects(p.x, p.y, currentValues.get(id)));
    }
  }
 
  static void initializeC_0(Vector<Short> myObjectIds, Vector<Objects> myObjects, DistSlabfile osf, short coverage_height){
    //hard-coded ids for each cluster
    myObjectIds.add((short)0);
    myObjectIds.add((short)1);
    myObjectIds.add((short)2);
    myObjectIds.add((short)6);
    myObjectIds.add((short)7);  //myid
    myObjectIds.add((short)8);
    myObjectIds.add((short)12);
    myObjectIds.add((short)13);
    myObjectIds.add((short)14);
        
    //object id-s from cluster-2
    myObjectIds.add((short)18);
    myObjectIds.add((short)19);
    myObjectIds.add((short)20);
    myObjectIds.add((short)24);
    myObjectIds.add((short)25);  //c-2 principal id
    myObjectIds.add((short)26);
    myObjectIds.add((short)30);
    myObjectIds.add((short)31);
    myObjectIds.add((short)32);
        
    //setting up the objects for once
    for(int i=0; i<9; i++){
      short id = myObjectIds.get(i);
      Point p = Constants.getNodeLocation(id);
      myObjects.add(new Objects(p.x, p.y, currentValues.get(id)));
    }
    //Add adjustments, meaning newobjects here
    //from cluster-2
    short i=9;
    short j=0;
    int limit = (coverage.height/Constants.GAP_HEIGHT)<=3?(coverage.height/Constants.GAP_HEIGHT):3;
    for(int k=0; k<limit; k++){
      for(;i<(3 *(4+k));i++){
        Point obj = Constants.getNodeLocation(myObjectIds.get(i));
        myObjects.add(new Objects(obj.x, obj.y, osf.neededValues.get(j++)));            
      }
    }        
  }
    
  static void initializeC_3(Vector<Short> myObjectIds, Vector<Objects> myObjects, DistSlabfile osf, short coverage_width){
    //hard-coded ids for each cluster
    myObjectIds.add((short)21);
    myObjectIds.add((short)22);
    myObjectIds.add((short)23);
    myObjectIds.add((short)27);
    myObjectIds.add((short)28);  //myid
    myObjectIds.add((short)29);
    myObjectIds.add((short)33);
    myObjectIds.add((short)34);
    myObjectIds.add((short)35);
        
    //object id-s from cluster-2
                                            
    myObjectIds.add((short)20);
    myObjectIds.add((short)26);
    myObjectIds.add((short)32);
    myObjectIds.add((short)19);    
    myObjectIds.add((short)25);  //c-2 principal id 
    myObjectIds.add((short)31);
    myObjectIds.add((short)18);   
    myObjectIds.add((short)24);
    myObjectIds.add((short)30);



    //setting up the objects for once
    for(int i=0; i<9; i++){
      short id = myObjectIds.get(i);
      Point p = Constants.getNodeLocation(id);
      myObjects.add(new Objects(p.x, p.y, currentValues.get(id)));
    }
    //Add adjustments, meaning new objects here
    //from cluster-2
    short i=9;
    short j=0;
    int limit = (coverage.width/Constants.GAP_WIDTH)<=3?(coverage.width/Constants.GAP_WIDTH):3;
    for(int k=0; k<limit; k++){
      for(;i<(3 *(4+k));i++){
        Point obj = Constants.getNodeLocation(myObjectIds.get(i));
        myObjects.add(new Objects(obj.x, obj.y, osf.neededValues.get(j++)));               
      }
    }        
  }
    
  static void initializeC_1(Vector<Short> myObjectIds, Vector<Objects> myObjects, DistSlabfile osf1, DistSlabfile osf2, short coverage_height, short coverage_width){
    //hard-coded ids for each cluster
    myObjectIds.add((short)3);
    myObjectIds.add((short)4);
    myObjectIds.add((short)5);
    myObjectIds.add((short)9);
    myObjectIds.add((short)10);  //myid
    myObjectIds.add((short)11);
    myObjectIds.add((short)15);
    myObjectIds.add((short)16);
    myObjectIds.add((short)17);
        
    //ids from cluster - 0 (left)
    myObjectIds.add((short)2);
    myObjectIds.add((short)8);
    myObjectIds.add((short)14);
    myObjectIds.add((short)1);
    myObjectIds.add((short)7);  //myid
    myObjectIds.add((short)13);
    myObjectIds.add((short)0);
    myObjectIds.add((short)6);
    myObjectIds.add((short)12);
        
    //ids from cluster - 3
    myObjectIds.add((short)21);
    myObjectIds.add((short)22);
    myObjectIds.add((short)23);
    myObjectIds.add((short)27);
    myObjectIds.add((short)28);  //myid
    myObjectIds.add((short)29);
    myObjectIds.add((short)33);
    myObjectIds.add((short)34);
    myObjectIds.add((short)35);
        
    //ids from cluster - 2
    myObjectIds.add((short)20);
    myObjectIds.add((short)26);
    myObjectIds.add((short)32);
    myObjectIds.add((short)19);    
    myObjectIds.add((short)25);  //c-2 principal id 
    myObjectIds.add((short)31);
    myObjectIds.add((short)18);   
    myObjectIds.add((short)24);
    myObjectIds.add((short)30);
        
    //setting up the objects for once
    for(int i=0; i<9; i++){
      short id = myObjectIds.get(i);
      Point p = Constants.getNodeLocation(id);
      myObjects.add(new Objects(p.x, p.y, currentValues.get(id)));
    }
        
    //Add adjustments, meaning new rectangles here
    //from cluster-0
    short i=9;
    short j=0;
    int limit = (coverage.width/Constants.GAP_WIDTH)<=3?(coverage.width/Constants.GAP_WIDTH):3;
    for(int k=0; k<limit; k++){
      for(;i<(3 *(4+k));i++){
        Point obj = Constants.getNodeLocation(myObjectIds.get(i));
        myObjects.add(new Objects(obj.x, obj.y, osf1.neededValues.get(j++)));               
      }
    }

    //from cluster-3
    i=18;
    j=0;
    limit=(coverage.height/Constants.GAP_HEIGHT)<=3?(coverage.height/Constants.GAP_HEIGHT):3;
    for(int k=0; k<limit; k++){
      for(;i<(3 *(7+k));i++){
        Point obj = Constants.getNodeLocation(myObjectIds.get(i));
        myObjects.add(new Objects(obj.x, obj.y, osf2.neededValues.get(j++)));              
      }
    }

    //from cluster-2
    int k=(coverage.height/Constants.GAP_HEIGHT)<=3?(coverage.height/Constants.GAP_HEIGHT):3;
    int limit2 = (coverage.width/Constants.GAP_WIDTH)<=3?(coverage.width/Constants.GAP_WIDTH):3;       
    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    for(i=0; i<limit2; i++){
      for(short p=0;p<k; p++){
        Point obj = Constants.getNodeLocation(myObjectIds.get(27+(i*3+p)));
        System.out.println(osf2.neededValues.get(j));
        myObjects.add(new Objects(obj.x, obj.y, osf2.neededValues.get(j++)));   
      }
    }    
  }
  
//  static DistSlabfile[] processingC_2ttk() {
//    System.out.println("Cluster 2 ------------------------------------");
//    Vector<Short> myObjectIds = new Vector<Short>();
//    Vector<Objects> myObjects = new Vector<Objects>();        
//    Meit meit = new Meit();
//    initializeC_2(myObjectIds, myObjects);
//    // DictWindow dic_window = new DictWindow(coverage, area);
//    meit.dic_window = new DictWindow(coverage, area);
//    // TOP: modification
//    // Vector<Window> opt_interval = new Vector<Window>();
//    Vector<Objects> myObjects_sorted = (Vector<Objects>) myObjects.clone();
//    // sort the objects, add the rectangles
//    Collections.sort(myObjects_sorted);        
//    /*for (Objects obj: myObjects_sorted)
//      System.out.println("clu:2 obj x: " + obj.x + " y: " + obj.y + " w: " + obj.weight);
//    */ 
//    //System.out.println(" Object#: " + myObjects_sorted.size());
//
//    Vector<Rectangle> myRectangles = new Vector<Rectangle>();
//    object_to_rectangles(myRectangles, myObjects_sorted);
//    Vector<Short> aListOfX1 = new Vector<Short>();
//    for(int i = 0; i < myRectangles.size(); i++)
//    {
//      aListOfX1.add(myRectangles.get(i).x1);
//      aListOfX1.add(myRectangles.get(i).x2);
//    }
//
//    Collections.sort(aListOfX1);
//    Vector<Short> aListOfX = new Vector<Short>(); // xs in python code
//    for(Short d : aListOfX1)
//    {
//      if(!aListOfX.contains(d))
//        aListOfX.add(d);
//    }
//
//    IntervalTree root = meit.buildIntervalTree(0, aListOfX.size()-1, aListOfX, null);
//    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
//    // meit.preOrderTraverse(root);
//    meit.interval_tree_root = root;
//    root.window = new Window(aListOfX.get(0), aListOfX.get(aListOfX.size() - 1),
//                             (short) 0, (short) 0);
//
//    Target cl2_target = meit.maxEnclosing_k(myRectangles, coverage, area, 
//                                            root, meit.numberOfRectangle,
//                                            null);
//  
//    System.out.println("cl2_target: " + cl2_target);
//    System.out.println("dic_window: " + meit.dic_window);
//    // Vector<Window> hintervals = new Vector<Window>();
//    /*for(short sfk : slabFile.keySet()) {
//      Window sf = (Window) slabFile.get(sfk);
//      hintervals.add(sf);
//    }
//    Collections.sort(hintervals); // unused
//    for(Window h : hintervals){
//      System.out.println(h.h+"-----"+h.l+ " "+h.r+" "+h.score);
//    }*/
//    //System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"); 
//
//    System.out.println("Clus2: opt interval ------------------------------------------");
//    //    for(Window win : opt_interval)
//    //      System.out.println("Opt " + win);
//    meit.writeOutput("cl-2.txt", area, coverage, myObjects, cl2_target.target);
//    
//    Vector<Short> values = new Vector<Short>();
//    int limit = (coverage.height/Constants.GAP_HEIGHT)<=3?(coverage.height/Constants.GAP_HEIGHT):3;
//    short j=0;
//    for(int k=0; k<limit; k++){
//      values.add(myObjects.get(j++).weight);
//      values.add(myObjects.get(j++).weight);
//      values.add(myObjects.get(j++).weight);
//    }
//        
//    Vector<Short> values1 = new Vector<Short>();
//    j=2;
//    limit = (coverage.width/Constants.GAP_WIDTH)<=3?(coverage.width/Constants.GAP_WIDTH):3;
//    for(int k=0; k<limit; k++, j--){
//      values1.add(myObjects.get(j).weight);
//      values1.add(myObjects.get(j+3).weight);
//      values1.add(myObjects.get(j+6).weight);
//    }
//        
//    DistSlabfile[] result = new DistSlabfile[2];
//    // result[0] = new DistSlabfile(cl2_target.target, values);
//    result[0] = new DistSlabfile(cl2_target.target, values, meit.dic_window.dic, meit.numberOfRectangle);
//    System.out.println("result[0]: " + result[0]);
//    // make a deep copy of the opt_interval
//    // the clone method makes only the shallow copy of the vector and 
//    // the objects inside.
//    Vector<Window> clu_3interval = new Vector<Window>();
//    for (Window win: cl2_target.target)
//      clu_3interval.add(win.clone());
//    // result[1] = new DistSlabfile(clu_3interval, values1);
//    result[1] = new DistSlabfile(clu_3interval, values1, meit.dic_window.dic, meit.numberOfRectangle);
//    System.out.println("result[1]: " + result[1]);        
//    return result;
//  }   
//  
//  static DistSlabfile processingC_0ttk(DistSlabfile osf) {
//    System.out.println("Cluster 0 ------------------------------------");    
//    Vector<Short> myObjectIds = new Vector<Short>();
//    Vector<Objects> myObjects = new Vector<Objects>();
//    Meit meit = new Meit();
//    short coverage_height=(short)Math.min((Constants.getNodeLocation((short)12).y+ (coverage.height/2)), area.height);
//    Target cl2_target = new Target(meit.numberOfRectangle, coverage, area, true);
//    cl2_target.target = osf.hintervals;
//    System.out.println("cl2_target: " + cl2_target);
//    // DictWindow dic_window = new DictWindow(osf.dic, coverage, area);
//    meit.dic_window = new DictWindow(osf.dic, coverage, area);
//    System.out.println("dic_window: " + meit.dic_window);
//    // TOP: modification
//    // Hashtable<Short, Window> slabFile = new Hashtable<Short, Window>();
//    // Vector<Window> opt_interval = new Vector<Window>();    
//    // Window optimal_win = null;
//    initializeC_0(myObjectIds, myObjects, osf, coverage_height);
//    Vector<Objects> myObjects_sorted = (Vector<Objects>) myObjects.clone();
//    //sort the objects, add the rectangles
//    Collections.sort(myObjects_sorted);       
//    
//    //for (Objects obj: myObjects_sorted)
//    //  System.out.println("clu:0 obj x: " + obj.x + " y: " + obj.y + " w: " + obj.weight);
//    
//    System.out.println(" Object#: " + myObjects_sorted.size());
//    Vector<Rectangle> myRectangles = new Vector<Rectangle>();      
//    object_to_rectangles(myRectangles, myObjects_sorted);
//    Vector<Short> aListOfX1 = new Vector<Short>();
//    for(int i = 0; i < myRectangles.size(); i++)
//    {
//      aListOfX1.add(myRectangles.get(i).x1);
//      aListOfX1.add(myRectangles.get(i).x2);
//    }
//
//    Collections.sort(aListOfX1);
//    Vector<Short> aListOfX = new Vector<Short>(); // xs in python code
//    for(Short d : aListOfX1)
//    {
//      if(!aListOfX.contains(d))
//        aListOfX.add(d);
//    }
//
//    IntervalTree root = meit.buildIntervalTree(0, aListOfX.size()-1, aListOfX, null);
//    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
//    
//    meit.interval_tree_root = root;
//    root.window = new Window(aListOfX.get(0), aListOfX.get(aListOfX.size() - 1),
//                             (short) 0, (short) 0);
//
//    Target cl0_target = meit.maxEnclosing_k(myRectangles, coverage, area, 
//                                            root, meit.numberOfRectangle,
//                                            cl2_target);
//    System.out.println("cl0_target: " + cl2_target);
//    System.out.println("dic_window: " + meit.dic_window);
//    
//    Vector<Short> values = new Vector<Short>();
//    short j=2;
//    int limit = (coverage.width/Constants.GAP_WIDTH)<=3?(coverage.width/Constants.GAP_WIDTH):3;
//    for(int k=0; k<limit; k++, j--) {
//      values.add(myObjects.get(j).weight);
//      values.add(myObjects.get(j+3).weight);
//      values.add(myObjects.get(j+6).weight);
//    }
//    
//    meit.writeOutput("cl-0.txt", area, coverage, myObjects, cl0_target.target);
//    // DistSlabfile result = new DistSlabfile(cl0_target.target, values);
//    DistSlabfile result = new DistSlabfile(cl0_target.target, 
//                                           values,
//                                           meit.dic_window.dic, 
//                                           meit.numberOfRectangle);
//    System.out.println("result: " + result);
//    return result;
//  }
//
//  static DistSlabfile processingC_3ttk(DistSlabfile osf){
//    System.out.println("Cluster 3 ------------------------------------");
//    Vector<Short> myObjectIds = new Vector<Short>();
//    Vector<Objects> myObjects = new Vector<Objects>();
//    Meit meit = new Meit();
//    short coverage_width=(short)Math.max((Constants.getNodeLocation((short)21).x-(coverage.width/2)), 0);
//    // Hashtable<Short, Window> slabFile = new Hashtable<Short, Window>();
//    Target cl2_target = new Target(meit.numberOfRectangle, coverage, area, true);
//    cl2_target.target = osf.hintervals;
//    System.out.println("cl2_target: " + cl2_target);
//    // DictWindow dic_window = new DictWindow(osf.dic, coverage, area);
//    meit.dic_window = new DictWindow(osf.dic, coverage, area);
//    System.out.println("dic_window: " + meit.dic_window);
//    // TOP: modification
//    // Vector<Window> opt_interval = new Vector<Window>();
//    // Window optimal_win = null;
//    
//    initializeC_3(myObjectIds, myObjects, osf, coverage_width);
//    Vector<Objects> myObjects_sorted = (Vector<Objects>) myObjects.clone();
//    Collections.sort(myObjects_sorted); 
//    
//    //for (Objects obj: myObjects_sorted)
//    //  System.out.println("clu:3 obj x: " + obj.x + " y: " + obj.y + " w: " + obj.weight);
//
//    //System.out.println(" Object#: " + myObjects_sorted.size());      
//
//    Vector<Rectangle> myRectangles = new Vector<Rectangle>();    
//    object_to_rectangles(myRectangles, myObjects_sorted);
//    Vector<Short> aListOfX1 = new Vector<Short>();
//    for(int i = 0; i < myRectangles.size(); i++)
//    {
//      aListOfX1.add(myRectangles.get(i).x1);
//      aListOfX1.add(myRectangles.get(i).x2);
//    }
//
//    Collections.sort(aListOfX1);
//    Vector<Short> aListOfX = new Vector<Short>(); // xs in python code
//    for(Short d : aListOfX1)
//    {
//      if(!aListOfX.contains(d))
//        aListOfX.add(d);
//    }
//
//    IntervalTree root = meit.buildIntervalTree(0, aListOfX.size()-1, aListOfX, null);
//    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
//    //meit.preOrderTraverse(root);
//    meit.interval_tree_root = root;
//    root.window = new Window(aListOfX.get(0), aListOfX.get(aListOfX.size() - 1),
//                             (short) 0, (short) 0);
//
//    Target cl3_target = meit.maxEnclosing_k(myRectangles, coverage, area, 
//                                            root, meit.numberOfRectangle,
//                                            cl2_target);
//    System.out.println("cl2_target: " + cl2_target);
//    System.out.println("dic_window: " + meit.dic_window);        
//    /*
//    Vector<Window> hintervals = new Vector<Window>();
//    for(short sfk : slabFile.keySet()){
//      Window sf = (Window) slabFile.get(sfk);
//      hintervals.add(sf);
//    } 
//    */
//    meit.writeOutput("cl-3.txt", area, coverage, myObjects, cl3_target.target);
//    /*
//    for(Window win : hintervals)
//      System.out.println("cl3 intervals: " + win);
//    */
//    Vector<Short> values = new Vector<Short>();
//    short j=0;
//    int limit = (coverage.height/Constants.GAP_HEIGHT)<=3?(coverage.height/Constants.GAP_HEIGHT):3;
//    for(int k=0; k<limit; k++) {
//      values.add(myObjects.get(j++).weight);
//      values.add(myObjects.get(j++).weight);
//      values.add(myObjects.get(j++).weight);
//    }
//        
//    int limit2 = (coverage.width/Constants.GAP_WIDTH)<=3?(coverage.width/Constants.GAP_WIDTH):3;       
//    for(int i=0; i<limit2; i++) {
//      for(j=0;j<limit; j++) {
//        values.add(myObjects.get(9+(i*3+j)).weight);
//      }
//    }
//        
//    // DistSlabfile result = new DistSlabfile(cl3_target.target, values);
//    DistSlabfile result = new DistSlabfile(cl3_target.target, 
//                                           values, 
//                                           meit.dic_window.dic, 
//                                           meit.numberOfRectangle);
//    System.out.println("result: " + result);
//    return result;
//  }
// 
//  static Vector<Window> processingC_1ttk(DistSlabfile osf1,DistSlabfile osf2){
//    System.out.println("Cluster 1 ------------------------------------------");            
//    Vector<Short> myObjectIds = new Vector<Short>();
//    Vector<Objects> myObjects = new Vector<Objects>();
//    Vector<Rectangle> myRectangles = new Vector<Rectangle>();
//    // Hashtable<Short, Window> slabFile = new Hashtable<Short, Window>();
//    Meit meit = new Meit();
//    short coverage_width=(short)Math.max((Constants.getNodeLocation((short)3).x-(coverage.width/2)), 0);
//    short coverage_height=(short)Math.min((Constants.getNodeLocation((short)15).y+(coverage.height/2)), area.height);
//        
//    Target other_clusters = new Target(meit.numberOfRectangle, coverage, area, true);
//    meit.dic_window = new DictWindow(osf1.dic, coverage, area);
//    System.out.println("osf1: " + osf1);
//    System.out.println("osf2: " + osf2);
//    meit.dic_window.update_dic(osf2.dic);
//    System.out.println("dic_window " + meit.dic_window);
//    
//    //adding from previous slab (c-3)
//    for(Window sf :  osf2.hintervals){
//      System.out.println("cluster 3: " + sf);
//      if(sf.h>=coverage_height){
//        other_clusters.add_window(sf, meit.dic_window);
//      }
//    }
//    System.out.println("------------------------------------------");
//    //adding from previous slab (c-0)
//    for(Window sf :  osf1.hintervals) {
//      System.out.println("cluster 0: " + sf);
//      if(sf.r<=coverage_width) {
//        other_clusters.add_window(sf, meit.dic_window);
//      }
//      else{// unused lines
//        if(sf.l<coverage_width) {
//          System.out.println(" ++++++++++++++++++++++++++++++ ");
//          sf.r=coverage_width;
//          other_clusters.add_window(sf, meit.dic_window);
//        }
//      }
//    }    
//    
//    System.out.println("before other_clusters: " + other_clusters);
//    System.out.println("before dic_window: " + meit.dic_window);
//    
//    initializeC_1(myObjectIds, myObjects, osf1, osf2, coverage_height, coverage_width);
//    Vector<Objects> myObjects_sorted = (Vector<Objects>) myObjects.clone();
//    Collections.sort(myObjects_sorted);     
//    object_to_rectangles(myRectangles, myObjects_sorted);
//    Vector<Short> aListOfX1 = new Vector<Short>();
//    for(int i = 0; i < myRectangles.size(); i++)
//    {
//      aListOfX1.add(myRectangles.get(i).x1);
//      aListOfX1.add(myRectangles.get(i).x2);
//    }
//        
//    Collections.sort(aListOfX1);
//    Vector<Short> aListOfX = new Vector<Short>(); // xs in python code
//    for(Short d : aListOfX1)
//    {
//      if(!aListOfX.contains(d))
//        aListOfX.add(d);
//    }
//        
//    IntervalTree root = meit.buildIntervalTree(0, aListOfX.size()-1, aListOfX, null);
//    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
//    //meit.preOrderTraverse(root);
//    meit.interval_tree_root = root;
//    root.window = new Window(aListOfX.get(0), aListOfX.get(aListOfX.size() - 1),
//                             (short) 0, (short) 0);
//
//    Target cl1_target = meit.maxEnclosing_k(myRectangles, coverage, area, 
//                                            root, meit.numberOfRectangle, 
//                                            other_clusters);
//    System.out.println("cl1_target: " + cl1_target);
//    System.out.println("dic_other: " + meit.dic_window);
//    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");              
//        
//    // Collections.sort(cl1_target.target); // unused 
//    // Window opt_window = new Window((short)0, (short)0, (short)0, (short)0); 
//    /*Target optimal_target = new Target(meit.numberOfRectangle, 
//                                       coverage, area, false);
//    for(Window hwin : hintervals){
//      //System.out.println(h.h+"-----"+h.l+" "+h.r+" "+h.score);
//      optimal_target.add_window(hwin, null);
//    } */
//    
//    // TEST
//    Enumeration<Window> enu = meit.dic_window.dic.keys();
//    
//    while (enu.hasMoreElements()) {
//      Window key_win = enu.nextElement();
//      cl1_target.add_window(key_win, null);
//      Vector<Window> vwin = meit.dic_window.dic.get(key_win);
//      for (Window win: vwin)
//        cl1_target.add_window(win, null);
//    }
//
////    while (enu.hasMoreElements()) {
////      Window key_win = enu.nextElement();
////      Vector<Window> vwin = meit.dic_window.dic.get(key_win);
////      for (Window win: vwin) {
////        if (meit.dic_window.dic.containsKey(win)) {
////          Vector<Window> val_vwin = meit.dic_window.dic.get(win);
////          for (Window val_win: val_vwin) 
////            cl1_target.add_window(val_win, null);
////        }
////      }
////    }
//    
//    meit.writeOutput("cl-1.txt", area, coverage, myObjects, cl1_target.target);
//    return cl1_target.target;
//  }
//     
  static Area computeCoverage(int Energy){
    //do compute the size of the rectangle from Energy threshold given by the user
    //Always make the area EVEN!!!!!!!!!!!!!!!!!!!!!!!
    return new Area((short)100, (short)100);
  }
    
  /////////////////////////////////////*********** Testing Purposes ************************////////////////////////////////////
  static Vector<Window> centralised() {
    Meit meit = new Meit();
    Vector<Window> opt_interval = new Vector<Window>();
    initialize();   
    for(int i=0; i<Constants.TOTAL_MOTES; i++) {
      short val=currentValues.get(i);
      currentObjects.get(i).weight=val;
      //currentValues.set(i, val);
      //currentRectangle.get(i).weight=val;
    }

///////////////////////////////////////////////////////////////////////////////////////////////////
    System.out.println("area (width, height) : ( " + area.width + ", "
                       + area.height + " ) ");
    System.out.println("coverage (width, height) : ( " + coverage.width + ", "
                       + coverage.height + " ) ");

    Vector<Objects> myObjects_sorted = (Vector<Objects>) currentObjects.clone();
    Collections.sort(myObjects_sorted); 
    
    for (int k_index = 0; k_index < meit.numberOfRectangle; k_index++) {
      System.out.print("k_index: " + k_index);
      if (k_index > 0 && (myObjects_sorted.size() > 0)){
        remove_objects_within_window(myObjects_sorted, opt_interval.elementAt(opt_interval.size()-1), area, coverage);
      }
      
      System.out.println(" Object#: " + myObjects_sorted.size());       
      if (myObjects_sorted.size() > 0) {
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

        //        for(Short d : aListOfX)
        //          System.out.println("x:" + d.doubleValue());

        IntervalTree root = meit.buildIntervalTree(0, aListOfX.size()-1, aListOfX, null);
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$");
        //meit.preOrderTraverse(root);
        meit.interval_tree_root = root;
        root.window = new Window(aListOfX.get(0), aListOfX.get(aListOfX.size() - 1),
                                 (short) 0, (short) 0);

        Window optimal_window = meit.maxEnclosing2(myRectangles, coverage, root);
        System.out.println("optimal_window: " + optimal_window);      
        opt_interval.add(optimal_window);
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$");    
      }
    }

    meit.writeOutput("cent-output.txt", area, coverage, currentObjects, opt_interval);
    return opt_interval;
  }
    
  static void initialize(){
    // initialize for the centralized algorithm for testing purposes
    area = new Area(Constants.AREA_WIDTH, Constants.AREA_HEIGHT);
    coverage = computeCoverage(10);
    short def_val=40;
    if(current_phenomena == Constants.TEMP_PHENOMENA){
      def_val=75;
    }
    short offsetw = Constants.AREA_WIDTH/7;
    short offseth = Constants.AREA_HEIGHT/7;
    for(int i=0; i<Constants.TOTAL_MOTES;i++){
      currentValues.add(def_val);
      int indw = i%6;
      int indh = i/6;
      currentObjects.add(new Objects((short)(offsetw+(indw*offsetw)),(short)(offseth+(indh*offseth)),def_val));
    }
//    for(Objects obj : currentObjects){
//      currentRectangle.add(new Rectangle((short)Math.max(0, obj.x - coverage.width/2),
//                                         (short)Math.max(0, obj.y - coverage.height/2),
//                                         (short)Math.min(area.width,
//                                                         obj.x + coverage.width/2),
//                                         (short)Math.min(area.height,
//                                                         obj.y + coverage.height/2),
//                                         obj.weight));
//    }
  }
  /////////////////////////////////////*********** Testing Purposes ************************////////////////////////////////////    
}
