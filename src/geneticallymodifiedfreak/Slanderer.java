package geneticallymodifiedfreak;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import static geneticallymodifiedfreak.RobotPlayer.randomDirection;

public class Slanderer extends GenericRobot {
    public Slanderer(RobotController rc) {
        super(rc);
    }

    @Override
    void run() throws GameActionException {
        if (RobotPlayer.tryMove(randomDirection()))
            System.out.println("I moved!");
    }
}
