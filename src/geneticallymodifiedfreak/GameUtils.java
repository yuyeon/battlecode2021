package geneticallymodifiedfreak;

import battlecode.common.*;

import java.util.Random;

public class GameUtils {
//    public static final RobotType[] spawnableRobot = {
//            RobotType.POLITICIAN,
//            RobotType.SLANDERER,
//            RobotType.MUCKRAKER,
//    };

    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public static final int BITS = 7;

    private static final Random random = new Random(1);

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    /**
     * Encodes location into flag, using the rightmost 14 bits. The remaining leftmost bits are used to encode any
     * other information.
     **/
    public static void sendLocation(RobotController rc, int extraInfo) throws GameActionException {
        MapLocation loc = rc.getLocation();
        int x = loc.x, y = loc.y;
        int encodedLocation = ((x % 128) * 128) + (y % 28) + (extraInfo * 128 * 128);
        if (rc.canSetFlag(encodedLocation)) {
            rc.setFlag(encodedLocation);
        }
    }

    /**
     * Decodes location from given flag.
     **/
    public static MapLocation getLocation(RobotController rc, int flag) throws GameActionException {
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

    static Direction pathfind(RobotController rc, MapLocation target) throws GameActionException {
        if(rc.getLocation().compareTo(target) == 0) return null;

        MapLocation curr = rc.getLocation();
        Direction step = curr.directionTo(target);
        Direction temp = step;
        MapLocation afterStep = curr.add(step);

        int distSquared = target.distanceSquaredTo(afterStep);
        double dist = Math.sqrt(distSquared);
        double estMinMoves = Integer.MAX_VALUE;
        if(rc.canSenseLocation(afterStep)) {
            estMinMoves = dist * Math.sqrt(2) + (2.0 / rc.sensePassability(afterStep));
        }
        boolean canMove = rc.canMove(step);

        for (int i = 0; i < 2; i++) {
            temp = temp.rotateLeft();
            afterStep = curr.add(temp);
            dist = Math.sqrt(target.distanceSquaredTo(afterStep));
            double estMoves = Integer.MAX_VALUE;
            if(rc.canSenseLocation(afterStep)) {
                estMoves = dist * Math.sqrt(2) + (2.0 / rc.sensePassability(afterStep));
            }
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
            double estMoves = Integer.MAX_VALUE;
            if(rc.canSenseLocation(afterStep)) {
                estMoves = dist * Math.sqrt(2) + (2.0 / rc.sensePassability(afterStep));
            }
            if ((estMoves < estMinMoves && rc.canMove(temp)) || (!canMove && rc.canMove(temp))) {
                canMove = false;
                estMinMoves = estMoves;
                step = temp;
            }
        }

        if(!canMove) return null;

        return step;
    }

    public static boolean enemyAt(int flag){
        int first = flag >> 23;
        return (flag == 1);
    }


    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    public static Direction randomDirection() {
        return directions[(int) (random.nextDouble() * directions.length)];
    }
}
