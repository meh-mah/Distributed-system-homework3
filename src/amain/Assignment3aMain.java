
package amain;

import application.Application;
import application.ApplicationInit;
import atomicregisters.port.AtomicRegisterPort;
import atomicregisters.riwcC.ReadImposeWriteConsult;
import atomicregisters.riwcC.ReadImposeWriteConsultInit;
import bebbroadcast.component.BebInit;
import bebbroadcast.component.BestEffortBroadcastComponent;
import bebbroadcast.port.BebPort;
import id2203.detectors.pfdcomponent.PFD;
import id2203.detectors.pfdcomponent.PFDInit;
import id2203.link.pp2p.PerfectPointToPointLink;
import id2203.link.pp2p.delay.DelayLink;
import id2203.link.pp2p.delay.DelayLinkInit;
import id2203.ports.pfd.PFDPort;
import java.util.Set;
import org.apache.log4j.PropertyConfigurator;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Fault;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
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
public class Assignment3aMain extends ComponentDefinition {
    static {
        PropertyConfigurator.configureAndWatch("log4j.properties");
    }
    private static int selfId;
    private static String commandScript;
    Topology topology = Topology.load(System.getProperty("topology"), selfId);

    public static void main(String[] args) {
        selfId = Integer.parseInt(args[0]);
        commandScript = args[1];

        Kompics.createAndStart(Assignment3aMain.class);
    }

    public Assignment3aMain() {
        // create components
        Component time = create(JavaTimer.class);
        Component network = create(MinaNetwork.class);
        Component pp2p = create(DelayLink.class);
        Component pfd = create(PFD.class);
        Component beb = create(BestEffortBroadcastComponent.class);
        Component riwc = create(ReadImposeWriteConsult.class);
        Component app = create(Application.class);

        // handle possible faults in the components
        subscribe(faultHandler, time.control());
        subscribe(faultHandler, network.control());
        subscribe(faultHandler, pp2p.control());
        subscribe(faultHandler, pfd.control());
        subscribe(faultHandler, beb.control());
        subscribe(faultHandler, riwc.control());
        subscribe(faultHandler, app.control());

        // initialize the components
        Address self = topology.getSelfAddress();
        Set<Address> neighborSet = topology.getNeighbors(self);
        Set<Address> II=topology.getAllAddresses();
        int heartbeatInterval = 1000;
        int checkInterval = 4000;
        int numRegisters = 1;

        trigger(new MinaNetworkInit(self, 5), network.control());
        trigger(new DelayLinkInit(topology), pp2p.control());
        trigger(new BebInit(II, self), beb.control());
        trigger(new PFDInit(neighborSet, self, heartbeatInterval, checkInterval), pfd.control());
        trigger(new ReadImposeWriteConsultInit(neighborSet, self, numRegisters), riwc.control());
        trigger(new ApplicationInit(commandScript, self), app.control());

        // connect the components
        connect(app.required(AtomicRegisterPort.class), riwc.provided(AtomicRegisterPort.class));
        connect(app.required(Timer.class), time.provided(Timer.class));

        connect(riwc.required(BebPort.class), beb.provided(BebPort.class));
        connect(riwc.required(PerfectPointToPointLink.class), pp2p.provided(PerfectPointToPointLink.class));
        connect(riwc.required(PFDPort.class), pfd.provided(PFDPort.class));

        connect(beb.required(PerfectPointToPointLink.class), pp2p.provided(PerfectPointToPointLink.class));

        connect(pfd.required(PerfectPointToPointLink.class), pp2p.provided(PerfectPointToPointLink.class));
        connect(pfd.required(Timer.class), time.provided(Timer.class));

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

