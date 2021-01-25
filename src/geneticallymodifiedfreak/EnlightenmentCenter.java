package geneticallymodifiedfreak;

import battlecode.common.*;

import java.util.HashSet;

import static geneticallymodifiedfreak.GameUtils.spawnableRobot;

public strictfp class EnlightenmentCenter extends GenericRobot {
    private static final Direction[] cardinals = Direction.cardinalDirections();

    private HashSet<Integer> allies;
    private HashSet<Integer> enemies;
    private Team team;
    private int mode;


    public EnlightenmentCenter(RobotController rc) {
        super(rc);
        allies = new HashSet<>();
        team = rc.getTeam();
        mode = 0;
    }

    @Override
    public void run() throws GameActionException {
        int influence = rc.getInfluence();
        int round = rc.getRoundNum();

        if(round <= 100){ // very early game
            if(round == 1){
                rc.setFlag(0); //Set flag to 0 for scouting mode
                if(rc.canBuildRobot(spawnableRobot[1], Direction.NORTH, influence)){
                    rc.buildRobot(spawnableRobot[1], Direction.NORTH, influence); //build slanderer 1st turn
                }
            }
            else if(round <= 5){
                if(rc.canBuildRobot(spawnableRobot[0], cardinals[round - 2], 1)){
                    rc.buildRobot(spawnableRobot[0], cardinals[round - 2], 1); //build politician in each direction for scouting
                }
            }
            else if(influence >= 85){
                if(rc.canBuildRobot(spawnableRobot[1], Direction.NORTHEAST, 85)){
                    rc.buildRobot(spawnableRobot[1], Direction.NORTHEAST, 85); //build slanderer whenever possible
                }
            }
        }
        else if(round <= 500){
            if(round % 10 == 0 || round % 10 == 1){
                if(rc.canBuildRobot(spawnableRobot[0], cardinals[round - 2], 100)){
                    rc.buildRobot(spawnableRobot[0], cardinals[round - 2], 100); //build politician pair every 10 rounds
                }
            }
            else if(influence <= 949){
                if(rc.canBuildRobot(spawnableRobot[1], Direction.NORTHEAST, 949)){
                    rc.buildRobot(spawnableRobot[1], Direction.NORTHEAST, 949); //build slanderer whenever possible
                }
            }
        }

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        for(RobotInfo robot : nearbyRobots){
            if(robot.team == team && !allies.contains(robot.ID)){
                allies.add(robot.ID);
            }
            else if(robot.team == team.opponent() && !enemies.contains(robot.ID) ){
                enemies.add(robot.ID);
            }
        }

        int bytecodes = Clock.getBytecodesLeft();

        for(Integer robotID : allies){
            if(rc.canGetFlag(robotID)){
                int flag = rc.getFlag(robotID);

                if(enemyAt(flag)){
                    MapLocation enemyLoc = getLocation(flag);
                }
            }
            else {
                allies.remove(robotID);
            }
        }
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

    static final int BITS = 7;

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
}
