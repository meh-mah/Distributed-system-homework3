
package atomicregisters.port;

import se.sics.kompics.Event;

/**
 *
 * @author M&M
 */
public class WriteResponse extends Event {
    private int reg;

    public WriteResponse(int reg) {
        this.reg = reg;
    }

    public int getReg() {
        return reg;
    }
}
