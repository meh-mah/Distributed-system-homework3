
package bebbroadcast.component;

import bebbroadcast.port.BebDeliver;
import id2203.link.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class BebMsg extends Pp2pDeliver {
    private BebDeliver bebDeliver;

    public BebMsg(Address src, BebDeliver bebDeliver) {
        super(src);
        this.bebDeliver = bebDeliver;
    }

    public BebDeliver getBebDeliver() {
        return bebDeliver;
    }
}
