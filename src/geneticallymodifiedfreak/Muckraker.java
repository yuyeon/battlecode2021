package examplefuncsplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class Muckraker extends GenericRobot {
    public Muckraker(RobotController rc) {
        super(rc);
    }

    @Override
    void run() throws GameActionException {
        Team opponent = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] enemies = rc.senseNearbyRobots(actionRadius, opponent);
        RobotInfo bestEnemySlanderer = null;
        for (RobotInfo enemy : enemies) {
            int ei = enemy.getInfluence();
            if (enemy.getType().canBeExposed()
                    && (bestEnemySlanderer == null || ei > bestEnemySlanderer.getInfluence())) {
                bestEnemySlanderer = enemy;
            }
        }
        if (bestEnemySlanderer != null && rc.canExpose(bestEnemySlanderer.getLocation())) {
            rc.expose(bestEnemySlanderer.getLocation());
        }
    }
}
