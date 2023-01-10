package First;

import battlecode.common.*;

public class Carrier extends Robot {

  private static boolean well_found;
  private static WellInfo[] nearby_wells;
  private static MapLocation start;
  private static boolean first_turn;
  private static MapLocation goal;

  public Carrier() {
    first_turn = true;
  }

  public void run(RobotController rc) throws GameActionException {
    if (first_turn) {
      start = rc.getLocation();
      well_found = false;

      first_turn = false;
    }
    
    if (!well_found) {
      nearby_wells = rc.senseNearbyWells();
      well_found = (nearby_wells.length > 0) ? true : false;
      if (well_found) goal = getClosest(rc, nearby_wells);
    }

    if (well_found) {
      rc.move(pathfind(rc, goal));
    } else {
      if (rc.canMove(start.directionTo(rc.getLocation()))) {
        rc.move(start.directionTo(rc.getLocation()));
      } else {
        // Move random direction idk ill figure this out later im sleepy
      }
    }
  }
}
