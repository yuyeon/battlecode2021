package geneticallymodifiedfreak;

import battlecode.common.*;

import static geneticallymodifiedfreak.GameUtils.*;

public class Muckraker extends GenericRobot {
    private final int actionRadius, sensorRadius;
    private Team team;
    private MapLocation parentLoc;
    private int parentID;

    public Muckraker(RobotController rc) {
        super(rc);
        this.actionRadius = rc.getType().actionRadiusSquared;
        this.sensorRadius = rc.getType().sensorRadiusSquared;

        team = this.rc.getTeam();

        RobotInfo[] nearbyRobots = this.rc.senseNearbyRobots();

        for(RobotInfo nearbyRobot : nearbyRobots) {
            int id = nearbyRobot.getID();
            if (nearbyRobot.team.equals(team) && nearbyRobot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                parentID = id;
                parentLoc = nearbyRobot.location;
            }
        }
    }

    @Override
    void run() throws GameActionException {
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(sensorRadius, opponent);
        Team team = rc.getTeam();
        RobotInfo[] friendlies = rc.senseNearbyRobots(sensorRadius, team);

        if (tryAttack(enemies) || polMicroSpace(friendlies)) {
            return;
        }

        MapLocation curr = rc.getLocation();
        MapLocation temp = curr.subtract(curr.directionTo(parentLoc));

        Direction step = curr.directionTo(temp);

        int ind = 0;

        while(!rc.canMove(step) && ind++ < 8){
            step = step.rotateRight();
        }

        if(rc.canMove(step)) rc.move(step);
    }


    private boolean tryAttack(RobotInfo[] enemies) throws GameActionException {
        MapLocation thisLoc = rc.getLocation();
        RobotInfo bestEnemySlanderer = null;
        int incXTotal = 0, incYTotal = 0, incCount = 0, decXTotal = 0, decYTotal = 0, decCount = 0;
        boolean hardEscape = false;
        for (RobotInfo enemy : enemies) {
            int eInf = enemy.getInfluence(), eConv = enemy.getConviction();
            RobotType eType = enemy.getType();
            MapLocation eLoc = enemy.getLocation();
            if (eType.canBeExposed()
                    && thisLoc.isWithinDistanceSquared(eLoc, actionRadius)
                    && (bestEnemySlanderer == null || eInf > bestEnemySlanderer.getInfluence())) {
                bestEnemySlanderer = enemy;
            } else if (eType == RobotType.SLANDERER) {
                decXTotal += eLoc.x;
                decYTotal += eLoc.y;
                decCount++;
            } else if (eType == RobotType.POLITICIAN
                    && thisLoc.isWithinDistanceSquared(eLoc, eType.sensorRadiusSquared)
                    && eConv >= rc.getConviction()) {
                if (thisLoc.isWithinDistanceSquared(eLoc, eType.actionRadiusSquared)) {
                    hardEscape = true;
                }
                incXTotal += eLoc.x;
                incYTotal += eLoc.y;
                incCount++;
            }
        }
        if (bestEnemySlanderer != null && rc.canExpose(bestEnemySlanderer.getLocation())) {
            rc.expose(bestEnemySlanderer.getLocation());
            return true;
        } else if (hardEscape) {
            return optimizeDistanceFrom(incXTotal, incYTotal, incCount, 0, 0, 0);
        } else if (incCount != 0 || decCount != 0) {
            return optimizeDistanceFrom(incXTotal, incYTotal, incCount, decXTotal, decYTotal, decCount);
        }
        return false;
    }

    private boolean polMicroSpace(RobotInfo[] friendlies) throws GameActionException {
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

        return optimizeDistanceFrom(incXTotal, incYTotal, incCount, decXTotal, decYTotal, decCount);
    }

    private boolean optimizeDistanceFrom(
            int incXTotal, int incYTotal, int incCount, int decXTotal, int decYTotal, int decCount
    ) throws GameActionException {
        MapLocation avgIncLoc = incCount != 0 ? new MapLocation(incXTotal / incCount, incYTotal / incCount) : null;
        MapLocation avgDecLoc = decCount != 0 ? new MapLocation(decXTotal / decCount, decYTotal / decCount) : null;
        return optimizeDistanceFrom(avgIncLoc, avgDecLoc);
    }

    private boolean optimizeDistanceFrom(MapLocation avgIncLoc, MapLocation avgDecLoc) throws GameActionException {
        MapLocation thisLoc = rc.getLocation();
        MapLocation dirLoc = thisLoc;
        if (avgIncLoc != null) {
            dirLoc = dirLoc.add(avgIncLoc.directionTo(thisLoc));
        }
        if (avgDecLoc != null) {
            dirLoc = dirLoc.add(thisLoc.directionTo(avgDecLoc));
        }
        if (thisLoc.equals(dirLoc)) {
            return false;
        }

        Direction bestDir = thisLoc.directionTo(dirLoc);
        Direction left = bestDir.rotateLeft();
        Direction right = bestDir.rotateRight();
        return tryMove(rc, bestDir)
                || tryMove(rc, left)
                || tryMove(rc, right)
                || tryMove(rc, left.rotateLeft())
                || tryMove(rc, right.rotateRight());
    }
}
