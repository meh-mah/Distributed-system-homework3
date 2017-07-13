
package atomicregisters.port;

import se.sics.kompics.Event;

/**
 *
 * @author M&M
 */
public class ReadRequest extends Event {
    private int reg;

    public ReadRequest(int reg) {
        this.reg = reg;
    }

    public int getReg() {
        return reg;
    }
}

