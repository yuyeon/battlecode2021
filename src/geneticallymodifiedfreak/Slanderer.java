package geneticallymodifiedfreak;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.HashSet;

import static geneticallymodifiedfreak.RobotPlayer.randomDirection;

public class Slanderer extends GenericRobot {
    private HashSet<Integer> allies;
    int parent;

    public Slanderer(RobotController rc) {
        super(rc);
    }

    @Override
    void run() throws GameActionException {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        for(RobotInfo nearbyRobot : nearbyRobots){

        }
    }

    void findParent(){

    }
}
