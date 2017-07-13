
package bebbroadcast.port;

import java.io.Serializable;
import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public abstract class BebDeliver extends Event implements Serializable {
    private final Address src;

    public BebDeliver(Address src) {
        this.src = src;
    }

    public Address getSource() {
        return src;
    }
}

