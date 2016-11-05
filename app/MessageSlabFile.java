package sidnet.stack.users.sample_p2p.app;

import sidnet.core.misc.Location2D;
import jist.swans.misc.Message;
import sidnet.stack.users.sample_p2p.kmaxrs_centralized.DistSlabfile;

public class MessageSlabFile implements Message { 
  public final int queryId;
  public final long sequenceNumber;
  public int producerNodeId;
  public int sourceNodeId;
  public Location2D sourceLocation;
  public DistSlabfile dist_sf;
      
  public MessageSlabFile(DistSlabfile dis_sf, int queryId,
                         long sequenceNumber, int producerNodeId, 
                         int sourceNodeId, Location2D location2d) {
    this.dist_sf = dis_sf;
    this.queryId   = queryId;
    this.sequenceNumber = sequenceNumber;
    this.producerNodeId = producerNodeId;
    this.sourceNodeId = sourceNodeId;
    this.sourceLocation = location2d;
  }
    
  /** {@inheritDoc} */
  public int getSize() 
  { 
    return 50;
  }
  
  public int size() {
    int size = 0;
    size += 4; // double dataValue;
    size += 2; // double sequenceNumber;
    size += 4; // double sequenceNumber;
    size += 2; // int producerNodeId;
    size += 2; // int sourceNodeId;
    size += dist_sf.size();
    return size;
  }
  
  /** {@inheritDoc} */
  public void getBytes(byte[] b, int offset)
  {
    throw new RuntimeException("not implemented");
  }
  
  public MessageSlabFile clone () {
    return new MessageSlabFile(this.dist_sf, this.queryId, 
                               this.sequenceNumber, this.producerNodeId,
                               this.sourceNodeId, this.sourceLocation);
  }
}
