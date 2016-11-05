package sidnet.stack.users.sample_p2p.kmaxrs_centralized;

import java.util.Collections;
import java.util.Vector;

import sidnet.stack.users.sample_p2p.driver.Driver_SampleP2P;

public class centralized_kMaxRS {
  public static Area field;
  public static Area coverage;
  
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
    
  public static void centralized(Objects[] obj, int k) {

    Vector<Objects> myObjects = new Vector<Objects>();
    
    for (int i = 0; i < obj.length; i++) {
      if (obj[i] != null) myObjects.add((Objects) obj[i].clone());
    }
    
    Meit meit = new Meit();
    meit.numberOfRectangle = k;
    // TOP: modification
    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      System.out.println("obj: " + obj.length);
      System.out.println("k: " + meit.numberOfRectangle);
    }

    Vector<Objects> myObjects_sorted = (Vector<Objects>) myObjects.clone();
    // sort the objects, add the rectangles
    Collections.sort(myObjects_sorted);   
    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      for (Objects obj1: myObjects_sorted)
        System.out.println("obj sorted: " + obj1);
    }
    //System.out.println(" Object#: " + myObjects_sorted.size());

    Vector<Rectangle> myRectangles = new Vector<Rectangle>();
    object_to_rectangles(myRectangles, myObjects_sorted);
    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
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
    if (Driver_SampleP2P.DEBUG_ALGORITHM) {
      System.out.println("Pre order traversal the tree");
      meit.preOrderTraverse(root, 0);
    }
    
    meit.interval_tree_root = root;
    root.window = new Window(aListOfX.get(0), aListOfX.get(aListOfX.size() - 1),
                             (short) 0, (short) 0);

    Target cl2_target = meit.maxEnclosing_k(myRectangles, coverage, field, 
                                            root, k,
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
    meit.writeOutput("cen.txt", field, coverage, myObjects, cl2_target.target);
    System.out.println("the rescen.txt is written.");
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
        
//    DistSlabfile[] result = new DistSlabfile[2];
    // result[0] = new DistSlabfile(cl2_target.target, values);
//    result[0] = new DistSlabfile(cl2_target.target, values, meit.dic_window.dic, meit.numberOfRectangle);
//    System.out.println("result[0]: " + result[0]);
    // make a deep copy of the opt_interval
    // the clone method makes only the shallow copy of the vector and 
    // the objects inside.
//    Vector<Window> clu_3interval = new Vector<Window>();
//    for (Window win: cl2_target.target)
//      clu_3interval.add(win.clone());
    // result[1] = new DistSlabfile(clu_3interval, values1);
//    result[1] = new DistSlabfile(clu_3interval, values1, meit.dic_window.dic, meit.numberOfRectangle);
//    System.out.println("result[1]: " + result[1]);        
//    return result;
  }
  
  static void object_to_rectangles(Vector<Rectangle> myRectangles, Vector<Objects> myObjects_sorted) {
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
