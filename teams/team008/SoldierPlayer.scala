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

                val enemy_dir = senseEnemies(current_loc)
                if (enemy_dir != Direction.NONE && rc.canMove(enemy_dir)) {
                    rc.move(enemy_dir)
                } else if (rc.senseEncampmentSquare(current_loc)) {
                    rc.captureEncampment(RobotType.SUPPLIER)
                }
                else if(defuse_mines(current_loc)) {}
                else if(move(current_loc)) {}
            }
            rc.setIndicatorString(1, "" + current_action)
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

    def senseEnemies(current_loc: MapLocation): Direction = {
        val enemies = rc.senseNearbyGameObjects(classOf[Robot], 16, rc.getTeam().opponent())
        if (enemies.length > 0) {
            val enemy_loc = rc.senseLocationOf(enemies(0))
            val new_mine = rc.senseMine(enemy_loc)
            if (new_mine==null || new_mine==rc.getTeam()) {
                return Direction.NONE
            }
            return current_loc.directionTo(enemy_loc)
        }
        return Direction.NONE
    }

    def move(current_loc: MapLocation): Boolean = {
        var move_dir = current_loc.directionTo(rc.senseEnemyHQLocation())
        var new_loc = current_loc.add(move_dir)
        val new_mine = rc.senseMine(new_loc)

        if (current_action == SoldierAction.WAIT) {
            if (rc.canMove(move_dir) && current_loc.distanceSquaredTo(rc.senseHQLocation()) < 10) {
                rc.move(move_dir)
                return true
            }
            val friends = rc.senseNearbyGameObjects(classOf[Robot], 25, rc.getTeam())
            if (friends.length > 2) {
                current_action = SoldierAction.SWARM
                return true
            }
        } else if (current_action == SoldierAction.SWARM) {
            val friends = rc.senseNearbyGameObjects(classOf[Robot], 25, rc.getTeam())
            if (friends.length < 1) {
                current_action = SoldierAction.RETREAT
            }

            move_dir = find_move_dir(current_loc)

            if (move_dir != Direction.NONE) {
                rc.move(move_dir)
                return true
            } else {
                rc.setIndicatorString(0, "Nothing to do")
                return false
            }
        } else if (current_action == SoldierAction.RETREAT) {
            move_dir = current_loc.directionTo(rc.senseHQLocation())
            val friends = rc.senseNearbyGameObjects(classOf[Robot], 25, rc.getTeam())
            if (friends.length > 2) {
                current_action = SoldierAction.SWARM
            } else if (rc.canMove(move_dir)) {
                rc.move(move_dir)
                return true
            } else {
                rc.setIndicatorString(0, "Nothing to do")
                return false
            }
        }
        return false
    }

    def find_move_dir(current_loc: MapLocation) : Direction = {
        var move_dir = current_loc.directionTo(rc.senseEnemyHQLocation())
        for (i <- 1 to 8) {
            var new_loc = current_loc.add(move_dir)
            val new_mine = rc.senseMine(new_loc)

            if (rc.canMove(move_dir) && (new_mine==null || new_mine==rc.getTeam())) {
                return move_dir
            }
            else {
                var move_arr = Array[Direction](move_dir.rotateRight(), move_dir.rotateLeft())
                move_dir = Random.shuffle(move_arr.toList).head
            }
        }
        return Direction.NONE
    }

    def defuse_mines(current_loc: MapLocation) : Boolean = {
        var adj_mines = rc.senseNonAlliedMineLocations(current_loc, 1)
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
    val RETREAT = Value("RETREAT")
}
