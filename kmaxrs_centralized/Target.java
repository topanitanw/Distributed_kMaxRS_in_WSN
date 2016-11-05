/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sidnet.stack.users.sample_p2p.kmaxrs_centralized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import sidnet.stack.users.sample_p2p.driver.Driver_SampleP2P;

/**
 *
 * @author PanitanW
 */
public class Target {
  // ArrayList<Window> target;
  public Vector<Window> target;
  Area coverage;
  Area field;
  int k;
  boolean local;
  // ArrayList<Window> toadd = null;
  Vector<Window> toadd = null;
  
  public Target(int k, Area coverage, Area field, boolean local) {
    this.target = new Vector<Window>();
    this.coverage = coverage;
    this.field = field;
    this.k = k;
    this.local = local;
  }
  
  @Override
  public String toString() {
    String txt = "Target[\n";
    for(Window win: this.target) {
      txt += String.valueOf(win) + "\n";
    }
    txt += "]\n";
    txt += "local: " + String.valueOf(this.local) + "\n";
    return txt;
  }
  
  public void sort_target() {
    // Comparator comparator = Collections.reverseOrder();
    // Collections.sort(this.target, comparator);
    Collections.sort(this.target);
  }
  
  /// ??? does not need dic_window parameter
  public void insert_window(Window win, DictWindow dic_window) {
    // insert the win into the end of the target vector and sort the target
    this.target.add(win.clone());
    
    if (this.target.size() > 1)
      this.sort_target();
    
    if (win.score == 820)
      System.out.println(win + " : " + this + " K: " + this.k);
    // if the size of the target vector is more than k,
    // keep deleting the the lowest windows in the list.
    while (this.target.size() > this.k) {
//      if (this.local && dic_window != null)
//        dic_window.add_dict(win, this.target.get(this.target.size()-1));
       this.target.removeElementAt(this.target.size()-1);
//      this.target.remove(this.target.size()-1);
    }
  }
  
//  public void put_dic(Window key_win, Window val_win, DictWindow dic_window) {
//    
//    if (this.local && dic_window != null) {
//      Vector<Window> toadd = new Vector<Window> ();
//      dic_window.add_dict(key_win.clone(), val_win.clone());
//      if (dic_window.dic.containsKey(val_win)) {
//        toadd.addAll(dic_window.dic.remove(val_win));
//      }      
//      
//      for (Window win : toadd)
//        this.add_window(win, dic_window);
//    } 
//  }
  
  public boolean add_window(Window win, DictWindow dic_window) {
    // System.out.println("add_window " + win);
    // System.out.println("dic_window: " + dic_window);
    boolean found = false;
    if (win == null)
      return false;
    
    
    if (Driver_SampleP2P.DEBUG_ALGORITHM && 
        (win.l == (short) 82) && (win.r == (short) 82) &&
        (win.score == (short) 634)) {
      System.out.println("\nfound it " + win + " ---------------------------------------------- \n");
      found = true;
    }
      
    if (this.target.size() == 0) {
      // if there is no window in the vector, just insert it.
      this.insert_window(win, dic_window);
      if (found) System.out.println("e1 ----------------------------------------------------------------");
      return true;
    } else { 
      if (this.k == this.target.size() && 
          this.target.get(this.target.size()-1).score >= win.score) 
      {  
        // System.out.println("test1");
        if (found) System.out.println("e2 ----------------------------------------------------------------");        
        return false;
      } 
      
      for(int i = 0; i < this.target.size(); i++) {
       
        if (this.target.get(i).isWinOnLHS(win, this.coverage.width)) {
          if (found) System.out.println("e3 ----------------------------------------------------------------");          
          return false;
        }
        
        if (this.target.get(i).score >= win.score &&
            this.target.get(i).is_overlap(win, this.coverage, this.field)) 
        {
          // this.put_dic(this.target.get(i), win, dic_window);
          if (this.local && dic_window != null) {
            dic_window.add_dict(this.target.get(i), win);
            if (found) System.out.println("add " + this.target.get(i) + " ----------------------------------------------------------------");
          }
          if (found) System.out.println("e4 ----------------------------------------------------------------");
          return false;
        }
        
        if (win.score > this.target.get(i).score &&
            this.target.get(i).is_overlap(win, this.coverage, this.field)) {
          // this.put_dic(win, this.target.get(i), dic_window);
          // ArrayList<Window> toadd = new ArrayList<Window> ();
          toadd = new Vector<Window> ();
          if (this.local && dic_window != null) {
            dic_window.add_dict(win, this.target.get(i));
            if (dic_window.dic.containsKey(this.target.get(i))) {
              toadd.addAll(dic_window.dic.get(this.target.get(i)));
            }
          }
          
          this.target.set(i, win.clone());
          for (int j = this.target.size()-1; j >= i+1; j-- ) {
            // if this new window is overlap with other windows in the target,
            if (this.target.get(j).is_overlap(win, this.coverage, this.field)) {
              //this.put_dic(win, this.target.get(j), dic_window);  
              if (this.local && dic_window != null) {
                dic_window.add_dict(win, this.target.get(j));
                if (dic_window.dic.containsKey(this.target.get(j))) {
                  toadd.addAll(dic_window.dic.get(this.target.get(j)));
                }
              }
              this.target.remove(j);
            } // if (this.target.get(j).is ...
          } // for (int j ...
          
          if (this.local) {
            for (Window win_add: toadd) {
              this.add_window(win_add, dic_window);
            }
            toadd = null;
          }
          
          // System.out.println("test5");                    
          this.sort_target();
          if (found) System.out.println("e5 ----------------------------------------------------------------");          
          return true;
        }
      }

      this.insert_window(win, dic_window);
      if (found) System.out.println("e6 ----------------------------------------------------------------");      
      return true;      
    } // if (this.target.size() == 0)
  } // add_window

  public int size() {
    // int + 2 Area of (int) + 
    int sz = 4 + 2 * 4;
    // target contains windows
    sz += this.target.size() * 3;  
    return 0;
  }
} // class target