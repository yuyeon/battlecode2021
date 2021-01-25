package geneticallymodifiedfreak;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

import static geneticallymodifiedfreak.RobotPlayer.randomDirection;

public class Muckraker extends GenericRobot {
    public Muckraker(RobotController rc) {
        super(rc);
    }

    @Override
    void run() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        if (RobotPlayer.tryMove(randomDirection()))
            System.out.println("I moved!");
    }
}
