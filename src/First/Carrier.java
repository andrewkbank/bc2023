package First;

import battlecode.common.*;
import java.util.LinkedList;
public class Carrier extends Robot {
  private static boolean well_found;
  private static WellInfo[] nearby_wells;
  private static WellInfo goal;
  private static MapLocation goal_loc;
  private static boolean elixir;
  private static WellInfo goal2;
  private static MapLocation goal_loc2;
  private static MapLocation hqLoc;
  private static boolean hasAnchor;
  private MapLocation nearestIslandLoc;

  public Carrier(RobotController rc) throws GameActionException {
    super(rc);
    well_found = false;
    //elixir code below
    elixir=false;
    goal2=null;
    goal_loc2=null;
    this.hqLoc = null;
    this.hasAnchor = false;
    this.nearestIslandLoc = null;
  }

  public void run(RobotController rc) throws GameActionException {
    updatePersonalMap(rc);

    if (this.hqLoc == null) {
      this.scanHQ(rc);
    }
    if (this.nearestIslandLoc == null) {
      this.scanIslands(rc);
    }

    //TODO: change this around later to account for both standard and accel anchors
    if (rc.canTakeAnchor(this.hqLoc, Anchor.STANDARD)) {
      rc.takeAnchor(hqLoc, Anchor.STANDARD);
      this.hasAnchor = true;
    }

    if (this.hasAnchor) {
      if (nearestIslandLoc == null) {
        this.moveRandom(rc);
      }
      else {
//        Direction[] islandDirs = this.pathfindCarrier(rc, nearestIslandLoc);
      }
    }
    else {
      if (!well_found) {
        nearby_wells = rc.senseNearbyWells();
        int i;
        for (i = 0; i < nearby_wells.length; ++i) {//picks a well that isn't crowded
          if (rc.senseNearbyRobots(nearby_wells[i].getMapLocation(), 6, rc.getTeam()).length < 4) {
            well_found = true;
            break;
          }
        }
        if (well_found) {
          goal = nearby_wells[i];
          goal_loc = goal.getMapLocation();
          //elixir code below
          if (goal.getResourceType() != ResourceType.ELIXIR
              && rc.getRoundNum() % 10 == 0) {//one in 10 chance for carrier to be an elixir carrier
            elixir = true;
            rc.setIndicatorString("elixir=true");
          }
        }
      } else if (goal2 == null && elixir) {//get second well for elixir
        nearby_wells = rc.senseNearbyWells();
        for (int i = 0; i < nearby_wells.length; ++i) {
          if (nearby_wells[i].getResourceType() != ResourceType.ELIXIR
              && nearby_wells[i].getResourceType() != goal.getResourceType()) {
            goal2 = nearby_wells[i];
            goal_loc2 = goal2.getMapLocation();
          }
        }
      }
    }

      Direction[] go = getMove(rc);
      int moveNum = 0;
      while (rc.isMovementReady()) {//handles multiple movements in one turn
        if (go[moveNum] == Direction.CENTER) {
          break;
        }
        if (rc.canMove(go[moveNum])) {
          rc.move(go[moveNum]);
        } else if (rc.canMove(go[moveNum].rotateRight())) {
          rc.move(go[moveNum].rotateRight());
        } else if (rc.canMove(go[moveNum].rotateLeft())) {
          rc.move(go[moveNum].rotateLeft());
        } else if (rc.canMove(go[moveNum].opposite())) {
          rc.move(go[moveNum].opposite());
        } else {
          break;
        }
        if (rc.canPlaceAnchor()) rc.placeAnchor();
        if (moveNum == 0) {
          moveNum = 1;
        }
      }
  }

  public Direction[] getMove(RobotController rc) throws GameActionException{
    Direction[] go;

    if (hasAnchor && nearestIslandLoc != null) {
      go = pathfindCarrier(rc, nearestIslandLoc);
    }

    else if(goal2!=null&&elixir){//make elixir well
      rc.setIndicatorString("I'm an elixir carrier");
      if (rc.getResourceAmount(goal.getResourceType()) < GameConstants.CARRIER_CAPACITY) { //collect from goal1
        if (rc.getLocation().isAdjacentTo(goal_loc) && rc.canCollectResource(goal_loc, -1)) {
          rc.collectResource(goal_loc, -1);
        }
        go = pathfindCarrier(rc, goal_loc);
      } else { // COLLECTED 100% RESOURCES, dump off at goal2 well
        if (rc.getLocation().isAdjacentTo(goal_loc2) && rc.canTransferResource(goal_loc2, goal.getResourceType(), rc.getResourceAmount(goal.getResourceType()))) {
          rc.transferResource(goal_loc2, goal.getResourceType(), rc.getResourceAmount(goal.getResourceType()));
        }
        go = pathfindCarrier(rc, goal_loc2);
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
        go = pathfindCarrier(rc, goal_loc);
      } else { // COLLECTED 50% RESOURCES
        if (rc.getLocation().isAdjacentTo(hqInfo.location) && rc.canTransferResource(hqInfo.location, goal.getResourceType(), rc.getResourceAmount(goal.getResourceType()))) {
          rc.transferResource(hqInfo.location, goal.getResourceType(), rc.getResourceAmount(goal.getResourceType()));
        }
        go = pathfindCarrier(rc, hqInfo.location);
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
      if (dx != 0 || dy != 0) {
        go=new Direction[2];
        go[0] = makeDir(dx, dy); 
        go[1] = makeDir(dx, dy); 
      } else { // Not close to edge of map
        RobotInfo[] nearby = rc.senseNearbyRobots(-1, rc.getTeam());
        // No other robots nearby, just go away from HQ
        if (nearby.length == 0) { 
          go=new Direction[2];
          go[0] = hqInfo.location.directionTo(rc.getLocation()); 
          go[1] = hqInfo.location.directionTo(rc.getLocation());
        } else {  // Robots nearby, run away from their average position
          int avg_x = 0, avg_y = 0;
          for (RobotInfo r : nearby) {
            avg_x += r.getLocation().x;
            avg_y += r.getLocation().y;
          }
          avg_x /= nearby.length; avg_y /= nearby.length;
          go=new Direction[2];
          go[0] = new MapLocation(avg_x, avg_y).directionTo(rc.getLocation());
          go[1] = new MapLocation(avg_x, avg_y).directionTo(rc.getLocation());
        }
      }
    }

    return go;
  }

  //since carrier can move 2 spaces at a time, it needs its own version of pathfind that returns 2 directions
  public Direction[] pathfindCarrier(RobotController rc, MapLocation goal) throws GameActionException{
    LinkedList<MapLocation> stack=new LinkedList<MapLocation>();
    int[] discovered=new int[rc.getMapHeight()*rc.getMapWidth()];
    Direction[] path={null,null};
    stack.push(rc.getLocation());
    while(!stack.isEmpty()){  //while loop that runs dfs
      MapLocation m=stack.pop();
      if(discovered[m.y*rc.getMapWidth()+m.x]==0){
        discovered[m.y*rc.getMapWidth()+m.x]=1;
        Direction dirToGoal=rc.getLocation().directionTo(m);
        Direction[] pushOrder={//this is the priority order that we add stuff to the dfs queue
          dirToGoal,
          dirToGoal.rotateRight(),
          dirToGoal.rotateLeft(),
          dirToGoal.rotateRight().rotateRight(),
          dirToGoal.rotateLeft().rotateLeft(),
          dirToGoal.rotateRight().rotateRight().rotateRight(),
          dirToGoal.rotateLeft().rotateLeft().rotateLeft(),
          dirToGoal.opposite(),

        };
        for(int i=0;i<8;++i){//go through the priority order
          MapLocation toPush=m.add(pushOrder[i]);
          if(toPush.x>=0&&toPush.y>=0&&toPush.x<rc.getMapWidth()&&toPush.y<rc.getMapHeight()){//inbounds
            if(map[toPush.y*rc.getMapWidth()+toPush.x]!=1){//passable
              if(toPush==goal){ //break if you find the destination
                path[0]=pushOrder[i].opposite();
                return path;
              }
              path[1]=pushOrder[i].opposite();
              stack.push(m.add(pushOrder[i])); //and add them all to the stack
            }
          }
        }
      }
    }
    //backup (in case dfs doesn't work)
    path[1]=rc.getLocation().directionTo(goal);
    path[0]=rc.getLocation().directionTo(goal);
    return path;

  }

  // looks for the HQ and saves it as an instance variable
  private void scanHQ(RobotController rc) {
    RobotInfo[] robots = rc.senseNearbyRobots();
    for (RobotInfo robot : robots) {
      if ((robot.getTeam() == rc.getTeam()) && (robot.getType() == RobotType.HEADQUARTERS)) {
        this.hqLoc = robot.getLocation();
        break;
      }
    }
  }

  //TODO: find some way to scan for wells and use some shortest path algo to find nearest well?

  // gets info for neutral islands
  private void scanIslands(RobotController rc) throws GameActionException {
    int[] islandIDs = rc.senseNearbyIslands();
    for (int id : islandIDs) {
      if (rc.senseTeamOccupyingIsland(id) == Team.NEUTRAL) {
        MapLocation[] islandLocs = rc.senseNearbyIslandLocations(id);
        this.nearestIslandLoc = islandLocs[0]; // placeholder for now, will eventually find closest neutral island by euclidean distance
      }
    }
  }
}
