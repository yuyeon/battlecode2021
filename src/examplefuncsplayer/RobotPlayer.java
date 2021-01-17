package examplefuncsplayer;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

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

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
        RobotType toBuild = randomSpawnableRobotType();
        int influence = 50;
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
            } else {
                break;
            }
        }
    }

    static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runSlanderer() throws GameActionException {
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runMuckraker() throws GameActionException {
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
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * Encodes location into flag, using the rightmost 14 bits. The remaining leftmost bits are used to encode any
     * other information.
    **/
    static void sendLocation(int extraInfo) throws GameActionException {
        MapLocation loc = rc.getLocation();
        int x = loc.x, y = loc.y;
        int encodedLocation = ((x % 128) * 128) + (y % 28) + (extraInfo * 128 * 128);
        if(rc.canSetFlag(encodedLocation)){
            rc.setFlag(encodedLocation);
        }
    }

    static final int BITS = 7;

    /**
     * Decodes location from given flag.
    **/
    static MapLocation getLocation(int flag) throws GameActionException{
        int y = flag & BITS;
        int x = (flag >> BITS) & 127;

        MapLocation currLoc = rc.getLocation();

        int relX = currLoc.x % 128;
        int relY = currLoc.y % 128;

        int actualX = 0, actualY = 0;

        int xDiff = relX - x;

        if(Math.abs(xDiff) < 64){
            actualX = ((currLoc.x >> BITS) << BITS) + x;
        }
        else if(xDiff >= 64){
            actualX = ((currLoc.x >> BITS) << BITS) + 128 + x;
        }
        else if(xDiff <= -64){
            actualX = ((currLoc.x >> BITS) << BITS) - 128 + x;
        }

        int yDiff = relY - y;

        if(Math.abs(yDiff) < 64){
            actualY = ((currLoc.y >> BITS) << BITS) + y;
        }
        else if(yDiff >= 64){
            actualY = ((currLoc.y >> BITS) << BITS) + 128 + y;
        }
        else if(yDiff <= -64){
            actualY = ((currLoc.y >> BITS) << BITS) - 128 + y;
        }

        return new MapLocation(actualX, actualY);
    }

    static final double PASS_THRESHOLD = 0.5;
    static double shortestDist = Integer.MAX_VALUE;

    static Direction pathfind(MapLocation target) throws GameActionException{
        MapLocation curr = rc.getLocation();
        int distSquared = target.distanceSquaredTo(curr);
        Direction step = curr.directionTo(target);
        double dist = Math.sqrt(distSquared);
        MapLocation afterStep = curr.add(step);
        double estMinMoves = dist * Math.sqrt(2) + (2.0 / rc.sensePassability(afterStep));

        for(int i = 0; i < 2; i++){
            Direction temp = step.rotateLeft();
            afterStep = curr.add(temp);
            dist = Math.sqrt(target.distanceSquaredTo(afterStep));
            double estMoves = dist * Math.sqrt(2) + (2.0 / rc.sensePassability(afterStep));
            if(estMoves < estMinMoves){
                estMinMoves = estMoves;
            }
        }

        for(int j = 0; j < 2; j++){
            Direction temp = step.rotateRight();
            afterStep = curr.add(temp);
            dist = Math.sqrt(target.distanceSquaredTo(afterStep));
            double estMoves = dist * Math.sqrt(2) + (2.0 / rc.sensePassability(afterStep));
            if(estMoves < estMinMoves){
                estMinMoves = estMoves;
            }
        }

        return step;
    }
}
