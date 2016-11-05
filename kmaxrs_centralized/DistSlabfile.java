/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sidnet.stack.users.sample_p2p.kmaxrs_centralized;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author Muhammed
 */
public class DistSlabfile {
  public Vector<Window> hintervals = null;
  public Vector<Short> neededValues = null;
  public Vector<Objects> neededObj = null;
  public Hashtable<Window, Vector<Window>> dic = null;
  public int numberOfRectangle = 0;
  public int pkt_sz = 0;

  public DistSlabfile(Vector<Window> hi, Vector<Short> nv){
    this.hintervals = hi;
    this.neededValues = nv;
  }

  public DistSlabfile(Vector<Window> hi, Vector<Objects> no, 
                      Hashtable<Window, Vector<Window>> htable, 
                      int k) 
  {
    //System.out.println("DistSlabfile initialization: ===============");
    int count_element = 0;
    this.hintervals = hi;
    this.neededObj = no;
    this.numberOfRectangle = k;
    this.dic = new Hashtable<Window, Vector<Window>> ();
    
    int key_count = 0;
    int data_count = 0;
    if (htable != null) {
      Enumeration<Window> en = htable.keys();
      key_count++;
      for (Window win: hi) {
        //System.out.println("win: " + win);
        if (htable.containsKey(win)) {
          win = win.clone();        
          Vector<Window> val_htable = htable.get(win);
          //System.out.println("val_htable: " + val_htable);
          Collections.sort(val_htable);
          Vector<Window> vwin = new Vector<Window> ();
          for (int i = 0; i < k*5 && i < val_htable.size(); i++) {
            // choose only the k highest score windows
            // System.out.println("i: " + i + " " + val_htable.elementAt(i));
            vwin.addElement(val_htable.elementAt(i).clone());
          }
          
          data_count += vwin.size();
          this.dic.put(win, vwin);
        }
      }
    }
    
    // intervals/windows y or x, h, and score -> 3 
    count_element = hintervals.size() * 3;
    // objects x, y, score -> 3
    count_element += neededObj.size() * 3;
    // hintervals both keys and values are windows 
    count_element += (data_count + key_count) * 3;
    this.pkt_sz = count_element;
  }

  @Override
  public String toString()
  {
    String result = "k = " + this.numberOfRectangle + "\n";
    result += "hintervals: [";
    for (Window win: this.hintervals) {
      result += win + " ";
    }
    result += " ]\n";
    if (this.neededValues != null) {
      result += "values: ";
      for (Short sh : this.neededValues)
        result += sh.shortValue() + " ";
    }
    
    if (this.neededObj != null) {
      result += "obj: ";
      for (Objects o : this.neededObj)
        result += o + " ";
    }
    result += "\n";
    result += "dic: {\n";
    Enumeration<Window> en = this.dic.keys();
    while (en.hasMoreElements()) {
      Window key_win = (Window) en.nextElement();
      result += key_win + " = ";
      Vector<Window> vwin = this.dic.get(key_win);
      for (Window val_win: vwin) {
        result += val_win + " ";
      }
      result += "\n";
    }
    result += "}";
    return result;
  }  
  
  public void update_dic(Hashtable<Window, Vector<Window>> d) {
    if (this.dic == null || d == null)
      return;

//    Enumeration<Window> enu1 = this.dic.keys();
//    while (enu1.hasMoreElements()) {
//      Window key_win = enu1.nextElement();
//      System.out.println("key: " + key_win + " = ");
//      for (Window win: this.dic.get(key_win))
//        System.out.println(win + " ");
//      System.out.println(" | ");
//    }
    
    Enumeration<Window> enu = d.keys();
    while (enu.hasMoreElements()) {
      Window key_win = enu.nextElement();
      Vector<Window> val_vwin = d.get(key_win);
      if (this.dic.containsKey(key_win)) {
        this.dic.get(key_win).addAll(val_vwin);
      } else {
        this.dic.put(key_win, val_vwin);
      }
    }
  }

  public int size() {
    return this.pkt_sz;
  }
}
