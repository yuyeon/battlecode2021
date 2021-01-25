package geneticallymodifiedfreak;

import battlecode.common.*;

import java.util.HashSet;

import static geneticallymodifiedfreak.GameUtils.pathfind;
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

            Direction step = pathfind(rc, toMove);

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

                    Direction step = pathfind(rc, edgeCheck);

                    if(step != null) rc.move(step);
                    break;
                }
            }
        }

        turnCount++;
    }
}
