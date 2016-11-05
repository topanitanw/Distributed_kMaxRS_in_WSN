/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sidnet.stack.users.sample_p2p.kmaxrs_centralized;

/**
 *
 * @author Muhammed
 */

import java.io.IOException;
import java.util.Collections;
import java.util.Vector;     // Vector // Collections.sort

import sidnet.stack.users.sample_p2p.driver.Driver_SampleP2P;

import java.io.*;            // FileReader, BufferedReader
import java.util.Hashtable;

public class Meit
{
  // meit = maximum enclosing interval tree
  // global variables
  // used in incToNode functions to determine whether an interval
  // overlapping/intersecting the left or right of current interval has
  // been found  
  boolean left_found = false;
  boolean right_found = false;

  // 1 is the left one, 2 is the right one   
  // used to help determine whether to merge two adjacent intervals in
  // decToNode leftInoput 1 and 2 are the intervals to the left and to the right
  // of the left point of current interval respectively. e.g current
  // interval -> [2,4] left_intersect1 = [x,2] left_intersect2 = [2,y]
  // x < 2 and 2<y<=4 same applies to right_intersect
  IntervalTree left_intersect1 = null;
  IntervalTree left_intersect2 = null;
  IntervalTree right_intersect1 = null;
  IntervalTree right_intersect2 = null;

  IntervalTree interval_tree_root = null;
  int numberOfRectangle = 2;
  DictWindow dic_window = null;
  
  Boolean DEBUG_MEIT = false;
  // build a balanced interval tree
  // use the average of two median points as root; attach left nodes(nodes
  // to the left of right medians) as left subtrees and right nodes
  // (nodes to the right of left median) as right subtees
  public IntervalTree buildIntervalTree(int st, int ed,
                                        Vector listOfPoints,
                                        IntervalTree root)
  {
    if (st == ed)
    {
      Short pt = (Short) listOfPoints.elementAt(st);
      IntervalTree leaf_node = new IntervalTree((double)pt.shortValue(),
                                                root);
      Short p1 = (Short) listOfPoints.elementAt(st);
      Short p2 = (Short) listOfPoints.elementAt(st);
      leaf_node.window = new Window(p1.shortValue(), // l
                                    p2.shortValue(), // r
                                    (short)-5, // h
                                    (short)0); // score
      return leaf_node;
    }

    int mid = (st + ed) / 2;
    // System.out.println("st: " + st + " mid: " + mid + " ed: " + ed);
    Short melem1 = ((Short) listOfPoints.elementAt(mid)).shortValue();
    Short melem2 = ((Short) listOfPoints.elementAt(mid+1)).shortValue();
    //    System.out.println("listOfPoint: [mid]:" + melem1.shortValue() + 
    //                       " [mid+1]:" + melem2 +
    //                       " div2:" + (melem1.shortValue() + melem2.shortValue())/2);    
    IntervalTree new_node = new IntervalTree((double)(melem1.shortValue() + melem2.shortValue())/2.0, root);
    
    new_node.left_child = buildIntervalTree(st, mid, listOfPoints, new_node);
    new_node.right_child = buildIntervalTree(mid+1, ed, listOfPoints, new_node);
    return new_node;
  }

  public int preOrderTraverse(IntervalTree root, int x)
  {
    if(root == null)
      return x;

    x = this.preOrderTraverse(root.left_child, x);
    System.out.println(String.valueOf(x) + " " +  root); 
    x++;
    x = this.preOrderTraverse(root.right_child, x);
    return x;
  }

  public Target maxEnclosing(Vector aListOfRectangles,
                             Area coverage, Area field, 
                             IntervalTree root, int k)
  {
    //Addition for the distributed process
    // Hashtable slabFile = new Hashtable();
    // Window curWindow = new Window((short)0, (short)0, (short)0, (short)-5); 
    // optimal answer 
    // Window optimalWindow = new Window((short)0, (short)0, (short)0, (short)0);
    Window optimal_window = null;
    Target optimal_target = new Target(k, coverage, field, true);
    // top index is the index of the next rectangle whose bottom should
    // be added into interval tree (Note: top index is for bottom edge
    // of a rectangle) it can be interpreted as the top lane of a sweep
    // lane algorithm. the active rectangles are between top index and
    // bot index
    int topIndex = 0;
    // bot index is the index of the next rectangle whose top should be
    // removed from interval tree (Note: bot index is for top edge of a
    // rectangle.)
    int botIndex = 0;
    DictWindow dic_window = new DictWindow(coverage, field);
    
    while(topIndex < aListOfRectangles.size())
    {
      // bottom index is always smaller than top index because we
      // process the bottom of a rectangle before we process the top
      // of a rectangle
      Rectangle top_rect = (Rectangle)(aListOfRectangles.elementAt(topIndex));
      Rectangle bot_rect = (Rectangle)(aListOfRectangles.elementAt(botIndex));
      if(top_rect.y1 <= bot_rect.y2)
      {
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");        
        /*System.out.println("bot line: y1, x1, x2, score: " +
                           aListOfRectangles.get(topIndex).y1 + ", " + 
                           aListOfRectangles.get(topIndex).x1 + ", " + 
                           aListOfRectangles.get(topIndex).x2 +", "+aListOfRectangles.get(topIndex).weight);*/
        this.incIntervalTree(top_rect.y1, 
                             top_rect.x1, 
                             top_rect.x2,
                             top_rect.weight,
                             root);

        //--------------------------------------------------------------------------------------------
        //        if(root.maxscore > optimalWindow.score)
        //        {
        //          optimalWindow = (Window) root.target.clone();
        //          optimalWindow.score = root.maxscore;
        //          optimalWindow.h = top_rect.y1;
        //        }
        // System.out.println("root.target: " + root.target);
        optimal_window = (Window) root.target.clone();
        optimal_window.score = root.maxscore;
        optimal_window.h = top_rect.y1;
        Target temp_target = new Target(k, coverage, field, false);
        // dic_window.remove_key(top_rect.y1);
        temp_target.add_window(optimal_window, dic_window);
        IntervalTree nodev = this.traverseToNodeV(root, 
                                                  top_rect.x1,
                                                  top_rect.x2,
                                                  top_rect.y1,
                                                  temp_target);
        this.traverseToLeafNode(nodev, top_rect.x1, top_rect.y1, temp_target);
        this.traverseToLeafNode(nodev, top_rect.x2, top_rect.y1, temp_target);
        // System.out.println("temp_target: " + temp_target);
        for (int i = 0; i < temp_target.target.size(); i++) {
          optimal_target.add_window(temp_target.target.get(i), dic_window);
        }
        
        // System.out.println("optimal_target: " + optimal_target);
        topIndex++;

        /*if(root != null)
          //System.out.println("bot local_best: " + root.target.h + ", " + root.target.l
                            + ", " + root.target.r + ", " + root.maxscore);*/
      } else
      {
        /*System.out.println("top line: y2, x1, x2, Score: " +
                           aListOfRectangles.get(botIndex).y2 + ", " + 
                           aListOfRectangles.get(botIndex).x1 + ", " + 
                           aListOfRectangles.get(botIndex).x2 +", "+aListOfRectangles.get(botIndex).weight);*/
        
        this.decIntervalTree(bot_rect.y2, 
                             bot_rect.x1, 
                             bot_rect.x2,
                             bot_rect.weight,
                             root);
        //Distributed part extension -------------------------------------------------------------
       /* short curh=aListOfRectangles.get(botIndex).y2;
        if(slabFile.containsKey(curh)){
            if(root.maxscore>slabFile.get(curh).score){
                Window curWindow = (Window) root.target.clone();
                curWindow.score = root.maxscore;
                curWindow.h = curh;
                slabFile.put(curWindow.h, curWindow);
            }
        }
        else{
            Window curWindow = (Window) root.target.clone();
            curWindow.score = root.maxscore;
            curWindow.h = curh;
            slabFile.put(curWindow.h, curWindow);
        }*/
        //--------------------------------------------------------------------------------------------
        botIndex++;
       /* if(root != null)
          System.out.println("top local_best: " + root.target.h + ", " + root.target.l
                             + ", " + root.target.r + ", " + root.maxscore);*/
      } // end else
    } // end while
    // return slabFile; 
    return optimal_target;
  }
  
  public Target maxEnclosing_k(Vector aListOfRectangles,
                               Area coverage, Area field, 
                               IntervalTree root, int k, 
                               Target optimal_target) {
    // Addition for the distributed process
    // Hashtable slabFile = new Hashtable();
    // Window curWindow = new Window((short)0, (short)0, (short)0, (short)-5); 
    // optimal answer 
    // Window optimalWindow = new Window((short)0, (short)0, (short)0, (short)0);
    Window optimal_window = null;
    if (optimal_target == null)
      optimal_target = new Target(k, coverage, field, true);
    // top index is the index of the next rectangle whose bottom should
    // be added into interval tree (Note: top index is for bottom edge
    // of a rectangle) it can be interpreted as the top lane of a sweep
    // lane algorithm. the active rectangles are between top index and
    // bot index
    int topIndex = 0;
    // bot index is the index of the next rectangle whose top should be
    // removed from interval tree (Note: bot index is for top edge of a
    // rectangle.)
    int botIndex = 0;
    if (dic_window == null)
      this.dic_window = new DictWindow(coverage, field);
    
    while(topIndex < aListOfRectangles.size())
    {
      // bottom index is always smaller than top index because we
      // process the bottom of a rectangle before we process the top
      // of a rectangle
      Rectangle top_rect = (Rectangle)(aListOfRectangles.elementAt(topIndex));
      Rectangle bot_rect = (Rectangle)(aListOfRectangles.elementAt(botIndex));
      if(top_rect.y1 <= bot_rect.y2)
      {
         if (DEBUG_MEIT && Driver_SampleP2P.DEBUG_ALGORITHM) {
           System.out.println("topIndex %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% " + topIndex + " | " + optimal_target.k);
           System.out.println(top_rect);
         }
//        System.out.println("bot line: y1, x1, x2, score: " +
//                           aListOfRectangles.get(topIndex).y1 + ", " + 
//                           aListOfRectangles.get(topIndex).x1 + ", " + 
//                           aListOfRectangles.get(topIndex).x2 +", "+aListOfRectangles.get(topIndex).weight);
        // System.out.println("top_rect: " + top_rect);
        this.incIntervalTree(top_rect.y1, 
                             top_rect.x1, 
                             top_rect.x2,
                             top_rect.weight,
                             root);

        //--------------------------------------------------------------------------------------------
        //        if(root.maxscore > optimalWindow.score)
        //        {
        //          optimalWindow = (Window) root.target.clone();
        //          optimalWindow.score = root.maxscore;
        //          optimalWindow.h = top_rect.y1;
        //        }
        // System.out.println("root.target: " + root.target);
        if (root.target != null) {
          optimal_window = (Window) root.target.clone();
          optimal_window.score = root.maxscore;
          optimal_window.h = top_rect.y1;
        }
        // test @TOP use only the optimal_target and compare this approach with
        // centralized version in the python already
        // test @TOP false -> true
        Target temp_target = new Target(k, coverage, field, true);
        // dic_window.remove_key(top_rect.y1);
        temp_target.add_window(optimal_window, dic_window);
        // optimal_target.add_window(optimal_window, dic_window);
        IntervalTree nodev = this.traverseToNodeV(root, 
                                                  top_rect.x1,
                                                  top_rect.x2,
                                                  top_rect.y1,
                                                  temp_target);
//                                                  optimal_target);
//        this.traverseToLeafNode(nodev, top_rect.x1, top_rect.y1, optimal_target);
         this.traverseToLeafNode(nodev, top_rect.x1, top_rect.y1, temp_target);
//        this.traverseToLeafNode(nodev, top_rect.x2, top_rect.y1, optimal_target);
         this.traverseToLeafNode(nodev, top_rect.x2, top_rect.y1, temp_target);
        // System.out.println("optimal_target: " + optimal_target);

        for (int i = 0; i < temp_target.target.size(); i++) {
          optimal_target.add_window(temp_target.target.get(i), dic_window);
        }
        
        if (DEBUG_MEIT && Driver_SampleP2P.DEBUG_ALGORITHM) 
          System.out.println("optimal_target: " + optimal_target);
        // System.out.println("dic_window: " + dic_window);
        topIndex++;

        /*if(root != null)
          //System.out.println("bot local_best: " + root.target.h + ", " + root.target.l
                            + ", " + root.target.r + ", " + root.maxscore);
        */
      } else
      {
        this.decIntervalTree(bot_rect.y2, 
                             bot_rect.x1, 
                             bot_rect.x2,
                             bot_rect.weight,
                             root);
        //Distributed part extension -------------------------------------------------------------
        //--------------------------------------------------------------------------------------------
        botIndex++;
       /* if(root != null)
          System.out.println("top local_best: " + root.target.h + ", " + root.target.l
                             + ", " + root.target.r + ", " + root.maxscore);*/
      } // end else
    } // end while
    // return slabFile; 
    return optimal_target;
  }
    
  public Window maxEnclosing2(Vector<Rectangle> aListOfRectangles,
                               Area coverage, IntervalTree root)
  {
      // optimal answer 
      Window optimalWindow = new Window((short)0, (short)0, (short)0, (short)0);
      // top index is the index of the next rectangle whose bottom should
      // be added into interval tree (Note: top index is for bottom edge
      // of a rectangle) it can be interpreted as the top lane of a sweep
      // lane algorithm. the active rectangles are between top index and
      // bot index
      int topIndex = 0;
      // bot index is the index of the next rectangle whose top should be
      // removed from interval tree (Note: bot index is for top edge of a
      // rectangle.)
      int botIndex = 0;
      while(topIndex < aListOfRectangles.size())
      {
        // bottom index is always smaller than top index because we
        // process the bottom of a rectangle before we process the top
        // of a rectangle
        if(aListOfRectangles.get(topIndex).y1 <= aListOfRectangles.get(botIndex).y2)
        {
          //        System.out.println("bot line: y1, x1, x2, score: " +
          //                           aListOfRectangles.get(topIndex).y1 + ", " + 
          //                           aListOfRectangles.get(topIndex).x1 + ", " + 
          //                           aListOfRectangles.get(topIndex).x2 +", "+aListOfRectangles.get(topIndex).weight);
          this.incIntervalTree(aListOfRectangles.get(topIndex).y1, 
                               aListOfRectangles.get(topIndex).x1, 
                               aListOfRectangles.get(topIndex).x2,
                               aListOfRectangles.get(topIndex).weight,
                               root);
          if(root.maxscore > optimalWindow.score)
          {
            optimalWindow = (Window) root.target.clone();
            optimalWindow.score = root.maxscore;
            optimalWindow.h = aListOfRectangles.get(topIndex).y1;
          }
          topIndex++;

  //        if(root != null)
  //          System.out.println("bot local_best: " + root.target.h + ", " + root.target.l
  //                             + ", " + root.target.r + ", " + root.maxscore);
        } else
        {
  //        System.out.println("top line: y2, x1, x2, Score: " +
  //                           aListOfRectangles.get(botIndex).y2 + ", " + 
  //                           aListOfRectangles.get(botIndex).x1 + ", " + 
  //                           aListOfRectangles.get(botIndex).x2 +", "+aListOfRectangles.get(botIndex).weight);

          this.decIntervalTree(aListOfRectangles.get(botIndex).y2, 
                               aListOfRectangles.get(botIndex).x1, 
                               aListOfRectangles.get(botIndex).x2,
                               aListOfRectangles.get(botIndex).weight,
                               root);
          botIndex++;
  //        if(root != null)
  //          System.out.println("top local_best: " + root.target.h + ", " + root.target.l
  //                             + ", " + root.target.r + ", " + root.maxscore);
        } // end else
      } // end while
      return optimalWindow; 
  }
  
  public IntervalTree traverseToNodeV(IntervalTree root, short l,
                                      short r, short h, Target win_target) {
    if (root.target != null) {
      Window new_window = (Window) root.target.clone();
      new_window.score = root.maxscore;
      new_window.h = h;
      win_target.add_window(new_window, dic_window);
    }
    
    if(root.discriminant < l)
      return this.traverseToNodeV(root.right_child, l, r, h, win_target);
    else if(root.discriminant > r)
      return this.traverseToNodeV(root.left_child, l, r, h, win_target);
    else
      return root;    
  }

  public IntervalTree traverseToLeafNode(IntervalTree root, short v, short h,
                                         Target win_target) {
    if (root.target != null) {
      Window new_window = (Window) root.target.clone();
      new_window.score = root.maxscore;
      new_window.h = h;
      win_target.add_window(new_window, dic_window);
    }    
    
    if(root.discriminant == v)
      return root;
    if(root.discriminant> v)
      return this.traverseToLeafNode(root.left_child, v, h, win_target);
    if(root.discriminant < v)
      return this.traverseToLeafNode(root.right_child, v, h, win_target);
    return null; // suppress warning     
  }
  
  public IntervalTree findNodeV(IntervalTree root, short l, short r, short h)
  {
    // System.out.println("find node v root: " + root + " d: " + root.discriminant + " l: " + l + " r: " + r + " h: " + h);    
    propagateExcess(root, h);
    if(root.discriminant < l)
      return this.findNodeV(root.right_child, l, r, h);
    else if(root.discriminant > r)
      return this.findNodeV(root.left_child, l, r, h);
    else
      return root;
  }
  
  public IntervalTree findLeafNode(IntervalTree root, double v, short h)
  {
    this.propagateExcess(root, h);
    if(root.discriminant == v)
      return root;
    if(root.discriminant > v)
      return this.findLeafNode(root.left_child, v, h);
    if(root.discriminant < v)
      return this.findLeafNode(root.right_child, v, h);
    return null; // suppress warning 
  }

  // propagate Excess of a father node to its two child nodes.
  public void propagateExcess(IntervalTree root, short h)
  {
    // System.out.println("propagateExcess: root " + root);
    if(root.excess != 0)
    {
      if(root.left_child != null)
      {
        root.left_child.excess += root.excess;
        root.left_child.maxscore += root.excess;
        if(root.left_child.window != null)
        {
          root.left_child.window.score += root.excess;
          root.left_child.window.h = h;
        }
      }
      if(root.right_child != null)
      {
        root.right_child.excess += root.excess;
        root.right_child.maxscore += root.excess;
        if(root.right_child.window != null)
        {
          root.right_child.window.score += root.excess;
          root.right_child.window.h = h;
        }              
      }
    }
    root.excess = 0;
  }

  // insert window [a,b] to the first node with discriminant larger than a
  // and smaller than b
  public IntervalTree insertWindow(Window window, IntervalTree root)
  {
    if(window == null)
      return null;
    if(root.discriminant <= window.r && root.discriminant >= window.l)
    {
      root.window = window;
      return root;
    } else if(root.discriminant< window.l)
      return this.insertWindow(window, root.right_child);
    else
      return this.insertWindow(window, root.left_child);
  }

  // when the bottom of a rectangle is processed, we add ([l,r]) it
  // into the interval tree
  public IntervalTree incToNodeV(short l, short r, short h, short weight,
                                 IntervalTree root)
  {
    if(root.window != null)
    {
      // a window [a,b] in the tree contains interval [l,r]
      // in this case , we break it into three new windows [a,l] [l,r] and [r,b]
      if((!this.left_found) && (!this.right_found) && (root.window.l <= l)
         && (root.window.r >= r))
      {
        Window left_window = null;
        Window right_window = null;
        // [l, r] -> [root.window.l, l] [l,r] [r, root.window.r]
        if(root.window.l < l)
          left_window = new Window(root.window.l, l, h, root.window.score);
        if(root.window.r > r)
          right_window = new Window(r, root.window.r, h, root.window.score);
        Window mid_window = new Window(l, r, h, (short) (root.window.score + weight));
        this.insertWindow(left_window, root);
        this.insertWindow(right_window, root);
        this.insertWindow(mid_window, root);
        this.left_found = true;
        this.right_found = true;
      } else if ((root.window.l < l) && (root.window.r > l) && (!this.left_found))
      {
        // a window [a,b] overlap with the left part of interval
        // breaks the window into two windows [a,l] [l,b]
        // or [root.window.l, l] [l, root.window.r]
        Window mid_window = new Window(l, root.window.r, h, (short) (root.window.score+weight));
        Window left_window = new Window(root.window.l, l, h, root.window.score);
        this.insertWindow(left_window, root);
        this.insertWindow(mid_window, root);
        this.left_found = true;
      } else if((root.window.l < r) && (root.window.r > r) && (!this.right_found))
      {
        // a window [a,b] overlap with the right part of interval
        // breaks the window into two windows [a,r] [r,b]
        // [root.window.l, r] [r, root.window. r]
        Window right_window = new Window(r, root.window.r, h, root.window.score);
        Window mid_window = new Window(root.window.l, r, h, (short) (root.window.score+weight));
        this.insertWindow(right_window, root);
        this.insertWindow(mid_window, root);
        this.right_found = true;
      } else if((root.window.l >= l) && (root.window.r <= r) && 
                (! ((this.left_found && (root.window.l == l)) ||  
                    (this.right_found && (root.window.r == r)))))
      {
        root.window.score += weight;
        root.window.h = h;
      }

      if(root.discriminant > r)
        return this.incToNodeV(l, r, h, weight, root.left_child);
      else if(root.discriminant < l)
        return this.incToNodeV(l, r, h, weight, root.right_child);
      else
        return root;
    }
	return null; // suppress warnings
  }

  public IntervalTree incToNodeL(short l, short r, short h, short weight,
                                 IntervalTree root)
  {
    if(root.window != null)
      // left overlapping 
      // a window [a,b] overlap with the left part of interval
      // breaks the window into two windows [a,l] [l,b]
      if((root.window.l < l) && (root.window.r > l) &&
         (!this.left_found))
      {
        Window mid_window = new Window(l, root.window.r, h, (short) (root.window.score+weight));
        Window left_window = new Window(root.window.l, l, h, root.window.score);
        this.insertWindow(left_window, root);
        this.insertWindow(mid_window, root);
        this.left_found = true;
      } else if ((root.window.l >= l) && (root.window.r <= r) && 
                 ((! ((this.left_found && (root.window.l == l)) || 
                      (this.right_found && root.window.r == r))) || 
                  (root.window.r == root.window.l)))
      { // interval contains window 
        root.window.score += weight;
        root.window.h = h;
      }

    if(root.discriminant == l)
      return root;
    else if(root.discriminant < l)
      return this.incToNodeL(l, r, h, weight, root.right_child);
    else if (root.discriminant > l)
    {
      // right subtree must be contained in the interval
      // change the root's excess
      root.right_child.excess += weight;
      root.right_child.maxscore += weight;
      if(root.right_child.window != null)
      {
        root.right_child.window.score += weight;
        root.right_child.window.h = h;
      }
      return this.incToNodeL(l, r, h, weight, root.left_child);
    }
	  return null;
  }

  public IntervalTree incToNodeR(short l, short r, short h, short weight,
                                 IntervalTree root)
  {
    if(root.window != null)
    {
      if((root.window.r > r) && (root.window.l < r) && (!this.right_found))
      {
        // a window [a,b] overlap with the right part of interval
        // breaks the window into two windows [a,r] [r,b]        
        Window right_window = new Window(r, root.window.r, h, root.window.score);
        Window mid_window = new Window(root.window.l, r, h, (short) (root.window.score+weight));
        this.insertWindow(right_window, root);
        this.insertWindow(mid_window, root);
        this.right_found = true;        
      } else if ((root.window.l >= l) && (root.window.r <= r) && 
                 ((!((this.left_found && (root.window.l == l)) || 
                     (this.right_found && (root.window.r == r)))) ||
                  (root.window.r == root.window.l)))
      {
        root.window.score += weight;
        root.window.h = h;
      }
    }

    if(root.discriminant == r)
      return root;
    else if(root.discriminant > r)
      return this.incToNodeR(l, r, h, weight, root.left_child);
    else if(root.discriminant < r)
    {
      // left subtree must be contained in the interval
      // change the root's excess      
      root.left_child.excess += weight;
      root.left_child.maxscore += weight;
      if(root.left_child.window != null)
      {
        root.left_child.window.score += weight;
        root.left_child.window.h = h;
      }
      return this.incToNodeR(l, r, h, weight, root.right_child);
    }
    return null; // suppress warnings
  }  

  // backward path. compare the maximum of a node's two children and the
  // score of the window on current node
  // chose the largest one as the local maximum
  public Window updateToNode(IntervalTree cur, IntervalTree end_node)
  {
    if((cur.window != null) && 
       ((cur.left_child == null) || (cur.window.score > cur.left_child.maxscore)) && 
       ((cur.right_child == null) || (cur.window.score > cur.right_child.maxscore)))
    {
      cur.maxscore = cur.window.score;
      cur.target = cur.window;
    } else if((cur.left_child != null) &&    
              ((cur.right_child == null) ||  
               cur.left_child.maxscore > cur.right_child.maxscore))
    {
      cur.maxscore = cur.left_child.maxscore;
      cur.target = cur.left_child.target;
    } else if(cur.right_child != null)
    {
      cur.maxscore = cur.right_child.maxscore;
      cur.target = cur.right_child.target;
    }

    if(cur == end_node)
      return cur.target;
    else
      return this.updateToNode(cur.father, end_node);
  }

  // processing the bottom of a rectangle
  // propogate excess first
  // then go through the tree to find overlapping or containing windows   
  // the bottom of the rectangle is namecoded as "the interval"
  // the intervals or windows in the interval tree are namecoded "window"      
  public void incIntervalTree(short h, short l, short r, short weight, 
                              IntervalTree root)
  {
    this.left_found = false;
    this.right_found = false;

    IntervalTree node_v = this.findNodeV(root, l, r, h);
    IntervalTree node_l = this.findLeafNode(root, l, h);
    IntervalTree node_r = this.findLeafNode(root, r, h);
    //System.out.println("node_v: " + node_v);
    //System.out.println("node_l: " + node_l);
    //System.out.println("node_r: " + node_r);
    this.incToNodeV(l, r, h, weight, root);
    this.incToNodeL(l, r, h, weight, node_v.left_child);
    this.incToNodeR(l, r, h, weight, node_v.right_child);

    this.updateToNode(node_l, node_v);
    this.updateToNode(node_r, node_v);
    this.updateToNode(node_v, root);
  }

  // processing the top of a rectangle    
  // propogate excess first
  // then traverse the interval tree to merge or change affected windows        
  public void decIntervalTree(short h, short l, short r, short weight,
                              IntervalTree root)
  {
    this.left_intersect1 = null;
    this.left_intersect2 = null;
    this.right_intersect1 = null;
    this.right_intersect2 = null;
    IntervalTree node_v = findNodeV(root, l, r, h);
    IntervalTree node_l = findLeafNode(root, l, h);
    IntervalTree node_r = findLeafNode(root, r, h);
   // System.out.println("node_v: " + node_v);
   // System.out.println("node_l: " + node_l);
   // System.out.println("node_r: " + node_r);

    this.decToNode(l, r, h, weight, root, 'v');
    this.decToNode(l, r, h, weight, node_v.left_child, 'l');
    this.decToNode(l, r, h, weight, node_v.right_child, 'r');

    this.updateToNode(node_l, node_v);
    this.updateToNode(node_r, node_v);
    this.updateToNode(node_v, root);
  }
  // processing the top of a rectangle case by case
  // leaf nodes represent a point and have windows [a,a]  
  public void decToNode(short l, short r, short h, short weight, 
                        IntervalTree root, char flag)
  {
    if(root.window != null)
    {
      if(root.window.l < root.window.r)
      {
        // since we do not break windows on leaf nodes, we need to
        // check whether the current node is a leaf node first
        // try to find the adjacent pairs that intersect on l and r        
        if(root.window.l == l)
          this.left_intersect2 = root;
        if(root.window.r == l)
          this.left_intersect1 = root;
        if(root.window.l == r)
          this.right_intersect2 = root;
        if(root.window.r == r)
          this.right_intersect1 = root;
      }
      // if the interval contains the window, change the score of the window
      if((root.window.l >= l) && (root.window.r <= r))
      {
        root.window.score -= weight;
        root.window.h = h;
      }

      if((this.left_intersect1 != null) && (this.left_intersect2 != null))
      { // two adjacent windows that intersect on l are found
        // delete the current window(if the weight difference is
        // equal to the current weight of the interval)
        // merge two windows into the one that is closer to the root        
        if((this.left_intersect1.window.score == this.left_intersect2.window.score))
        {
          Window new_window = new Window(this.left_intersect1.window.l, 
                                         this.left_intersect2.window.r,
                                         this.left_intersect2.window.h,
                                         this.left_intersect2.window.score);
          this.left_intersect1.window = null;
          this.left_intersect2.window = null;
          if(new_window.r == r)
            this.right_intersect1 = this.insertWindow(new_window, 
                                                      this.interval_tree_root);
        }
        this.left_intersect1 = null;
        this.left_intersect2 = null;
      }

      if((this.right_intersect1 != null) && (this.right_intersect2 != null))
      {
        // two adjacent windows that intersect on r are found
        // delete the current window, merge both windows into the
        // adjacent one which is closer to the root        
        if(this.right_intersect1.window.score == this.right_intersect2.window.score)
        {
          Window new_window = new Window(this.right_intersect1.window.l, 
                                         this.right_intersect2.window.r,
                                         this.right_intersect1.window.h,
                                         this.right_intersect1.window.score);
          this.right_intersect1.window = null;
          this.right_intersect2.window = null;
          // if the left window happens to intersect with point
          // r, the new merged window needs to be marked
          if(new_window.l == l)
            this.left_intersect2 = this.insertWindow(new_window,
                                                     this.interval_tree_root);

          root.window = null;
        }
        this.right_intersect1 = null;
        this.right_intersect2 = null;
      }
    }

    if(flag == 'v')
    {
      if((root.discriminant > l) && (root.discriminant < r))
        return;
      else if(r < root.discriminant)
        this.decToNode(l, r, h, weight, root.left_child, flag);
      else if(l > root.discriminant)
        this.decToNode(l, r, h, weight, root.right_child, flag);
    } else if(flag == 'l')
    {
      if(root.discriminant == l)
        return;
      else if(l < root.discriminant)
      {
        root.right_child.excess -= weight;
        root.right_child.maxscore -= weight;
        if(root.right_child.window != null)
        {
          root.right_child.window.score -= weight;
          root.right_child.window.h = h;
        }
        this.decToNode(l, r, h, weight, root.left_child, flag);
      } else if(l > root.discriminant)
          this.decToNode(l, r, h, weight, root.right_child, flag);
    } else if(flag == 'r')
    {
      if(root.discriminant == r)
        return;
      else if(r < root.discriminant)
        this.decToNode(l, r, h, weight, root.left_child, flag);
      else if(r > root.discriminant)
      {
        root.left_child.excess -= weight;
        root.left_child.maxscore -= weight;
        if(root.left_child.window != null)
        {
          root.left_child.window.score -= weight;
          root.left_child.window.h = h;
        }
        this.decToNode(l, r, h, weight, root.right_child, flag);
      }
    }
  }
  
  public void writeOutput(String file_name, Area area, Area coverage,
                          Vector<Objects> listOfObjects, Vector<Window> optimal_window)
  {
    System.out.println("input file_name: " + file_name);
//    String[] name_split = file_name.split("/");
//    System.out.println("name_split size: " + String.valueOf(name_split.length));
//    for(String name : name_split)
//      System.out.print("file_name_split: " + name + "|");
//    System.out.println("");
//    
    String new_file_name = "res" + file_name;
    System.out.println("output file: " + new_file_name);
    File oldFile = new File(new_file_name);
    // if the file exists, delete it
    if (oldFile.isFile() && oldFile.exists())
      oldFile.delete();
    
    try
    {
      FileWriter fwriter = new FileWriter(new_file_name);
      System.out.println("Write the output file: " + new_file_name);
      fwriter.write(String.valueOf(area.width) + " " +  String.valueOf(area.height)
                    + "\r\n");
      fwriter.write(String.valueOf(coverage.width) + " " +
                    String.valueOf(coverage.height) + "\r\n");
      fwriter.write(String.valueOf(listOfObjects.size()) + "\r\n");

      for(int i = 0; i < listOfObjects.size(); i++)
      {
        fwriter.write(String.valueOf(listOfObjects.get(i).x) + " " +
                      String.valueOf(listOfObjects.get(i).y) + " "
                      + String.valueOf(listOfObjects.get(i).weight)
                      +"\r\n");
      }

      fwriter.write(optimal_window.size() + "\r\n");
      for(Object obj: optimal_window) {
        Window w = (Window) obj;
        fwriter.write(String.valueOf(w.l) + " " +
                      String.valueOf(w.r) + " " +
                      String.valueOf(w.h) + " " +
                      String.valueOf(w.score) + "\r\n");
      }
      
      fwriter.close();
    } catch (IOException ex)
    {
      ex.printStackTrace();
    }

    System.out.println("Writing the output " + new_file_name + " is done");
    return;
  }
}
