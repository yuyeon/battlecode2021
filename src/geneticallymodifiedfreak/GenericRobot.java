package geneticallymodifiedfreak;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public abstract class GenericRobot {
    protected RobotController rc;

    public GenericRobot(RobotController rc) {
        this.rc = rc;
    }

    abstract void run() throws GameActionException;
}
