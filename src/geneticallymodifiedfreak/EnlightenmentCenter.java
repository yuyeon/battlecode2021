package geneticallymodifiedfreak;

import battlecode.common.*;

import java.util.HashSet;
import java.util.LinkedList;

import static geneticallymodifiedfreak.GameUtils.*;

public strictfp class EnlightenmentCenter extends GenericRobot {
    private static final Direction[] cardinals = Direction.cardinalDirections();
    public static final int POL_SCOUTING_INF = 1, POL_NORMAL_INF = 1000;

    private HashSet<Integer> scouts;
    private HashSet<Integer> enemies;
    private Team team;
    private int mode;
    private Direction enemyDir;


    public EnlightenmentCenter(RobotController rc) {
        super(rc);
        scouts = new HashSet<>();
        team = rc.getTeam();
        mode = 0;
    }

    @Override
    public void run() throws GameActionException {
        int influence = rc.getInfluence();
        int round = rc.getRoundNum();

        if(round <= 80){ // very early game
            if(round == 1){
                rc.setFlag(0); //Set flag to 0 for scouting mode
                if(rc.canBuildRobot(RobotType.SLANDERER, Direction.NORTH, influence)){
                    rc.buildRobot(RobotType.SLANDERER, Direction.NORTH, influence); //build slanderer 1st turn
                }
            }
            else if(round <= 5){
                if(rc.canBuildRobot(RobotType.POLITICIAN, cardinals[round - 2], 1)){
                    rc.buildRobot(RobotType.POLITICIAN, cardinals[round - 2], 1); //build politician in each direction for scouting
                    RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

                    for(RobotInfo nearbyRobot : nearbyRobots){
                        if(nearbyRobot.influence == 1 && !scouts.contains(nearbyRobot.ID)){
                            scouts.add(nearbyRobot.ID);
                        }
                    }
                }
            }
            else if(influence >= 85){
                if(rc.canBuildRobot(RobotType.SLANDERER, Direction.NORTHEAST, 85)){
                    rc.buildRobot(RobotType.SLANDERER, Direction.NORTHEAST, 85); //build slanderer whenever possible
                }
            }
        }
        else if(round <= 500){
            if(round % 10 == 0 || round % 10 == 1){
                Direction spawnDir = randomDirection();
                if(enemyDir != null) spawnDir = enemyDir;
                if(rc.canBuildRobot(RobotType.POLITICIAN, spawnDir, POL_NORMAL_INF)){
                    rc.buildRobot(RobotType.POLITICIAN, spawnDir, POL_NORMAL_INF); //build politician pair every 10 rounds
                }
            }
            else if(influence >= 949){
                if(rc.canBuildRobot(RobotType.SLANDERER, Direction.NORTHEAST, 949)){
                    rc.buildRobot(RobotType.SLANDERER, Direction.NORTHEAST, 949); //build slanderer whenever possible
                }
            }
        }
        else {
            if(round % 4 == 0){
                Direction spawnDir = randomDirection();
                if(enemyDir != null) spawnDir = enemyDir;
                if(rc.canBuildRobot(RobotType.POLITICIAN, spawnDir, POL_NORMAL_INF)){
                    rc.buildRobot(RobotType.POLITICIAN, spawnDir, POL_NORMAL_INF); //build politician pair every 10 rounds
                }
            }
            else if(round % 4 == 1){
                Direction spawnDir = randomDirection();
                if(enemyDir != null) spawnDir = enemyDir;
                if(rc.canBuildRobot(RobotType.MUCKRAKER, spawnDir, POL_NORMAL_INF)){
                    rc.buildRobot(RobotType.MUCKRAKER, spawnDir, POL_NORMAL_INF); //build politician pair every 10 rounds
                }
            }
            else if(round % 8 == 2){
                Direction spawnDir = randomDirection();
                if(enemyDir != null) spawnDir = enemyDir;
                if(rc.canBuildRobot(RobotType.MUCKRAKER, spawnDir, POL_SCOUTING_INF)){
                    rc.buildRobot(RobotType.MUCKRAKER, spawnDir, POL_SCOUTING_INF); //build politician pair every 10 rounds
                }
            }
            else if(influence >= 949){
                if(rc.canBuildRobot(RobotType.SLANDERER, Direction.NORTHEAST, 949)){
                    rc.buildRobot(RobotType.SLANDERER, Direction.NORTHEAST, 949); //build slanderer whenever possible
                }
            }
        }

        for(Integer scoutID : scouts){
            if(rc.canGetFlag(scoutID)){
                int flag = rc.getFlag(scoutID);

                if(enemyAt(flag)){
                    MapLocation target = getLocation(rc, flag);
                    int first = 1 << 23;
                    enemyDir = rc.getLocation().directionTo(target);
                    sendLocation(rc, first);
                }
                else if(enlightenmentCenterAt(flag)){
                    MapLocation target = getLocation(rc, flag);
                    int sec = 1 << 22;
                    enemyDir = rc.getLocation().directionTo(target);
                    sendLocation(rc, sec);
                }
            }
        }

        /*for(RobotInfo robot : nearbyRobots){
            if(robot.team.equals(team) && !allies.contains(robot.ID)){
                allies.add(robot.ID);
            }
            else if(robot.team.equals(team.opponent()) && !enemies.contains(robot.ID) ){
                enemies.add(robot.ID);
            }
        }

        int bytecodes = Clock.getBytecodesLeft();

        LinkedList<Integer> deadAllies = new LinkedList<Integer>();

        for(Integer robotID : allies){
            if(rc.canGetFlag(robotID)){
                int flag = rc.getFlag(robotID);
                if(enemyAt(flag)){
                    MapLocation enemyLoc = getLocation(rc, flag);
                    enemyDir = rc.getLocation().directionTo(enemyLoc);
                }
            }
            else {
                deadAllies.add(robotID);
            }
        }

        for(Integer deadAlly : deadAllies){
            allies.remove(deadAlly);
        }*/
    }
}
