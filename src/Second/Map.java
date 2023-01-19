package Second;

import battlecode.common.*;

/* == MAP LOCATION VALUE MEANING ==
 * [ LEAST TO MOST SIGNIFICANT BITS LISTED ]
 * Bits:    Meaning:      Value:
 * 0        Set           0 - Not set yet
 *                        1 - Set
 * 1        Current       0 - No current
 *                        1 - Current
 * 2-4      Current Dir   000 - North
 *                        001 - Northeast
 *                        010 - East
 *                        011 - Southeast
 *                        100 - South
 *                        101 - Southwest
 *                        110 - West
 *                        111 - Northwest
 * 5        Cloud         0 - No cloud
 *                        1 - Cloud
 * 6        Our HQ        0 - No
 *                        1 - Yes
 * 7        Other HQ      0 - No
 *                        1 - Yes
 * 8-9      Wells         00 - None
 *                        01 - Adamantium
 *                        10 - Mana
 *                        11 - Elixir
 * 10-11    Island        00 - No island
 *                        01 - Our Island
 *                        10 - Other Island
 *                        11 - Unclaimed Island
 * 12       Tempest       0 - No (Passable)
 *                        1 - Yes (Impassable)
 * 13-15    UNUSED
 */


/* == MAP STORAGE METHOD ==
 * [Values represent index]
 *      20 21 22 23 24
 *      15 16 17 18 19
 *   ^  10 11 12 13 14
 *   |   5  6  7  8  9
 *   y   0  1  2  3  4
 *       x ---->
 */

public class Map {

  // Masks used for getting / unsetting data
  public static final int KNOWN                 = 0b0000_0000_0000_0001;
  public static final int CURRENT               = 0b0000_0000_0000_0010;
  public static final int CLOUD                 = 0b0000_0000_0010_0000;
  public static final int OUR_HQ                = 0b0000_0000_0100_0000;
  public static final int OTHER_HQ              = 0b0000_0000_1000_0000;
  public static final int WELL                  = 0b0000_0011_0000_0000;
  public static final int ISLAND                = 0b0000_1100_0000_0000;
  public static final int TEMPEST               = 0b0001_0000_0000_0000;

  // Masks used for setting data
  public static final int SET_KNOWN             = 0b0000_0000_0000_0001;
  public static final int SET_CURRENT_N         = 0b0000_0000_0000_0010;
  public static final int SET_CURRENT_NE        = 0b0000_0000_0000_0110;
  public static final int SET_CURRENT_E         = 0b0000_0000_0000_1010;
  public static final int SET_CURRENT_SE        = 0b0000_0000_0000_1110;
  public static final int SET_CURRENT_S         = 0b0000_0000_0001_0010;
  public static final int SET_CURRENT_SW        = 0b0000_0000_0001_0110;
  public static final int SET_CURRENT_W         = 0b0000_0000_0001_1010;
  public static final int SET_CURRENT_NW        = 0b0000_0000_0001_1110;
  public static final int SET_CLOUD             = 0b0000_0000_0010_0000;
  public static final int SET_OUR_HQ            = 0b0000_0000_0100_0000;
  public static final int SET_OTHER_HQ          = 0b0000_0000_1000_0000;
  public static final int SET_AD_WELL           = 0b0000_0001_0000_0000;
  public static final int SET_MN_WELL           = 0b0000_0010_0000_0000;
  public static final int SET_EX_WELL           = 0b0000_0011_0000_0000;
  public static final int SET_OUR_ISLAND        = 0b0000_0100_0000_0000;
  public static final int SET_OTHER_ISLAND      = 0b0000_1000_0000_0000;
  public static final int SET_UNCLAIMED_ISLAND  = 0b0000_1100_0000_0000;
  public static final int SET_TEMPEST           = 0b0001_0000_0000_0000;

  // Member variables
  private int[] data;
  private int width, height;

  // Constructor
  public Map(int width_, int height_) {
    width = width_; height = height_;
    data = new int[width*height];
    for(int i = 0; i < data.length; ++i) { data[i] = 0; }
  }

  // Valid value checking
  public void assertOnMap(int x, int y) throws IllegalArgumentException { 
    if (x < 0 || x >= width || y < 0 || y >= height) {
      throw new IllegalArgumentException("Location must be on map.");
    } 
  }

  // WRITE INFORMATION TO MAP ==================================================
  public void writeData(MapLocation loc, int val) throws IllegalArgumentException {
    writeData(loc.x, loc.y, val);
  }
  public void writeData(int x, int y, int val) throws IllegalArgumentException {
    assertOnMap(x, y);
    // Island ownership can flip flop, so needs to be reset before written
    if ((val & 3072) > 0) { unsetData(x, y, ISLAND); }
    data[width*y + x] |= val;
  }
  // UNSET INFORMATION IN MAP ==================================================
  public void unsetData(MapLocation loc, int val) throws IllegalArgumentException {
    unsetData(loc.x, loc.y, val);
  }
  public void unsetData(int x, int y, int val) throws IllegalArgumentException {
    assertOnMap(x, y);
    data[width*y + x] &= ~val;
  }
  // READ INFORMATION FROM MAP =================================================
  public int readData(MapLocation loc, int val) throws IllegalArgumentException {
    return readData(loc.x, loc.y, val);
  }
  public int readData(int x, int y, int val) throws IllegalArgumentException {
    assertOnMap(x, y);
    return data[width * y + x] & val;
  }
}
