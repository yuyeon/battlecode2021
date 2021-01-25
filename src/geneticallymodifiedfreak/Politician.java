package geneticallymodifiedfreak;

import battlecode.common.*;

import static geneticallymodifiedfreak.GameUtils.*;

public strictfp class Politician extends GenericRobot {

    private static final int CONVICTION_THRESHOLD = 10, EMPOWER_COST = 10, POWERUP_DELAY = 0;
    private Team team;
    private MapLocation parentLoc;
    private int parentID;

    public Politician(RobotController rc) {
        super(rc);
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
    public void run() throws GameActionException {
        if (isScouting()) {
            scoutingRun();
        } else {
            normalRun();
        }
    }

    private void scoutingRun() throws GameActionException {
        MapLocation curr = rc.getLocation();
        MapLocation temp = curr.subtract(curr.directionTo(parentLoc));

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        RobotInfo targetEnemy = null;

        for(RobotInfo nearbyRobot : nearbyRobots) {
            if(!nearbyRobot.team.equals(team) && !nearbyRobot.type.equals(RobotType.ENLIGHTENMENT_CENTER)){
                int first = 1 << 23;
                sendLocation(rc, first);
                targetEnemy = nearbyRobot;
                break;
            }
            else if (!nearbyRobot.team.equals(team) && nearbyRobot.type.equals(RobotType.ENLIGHTENMENT_CENTER)){
                int sec = 1 << 22;
                sendLocation(rc, sec);
                targetEnemy = nearbyRobot;
                break;
            }
        }

        if(targetEnemy != null){
            normalRun();
        }
        else{
            Direction step = curr.directionTo(temp);

            int ind = 0;

            while(!rc.canMove(step) && ind++ < 8){
                step = step.rotateRight();
            }

            if(rc.canMove(step)) rc.move(step);
        }
    }

    private void normalRun() throws GameActionException {
        int actionRadius = rc.getType().actionRadiusSquared;
        Team team = rc.getTeam();
        RobotInfo[] friendlies = rc.senseNearbyRobots(actionRadius, team);
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(actionRadius, opponent);
        boolean canEmpower = rc.canEmpower(actionRadius);
        int empowerNum = friendlies.length + enemies.length;
        int conviction = rc.getConviction();
        int convictionReceivedIfEmpowered;
        if(empowerNum > 0) convictionReceivedIfEmpowered = (conviction - EMPOWER_COST) / empowerNum;
        else convictionReceivedIfEmpowered = 0;

        // TODO: if no robots are present, maybe move?
        if(enemies.length == 0 && rc.canGetFlag(parentID)){
            int flag = rc.getFlag(parentID);
            MapLocation target = getLocation(rc, flag);
            Direction step = pathfind(rc, target);

            if(rc.canMove(step)) rc.move(step);
        }
        // TODO: strategy, if enemy in sensing distance, don't move

        RobotInfo bestBadPolitician = null, worstFriendly = null, bestFriendly = null;
        for (RobotInfo friendly : friendlies) {
            int fc = friendly.getConviction();
            if (fc < CONVICTION_THRESHOLD && friendly.getType() == RobotType.POLITICIAN
                    && (bestBadPolitician == null || fc > bestBadPolitician.getConviction())) {
                bestBadPolitician = friendly;
            }
            if (worstFriendly == null || fc < worstFriendly.getConviction()) {
                worstFriendly = friendly;
            }
            if (bestFriendly == null || fc > bestFriendly.getConviction()) {
                bestFriendly = friendly;
            }
        }

        RobotInfo worstEnemy = null, bestEnemy = null;
        for (RobotInfo enemy : enemies) {
            int ec = enemy.getConviction();
            if (worstEnemy == null || ec < worstEnemy.getConviction()) {
                worstEnemy = enemy;
            }
            if (bestEnemy == null || ec > bestEnemy.getConviction()) {
                bestEnemy = enemy;
            }
        }

        boolean shouldEmpower = shouldKms(conviction, bestFriendly)
                || shouldPowerUpFriendlyPolitician(conviction, convictionReceivedIfEmpowered, bestBadPolitician)
                || shouldAttack(conviction, convictionReceivedIfEmpowered, worstEnemy, friendlies.length);
        if (canEmpower && shouldEmpower) {
            rc.empower(actionRadius);
        }

        // TODO: implement flight (run away) logic
    }

    private boolean isScouting() throws GameActionException {
        // TODO: return scouting
        if(rc.canGetFlag(parentID)){
            int flag = rc.getFlag(parentID);
            if(flag == 0){
                return true;
            }
            else return false;
        }
        return true;
    }

    private static boolean shouldKms(int conviction, RobotInfo bestFriendly) {
        if (bestFriendly == null) {
            return false;
        }
        return conviction < CONVICTION_THRESHOLD
                && bestFriendly.getConviction() < CONVICTION_THRESHOLD;
    }

    private boolean shouldPowerUpFriendlyPolitician(int conviction, int received, RobotInfo worstFriendly) {
        if (worstFriendly == null) {
            return false;
        }
        return conviction >= CONVICTION_THRESHOLD
                && worstFriendly.getType() == RobotType.POLITICIAN
                && worstFriendly.getConviction() + received >= CONVICTION_THRESHOLD
                && (worstFriendly.getConviction() < CONVICTION_THRESHOLD || turnCount >= POWERUP_DELAY);
    }

    private static boolean shouldAttack(int conviction, int received, RobotInfo worstEnemy, int numFriendlies) {
        if (worstEnemy == null) {
            return false;
        }
        return conviction >= CONVICTION_THRESHOLD
                && (worstEnemy.getConviction() - received <= 0 || numFriendlies > 0);
    }
}