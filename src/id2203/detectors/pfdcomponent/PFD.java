
package id2203.detectors.pfdcomponent;

import id2203.detectors.events.CheckTimeout;
import id2203.detectors.events.HBMsg;
import id2203.detectors.events.HBTimeout;
import id2203.link.pp2p.PerfectPointToPointLink;
import id2203.link.pp2p.Pp2pSend;
import id2203.ports.pfd.Crash;
import id2203.ports.pfd.PFDPort;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

/**
 *
 * @author M&M
 */
public class PFD extends ComponentDefinition {
    
    Negative<PFDPort> pfd = provides(PFDPort.class);
    Positive<PerfectPointToPointLink> pp2pl = requires(PerfectPointToPointLink.class);
    Positive<Timer> timer = requires(Timer.class);
    
    private static final Logger logger = LoggerFactory.getLogger(PFD.class);
    
    private Set<Address> neighbors;
    private Set<Address> corrects;
    private Set<Address> crashed;
    
    private int HBfrequency;
    private int checkfrquency;
    private Address myAddress;

    public PFD() {
         
        subscribe(hInit, control);
        subscribe(hHBTimeout, timer);
        subscribe(hTimeout, timer);
        subscribe(hHBMsg, pp2pl);
        
        crashed = new HashSet<>();
        corrects = new HashSet<>();
        
    }
    Handler<PFDInit> hInit = new Handler<PFDInit>() {
        @Override
        public void handle(PFDInit event) {
            
            neighbors = event.getNeighbors(); 
            myAddress = event.getMyAddress();
            corrects.addAll(neighbors);
            HBfrequency = event.HBfrequency();
            checkfrquency = event.checkfrquency();

            ScheduleTimeout HBSchedule = new ScheduleTimeout(HBfrequency);
            HBSchedule.setTimeoutEvent(new HBTimeout(HBSchedule));
            trigger(HBSchedule, timer);

            ScheduleTimeout checkInterval = new ScheduleTimeout(HBfrequency + checkfrquency);
            checkInterval.setTimeoutEvent(new CheckTimeout(checkInterval));
            trigger(checkInterval, timer);
        }
    };
    Handler<HBTimeout> hHBTimeout = new Handler<HBTimeout>() {
        @Override
        public void handle(HBTimeout event) {
            
            for (Address node : neighbors) {
                trigger(new Pp2pSend(node, new HBMsg(myAddress)), pp2pl);
//                logger.debug("send hb to {}", node);
            }

            ScheduleTimeout HBSchedule = new ScheduleTimeout(HBfrequency);
            HBSchedule.setTimeoutEvent(new HBTimeout(HBSchedule));
            trigger(HBSchedule, timer);
        }
    };
    Handler<CheckTimeout> hTimeout = new Handler<CheckTimeout>() {
        @Override
        public void handle(CheckTimeout event) {
            for (Address node : neighbors) {
                if (!corrects.contains(node) && (!crashed.contains(node))) {
                    crashed.add(node);
                    logger.debug("crashed {}", node);
                    trigger(new Crash(node), pfd);
                    
                }
            }

            corrects.clear();
            ScheduleTimeout checkInterval = new ScheduleTimeout(HBfrequency + checkfrquency);
            checkInterval.setTimeoutEvent(new CheckTimeout(checkInterval));
            trigger(checkInterval, timer);
        }
    };
    Handler<HBMsg> hHBMsg = new Handler<HBMsg>() {
        @Override
        public void handle(HBMsg event) {
            corrects.add(event.getSource());
//            logger.debug("hb received {}", event.getSource());
        }
    };
}

