
package atomicregisters.riwcC;

import java.util.Set;
import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class ReadImposeWriteConsultInit extends Init {
    private Set<Address> neighbors;
    private Address myAddress;
    private int regSize;

    public ReadImposeWriteConsultInit(Set<Address> neighbors, Address myaddress, int regSize) {
        this.neighbors = neighbors;
        this.myAddress = myaddress;
        this.regSize = regSize;
    }

    public Set<Address> getNeighbors() {
        return neighbors;
    }

    public Address getMyAddress() {
        return myAddress;
    }

    public int getRegSize() {
        return regSize;
    }
}

