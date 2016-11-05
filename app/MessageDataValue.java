/*
 * MessageDataP2P.java
 *
 * Created on December 18, 2007, 3:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sidnet.stack.users.sample_p2p.app;

import sidnet.core.misc.Location2D;
import jist.swans.misc.Message;

/**
 *
 * @author Oliver
 */
public class MessageDataValue implements Message {
  public final double dataValue;
  public final int queryId;
  public final long sequenceNumber;
  public int producerNodeId;
  public int sourceNodeId;
  public Location2D sourceLocation;
    
  public MessageDataValue(double dataValue) {
    this.dataValue = dataValue;
    queryId        = -1;
    sequenceNumber = -1;
  }
    
  public MessageDataValue(double dataValue, int queryId,
                          long sequenceNumber, int producerNodeId, 
                          int sourceNodeId, Location2D location2d) {
    this.dataValue = dataValue;
    this.queryId   = queryId;
    this.sequenceNumber = sequenceNumber;
    this.producerNodeId = producerNodeId;
    this.sourceNodeId = sourceNodeId;
    this.sourceLocation = location2d;
  }
    
  /** {@inheritDoc} */
  public int getSize() 
  { 
    int size = 0;
    size += 8; // double dataValue;
    size += 4; // int queryId
    size += 8; // double sequenceNumber;
    size += 4; // int producerNodeId;
    size += 4; // int sourceNodeId;
    size += 8; // int + int location2D;
  
    return size;
  }
  /** {@inheritDoc} */
  public void getBytes(byte[] b, int offset)
  {
    throw new RuntimeException("not implemented");
  }
  
  
  public MessageDataValue clone (MessageDataValue msgdv) {
    return new MessageDataValue(msgdv.dataValue, msgdv.queryId, 
                                msgdv.sequenceNumber, msgdv.producerNodeId,
                                msgdv.sourceNodeId, msgdv.sourceLocation);
  }
} // class: MessageP2P
