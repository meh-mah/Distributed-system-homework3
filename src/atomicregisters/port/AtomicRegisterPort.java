
package atomicregisters.port;

import se.sics.kompics.PortType;

/**
 *
 * @author M&M
 */
public class AtomicRegisterPort extends PortType {
    {
        indication(ReadResponse.class);
        indication(WriteResponse.class);
        request(ReadRequest.class);
        request(WriteRequest.class);
    }
}
