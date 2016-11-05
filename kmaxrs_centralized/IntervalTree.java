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
public class IntervalTree
{
  double discriminant;
  IntervalTree left_child;
  IntervalTree right_child;
  Window window;
  short maxscore; // not sure
  Window target;
  short excess;   // not sure
  IntervalTree father;
  
  public IntervalTree(double discriminant, IntervalTree father)
  {
    this.discriminant = discriminant;
    this.left_child = null;
    this.right_child = null;
    this.window = null;
    this.maxscore = 0;
    this.target = null;
    this.excess = 0;
    this.father = father;
  }
  
  public String toString()
  {
    String txt = "----------------------------------------\n";
    txt += "d: " + this.discriminant + " excess: " + this.excess;
    txt += " maxscore: " + this.maxscore + " \n";
    if (this.father != null) {
      txt += "father: " + this.father.discriminant;
    } else {
      txt += "father: null";
    }
    
    if (this.left_child != null) {
      txt += " left_ch: " + this.left_child.discriminant;
    } else {
      txt += " left_ch: null";
    }
    
    if (this.right_child != null) {
      txt += " right_ch: " + this.right_child.discriminant + "\n";
    } else {
      txt += " right_ch: null\n";
    }
    
    txt += "win: " + String.valueOf(this.window);
    txt += " target: " + String.valueOf(this.target) + "\n";
    txt += "----------------------------------------\n";
    return txt;
  }  
}