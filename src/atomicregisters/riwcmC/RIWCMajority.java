
package atomicregisters.riwcmC;

import atomicregisters.events.AckMsg;
import atomicregisters.events.ReadMsg;
import atomicregisters.events.ReadValMsg;
import atomicregisters.events.WriteMsg;
import atomicregisters.port.AtomicRegisterPort;
import atomicregisters.port.ReadRequest;
import atomicregisters.port.ReadResponse;
import atomicregisters.port.WriteRequest;
import atomicregisters.port.WriteResponse;
import bebbroadcast.port.BebBroadcast;
import bebbroadcast.port.BebPort;
import id2203.link.pp2p.PerfectPointToPointLink;
import id2203.link.pp2p.Pp2pSend;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class RIWCMajority extends ComponentDefinition {

    private static final Logger logger = LoggerFactory.getLogger(RIWCMajority.class);

    Negative<AtomicRegisterPort> nn_AReg = provides(AtomicRegisterPort.class);
    Positive<BebPort> beb = requires(BebPort.class);
    Positive<PerfectPointToPointLink> pp2pl = requires(PerfectPointToPointLink.class);

    private Address myAddress;
    private Set<Address> nodes;
    private int rank;
    private int regNumber;
    private List<Set<Address>> wSet;
    private List<Set<ReadSet>> rSet;
    private boolean[] reading;
    private int[] reqid;
    private int[] value;
    private int[] timestamp;
    private int[] mrank;
    private int[] wValue;
    private int[] rValue;
    private boolean majorityRead;
    private boolean majorityWrite;

    public RIWCMajority() {
        subscribe(hInit, control);
        subscribe(hReadRequest, nn_AReg);
        subscribe(hWriteRequest, nn_AReg);
        subscribe(hReadMsg, beb);
        subscribe(hWriteMsg, beb);
        subscribe(hReadValMsg, pp2pl);
        subscribe(hAckMsg, pp2pl);
    }

    Handler<RIWCMajorityInit> hInit = new Handler<RIWCMajorityInit>() {
        @Override
        public void handle(RIWCMajorityInit e) {
            nodes = e.getNodes();
            myAddress = e.getMyAddress();
            regNumber = e.getRegNumber();

            wSet = new ArrayList<>(regNumber);
            rSet = new ArrayList<>(regNumber);
            reading = new boolean[regNumber];
            reqid = new int[regNumber];
            value = new int[regNumber];
            timestamp = new int[regNumber];
            mrank = new int[regNumber];
            wValue = new int[regNumber];
            rValue = new int[regNumber];

            rank = myAddress.getId();
            for (int i = 0; i < regNumber; i++) {
                wSet.add(new HashSet<Address>());
                rSet.add(new HashSet<ReadSet>());
            }
        }
    };
    Handler<ReadRequest> hReadRequest = new Handler<ReadRequest>() {
        @Override
        public void handle(ReadRequest e) {
            logger.info("READ REQUEST ON REGISTER {} RECEIVED", e.getReg());
            int reg = e.getReg();
            ++reqid[reg];
            reading[reg] = true;
            rSet.get(reg).clear();
            wSet.get(reg).clear();
            majorityRead = false;
            majorityWrite = false;
            trigger(new BebBroadcast(new ReadMsg(myAddress, reg, reqid[reg])), beb);
        }
    };
    Handler<WriteRequest> hWriteRequest = new Handler<WriteRequest>() {
        @Override
        public void handle(WriteRequest e) {
            logger.info("WRITE REQUEST WITH VALUE {} ON REGISTER {} RECEIVED FROM APPLICATION ", e.getValue(), e.getReg());
            int reg = e.getReg();
            int value = e.getValue();

            ++reqid[reg];
            wValue[reg] = value;
            rSet.get(reg).clear();
            wSet.get(reg).clear();
            majorityRead = false;
            majorityWrite = false;
            trigger(new BebBroadcast(new ReadMsg(myAddress, reg, reqid[reg])), beb);
        }
    };
    Handler<ReadMsg> hReadMsg = new Handler<ReadMsg>() {
        @Override
        public void handle(ReadMsg e) {
            
            Address src = e.getSource();
//            logger.debug("readmsg from {}", src);
            int reg = e.getRegNumber();
            int reqId = e.getReqId();

            trigger(new Pp2pSend(src, new ReadValMsg(myAddress, reg, reqId, timestamp[reg], mrank[reg], value[reg])), pp2pl);
        }
    };
    Handler<ReadValMsg> hReadValMsg = new Handler<ReadValMsg>() {
        @Override
        public void handle(ReadValMsg e) {

            int reg = e.getReg();
            int reqId = e.getReqId();
            int ts = e.getTimestamp();
            int rank = e.getRank();
            int value = e.getValue();

            if (reqId == reqid[reg] && !majorityRead) {
                rSet.get(reg).add(new ReadSet(ts, rank, value));
//                logger.debug("readval ts= {} rank= {}", ts, rank );
                rSetCheck();
            }
        }
    };
    Handler<WriteMsg> hWriteMsg = new Handler<WriteMsg>() {
        @Override
        public void handle(WriteMsg e) {
            Address src = e.getSource();
            int reg = e.getReg();
            int reqId = e.getReqId();
            int ts = e.getTimestamp();
            int rank = e.getRank();
            int v = e.getValue();

            if ((ts > timestamp[reg])
                    || (ts == timestamp[reg] && rank > mrank[reg])) {
                value[reg] = v;
                timestamp[reg] = ts;
                mrank[reg] = rank;
            }
            trigger(new Pp2pSend(src, new AckMsg(myAddress, reg, reqId)), pp2pl);
        }
    };
    Handler<AckMsg> hAckMsg = new Handler<AckMsg>() {
        @Override
        public void handle(AckMsg e) {
            Address src = e.getSource();
            int reg = e.getRegNumber();
            int reqId = e.getReqId();

            if (reqId == reqid[reg] && !majorityWrite) {
                wSet.get(reg).add(src);
                wSetCheck();
            }
        }
    };

    private void rSetCheck() {
        for (int reg = 0; reg < regNumber; reg++) {
            if (rSet.get(reg).size() > nodes.size() / 2) {

                majorityRead = true;
                int v = 0;
                int ts = -1;
                int r = -1;
                for (ReadSet rse : rSet.get(reg)) {
                    if ((rse.timestamp() > ts)
                            || (rse.timestamp() == ts && rse.rank() > r)) {
                        ts = rse.timestamp();
                        r = rse.rank();
                        v = rse.value();
                    }
                }
                rValue[reg] = v;
                if (reading[reg]) {
                    trigger(new BebBroadcast(new WriteMsg(myAddress, reg, reqid[reg], ts, r, rValue[reg])), beb);
                } else {
                    trigger(new BebBroadcast(new WriteMsg(myAddress, reg, reqid[reg], ts + 1, rank, wValue[reg])), beb);
                }
            }
        }
    }

    private void wSetCheck() {
        for (int reg = 0; reg < regNumber; reg++) {
            if (wSet.get(reg).size() > nodes.size() / 2) {

                majorityWrite = true;
                if (reading[reg]) {
                    reading[reg] = false;
                    trigger(new ReadResponse(reg, rValue[reg]), nn_AReg);
                } else {
                    trigger(new WriteResponse(reg), nn_AReg);
                }
            }
        }
    }

    private class ReadSet {
        private int ts;
        private int r;
        private int v;

        public ReadSet(int ts, int r, int v) {
            this.ts = ts;
            this.r = r;
            this.v = v;
        }

        public int rank() {
            return r;
        }

        public int timestamp() {
            return ts;
        }

        public int value() {
            return v;
        }

        @Override
        public String toString() {
            return "/" + ts + ";" + r + ";" + v + '/';
        }
    }
}

