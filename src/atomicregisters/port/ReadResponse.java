
package atomicregisters.port;

import se.sics.kompics.Event;

/**
 *
 * @author M&M
 */
public class ReadResponse extends Event {
    private int reg;
    private int val;

    public ReadResponse(int reg, int val) {
        this.reg = reg;
        this.val = val;
    }

    public int getReg() {
        return reg;
    }

    public int getValue() {
        return val;
    }
}

