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
public class Objects implements Comparable
{
  public short x;
  public short y;
  public short weight;

  public Objects(short x, short y, short weight)
  {
    this.x = x;
    this.y = y;
    this.weight = weight;
  }

  public String toString() {
    String txt = "obj[x: " + this.x + " y: " + this.y + " w: " + this.weight + "]\n";
    return txt;
  }

  public Objects clone() {
    return new Objects(this.x, this.y, this.weight);
  }
  
  public int compareTo(Object o) 
  { // used in the collections.sort()
    Objects other = (Objects) o;
    if (this.y > other.y) {
      return 1;  // more than the one we are checking 
    } else if (this.y == other.y){
      if (this.x > other.x){
          return 1;
      } else if(this.x < other.x){
          return -1;
      }
      return 0;  // equal to the one we are checking 
    } else
      return -1; // less then the one we are checking 
  }
  
  @Override
  public boolean equals(Object o) {
    Objects other = (Objects) o;    
    return (this.x == other.x && this.y == other.y);
  }
}
