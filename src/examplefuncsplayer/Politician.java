package examplefuncsplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public strictfp class Politician {

    private static final int CONVICTION_THRESHOLD = 10;

    static void run(RobotController rc) throws GameActionException {
        int actionRadius = rc.getType().actionRadiusSquared;
        Team team = rc.getTeam();
        RobotInfo[] friendlies = rc.senseNearbyRobots(actionRadius, team);
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(actionRadius, opponent);
        boolean canEmpower = rc.canEmpower(actionRadius);
        int empowerNum = friendlies.length + enemies.length;
        int conviction = rc.getConviction();
        int convictionReceivedIfEmpowered = conviction / empowerNum;

        // TODO: if no robots are present, maybe move?

        int minFriendlyConviction = Integer.MIN_VALUE, maxFriendlyConviction = Integer.MIN_VALUE;
        for (RobotInfo friendly : friendlies) {
            int fc = friendly.conviction;
            if (fc < CONVICTION_THRESHOLD) {
                minFriendlyConviction = Math.max(fc, minFriendlyConviction);
            }
            maxFriendlyConviction = Math.max(fc, maxFriendlyConviction);
        }

        int minEnemyConviction = Integer.MAX_VALUE, maxEnemyConviction = Integer.MIN_VALUE;
        for (RobotInfo enemy : enemies) {
            int ec = enemy.conviction;
            minEnemyConviction = Math.min(ec, minEnemyConviction);
            maxEnemyConviction = Math.max(ec, maxEnemyConviction);
        }

        boolean shouldEmpower = shouldKms(conviction, maxFriendlyConviction)
                || shouldPowerUpFriendly(conviction, convictionReceivedIfEmpowered, minFriendlyConviction)
                || shouldConvertEnemy(conviction, convictionReceivedIfEmpowered, minEnemyConviction);
        if (canEmpower && shouldEmpower) {
            rc.empower(actionRadius);
        }
    }

    private static boolean shouldKms(int conviction, int maxFriendlyConviction) {
        return conviction < CONVICTION_THRESHOLD
                && maxFriendlyConviction < CONVICTION_THRESHOLD;
    }

    private static boolean shouldPowerUpFriendly(int conviction, int received, int minFriendlyConviction) {
        return conviction >= CONVICTION_THRESHOLD
                && minFriendlyConviction + received >= CONVICTION_THRESHOLD;
    }

    private static boolean shouldConvertEnemy(int conviction, int received, int minEnemyConviction) {
        return conviction >= CONVICTION_THRESHOLD
                && minEnemyConviction - received <= 0;
    }
}