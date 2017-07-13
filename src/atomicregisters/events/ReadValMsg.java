
package atomicregisters.events;

import id2203.link.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class ReadValMsg extends Pp2pDeliver {
    private int reg;
    private int reqid;
    private int ts;
    private int mrank;
    private int val;

    public ReadValMsg(Address src, int reg, int reqId, int ts, int mrank, int val) {
        super(src);
        this.reg = reg;
        this.reqid = reqId;
        this.ts = ts;
        this.mrank = mrank;
        this.val = val;
    }

    public int getReg() {
        return reg;
    }
    
    public int getTimestamp() {
        return ts;
    }

    public int getReqId() {
        return reqid;
    }
    
    public int getValue() {
        return val;
    }
    
    public int getRank() {
        return mrank;
    }

    

    
}

