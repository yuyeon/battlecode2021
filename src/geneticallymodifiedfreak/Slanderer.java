package geneticallymodifiedfreak;

import battlecode.common.*;

import java.util.HashSet;

import static geneticallymodifiedfreak.RobotPlayer.randomDirection;

public class Slanderer extends GenericRobot {
    private HashSet<Integer> allies;
    private Team team;
    private MapLocation nearestBoundary;
    private int parentID;
    private int turnCount = 0;

    public Slanderer(RobotController rc) {
        super(rc);
        allies = new HashSet<>();
        team = rc.getTeam();

        RobotInfo[] nearbyRobots = this.rc.senseNearbyRobots();

        for(RobotInfo nearbyRobot : nearbyRobots){
            int id = nearbyRobot.getID();
            if(nearbyRobot.team == team && nearbyRobot.type == RobotType.ENLIGHTENMENT_CENTER){
                parentID = id;
            }
            else if(nearbyRobot.team == team){
                allies.add(id);
            }
        }
    }

    @Override
    void run() throws GameActionException {
        /*Direction moveDir = RobotPlayer.randomDirection();

        if(turnCount == 0){
            if(rc.canGetFlag(parentID)){
                int flag = rc.getFlag(parentID);

                if(flag == 0){
                    moveDir = RobotPlayer.randomDirection();

                    while(!rc.canMove(moveDir)){
                        moveDir.rotateLeft();
                    }
                }
                else {
                    nearestBoundary = getLocation(flag);
                }
            }
        }
        else if (nearestBoundary == null) {
            Direction check;

            for (Direction dir : Direction.allDirections()) {
                MapLocation adjTile = rc.getLocation().add(dir);
                if (!rc.canDetectLocation(adjTile)) {
                    nearestBoundary = adjTile;
                }
            }
        }

        if(nearestBoundary != null && rc.getLocation().distanceSquaredTo(nearestBoundary) == 0){
            return;
        }
        else {
            if(rc.canMove(moveDir)){
                rc.move(moveDir);
            }
        }*/

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        int avgX = 0;
        int avgY = 0;
        int enemies = 0;

        for(RobotInfo nearbyRobot : nearbyRobots){
            if(nearbyRobot.team != team) {
                enemies++;
                MapLocation enemyLoc = nearbyRobot.location;
                avgX += enemyLoc.x;
                avgY += enemyLoc.y;
            }
        }

        avgX = avgX / enemies;
        avgY = avgY / enemies;

        MapLocation curr = rc.getLocation();

        if(avgX > 0 || avgY > 0){
            MapLocation avgLoc = new MapLocation(avgX, avgY);

            MapLocation temp = curr.subtract(curr.directionTo(avgLoc));
            MapLocation toMove = new MapLocation(temp.x + 4, temp.x + 4);

            Direction step = pathfind(toMove);

            if(step != null) rc.move(step);
        }
        else {
            MapLocation edgeCheck;
            for(int i = 0; i < 4; i++){
                switch(i){
                    case 0: edgeCheck = new MapLocation(curr.x + 4, curr.y);
                    case 1: edgeCheck = new MapLocation(curr.x - 4, curr.y);
                    case 2: edgeCheck = new MapLocation(curr.x, curr.y + 4);
                    case 3: edgeCheck = new MapLocation(curr.x, curr.y - 4);
                    default: edgeCheck = curr;
                }

                if(!rc.canDetectLocation(edgeCheck)){
                    nearestBoundary = edgeCheck;

                    Direction step = pathfind(edgeCheck);

                    if(step != null) rc.move(step);
                    break;
                }
            }
        }

        turnCount++;
    }

    /**
     * Encodes location into flag, using the rightmost 14 bits. The remaining leftmost bits are used to encode any
     * other information.
     **/
    public void sendLocation(int extraInfo) throws GameActionException {
        MapLocation loc = rc.getLocation();
        int x = loc.x, y = loc.y;
        int encodedLocation = ((x % 128) * 128) + (y % 28) + (extraInfo * 128 * 128);
        if (rc.canSetFlag(encodedLocation)) {
            rc.setFlag(encodedLocation);
        }
    }

    final int BITS = 7;

    /**
     * Decodes location from given flag.
     **/
    public MapLocation getLocation(int flag) throws GameActionException {
        int y = flag & BITS;
        int x = (flag >> BITS) & 127;

        MapLocation currLoc = rc.getLocation();

        int relX = currLoc.x % 128;
        int relY = currLoc.y % 128;

        int actualX = 0, actualY = 0;

        int xDiff = relX - x;

        if (Math.abs(xDiff) < 64) {
            actualX = ((currLoc.x >> BITS) << BITS) + x;
        } else if (xDiff >= 64) {
            actualX = ((currLoc.x >> BITS) << BITS) + 128 + x;
        } else if (xDiff <= -64) {
            actualX = ((currLoc.x >> BITS) << BITS) - 128 + x;
        }

        int yDiff = relY - y;

        if (Math.abs(yDiff) < 64) {
            actualY = ((currLoc.y >> BITS) << BITS) + y;
        } else if (yDiff >= 64) {
            actualY = ((currLoc.y >> BITS) << BITS) + 128 + y;
        } else if (yDiff <= -64) {
            actualY = ((currLoc.y >> BITS) << BITS) - 128 + y;
        }

        return new MapLocation(actualX, actualY);
    }

    public boolean enemyAt(int flag){
        int first = flag >> 23;
        return (flag == 1);
    }

    public Direction pathfind(MapLocation target) throws GameActionException {
        MapLocation curr = rc.getLocation();
        Direction step = curr.directionTo(target);
        Direction temp = step;
        MapLocation afterStep = curr.add(step);

        int distSquared = target.distanceSquaredTo(afterStep);
        double dist = Math.sqrt(distSquared);
        double estMinMoves = dist * Math.sqrt(2) + (2.0 / rc.sensePassability(afterStep));
        boolean canMove = rc.canMove(step);

        for (int i = 0; i < 2; i++) {
            temp = temp.rotateLeft();
            afterStep = curr.add(temp);
            dist = Math.sqrt(target.distanceSquaredTo(afterStep));
            double estMoves = dist * Math.sqrt(2) + (2.0 / rc.sensePassability(afterStep));
            if ((estMoves < estMinMoves && rc.canMove(temp)) || (!canMove && rc.canMove(temp))) {
                canMove = false;
                estMinMoves = estMoves;
                step = temp;
            }
        }

        temp = curr.directionTo(target);

        for (int j = 0; j < 2; j++) {
            temp = step.rotateRight();
            afterStep = curr.add(temp);
            dist = Math.sqrt(target.distanceSquaredTo(afterStep));
            double estMoves = dist * Math.sqrt(2) + (2.0 / rc.sensePassability(afterStep));
            if ((estMoves < estMinMoves && rc.canMove(temp)) || (!canMove && rc.canMove(temp))) {
                canMove = false;
                estMinMoves = estMoves;
                step = temp;
            }
        }

        if(!canMove) return null;

        return step;
    }
}
