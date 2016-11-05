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
 * @author PanitanW
 */
public class DictWindow {
  
  Hashtable<Window, Vector<Window>> dic;
  Area field;
  Area coverage;
  
  public DictWindow(Area coverage, Area field) {
    this.dic = new Hashtable<Window, Vector<Window>> ();
    this.coverage = coverage;
    this.field = field;
  }

  public DictWindow(Hashtable<Window, Vector<Window>> dic, Area coverage, Area field) {
    this.dic = dic;
    this.coverage = coverage;
    this.field = field;
  }
  
  public void add_dict(Window key_win, Window val_win) {
    Window key_win_clone = key_win.clone();
    Window val_win_clone = val_win.clone();
    Vector<Window> v_win = null;
    if (this.dic.containsKey(key_win_clone)) {
      v_win = this.dic.get(key_win_clone);
      if (!v_win.contains(val_win_clone))
        v_win.addElement(val_win_clone);
    } else {
      v_win = new Vector<Window> ();
      v_win.addElement(val_win_clone);
      this.dic.put(key_win, v_win);
    }
  }

  public void update_dic(Hashtable<Window, Vector<Window>> d) {
    Enumeration<Window> enu = d.keys();
    while (enu.hasMoreElements()) {
      Window key_win = enu.nextElement();
      // Vector<Window> val_vwin = d.get(key_win);
      if (this.dic.containsKey(key_win)) {
        // this.dic.get(key_win).addAll(val_vwin);
        this.dic.get(key_win).addAll(d.get(key_win));
      } else {
        // this.dic.put(key_win, val_vwin);
        this.dic.put(key_win, d.get(key_win));
      }
      
      Collections.sort(this.dic.get(key_win));
    }
  }
  
  @Override
  public String toString() {
    String result = "DictWindow: {\n";
    Enumeration<Window> en = this.dic.keys();
    while (en.hasMoreElements()) {
      Window win = en.nextElement();
      result += "key: " + win + " [ ";
      // Vector<Window> vwin = this.dic.get(win);
      
      // if (vwin != null) {
      if (this.dic.get(win) != null) {
        for (Window ewin: this.dic.get(win))
          result += " " + ewin;
      } else {
        // result += vwin;
        result += this.dic.get(win);
      }
      result += " ]\n";
    }
    result += " }";
    return result;
  }   
}
