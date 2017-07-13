
package application;

import atomicregisters.port.AtomicRegisterPort;
import atomicregisters.port.ReadRequest;
import atomicregisters.port.ReadResponse;
import atomicregisters.port.WriteRequest;
import atomicregisters.port.WriteResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.*;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

/**
 *
 * @author M&M
 */
public final class Application extends ComponentDefinition {

    Positive<AtomicRegisterPort> nn_Ar = requires(AtomicRegisterPort.class);
    Positive<Timer> timer = requires(Timer.class);

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private String[] commands;
    private int lastCommand;

    public Application() {
        subscribe(hInit, control);
        subscribe(hStart, control);
        subscribe(hContinue, timer);
        subscribe(hReadResponse, nn_Ar);
        subscribe(hWriteResponse, nn_Ar);
    }

    Handler<ApplicationInit> hInit = new Handler<ApplicationInit>() {
        @Override
        public void handle(ApplicationInit e) {
            commands = e.getCommandScript().split(":");
            lastCommand = -1;
        }
    };
    Handler<Start> hStart = new Handler<Start>() {
        @Override
        public void handle(Start e) {
            doNextCommand();
        }
    };
    Handler<ApplicationContinue> hContinue = new Handler<ApplicationContinue>() {
        @Override
        public void handle(ApplicationContinue e) {
            doNextCommand();
        }
    };
    Handler<ReadResponse> hReadResponse = new Handler<ReadResponse>() {
        @Override
        public void handle(ReadResponse e) {
            logger.info("ReadResponse: register[{}] = {}", e.getReg(), e.getValue());
            doNextCommand();
        }
    };
    Handler<WriteResponse> hWriteResponse = new Handler<WriteResponse>() {
        @Override
        public void handle(WriteResponse event) {
            logger.info("Write on r[{}] returned", event.getReg());
            doNextCommand();
        }
    };


    private void doNextCommand() {
        lastCommand++;

        if (lastCommand > commands.length) {
            return;
        }
        if (lastCommand == commands.length) {
            logger.info("DONE ALL OPERATIONS");
            Thread applicationThread = new Thread("ApplicationThread") {
                @Override
                public void run() {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(System.in));
                    while (true) {
                        try {
                            String line = in.readLine();
                            doCommand(line);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            applicationThread.start();
            return;
        }
        String op = commands[lastCommand];
        doCommand(op);
    }

    private void doCommand(String cmd) {
        if (cmd.startsWith("S")) {
            doSleep(Integer.parseInt(cmd.substring(1)));
        } else if (cmd.startsWith("X")) {
            doShutdown();
        } else if (cmd.equals("help")) {
            doHelp();
            doNextCommand();
        } else if (cmd.startsWith("W")) {
            doWrite(Integer.parseInt(cmd.substring(1)));
        } else if (cmd.equals("R")) {
            doRead();
        } else {
            logger.info("Bad command: '{}'. Try 'help'", cmd);
            doNextCommand();
        }
    }

    private void doHelp() {
        logger.info("Available commands: S<n>, help, X, W<v>, R");
        logger.info("help: shows this help message");
        logger.info("Sn: sleeps 'n' milliseconds before the next command");
        logger.info("X: terminates this process");
        logger.info("Wv: to write value 'v' ");
        logger.info("R: to read value ");
    }

    private void doSleep(long delay) {
        logger.info("Sleeping {} milliseconds...", delay);

        ScheduleTimeout st = new ScheduleTimeout(delay);
        st.setTimeoutEvent(new ApplicationContinue(st));
        trigger(st, timer);
    }

    private void doShutdown() {
        System.out.println("2DIE");
        System.out.close();
        System.err.close();
        Kompics.shutdown();
    }

   private void doWrite(int value) {
        logger.info("SEND WRITE REQUEST WITH VALUE {} ...", value);
        trigger(new WriteRequest(0, value), nn_Ar);
    }
   
    private void doRead() {
        logger.info("SEND READ REQUEST ...");
        trigger(new ReadRequest(0), nn_Ar);
    }

    
}
