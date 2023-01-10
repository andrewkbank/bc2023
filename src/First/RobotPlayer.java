package First;

import battlecode.common.*;

public strictfp class RobotPlayer {
  private static Robot robot;

  public static void run(RobotController rc) throws GameActionException {
    switch (rc.getType()) {
      case AMPLIFIER:       robot = new Amplifier(rc);      break;
      case BOOSTER:         robot = new Booster(rc);        break;
      case CARRIER:         robot = new Carrier(rc);        break;
      case DESTABILIZER:    robot = new Destabilizer(rc);   break;
      case HEADQUARTERS:    robot = new Headquarters(rc);   break;
      case LAUNCHER:        robot = new Launcher(rc);       break;
    }

    while (true) {
      try {
        robot.run(rc);
      } catch (GameActionException e) {
        System.out.println(rc.getType() + " Exception");
        e.printStackTrace();
      } catch (Exception e) {
        System.out.println(rc.getType() + " Exception");
        e.printStackTrace();
      } finally {
        Clock.yield();
      }
    }
  }
}
