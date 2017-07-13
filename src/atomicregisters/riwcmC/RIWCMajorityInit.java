
package atomicregisters.riwcmC;

import java.util.Set;
import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class RIWCMajorityInit extends Init {
    private Set<Address> Nodes;
    private Address myAddress;
    private int regNumber;

    public RIWCMajorityInit(Set<Address> Nodes, Address myAddress, int regNumber) {
        this.Nodes = Nodes;
        this.myAddress = myAddress;
        this.regNumber = regNumber;
    }

    public Set<Address> getNodes() {
        return Nodes;
    }

    public int getRegNumber() {
        return regNumber;
    }

    public Address getMyAddress() {
        return myAddress;
    }
}
