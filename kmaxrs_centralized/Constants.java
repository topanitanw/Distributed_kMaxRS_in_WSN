/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sidnet.stack.users.sample_p2p.kmaxrs_centralized;

import java.util.Hashtable;

/**
 *
 * @author beSim
 */
public class Constants {
    ////////////////////////Default static values..............................
    public static final short AREA_WIDTH= 350;  //area width
    public static final short AREA_HEIGHT= 350;  //area size
    public static final short GAP_HEIGHT= AREA_HEIGHT/7;  //area gap
     public static final short GAP_WIDTH= AREA_WIDTH/7;  //area gap
    
    public static final short TOTAL_MOTES = 36;
    
    public static final short LIGHT_PHENOMENA = 0;  //if the phenomena is light
    public static final short TEMP_PHENOMENA = 1;  //if the phenomena is temperature
    
    public static final short ENERGY_INACTIVE_MOTE = 3;
    public static final short ENERGY_ACTIVE_MOTE = 25;
    
    public static Hashtable<String, Integer> nodeIds= new Hashtable<String, Integer>();
    
    public static void setAddresIDMapping(){
        nodeIds.put("0000", 0);
        nodeIds.put("0101", 1);
        nodeIds.put("0202", 2);
        nodeIds.put("1003", 3);
        nodeIds.put("1104", 4);
        nodeIds.put("1205", 5);
        nodeIds.put("0306", 6);
        nodeIds.put("7EBA", 7);
        nodeIds.put("0407", 8);
        nodeIds.put("1308", 9);
        nodeIds.put("7F45", 10);
        nodeIds.put("1409", 11);
        nodeIds.put("0510", 12);
        nodeIds.put("0611", 13);
        nodeIds.put("0712", 14);
        nodeIds.put("1513", 15);
        nodeIds.put("1614", 16);
        nodeIds.put("1715", 17);
        nodeIds.put("2016", 18);
        nodeIds.put("2117", 19);
        nodeIds.put("2218", 20);
        nodeIds.put("3019", 21);
        nodeIds.put("3120", 22);
        nodeIds.put("3221", 23);
        nodeIds.put("2322", 24);
        nodeIds.put("79A3", 25);
        nodeIds.put("2423", 26);
        nodeIds.put("3324", 27);
        nodeIds.put("7997", 28);
        nodeIds.put("3425", 29);
        nodeIds.put("2526", 30);
        nodeIds.put("2627", 31);
        nodeIds.put("2728", 32);
        nodeIds.put("3529", 33);
        nodeIds.put("3630", 34);
        nodeIds.put("3731", 35);
    }
    
    public static short getNodeId(String addr){
        if(nodeIds.contains(addr))
            return nodeIds.get(addr).shortValue();
        return -1;    
    }
    
    public static Point getNodeLocation(short id){
        if(id<0 || id>35){
            return null;
        }
        short offsetw = Constants.AREA_WIDTH/7;
        short offseth = Constants.AREA_HEIGHT/7;
        int indw = id%6;
        int indh = id/6;
        return new Point((short)(offsetw+(indw*offsetw)),(short)(offseth+(indh*offseth)));
    }
    
    public static boolean isTelos(short id){
        if(id==7 || id==10 || id==25 || id==28){
            return false;
        }
        return true;
    } 
}
