
package application;

import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class ApplicationInit extends Init {
    private String cmd;
    private Address myAddress;

   
    public ApplicationInit(String cmd) {
        this.cmd = cmd;
    }

    public ApplicationInit(String cmd, Address myAddress) {
        this(cmd);
        this.myAddress = myAddress;
    }

    public String getCommandScript() {
        return cmd;
    }

    public Address getMyAddress() {
        return myAddress;
    }
}
