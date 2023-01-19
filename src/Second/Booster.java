package Second;

import battlecode.common.*;

public class Booster extends Robot {

  private RobotInfo[] nearbyFriends;

  public Booster(RobotController rc) throws GameActionException {
    super(rc);
  }

  public void run(RobotController rc) throws GameActionException {
    if(shouldBoost(rc)){
      rc.boost(); 
    }

    Direction go = movement(rc);
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

  private boolean shouldBoost(RobotController rc) throws GameActionException {
    this.nearbyFriends = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam());
    return this.nearbyFriends.length > 0 && rc.canBoost(); 
  }

  private Direction movement(RobotController rc) throws GameActionException{
        //try moving towards friends first 
        if(this.nearbyFriends.length >0){
          return pathfind(rc,this.nearbyFriends[0].getLocation());
        }
        //try moving towards islands
        int[] islands=rc.senseNearbyIslands();
        if(islands.length>0){
          return pathfind(rc,rc.senseNearbyIslandLocations(islands[rng.nextInt(islands.length)])[0]);
        }
        //default: move randomly
        return directions[rng.nextInt(directions.length)];
  }

}
