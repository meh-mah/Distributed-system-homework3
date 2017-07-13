
package bebbroadcast.port;

import se.sics.kompics.PortType;

/**
 *
 * @author M&M
 */
public class BebPort extends PortType {
    {
        indication(BebDeliver.class);
        request(BebBroadcast.class);
    }
   
}
