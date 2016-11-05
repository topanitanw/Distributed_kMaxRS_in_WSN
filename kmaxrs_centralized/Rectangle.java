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
public class Rectangle
{
  short x1;
  short x2;
  short y1;
  short y2;
  short weight;

  public Rectangle(short x1, short y1, short x2, short y2, short weight)
  {
    this.x1 = x1;
    this.x2 = x2;
    this.y1 = y1;
    this.y2 = y2;
    this.weight = weight;
  }
  
  @Override
  public String toString() {
    String txt = "Rec[(x2: " +  this.x2 + ",y2: " + this.y2 + " ), (x1: " + this.x1 + ",y1: " + this.y1 + " )";
    txt += "w: " + this.weight + "]\n";
    return txt;
  }
}
