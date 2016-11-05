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
public class Window implements Comparable
{ // root.window = Window(xs[0],xs[-1],-5,0)
  public short l;
  public short r;
  public short h;  // height = the sweeping line 
  public short score;

  public Window(short l, short r, short h, short score)
  {
    this.l = l;
    this.r = r;
    this.h = h;
    this.score = score;
  }
  
  public Window clone()
  { //Deep copy
    Window clone = new Window(this.l, this.r, this.h, this.score);
    return clone;
  }
  
  @Override
  public boolean equals(Object o) {
    Window other = (Window) o;    
    return (this.l == other.l && this.r == other.r && this.h == other.h && this.score == other.score);
  }
  
  @Override
  public int hashCode() {
    // used in the hash function so that the same window will be hashed 
    // at the same location
    return (int) this.l+this.r+this.h+this.score;
  }   

  public int compareTo(Object o)
  { // used in the collections.sort()
    Window other = (Window) o;
    if(this.score < other.score)
      return 1;  // more than the one we are checking 
    else if(this.score == other.score)
      return 0;  // equal to the one we are checking
    else
      return -1; // less then the one we are checking 
  }

  @Override
  public String toString()
  {
    String result = "win[l: " + this.l + " r: " + this.r;
    result += " h: " + this.h + " s: " + this.score + "]";
    return result;
  }
  
  public int to_rec_top(Area coverage, Area field) {
    return Math.min(field.height, this.h + coverage.height/2);
  }
  
  public int to_rec_bottom(Area coverage, Area field) {
    return Math.max(0, this.h - coverage.height/2);
  }
  
  public int to_rec_right(Area coverage, Area field) {
    return Math.min(field.width, this.l + coverage.width/2);
  }
  
  public int to_rec_left(Area coverage, Area field) {
    return Math.max(0, this.l - coverage.width/2);
  }
  
  public boolean isWinOnLHS(Window win, int rec_width) {
    return ((win.h == this.h) && (win.score == this.score) && 
            (win.r == this.l - rec_width));
  }
  
  public boolean is_overlap(Window other, Area coverage, Area field) {
    if ((this.to_rec_left(coverage, field) > 
         other.to_rec_right(coverage, field)) ||
        (this.to_rec_right(coverage, field) < 
         other.to_rec_left(coverage, field))) {
      return false;
    }
    
    if ((this.to_rec_top(coverage, field) < 
         other.to_rec_bottom(coverage, field)) ||
        (other.to_rec_top(coverage, field) < 
         this.to_rec_bottom(coverage, field))) {
      return false;
    }
    return true;
  }
}
