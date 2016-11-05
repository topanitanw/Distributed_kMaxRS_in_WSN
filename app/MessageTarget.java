package sidnet.stack.users.sample_p2p.app;

import sidnet.core.misc.Location2D;
import jist.swans.misc.Message;
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.DistSlabfile;
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.Target;

public class MessageTarget implements Message 
{
  public final int queryId;
  public final long sequenceNumber;
  public int producerNodeId;
  public int sourceNodeId;
  public Location2D sourceLocation;
  public Target target;
      
  public MessageTarget(Target tar, int queryId,
                       long sequenceNumber, int producerNodeId, 
                       int sourceNodeId, Location2D location2d) {
    this.target = tar;
    this.queryId   = queryId;
    this.sequenceNumber = sequenceNumber;
    this.producerNodeId = producerNodeId;
    this.sourceNodeId = sourceNodeId;
    this.sourceLocation = location2d;
  }
    
  /** {@inheritDoc} */
  public int getSize() 
  { 
    //throw new RuntimeException("not implemented");
    int size = 0;
    size += 4; // double dataValue;
    size += 2; // double sequenceNumber;
    size += 4; // double sequenceNumber;
    size += 2; // int producerNodeId;
    size += 2; // int sourceNodeId;

    return size;
  }

  public int size() {
    int size = 0;
    size += 4; // double dataValue;
    size += 2; // double sequenceNumber;
    size += 4; // double sequenceNumber;
    size += 2; // int producerNodeId;
    size += 2; // int sourceNodeId;
    size += this.target.size();
    return size;
  }
  
  /** {@inheritDoc} */
  public void getBytes(byte[] b, int offset)
  {
    throw new RuntimeException("not implemented");
  }
  
  public MessageTarget clone () {
    return new MessageTarget(this.target, this.queryId, 
                             this.sequenceNumber, this.producerNodeId,
                             this.sourceNodeId, this.sourceLocation);
  }
}
