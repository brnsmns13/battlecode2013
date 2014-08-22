package team008

import scala.util.Random
import util.control.Breaks._

import battlecode.common._

object RobotPlayer {

	def run(myRC : RobotController) {
		while(true) {
			try {
				if (myRC.isActive()) {
					if (myRC.getType() == RobotType.SOLDIER) {
						soldier_code(myRC)
					} else if (myRC.getType() == RobotType.ARTILLERY) {
						artillery_code(myRC)
					} else if (myRC.getType() == RobotType.HQ){
						hq_code(myRC)
					}
				}
				myRC.`yield`()
			} catch {
				case e : Exception => {
					myRC.setIndicatorString(0, "Error!")
					println("caught exception:")
					e.printStackTrace()
				}
			}
		}
	}

	def soldier_code(rc: RobotController) {
		val current_loc = rc.getLocation()
		if (rc.senseEncampmentSquare(current_loc)) {
    		//var camp_type = Array[RobotType](RobotType.SUPPLIER, RobotType.GENERATOR)
			//rc.captureEncampment(Random.shuffle(camp_type.toList).head)
    		rc.captureEncampment(RobotType.GENERATOR)
		} else {
			if (defuse_mines(rc, current_loc)) {rc.`yield`()}
			else if (find_camps(rc, current_loc)) {rc.`yield`()}
			else if (path_finding(rc, current_loc)) {rc.`yield`()}
		}
	}

	def defuse_mines(rc: RobotController, current_loc: MapLocation) : Boolean = {
		if (rc.getTeamPower() < 400)
			return false
		var adj_mines = rc.senseNonAlliedMineLocations(current_loc, 1)
		if (adj_mines.length > 0) {
			for (mine_loc <- adj_mines) {
				rc.defuseMine(mine_loc)
				return true
			}
		}
		return false
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

	def path_finding(rc: RobotController, current_loc: MapLocation) : Boolean = {
		var move_dir = current_loc.directionTo(rc.senseEnemyHQLocation())
		for (i <- 1 to 8) {
			var new_loc = current_loc.add(move_dir)
			val new_mine = rc.senseMine(new_loc)

			if (rc.canMove(move_dir) && (new_mine==null || new_mine==rc.getTeam())) {
				rc.move(move_dir)
				return true
			}
			else {
				var move_arr = Array[Direction](move_dir.rotateRight(), move_dir.rotateLeft())
				move_dir = Random.shuffle(move_arr.toList).head
			}
		}
		return false
	}

	def hq_code(rc: RobotController) : Boolean = {
		val current_loc = rc.getLocation()
		val x = Random.shuffle(Array(1, 0, -1).toList).head
		val y = Random.shuffle(Array(1, -1).toList).head
		var move_dir = current_loc.directionTo(current_loc.add(x, y))
		// var move_dir = current_loc.directionTo(rc.senseEnemyHQLocation())
		var new_mine = rc.senseMine(current_loc.add(move_dir))

		if (rc.canMove(move_dir) && (new_mine==null || new_mine==rc.getTeam())) {
			rc.spawn(move_dir)
			return true
		} else {
			val x = Random.shuffle(Array(1, -1).toList).head
			val y = Random.shuffle(Array(1, 0, -1).toList).head
			move_dir = current_loc.directionTo(current_loc.add(x, y))
			if (rc.canMove(move_dir) && (new_mine==null || new_mine==rc.getTeam())) {
				rc.spawn(move_dir)
				return true
			} else {
				if (!rc.hasUpgrade(Upgrade.FUSION)) {
					rc.researchUpgrade(Upgrade.FUSION)
					return true
				}
			}
		}

		/*for (i <- 1 to 8) {
			var new_loc = current_loc.add(move_dir)
			val new_mine = rc.senseMine(new_loc)

			if (rc.canMove(move_dir) && (new_mine==null || new_mine==rc.getTeam())) {
				rc.spawn(move_dir)
				return true
			}
			else {
				var move_arr = Array[Direction](move_dir.rotateRight(), move_dir.rotateLeft())
				move_dir = Random.shuffle(move_arr.toList).head
			}
		}*/
		return false
	}

	def artillery_code(rc: RobotController) {
		val current_loc = rc.getLocation()
	}
}
