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
      // If it sees too many carriers, it moves away from them and finds a new well
      if (rc.senseNearbyRobots(10, rc.getTeam()).length > 5 && rc.getResourceAmount(goal.getResourceType()) < 0.5*GameConstants.CARRIER_CAPACITY) {
        go = goal_loc.directionTo(hqInfo.location).rotateLeft().rotateLeft();
        well_found = false;
      }
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
      // Check if close to edge of map
      int dx, dy;
      if      (rc.getLocation().x < 4)                      { dx =  1; }
      else if (rc.getMapWidth() - rc.getLocation().x < 4)   { dx = -1; }
      else                                                  { dx =  0; }
      if      (rc.getLocation().y < 4)                      { dy =  1; }
      else if (rc.getMapHeight() - rc.getLocation().y < 4)  { dy = -1; }
      else                                                  { dy =  0; }
      if (dx != 0 || dy != 0) { go = makeDir(dx, dy); }
      else { // Not close to edge of map
        RobotInfo[] nearby = rc.senseNearbyRobots(-1, rc.getTeam());
        // No other robots nearby, just go away from HQ
        if (nearby.length == 0) { go = hqInfo.location.directionTo(rc.getLocation()); }
        else {  // Robots nearby, run away from their average position
          int avg_x = 0, avg_y = 0;
          for (RobotInfo r : nearby) {
            avg_x += r.getLocation().x;
            avg_y += r.getLocation().y;
          }
          avg_x /= nearby.length; avg_y /= nearby.length;
          go = new MapLocation(avg_x, avg_y).directionTo(rc.getLocation());
        }
      }
    }

    if(rc.canMove(go)){
      rc.move(go);
    } else if (rc.canMove(go.rotateRight())) {
      rc.move(go.rotateRight());
    } else if (rc.canMove(go.rotateLeft())) {
      rc.move(go.rotateLeft());
    } else if (rc.canMove(go.opposite())) {
      rc.move(go.opposite());
    }
  }
}
