package scalaplayer

import scala.util.Random

import battlecode.common._

object RobotPlayer {

	def run(myRC : RobotController) {
		while(true) {
			try {
				if (myRC.getType() == RobotType.SOLDIER) {
					soldier_code(myRC)
				} else {
					hq_code(myRC)
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

	def soldier_code(rc: RobotController) {
		if (rc.isActive()) {
			val current_loc = rc.getLocation()
			val adj_mines = rc.senseNonAlliedMineLocations(current_loc, 1)

			var move_dir = current_loc.directionTo(rc.senseEnemyHQLocation())

			if (adj_mines.length > 0) {
				for (mine_loc <- adj_mines) {
					rc.defuseMine(mine_loc)
				}
			} else {
				var run_loop = true
				while (run_loop) {
					var new_loc = current_loc.add(move_dir)
					val new_mine = rc.senseMine(new_loc)

					if (rc.canMove(move_dir) && (new_mine==null || new_mine==rc.getTeam())) {
						rc.move(move_dir)
						run_loop = false
					}
					else {
						var move_arr = Array[Direction](move_dir.rotateRight(), move_dir.rotateLeft())
						move_dir = Random.shuffle(move_arr.toList).head
					}
				}
			}
		}
	}

	def hq_code(rc: RobotController) {
		if (rc.isActive()) {
			val current_loc = rc.getLocation()
			var move_dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation())
			var new_loc = current_loc.add(move_dir)
			val new_mine = rc.senseMine(new_loc)

			var run_loop = true
			while (run_loop) {
				var new_loc = current_loc.add(move_dir)
				val new_mine = rc.senseMine(new_loc)

				if (rc.canMove(move_dir) && (new_mine==null || new_mine==rc.getTeam())) {
					rc.spawn(move_dir)
					run_loop = false
				}
				else {
					var move_arr = Array[Direction](move_dir.rotateRight(), move_dir.rotateLeft())
					move_dir = Random.shuffle(move_arr.toList).head
				}
			}
		}
	}

	def is_safe_move(loc: Direction, mines: Array[MapLocation]) : Boolean = {
		/* This is expensive */
		for (mine <- mines) {
			if (loc == mine)
				return false
		}
		return true
	}

	def defuse_mines(rc: RobotController, loc: MapLocation) {
		val mines = rc.senseNonAlliedMineLocations(loc, 1)
		for (mine_loc <- mines) {
			rc.defuseMine(mine_loc)
		}
	}

}
