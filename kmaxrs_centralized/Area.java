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
public class Area
{ // width x, height y
  public short height;
  public short width;

  public Area(short height, short width)
  {
    this.height = height;
    this.width = width;
  }
  
  public Area(int height, int width)
  {
    this.height = (short) height;
    this.width = (short) width;
  }
}
