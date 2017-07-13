
package id2203.detectors.events;

import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;

/**
 *
 * @author M&M
 */
public class HBTimeout extends Timeout {

    public HBTimeout(ScheduleTimeout req) {
        super(req);
    }
}
