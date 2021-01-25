package geneticallymodifiedfreak;

import battlecode.common.*;

import java.util.Random;

import static geneticallymodifiedfreak.GameUtils.directions;
import static geneticallymodifiedfreak.GameUtils.spawnableRobot;

public strictfp class RobotPlayer {
    static RobotController rc;
    static int turnCount;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        GenericRobot robot = null;
        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER:
                robot = new EnlightenmentCenter(rc);
                break;
            case POLITICIAN:
                robot = new Politician(rc);
                break;
            case SLANDERER:
                robot = new Slanderer(rc);
                break;
            case MUCKRAKER:
                robot = new Muckraker(rc);
                break;
        }

        if(robot == null) return;

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.

                robot.run();

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    private static final Random random = new Random(1);
    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (random.nextDouble() * directions.length)];
    }

    /**
     * Encodes location into flag, using the rightmost 14 bits. The remaining leftmost bits are used to encode any
     * other information.
     **/
    static void sendLocation(int extraInfo) throws GameActionException {
        MapLocation loc = rc.getLocation();
        int x = loc.x, y = loc.y;
        int encodedLocation = ((x % 128) * 128) + (y % 28) + (extraInfo * 128 * 128);
        if (rc.canSetFlag(encodedLocation)) {
            rc.setFlag(encodedLocation);
        }
    }

    static final int BITS = 7;

    /**
     * Decodes location from given flag.
     **/
    static MapLocation getLocation(int flag) throws GameActionException {
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

    static final double PASS_THRESHOLD = 0.5;
    static double shortestDist = Integer.MAX_VALUE;

    static Direction pathfind(MapLocation target) throws GameActionException {
        MapLocation curr = rc.getLocation();
        Direction step = curr.directionTo(target);
        MapLocation afterStep = curr.add(step);

        int distSquared = target.distanceSquaredTo(afterStep);
        double dist = Math.sqrt(distSquared);
        double estMinMoves = dist * Math.sqrt(2) + (2.0 / rc.sensePassability(afterStep));
        boolean canMove = rc.canMove(step);

        for (int i = 0; i < 2; i++) {
            Direction temp = step.rotateLeft();
            afterStep = curr.add(temp);
            dist = Math.sqrt(target.distanceSquaredTo(afterStep));
            double estMoves = dist * Math.sqrt(2) + (2.0 / rc.sensePassability(afterStep));
            if ((estMoves < estMinMoves && rc.canMove(step)) || canMove) {
                canMove = false;
                estMinMoves = estMoves;
                step = temp;
            }
        }

        for (int j = 0; j < 2; j++) {
            Direction temp = step.rotateRight();
            afterStep = curr.add(temp);
            dist = Math.sqrt(target.distanceSquaredTo(afterStep));
            double estMoves = dist * Math.sqrt(2) + (2.0 / rc.sensePassability(afterStep));
            if (estMoves < estMinMoves && rc.canMove(step)) {
                estMinMoves = estMoves;
                step = temp;
            }
        }

        return step;
    }
}
