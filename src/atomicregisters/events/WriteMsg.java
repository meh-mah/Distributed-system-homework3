
package atomicregisters.events;

import bebbroadcast.port.BebDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class WriteMsg extends BebDeliver {
    private int reg;
    private int reqId;
    private int ts;
    private int rank;
    private int val;

    public WriteMsg(Address src, int reg, int reqId, int ts, int rank, int val) {
        super(src);
        this.reg = reg;
        this.reqId = reqId;
        this.ts = ts;
        this.rank = rank;
        this.val = val;
    }
    
    public int getTimestamp() {
        return ts;
    }

    public int getReg() {
        return reg;
    }

    public int getReqId() {
        return reqId;
    }
    
    public int getRank() {
        return rank;
    }

    public int getValue() {
        return val;
    }
}