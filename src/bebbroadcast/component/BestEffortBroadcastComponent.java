
package bebbroadcast.component;

import bebbroadcast.port.BebBroadcast;
import bebbroadcast.port.BebPort;
import id2203.link.pp2p.PerfectPointToPointLink;
import id2203.link.pp2p.Pp2pSend;
import java.util.Set;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class BestEffortBroadcastComponent extends ComponentDefinition {

    Negative<BebPort> beb = provides(BebPort.class);
    Positive<PerfectPointToPointLink> pp2pl = requires(PerfectPointToPointLink.class);

    private Address myAddress;
    private Set<Address> II;

    public BestEffortBroadcastComponent() {
        subscribe(hInit, control);
        subscribe(hBebBroadcast, beb);
        subscribe(hBebMsg, pp2pl);
    }

    Handler<BebInit> hInit = new Handler<BebInit>() {
        @Override
        public void handle(BebInit e) {
            myAddress = e.getMyAddress();
            II = e.getAllAddress();
        }
    };
    Handler<BebBroadcast> hBebBroadcast = new Handler<BebBroadcast>() {
        @Override
        public void handle(BebBroadcast e) {
            BebMsg msg = new BebMsg(myAddress, e.getBebDeliver());
            for (Address add : II) {
                trigger(new Pp2pSend(add, msg), pp2pl);
            }
        }
    };
    Handler<BebMsg> hBebMsg = new Handler<BebMsg>() {
        @Override
        public void handle(BebMsg e) {
            trigger(e.getBebDeliver(), beb);
        }
    };
}

