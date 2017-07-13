
package atomicregisters.port;

import se.sics.kompics.Event;

/**
 *
 * @author M&M
 */
public class WriteRequest extends Event {
    private int reg;
    private int value;

    public WriteRequest(int reg, int value) {
        this.reg = reg;
        this.value = value;
    }

    public int getReg() {
        return reg;
    }

    public int getValue() {
        return value;
    }
}

