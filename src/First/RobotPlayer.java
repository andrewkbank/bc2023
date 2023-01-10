package First;

import battlecode.common.*;

public strictfp class RobotPlayer {
  private static Robot robot;

  public static void run(RobotController rc) throws GameActionException {
    switch (rc.getType()) {
      case AMPLIFIER:       robot = new Amplifier();      break;
      case BOOSTER:         robot = new Booster();        break;
      case CARRIER:         robot = new Carrier();        break;
      case DESTABILIZER:    robot = new Destabilizer();   break;
      case HEADQUARTERS:    robot = new Headquarters();   break;
      case LAUNCHER:        robot = new Launcher();       break;
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
