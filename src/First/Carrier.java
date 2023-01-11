package First;

import battlecode.common.*;

public class Carrier extends Robot {

  private static boolean well_found;
  private static WellInfo[] nearby_wells;
  private static WellInfo goal;
  private static MapLocation goal_loc;

  public Carrier(RobotController rc) throws GameActionException {
    super(rc);
    well_found = false;
  }

  public void run(RobotController rc) throws GameActionException {
    updatePersonalMap(rc);
    
    if (!well_found) {
      nearby_wells = rc.senseNearbyWells();
      well_found = (nearby_wells.length > 0) ? true : false;
      if (well_found) {
        goal = getClosest(rc, nearby_wells);
        goal_loc = goal.getMapLocation();
      }
    }
    
    Direction go;
    if (well_found) {
      if (rc.getResourceAmount(goal.getResourceType()) < 0.5*GameConstants.CARRIER_CAPACITY) {
        if (rc.getLocation().isAdjacentTo(goal_loc) && rc.canCollectResource(goal_loc, -1)) {
          rc.collectResource(goal_loc, -1);
        }
        go = pathfind(rc, goal_loc);
      } else { // COLLECTED 50% RESOURCES
        if (rc.getLocation().isAdjacentTo(hqInfo.location) && rc.canTransferResource(hqInfo.location, goal.getResourceType(), rc.getResourceAmount(goal.getResourceType()))) {
          rc.transferResource(hqInfo.location, goal.getResourceType(), rc.getResourceAmount(goal.getResourceType()));
        }
        go = pathfind(rc, hqInfo.location);
      }
    } else {
      go=hqInfo.location.directionTo(rc.getLocation());
    }

    if(rc.canMove(go)){
      rc.move(go);
    }
  }
}
