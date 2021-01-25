package geneticallymodifiedfreak;

import battlecode.common.*;

import static geneticallymodifiedfreak.GameUtils.*;

public class Muckraker extends GenericRobot {
    public Muckraker(RobotController rc) {
        super(rc);
    }

    @Override
    void run() throws GameActionException {
        RobotType thisType = rc.getType();
        MapLocation thisLoc = rc.getLocation();
        int actionRadius = thisType.actionRadiusSquared;
        int sensorRadius = thisType.sensorRadiusSquared;

        // slanderers, slanderers everywhere
        Team opponent = rc.getTeam().opponent();
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
            return;
        }

        // politician micro space / combat
        Team team = rc.getTeam();
        RobotInfo[] friendlies = rc.senseNearbyRobots(sensorRadius, team);
        polMicroSpace(friendlies);
    }

    private void polMicroSpace(RobotInfo[] friendlies) throws GameActionException {
        MapLocation thisLoc = rc.getLocation();
        final int polActionRadius = RobotType.POLITICIAN.actionRadiusSquared,
                polSensorRadius = RobotType.POLITICIAN.sensorRadiusSquared;
        int incXTotal = 0, incYTotal = 0, incCount = 0, decXTotal = 0, decYTotal = 0, decCount = 0;
        for (RobotInfo friendly : friendlies) {
            MapLocation fLoc = friendly.getLocation();
            if (friendly.getType() == RobotType.POLITICIAN) {
                int polDist = thisLoc.distanceSquaredTo(fLoc);
                if (polDist <= polActionRadius) {
                    incXTotal += fLoc.x;
                    incYTotal += fLoc.y;
                    incCount++;
                } else if (polDist > polSensorRadius) {
                    decXTotal += fLoc.x;
                    decYTotal += fLoc.y;
                    decCount++;
                }
            }
        }

        MapLocation dirLoc = thisLoc;
        if (incCount != 0) {
            MapLocation avgIncLoc = new MapLocation(incXTotal / incCount, incYTotal / incCount);
            dirLoc = dirLoc.add(avgIncLoc.directionTo(thisLoc));
        }
        if (decCount != 0) {
            MapLocation avgDecLoc = new MapLocation(decXTotal / decCount, decYTotal / decCount);
            dirLoc = dirLoc.add(thisLoc.directionTo(avgDecLoc));
        }
        if (thisLoc.equals(dirLoc)) {
            return;
        }

        Direction bestDir = thisLoc.directionTo(dirLoc);
        if (tryMove(rc, bestDir)) {
            return;
        }
        Direction pfDir = pathfind(rc, dirLoc);
        if (pfDir != null && tryMove(rc, pfDir)) {
            return;
        }
        for (Direction dir : directions) {
            if (dir != bestDir && dir != pfDir && tryMove(rc, dir)) {
                return;
            }
        }
    }
}
