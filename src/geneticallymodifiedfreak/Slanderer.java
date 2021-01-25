package geneticallymodifiedfreak;

import battlecode.common.*;

import java.util.HashSet;

import static geneticallymodifiedfreak.RobotPlayer.randomDirection;

public class Slanderer extends GenericRobot {
    private HashSet<Integer> allies;
    private Team team;
    int parentID;
    int turnCount = 0;

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
        Direction moveDir;
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
                    MapLocation boundary
                }
            }
        }

        turnCount++;

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        for(RobotInfo nearbyRobot : nearbyRobots){

        }
    }

    void findParent(){

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
