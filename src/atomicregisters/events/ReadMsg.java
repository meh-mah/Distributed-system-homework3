
package atomicregisters.events;

import bebbroadcast.port.BebDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class ReadMsg extends BebDeliver {
    private int reg;
    private int reqId;

    public ReadMsg(Address src, int reg, int reqId) {
        super(src);
        this.reqId = reqId;
        this.reg = reg;
    }

   public int getReqId() {
        return reqId;
    }
   
    public int getRegNumber() {
        return reg;
    }

    
}

