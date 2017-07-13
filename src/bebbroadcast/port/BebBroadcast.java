
package bebbroadcast.port;

import se.sics.kompics.Event;

/**
 *
 * @author M&M
 */
public class BebBroadcast extends Event {
    private BebDeliver bebDeliver;

    public BebBroadcast(BebDeliver bebDeliver) {
        this.bebDeliver = bebDeliver;
    }

    public BebDeliver getBebDeliver() {
        return bebDeliver;
    }
}
