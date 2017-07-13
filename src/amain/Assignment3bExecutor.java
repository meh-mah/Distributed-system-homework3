
package amain;

import se.sics.kompics.launch.Scenario;
import se.sics.kompics.launch.Topology;

/**
 *
 * @author M&M
 */
public class Assignment3bExecutor {
    private static final int NODES = 6;

    public static void main(String[] args) {
        Topology topology0 = new Topology() {
            {
                node(1, "127.0.0.1", 10001);
                node(2, "127.0.0.1", 10002);
                node(3, "127.0.0.1", 10003);
                node(4, "127.0.0.1", 10004);

                defaultLinks(100, 0.0);
            }
        };
        Topology topologyEx1And2 = new Topology() {
            {
                for (int i = 1; i <= 3; i++) {
                    node(i, "127.0.0.1", 23230 + i);
                }
                link(1, 2, 200, 0.0).bidirectional();
                link(1, 3, 200, 0.0).bidirectional();
                link(2, 3, 200, 0.0).bidirectional();
                defaultLinks(1000, 0.0);
            }
        };
        Topology topology3 = new Topology() {
            {
                node(1, "127.0.0.1", 22031);
                node(2, "127.0.0.1", 22032);
                node(3, "127.0.0.1", 22033);

//                link(1, 2, 1000, 0).bidirectional();
//                link(1, 3, 2000, 0).bidirectional();
//                link(2, 3, 1750, 0).bidirectional();
                //defaultLinks(1000, 0.0);
                link(1, 2, 0, 0).bidirectional();
                link(1, 3, 000, 0).bidirectional();
                link(2, 3, 500, 0).bidirectional();
            }
        };


        Scenario scenario0 = new Scenario(Assignment3bMain.class) {
            {
                command(1, "S500");
                command(2, "S500");
                command(3, "S500");
                command(4, "S500");
            }
        };
        Scenario scenarioEx1 = new Scenario(Assignment3bMain.class) {
            {
                command(1, "S30000");
                command(2, "S500:W4:S25000");
                command(3, "S10000:R");
            }
        };
        Scenario scenarioEx2 = new Scenario(Assignment3bMain.class) {
            {
                command(1, "S500:W5:R:S5000:R:S30000");
                command(2, "S500:W6:R:S5000:R:S30000");
                command(3, "S500:R:S500:R:S10000", 30000);
            }
        };
        Scenario scenario3 = new Scenario(Assignment3bMain.class) {
            {
                command(1, "S500:W1:R:S500:R:S8000", 0);
                command(2, "S500:W2:R:S500:R:S8000", 1000);
                command(3, "S500:W3:R:S500:R:S8000", 2000);
            }
        };

//        scenarioEx2.executeOn(topologyEx1And2);
//        scenarioEx1.executeOn(topologyEx1And2);
        scenario3.executeOn(topology3);

        System.exit(0);
    }
}

