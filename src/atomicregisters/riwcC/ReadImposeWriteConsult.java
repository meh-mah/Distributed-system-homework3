
package atomicregisters.riwcC;

import atomicregisters.events.AckMsg;
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
import id2203.ports.pfd.Crash;
import id2203.ports.pfd.PFDPort;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
public class ReadImposeWriteConsult extends ComponentDefinition {
    //    private static final Logger logger = LoggerFactory.getLogger(ReadImposeWriteConsult.class);
    Negative<AtomicRegisterPort> nn_AReg = provides(AtomicRegisterPort.class);
    Positive<PFDPort> pfd = requires(PFDPort.class);
    Positive<BebPort> beb = requires(BebPort.class);
    Positive<PerfectPointToPointLink> pp2pl = requires(PerfectPointToPointLink.class);
    
    private Set<Address> neighbors;
    private Set<Address> correctNodes;
    private Address myAddress;
    private int regSize;
    private Map<Integer, Set<Address>> wSet;
    private boolean[] reading;
    private int[] mrank, requestId, readValue, value, timestamp;
    private int myRank;


    public ReadImposeWriteConsult() {
        subscribe(initHandler, control);
        subscribe(hReadRequest, nn_AReg);
        subscribe(hWriteRequest, nn_AReg);
        subscribe(hWriteMsg, beb);
        subscribe(hAckMessage, pp2pl);
        subscribe(hCrash, pfd);

        correctNodes = new HashSet<>();
    }
    Handler<ReadImposeWriteConsultInit> initHandler = new Handler<ReadImposeWriteConsultInit>() {
        @Override
        public void handle(ReadImposeWriteConsultInit e) {
            neighbors = e.getNeighbors();
            correctNodes.addAll(neighbors);
            myAddress = e.getMyAddress();
            correctNodes.add(myAddress);
            regSize = e.getRegSize();
            wSet = new HashMap<>();
            reading = new boolean[regSize];
            mrank = new int[regSize];
            requestId = new int[regSize];
            readValue = new int[regSize];
            value = new int[regSize];
            timestamp = new int[regSize];
            myRank = myAddress.getId();
            for (int i = 0; i < regSize; i++) {
                wSet.put(i, new HashSet<Address>());
                reading[i] = false;
                mrank[i] = 0;
                requestId[i] = 0;
                readValue[i] = 0;
                value[i] = 0;
                timestamp[i] = 0;
            }
        }
    };
    Handler<ReadRequest> hReadRequest = new Handler<ReadRequest>() {
        @Override
        public void handle(ReadRequest e) {
            int reg = e.getReg();
            requestId[reg]++;
            reading[reg] = true;
            wSet.get(reg).clear();
            readValue[reg] = value[reg];
//            logger.info("READ REQUEST RECEIVED :: reqid= {}", requestId[reg]);
            WriteMsg wMsg = new WriteMsg(myAddress, reg, requestId[reg], timestamp[reg], mrank[reg], value[reg]);
            trigger(new BebBroadcast(wMsg), beb);
        }
    };
    Handler<WriteRequest> hWriteRequest = new Handler<WriteRequest>() {
        @Override
        public void handle(WriteRequest e) {
            int reg = e.getReg();
            int value = e.getValue();
            requestId[reg]++;
            wSet.get(reg).clear();
//            logger.debug("WRITE REQUEST RECEIVED reqId= {}", requestId[reg]);
            WriteMsg writeMsg = new WriteMsg(myAddress, reg, requestId[reg], timestamp[reg] + 1, myRank, value);
            trigger(new BebBroadcast(writeMsg), beb);
        }
    };
    Handler<WriteMsg> hWriteMsg = new Handler<WriteMsg>() {
        @Override
        public void handle(WriteMsg e) {
            int reg = e.getReg();
            int ts = e.getTimestamp();
            int rank = e.getRank();
//                logger.debug("WRITE REQUEST RECEIVED FROM {} reqId= {}", e.getSource(), requestId[reg]);
            if (ts > timestamp[reg] || (ts == timestamp[reg] && rank > mrank[reg])) {
                value[reg] = e.getValue();
                timestamp[reg] = ts;
                mrank[reg] = rank;   
            }
            trigger(new Pp2pSend(e.getSource(), new AckMsg(myAddress, reg, e.getReqId())), pp2pl);
        }
    };
    Handler<AckMsg> hAckMessage = new Handler<AckMsg>() {
        @Override
        public void handle(AckMsg e) {
            int reg = e.getRegNumber();
            int reqId = e.getReqId();
//            logger.debug("ACK FROM {} RECEIVED", e.getSource());
//            logger.debug("ACK INFO: reqId {}, id {}", requestId[reg], reqId);
            if (reqId == requestId[reg]) {
                wSet.get(reg).add(e.getSource());
            }
            checkAck(reg);
        }
    };
    Handler<Crash> hCrash = new Handler<Crash>() {
        @Override
        public void handle(Crash e) {
            correctNodes.remove(e.getDetected());
            for (int i = 0; i < regSize; i++) {
                checkAck(i);
            }
        }
    };

    private boolean checkAck(int regNo) {


        for (Address a : correctNodes) {

            if (!wSet.get(regNo).contains(a)) {
                return false;
            }
        }

        if (reading[regNo]) {
            reading[regNo] = false;
            trigger(new ReadResponse(regNo, readValue[regNo]), nn_AReg);
        } else {
            trigger(new WriteResponse(regNo), nn_AReg);
        }

        return true;
    }
}

