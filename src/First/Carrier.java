package First;

import battlecode.common.*;

public class Carrier extends Robot {
  private static boolean well_found;
  private static WellInfo[] nearby_wells;
  private static WellInfo goal;
  private static MapLocation goal_loc;
  private static boolean elixir;
  private static WellInfo goal2;
  private static MapLocation goal_loc2;

  public Carrier(RobotController rc) throws GameActionException {
    super(rc);
    well_found = false;
    //elixir code below
    elixir=false;
    goal2=null;
    goal_loc2=null;
  }

  public void run(RobotController rc) throws GameActionException {
    updatePersonalMap(rc);
    
    if (!well_found) {
      nearby_wells = rc.senseNearbyWells();
      int i;
      for(i=0;i<nearby_wells.length;++i){//picks a well that isn't crowded
        if(rc.senseNearbyRobots(nearby_wells[i].getMapLocation(),6,rc.getTeam()).length<4){
          well_found=true;
          break;
        }
      }
      if (well_found) {
        goal = nearby_wells[i];
        goal_loc = goal.getMapLocation();
        //elixir code below
        if(goal.getResourceType()!=ResourceType.ELIXIR&&rc.getRoundNum()%10==0){//one in 10 chance for carrier to be an elixir carrier
          elixir=true;
          rc.setIndicatorString("elixir=true");
        }
      }
    }else if(goal2==null&&elixir){//get second well for elixir
      nearby_wells = rc.senseNearbyWells();
      for(int i=0;i<nearby_wells.length;++i){
        if(nearby_wells[i].getResourceType()!=ResourceType.ELIXIR&&nearby_wells[i].getResourceType()!=goal.getResourceType()){
          goal2=nearby_wells[i];
          goal_loc2=goal2.getMapLocation();
        }
      }
    }
    
    Direction go;
    if(goal2!=null&&elixir){//make elixir well
      rc.setIndicatorString("I'm an elixir carrier");
      if (rc.getResourceAmount(goal.getResourceType()) < GameConstants.CARRIER_CAPACITY) { //collect from goal1
        if (rc.getLocation().isAdjacentTo(goal_loc) && rc.canCollectResource(goal_loc, -1)) {
          rc.collectResource(goal_loc, -1);
        }
        go = pathfind(rc, goal_loc);
      } else { // COLLECTED 100% RESOURCES, dump off at goal2 well
        if (rc.getLocation().isAdjacentTo(goal_loc2) && rc.canTransferResource(goal_loc2, goal.getResourceType(), rc.getResourceAmount(goal.getResourceType()))) {
          rc.transferResource(goal_loc2, goal.getResourceType(), rc.getResourceAmount(goal.getResourceType()));
        }
        go = pathfind(rc, goal_loc2);
      }
      //check to see if well converted
      //stop once the carrier has finished making the elixir well
      if(rc.canSenseLocation(goal_loc2)){
        goal2=rc.senseWell(goal_loc2);
        if(goal2.getResourceType()==ResourceType.ELIXIR){//elixir converted, reset carrier to default
          goal2=null;
          goal_loc2=null;
          elixir=false;
          rc.setIndicatorString("I finished my elixir job!");
        }
      }
    }else if (well_found) {
      if(rc.canSenseLocation(goal_loc)){//update well info (in case the well is now an elixir well)
        goal=rc.senseWell(goal_loc);
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
