
package amain;

import application.Application;
import application.ApplicationInit;
import atomicregisters.port.AtomicRegisterPort;
import atomicregisters.riwcmC.RIWCMajority;
import atomicregisters.riwcmC.RIWCMajorityInit;
import bebbroadcast.component.BebInit;
import bebbroadcast.component.BestEffortBroadcastComponent;
import bebbroadcast.port.BebPort;
import id2203.link.pp2p.PerfectPointToPointLink;
import id2203.link.pp2p.delay.DelayLink;
import id2203.link.pp2p.delay.DelayLinkInit;
import java.util.Set;
import org.apache.log4j.PropertyConfigurator;
import se.sics.kompics.*;
import se.sics.kompics.address.Address;
import se.sics.kompics.launch.Topology;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.mina.MinaNetwork;
import se.sics.kompics.network.mina.MinaNetworkInit;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

/**
 *
 * @author M&M
 */
public class Assignment3bMain extends ComponentDefinition {
    static {
        PropertyConfigurator.configureAndWatch("log4j.properties");
    }
    private final static int REGISTER_NUMBER = 5;
    private static int selfId;
    private static String commandScript;
    Topology topology = Topology.load(System.getProperty("topology"), selfId);

    public static void main(String[] args) {
        selfId = Integer.parseInt(args[0]);
        commandScript = args[1];

        Kompics.createAndStart(Assignment3bMain.class);
    }

    public Assignment3bMain() {
        // create components
        Component time = create(JavaTimer.class);
        Component network = create(MinaNetwork.class);
        Component pp2p = create(DelayLink.class);
        Component beb = create(BestEffortBroadcastComponent.class);
        Component nnar = create(RIWCMajority.class);
        Component app = create(Application.class);

        // handle possible faults in the components
        subscribe(faultHandler, time.control());
        subscribe(faultHandler, network.control());
        subscribe(faultHandler, pp2p.control());
        subscribe(faultHandler, beb.control());
        subscribe(faultHandler, nnar.control());
        subscribe(faultHandler, app.control());

        // initialize the components
        Address self = topology.getSelfAddress();
//        Set<Address> neighborSet = topology.getNeighbors(self);
        Set<Address> all = topology.getAllAddresses();

        trigger(new MinaNetworkInit(self, 5), network.control());
        trigger(new DelayLinkInit(topology), pp2p.control());
        trigger(new BebInit(all, self), beb.control());
        trigger(new RIWCMajorityInit(all, self, REGISTER_NUMBER), nnar.control());
        trigger(new ApplicationInit(commandScript), app.control());

        // connect the components
        connect(app.required(AtomicRegisterPort.class), nnar.provided(AtomicRegisterPort.class));
        connect(app.required(Timer.class), time.provided(Timer.class));

        connect(nnar.required(BebPort.class), beb.provided(BebPort.class));
        connect(nnar.required(PerfectPointToPointLink.class), pp2p.provided(PerfectPointToPointLink.class));

        connect(beb.required(PerfectPointToPointLink.class), pp2p.provided(PerfectPointToPointLink.class));

        connect(pp2p.required(Network.class), network.provided(Network.class));
        connect(pp2p.required(Timer.class), time.provided(Timer.class));
    }
    //handlers
    Handler<Fault> faultHandler = new Handler<Fault>() {
        @Override
        public void handle(Fault fault) {
            fault.getFault().printStackTrace(System.err);
        }
    };
}

