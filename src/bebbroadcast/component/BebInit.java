
package bebbroadcast.component;

import java.util.Set;
import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class BebInit extends Init {
    private final Set<Address> II;
    private final Address myAddress;

    public BebInit(Set<Address> II, Address myAddress) {
        this.II = II;
        this.myAddress = myAddress;
    }

    public final Set<Address> getAllAddress() {
        return II;
    }

    public final Address getMyAddress() {
        return myAddress;
    }
   
}

