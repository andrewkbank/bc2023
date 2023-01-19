package Second;

import battlecode.common.*;

import java.util.LinkedList;

public class Communication {
  /*
   * Uploads HQ location to communication array, returns bool of whether it worked
   * 
   * ONLY SHOULD BE CALLED BY HEADQUARTERS
   */

  public int heapPtr;
  public int wellPtr;
  public int islandPtr;

  public Communication() {
    heapPtr = 61;
    wellPtr = 62;
    islandPtr = 63;
  }

  public boolean uploadHQ(RobotController rc) throws GameActionException {
    if (!rc.canWriteSharedArray(0, 0)) return false;
    // Adds 1 to each value, ensures not set 
    int data = (rc.getLocation().y+1) << 6 | (rc.getLocation().x+1);
    switch (rc.getID() >> 1) { // Get hq number from ID
      case 1:
        // data [0-11] -> index[0] [0-11]
        rc.writeSharedArray(0, data);
        return true;
      case 2: 
        // data [0-3] -> index[0] [12-15]
        rc.writeSharedArray(0, (rc.readSharedArray(0) & 0xfff) | (data & 0xf) << 12);
        // data [4-11] -> index[1] [0-7]
        rc.writeSharedArray(1, data >> 4);
        return true;
      case 3:
        // data [0-7] -> index[1] [8-15]
        rc.writeSharedArray(1, rc.readSharedArray(1) | (data & 0xff) << 8);
        // data [8-11] -> index[2] [0-3]
        rc.writeSharedArray(2, data >> 8);
        return true;
      case 4:
        // data [0-11] -> index[2] [4-15]
        rc.writeSharedArray(2, rc.readSharedArray(2) | data << 4);
        return true;
      default: return false;
    }
  }
  public MapLocation[] downloadHQ(RobotController rc) throws GameActionException {
    int[] data = {
      rc.readSharedArray(0),
      rc.readSharedArray(1),
      rc.readSharedArray(2)
    };
    MapLocation[] locs = {
      new MapLocation( (data[0] & 0b0000_0000_0011_1111) - 1,
                      ((data[1] & 0b0000_1111_1100_0000) >> 6) - 1),  
      new MapLocation(((data[0] & 0b1111_0000_0000_0000) >> 12 &
                       (data[1] & 0b0000_0000_0000_0011) << 4) - 1,  
                      ((data[1] & 0b0000_0000_1111_1100) >> 2) - 1),
      new MapLocation(((data[1] & 0b0011_1111_0000_0000) >> 8) - 1,  
                      ((data[1] & 0b1100_0000_0000_0000) >> 14 &
                       (data[2] & 0b0000_0000_0000_1111) << 2) - 1), 
      new MapLocation(((data[2] & 0b0000_0011_1111_0000) >> 4) - 1,  
                      ((data[2] & 0b1111_1100_0000_0000) >> 10) - 1)
    };
    int i;
    for (i = 1; i < 4; ++i) {
      if (locs[i].x == -1) break;
    }
    MapLocation[] ret = new MapLocation[i];
    for (int j = 0; j < i; ++j) {
      ret[j] = locs[j];
    }
    return ret;
  }

  public void update(RobotController rc, LinkedList<Integer> known) throws GameActionException {
    while (rc.readSharedArray(wellPtr) != 0 && wellPtr > 32) {

    }
  }
  public boolean uploadWell(RobotController rc, MapLocation loc, ResourceType rt) throws GameActionException {
    if (rc.canWriteSharedArray(0, 0)) { return false; }
    int ra = wellPtr - heapPtr;
    if (ra > 4) { ra = 4; }
    int data = (ra-1) << 14 | rt.resourceID << 12 | (loc.y+1) << 6 | (loc.x+1);
    wellPtr = heapPtr--;
    rc.writeSharedArray(wellPtr, data);
    return true;
  }
}
