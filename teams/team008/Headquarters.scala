package dumb

import scala.util.Random
import battlecode.common._

// Soldier logic
// 1. Read broadcasts
// 2. Sense surroundings
// 3. Determine action
// 4. Execute
// 5. Save state

class Headquarters(val rc_c: RobotController, val spawn_dir_c: Direction) {
    val rc: RobotController = rc_c
    var current_action = HQAction.SPAWN
    var spawn_dir = spawn_dir_c

    def run() {
        while (true) {
            if (rc.isActive()) {
                val current_loc = rc.getLocation()
                val (broadcast_loc, broadcast_action) = readBroadcasts()

                if (findSpawn()) {}
                else if (research()) {}
            }
            rc.`yield`()
        }
    }

    def readBroadcasts(): (MapLocation, Int) = {
        return (null, 0)
    }

    def findSpawn(): Boolean = {
        val current_loc = rc.getLocation()
        var new_mine = rc.senseMine(current_loc.add(spawn_dir))

        if (rc.canMove(spawn_dir) && (new_mine==null || new_mine==rc.getTeam())) {
            rc.spawn(spawn_dir)
            var move_arr = Array[Direction](spawn_dir.rotateRight(), spawn_dir.rotateLeft())
            spawn_dir = Random.shuffle(move_arr.toList).head
            return true
        } else {
            spawn_dir = spawn_dir.opposite().rotateLeft()
            new_mine = rc.senseMine(current_loc.add(spawn_dir))
            if (rc.canMove(spawn_dir) && (new_mine==null || new_mine==rc.getTeam())) {
                rc.spawn(spawn_dir)
                var move_arr = Array[Direction](spawn_dir.rotateRight(), spawn_dir.rotateLeft())
                spawn_dir = Random.shuffle(move_arr.toList).head
                return true
            }
        }
        return false
    }

    def research(): Boolean = {
        if (!rc.hasUpgrade(Upgrade.FUSION)) {
            rc.researchUpgrade(Upgrade.FUSION)
            return true
        }
        return false
    }
}

object HQAction extends Enumeration {
    type HQAction = Value
    val SPAWN = Value("WAIT")
}
