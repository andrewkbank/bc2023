package First;

import battlecode.common.*;

public class Destabilizer extends Robot {
  private RobotInfo[] nearbyOpponents;
  static final Direction[] directions = {
      Direction.NORTH,
      Direction.NORTHEAST,
      Direction.EAST,
      Direction.SOUTHEAST,
      Direction.SOUTH,
      Direction.SOUTHWEST,
      Direction.WEST,
      Direction.NORTHWEST,
  };
  public Destabilizer(RobotController rc) throws GameActionException{
    super(rc);
  }

  public void run(RobotController rc) throws GameActionException {
    // TODO: check if there is a square within 13 units that is viable. if so, then attack
    if (this.canAttack(rc)) {
      Direction targetDirection = getTargetDirection(rc, true);
      if (rc.canMove(targetDirection)) {
        rc.move(targetDirection);
        rc.destabilize(rc.getLocation());
      } else if (rc.canMove(targetDirection.rotateLeft())) {
        rc.move(targetDirection.rotateLeft());
      } else if (rc.canMove(targetDirection.rotateRight())) {
        rc.move(targetDirection.rotateRight());
      } else if (rc.canMove(targetDirection.opposite())) {
        rc.move(targetDirection.opposite());
      }
    }
    else {
      Direction targetDirection = getTargetDirection(rc, false);
      if (rc.canMove(targetDirection)) {
        rc.move(targetDirection);
//        rc.destabilize(rc.getLocation());
        if (rc.isActionReady()) {
          this.attack(rc);
        }
      } else if (rc.canMove(targetDirection.rotateLeft())) {
        rc.move(targetDirection.rotateLeft());
      } else if (rc.canMove(targetDirection.rotateRight())) {
        rc.move(targetDirection.rotateRight());
      } else if (rc.canMove(targetDirection.opposite())) {
        rc.move(targetDirection.opposite());
      }
    }
  }

  public Direction getTargetDirection(RobotController rc, boolean isAttacking) {
    // TODO: implement basic targeting functionality
    if (isAttacking) {
      int randomOpponent = (int) (Math.random() * this.nearbyOpponents.length);
      return rc.getLocation().directionTo(this.nearbyOpponents[randomOpponent].getLocation());
    }
    else {
      int randomDirection = (int) (Math.random() * 8);
      return directions[randomDirection];
    }
  }

  public void attack(RobotController rc) throws GameActionException {
    //TODO: need to calculate the right spot to attack. based on radius from selected location?
    rc.destabilize(rc.getLocation());
  }

  // basic method checking if there are enough enemies in the destabilizer's current area to attack
  public boolean canAttack(RobotController rc) throws GameActionException {
    this.nearbyOpponents = rc.senseNearbyRobots(13, rc.getTeam().opponent());
    //TODO: have to find some way to make this smarter to know when to attack rather than just checking the number of
    // enemies within the radius
    if (this.nearbyOpponents.length > 0) {
      return true;
    }
    return false;
  }
}
