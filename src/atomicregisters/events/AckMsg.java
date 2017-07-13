
package atomicregisters.events;

import id2203.link.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class AckMsg extends Pp2pDeliver {
    private int reg;
    private int reqId;

    public AckMsg(Address src, int reg, int reqId) {
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

