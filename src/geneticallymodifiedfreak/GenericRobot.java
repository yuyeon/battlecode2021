package geneticallymodifiedfreak;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public abstract class GenericRobot {
    protected RobotController rc;
    protected int turnCount = 0;

    public GenericRobot(RobotController rc) {
        this.rc = rc;
    }

    abstract void run() throws GameActionException;
}
