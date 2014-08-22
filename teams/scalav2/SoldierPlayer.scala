package dumb

import scala.util.Random
import battlecode.common._

// Soldier logic
// 1. Read broadcasts
// 2. Sense surroundings
// 3. Determine action
// 4. Execute
// 5. Save state

class SoldierPlayer(val rc_c: RobotController, val state_c: Int) {
    val rc: RobotController = rc_c
    var count: Int = 0
    var current_action = SoldierAction.WAIT

    def run() {
        while (true) {
            if (rc.isActive()) {
                val current_loc = rc.getLocation()
                val (broadcast_loc, broadcast_action) = readBroadcasts()

                if(defuse_mines(current_loc)) {}
                else if(move(current_loc)) {}
            }
            rc.`yield`()
        }
    }

    def readBroadcasts(): (MapLocation, Int) = {
        return (null, 0)
    }

    def senseSurrounding() {

    }

    def saveState() {

    }

    def move(current_loc: MapLocation): Boolean = {
        var move_dir = current_loc.directionTo(rc.senseEnemyHQLocation())
        var new_loc = current_loc.add(move_dir)
        val new_mine = rc.senseMine(new_loc)

        if (current_action == SoldierAction.WAIT) {
            if (rc.canMove(move_dir) && current_loc.distanceSquaredTo(rc.senseHQLocation()) < 8) {
                rc.move(move_dir)
                return true
            }
            val friends = rc.senseNearbyGameObjects(classOf[Robot], 4, rc.getTeam())
            if (friends.length > 2) {
                current_action = SoldierAction.SWARM
                return true
            }
        } else if (current_action == SoldierAction.SWARM) {
            if (rc.canMove(move_dir)) {
                rc.move(move_dir)
                return true
            } else {
                rc.setIndicatorString(0, "Nothing to do")
                return false
            }
        }
        return false
    }

    def defuse_mines(current_loc: MapLocation) : Boolean = {
        if (rc.getTeamPower() < 100)
            return false
        var adj_mines = rc.senseNonAlliedMineLocations(current_loc, 2)
        if (adj_mines.length > 0) {
            rc.setIndicatorString(1, "Defusing mine")
            rc.defuseMine(Random.shuffle(adj_mines.toList).head)
            return true
        }
        return false
    }
}

object SoldierAction extends Enumeration {
    type SoldierAction = Value
    val WAIT = Value("WAIT")
    val DEFEND_HQ = Value("DEFEND_HQ")
    val SWARM = Value("SWARM")
}
