package dumb

import scala.util.Random
import util.control.Breaks._

import battlecode.common._

object RobotPlayer {

    // HQ logic
    // 1. Check resources
    // 2. Determine action
    // 3. Execute
    // 4. Save state

    def run(myRC : RobotController) {
        while(true) {
            try {
                if (myRC.isActive()) {
                    if (myRC.getType() == RobotType.SOLDIER) {
                        var sp = new SoldierPlayer(myRC, 0)
                        sp.run()
                    } else if (myRC.getType() == RobotType.ARTILLERY) {
                        artillery_code(myRC)
                    } else if (myRC.getType() == RobotType.HQ) {
                        val current_loc = myRC.getLocation()
                        var move_dir = current_loc.directionTo(myRC.senseEnemyHQLocation())
                        var hq = new Headquarters(myRC, move_dir)
                        hq.run()
                    }
                }
                myRC.`yield`()
            } catch {
                case e : Exception => {
                    println("caught exception:")
                    e.printStackTrace()
                }
            }
        }
    }

    def find_camps(rc: RobotController, current_loc: MapLocation) : Boolean = {
        val adj_encampments = rc.senseEncampmentSquares(current_loc, 1, Team.NEUTRAL)
        if (adj_encampments.length > 0) {
            for (camp <- adj_encampments) {
                var new_dir = current_loc.directionTo(camp)
                if (rc.canMove(new_dir))
                    rc.move(new_dir)
                    return true
            }
        }
        return false
    }

    def artillery_code(rc: RobotController) {
        val current_loc = rc.getLocation()
    }
}
