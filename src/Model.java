import java.awt.Point;
import java.util.*;

/**
 * The model class holds the model of the map. Here it stores important
 * information for the game, including number of dynamites holding and
 * where the important items are located. It also holds methods in relation
 * to the map such as what is the front tile's type, whether it is a wall or
 * water, and methods that return the nearest reachable tile, land or water. 
 * 
 * This class utilises linked lists of points to store locations of important
 * items and the agent's location where the items are visible. It also uses
 * boolean to keep track whether it has these important items like axe or key. 
 * 
 * 
 * @author Mendel Liang, Alexander Ong
 */

public class Model {
   
   
//Given constants to do with the map size/view
   final static int WINDOW_SIZE = 5;
   final static int MAXIMUM_X = 80;
   final static int MAXIMUM_Y = 80;
   
   
 //Definitions for the state
   final static int UP = 0;
   final static int RIGHT = 1;
   final static int DOWN = 2;
   final static int LEFT = 3;

   final static char PLAIN = ' ';
   final static char TREE = 'T';
   final static char DOOR = '-';
   final static char WALL = '*';
   final static char WATER = '~';
   
   final static char AXE = 'a';
   final static char KEY = 'k';
   final static char DYNAMITE = 'd';
   final static char TREASURE = '$';
   
   final static char UNEXPLORED = '?';
   
   final static char TURN_LEFT = 'L';
   final static char TURN_RIGHT = 'R';
   final static char MOVE_FORWARD = 'F';
   final static char CHOP_TREE = 'C';
   final static char USE_DYNAMITE = 'B';
   final static char UNLOCK_DOOR = 'U';
   
   private int xLoc;
   private int yLoc;
   private int direction;
   
   private boolean treasureVisible;
   
   private boolean haveKey;
   private boolean haveAxe;
   private boolean haveRaft;
   private boolean haveTreasure;
   private int numDynamites;
   
   private Set<Point> visited;
   private Map<Point, Character> world;
   //We need to keep an eye on what we're standing on because the map thinks we're on a ^
   private char currentTerrain;
   
   private Point treasureLoc;
   private LinkedList<Point> axes;
   private LinkedList<Point> dynamites;
   private LinkedList<Point> keys;
   private LinkedList<Point> trees;
   
   private LinkedList<Point> doors;
   
   private LinkedList<Point> axesSeen;
   private LinkedList<Point> dynamitesSeen;
   private LinkedList<Point> keysSeen;
   private LinkedList<Point> treesSeen;
   private LinkedList<Point> doorsSeen;
   private Point treasureSeen;
   
   public Model() {
      
      this.xLoc = 0;
      this.yLoc = 0;
      this.direction = DOWN;
      
      this.treasureVisible = false;
      
      this.haveKey = false;
      this.haveAxe = false;
      this.haveRaft = false;
      this.haveTreasure = false;
      this.numDynamites = 0;  
      
      this.visited = new HashSet<Point>();
      this.world = new HashMap<>();
      this.currentTerrain = ' ';
      
      this.axes = new LinkedList<Point>();
      this.dynamites = new LinkedList<Point>();
      this.keys = new LinkedList<Point>();
      this.trees = new LinkedList<Point>();
      
      this.axesSeen = new LinkedList<Point>();
      this.dynamitesSeen = new LinkedList<Point>();
      this.keysSeen = new LinkedList<Point>();
      this.treesSeen = new LinkedList<Point>();
      this.doorsSeen = new LinkedList<Point>();
      
      this.doors = new LinkedList<Point>();
      //We might start at the bottom which means that we can go MAXIMUM_Y Upwards...
      //But we might also start at the top which means we can go MAXIMUM_Y Downwards...
      //So we should just have MAXIMUM_Y in both directions. And the same for the x axis.
      for(int x = -MAXIMUM_X; x <= MAXIMUM_X; x++) {
         for(int y = -MAXIMUM_Y; y <= MAXIMUM_Y; y++) {
            //Pre-fill the world with UNEXPLORED;
            this.world.put(new Point(x,y), UNEXPLORED);
         }
      }
   }
   //A massive amount of getters
   public boolean haveAxe() {
      return haveAxe;
   }
   public boolean haveKey() {
      return haveKey;
   }
   public boolean haveRaft() {
      return haveRaft;
   }
   public int numDynamites() {
      return numDynamites;
   }
   public boolean haveTreasure() {
      return haveTreasure;
   }
   public Map<Point, Character> getWorld() {
      return world;
   }
   public Point getLoc() {
      return new Point(xLoc, yLoc);
   }
   public int getDirection() {
      return this.direction;
   }
   public Point getTreasureLoc() {
      return this.treasureLoc;
   }
   public LinkedList<Point> getAxeLocs(){
      return this.axes;
   }
   public LinkedList<Point> getKeyLocs(){
      return this.keys;
   }
   public LinkedList<Point> getTreeLocs(){
      return this.trees;
   }
   public LinkedList<Point> getDynamiteLocs(){
      return this.dynamites;
   }
   public LinkedList<Point> getDoorLocs(){
      return this.doors;
   }

   public LinkedList<Point> getAxeSeenLocs(){
      return this.axesSeen;
   }
   public LinkedList<Point> getKeySeenLocs(){
      return this.keysSeen;
   }
   public LinkedList<Point> getTreeSeenLocs(){
      return this.treesSeen;
   }
   public LinkedList<Point> getDynamiteSeenLocs(){
      return this.dynamitesSeen;
   }
   public LinkedList<Point> getDoorSeenLocs(){
      return this.doorsSeen;
   }
   public Point getTreasureSeen(){
      return this.treasureSeen;
   }

   public boolean treasureVisible() {
      return this.treasureVisible;
   }
   
   public char getCurrentTerrain() {
      return this.currentTerrain;
   }
   /**
    * Updates the information stored in model based on the information given in the 5x5 view.
    * @param view is what the AI can 'see' at the current turn
    */
   public void update(char view[][]) {
      //We need to rotate the view we're given so that it's the same orientation as our original map.
      int rotationsRequired = 0;
      
      switch(this.direction) {
         case UP:
            break;
         case RIGHT:
            rotationsRequired = 1;
            break;
         case DOWN:
            rotationsRequired = 2;
            break;
         case LEFT:
            rotationsRequired = 3;
            break;

      }
      for(int i = 0; i < rotationsRequired; i++) {
         view = rotateMap(view);
      }
      for(int i = 0; i < WINDOW_SIZE; i++) {
         for(int j = 0; j < WINDOW_SIZE; j++) {
            char currTile = view[i][j];
            int currX = xLoc + (j-2);
            int currY = yLoc + (2-i);

            Point tile = new Point(currX, currY);
            Point curr = new Point(xLoc, yLoc);
            
            switch(currTile) {
               case AXE:
                  if(!this.axes.contains(tile)) {
                     this.axes.add(tile);
                     this.axesSeen.add(curr);
                  }
                  break;
               case DYNAMITE:
                  if(!this.dynamites.contains(tile)) {
                     this.dynamites.add(tile);
                     this.dynamitesSeen.add(curr);
                  }
                  break;
               case TREASURE:
                  this.treasureVisible = true;
                  this.treasureLoc = tile;
                  this.treasureSeen = curr;
                  break;
               case KEY:
                  if(!this.keys.contains(tile)) {
                     this.keys.add(tile);
                     this.keysSeen.add(curr);
                  }
                  break;
               case TREE:
                  if(!this.trees.contains(tile)) {
                     this.trees.add(tile);
                     this.treesSeen.add(curr);
                  }
                  break;
               case DOOR:
                  if(!this.doors.contains(tile)) {
                     this.doors.add(tile);
                     this.doorsSeen.add(curr);
                  }
                  break;
            }
            this.world.put(tile, currTile);
            visited.add(getLoc());
         }
      }
      world.put(getLoc(), currentTerrain);
      //showMap();
   }
   /**
    * Rotates the given 2d array map such that it matches the orientation of our original map
    * @param map is the 5x5 view seen by the AI
    * @return the same 2d array rotated accordingly.
    */
   private static char[][] rotateMap(char[][] map){
      int x = map.length;
      int y = map[0].length;
      char[][] rotatedMap = new char[y][x];
      for(int l = 0; l < x; l++) {
         for(int h = 0; h < y; h++) {
            rotatedMap[h][x - 1 - l] = map[l][h];
         }
      }
      return rotatedMap;
   }
   /**
    * Updates the model after the AI has input a move.
    * Handles trees/doors/walls being removed and adds/removes inventory as required
    * @param move the move that is about to be made
    */
   public void updateMove(char move) {
      Point currTile = new Point(this.xLoc, this.yLoc);
      char frontTile = world.get(frontTile(currTile));
      switch(move) {
      //Right turn
         case 'R':
            switch (this.direction) {
               case UP:
                  direction = RIGHT;
                  break;
               case RIGHT:
                  direction = DOWN;
                  break;
               case DOWN:
                  direction = LEFT;
                  break;
               case LEFT:
                  direction = UP;
                  break;
            }
            break;
         //Left Turn
         case 'L':
            switch (this.direction) {
               case UP:
                  direction = LEFT;
                  break;
               case RIGHT:
                  direction = UP;
                  break;
               case DOWN:
                  direction = RIGHT;
                  break;
               case LEFT:
                  direction = DOWN;
                  break;
            }
            break;
         case 'F':
           if((frontTile == WALL) || (frontTile == DOOR) || (frontTile == TREE)) {
              break;
           }
           if((this.currentTerrain == WATER) && (canMoveOntoTile(frontTile))){
              this.haveRaft = false;
           }
           if (frontTile == AXE) {
              haveAxe = true;
           }
           else if (frontTile == KEY) {
              haveKey = true;
           }
           else if (frontTile == DYNAMITE) {
              numDynamites += 1;
           }
           else if (frontTile == TREASURE) {
              haveTreasure = true;
           }
           switch(this.direction) {
              case UP:
                 yLoc += 1;
                 break;
              case RIGHT:
                 xLoc += 1;
                 break;
              case DOWN:
                 yLoc -= 1;
                 break;
              case LEFT:
                 xLoc -= 1;
                 break;
           }
           this.currentTerrain = world.get(getLoc());
         case 'C':
            if(frontTile == TREE) {
               this.trees.remove(frontTile(currTile));
               this.haveRaft = true;
            }
            break;
         case 'U':
            this.doors.remove(frontTile(currTile));
            break;
         case 'B':
            world.put(frontTile(currTile), PLAIN);
            numDynamites -= 1;
            break;
      }
   }
   /**
    * Gets the tile in front of the given tile in the appropriate direction
    * @param tile is the tile we want the tile in front of of
    * @return the tile in front of the given tile
    */
   public Point frontTile(Point tile) {
      int x = (int) tile.getX();
      int y = (int) tile.getY();
      
      switch(this.direction) {
         case UP:
            y += 1;
            break;
         case RIGHT:
            x += 1;
            break;
         case DOWN:
            y -= 1;
            break;
         case LEFT:
            x -= 1;
            break;
      }
      return new Point(x,y);
   }
   /**
    * Tiles we can move onto without any tools.
    * @param tile is the tile we want to move onto
    * @return whether or not it is possible to move onto that tile without tools
    */
   public static boolean canMoveOntoTile(char tile) {
      return((tile == PLAIN) ||
             (tile == AXE) ||
             (tile == KEY) ||
             (tile == DYNAMITE) ||
             (tile == TREASURE) 
            );
   }
   /**
    * Tiles we can move onto with given tools.
    * @param tile the tile we want to move onto
    * @param haveAxe whether we have an axe or not
    * @param haveKey whether we have a key or not
    * @param haveRaft whether we have a raft or not
    * @return whether we can move onto that tile with the appropriate tools
    */
   public static boolean canPotentiallyMoveOntoTile(char tile, boolean haveAxe, boolean haveKey, boolean haveRaft) {
      return((tile == PLAIN) ||
             (tile == AXE) ||
             (tile == KEY) ||
             (tile == DYNAMITE) ||
             (tile == TREASURE) ||
             (tile == TREE && haveAxe) ||
             (tile == WATER && haveRaft) ||
             (tile == DOOR && haveKey) 
            );
   }
   /**
    * Tiles that can be removed by using Dynamite
    * @param tile the tile to be checked for removal
    * @return whether the tile can be removed via dynamite
    */
   public static boolean canBeBlownUp(char tile) {
      return ((tile == WALL) ||
            (tile == DOOR) ||
            (tile == TREE));
   }
   /**
    * Finds the nearest point to the given point that can be moved to without the usage of dynamite.
    * Used to find the most efficient spot to start dynamiting from (since there are less obstacles)
    * @param p the point to be traveled to
    * @return the nearest point to the given point which can be reached without dynamite
    */
   public Point nearestPointLeastObstaclesSurrounding(Point p) {
      int[] obstacles = new int[4];
      for(int i = 0; i < 4; i++) {
         int x = (int)p.getX();
         int y = (int)p.getY();
         switch(i) {
            case UP:
               y += 1;
               break;
            case RIGHT:
               x += 1;
               break;
            case DOWN:
               y -= 1;
               break;
            case LEFT:
               x -= 1;
               break;
         }
         Point next = new Point(x,y);
         boolean passable = false;
         while(!passable) {
            if(canBeBlownUp(world.get(next))) {
               obstacles[i] ++;
            }
            else {
               passable = true;
            }
         }
      }
      int smallest = obstacles[0];
      for(int i : obstacles) {
         if(i < smallest) {
            i = smallest;
         }
      }
      int x = (int)p.getX();
      int y = (int)p.getY();
      switch(smallest) {
         case UP:
            y += 1;
            break;
         case RIGHT:
            x += 1;
            break;
         case DOWN:
            y -= 1;
            break;
         case LEFT:
            x -= 1;
            break;
      }
      return new Point(x,y);
   }
   /**
    * Gives the next tile that should be explored when exploring.
    * @param curr the current location of the AI
    * @return the next point that should be explored
    */
   public Point nearestReachableRevealingTile(Point curr) {
      HashMap<Integer, Point> distances = new HashMap<>();
      for(Point p : this.world.keySet()) {
         if(!visited.contains(p) && world.get(p) != UNEXPLORED && canPotentiallyMoveOntoTile(world.get(p), this.haveAxe, this.haveKey, this.haveRaft)) {
            AStarSearch a = new AStarSearch(this.world, curr, p);
            a.aStar(this.haveAxe, this.haveKey, this.haveRaft);
            if(a.reachable()) {
               return p;
            }
         }
      }
      return null;
   }
          
 /*     Alternate method. Slower on real time, but more efficient on moves.         
               
               distances.put(manhattanDistance(curr, p), p);
            }
         }
      }
      if(distances.isEmpty()){
         return null;
      }
      else {
         int smallest = 9999999;
         for(Integer i : distances.keySet()) {
            if(i < smallest) {
               smallest = i;
            }
         }
         return(distances.get(smallest));
      }
   }*/
  /**
    * Gives the next tile that should be explored when exploring water.
    * @param curr the current location of the AI
    * @return the next point that should be explored
   */
   public Point nearestReachableRevealingWaterTile(Point curr) {
      HashMap<Integer, Point> distances = new HashMap<>();
      for(Point p : this.world.keySet()) {
         if(!visited.contains(p) && world.get(p) == WATER) {
            AStarSearch a = new AStarSearch(this.world, curr, p);
            a.aStar(this.haveAxe, this.haveKey, this.haveRaft);
            if(a.reachable()) {
               return p;
            }
         }
      }
      return null;
   }
               
               /*distances.put(manhattanDistance(curr, p), p);
            }
         }
      }
      if(distances.isEmpty()){
         return null;
      }
      else {
         int smallest = 9999999;
         for(Integer i : distances.keySet()) {
            if(i < smallest) {
               smallest = i;
            }
         }
         return(distances.get(smallest));
      }
   }   */
   /**
    * Returns whether the tile in front is a wall.
    * @param curr the current point
    * @return whether the wall in front is a wall.
    */
   public boolean frontTileIsWall(Point curr) {
      char frontTile = world.get(frontTile(curr));
      if(frontTile == WALL){
         return true;
      }
      return false;
   }
   /**
    * Checks whether an item is blocked by a wall
    * @param curr is the tile of the item
    * @return whether the item is blocked by a wall
    */
   public boolean wallBlocksItem(Point curr) {
      int x = (int) curr.getX();
      int y = (int) curr.getY();
      Point frontCurr = frontTile(curr);
      char leftFrontTile = world.get(new Point(x - 1, y));
      char rightFrontTile = world.get(new Point(x + 1, y));
      char frontFrontTile = world.get(new Point(x, y + 1));
      if(leftFrontTile == PLAIN || rightFrontTile == PLAIN || 
            frontFrontTile == PLAIN){
         return true;
      }
      if(leftFrontTile == WALL || rightFrontTile == WALL || 
            frontFrontTile == WALL){
         wallBlocksItem(frontCurr);
      }
      return false;
   }
   /**
    * Determines whether a point can see any unexplored tiles.
    * @param curr is the point to be tested for being able to see any unexplored locations
    * @return whether the point can see any unexplored locations
    */
   /*private boolean canSeeUnknowns(Point curr) {
      boolean canSee = false;
      for(int i = -2; i <= 2; i++) {
         for(int j = -2; j <= 2; j++) {
            if(world.get(new Point((int)(curr.getX()+i), (int)(curr.getY()+j))) == UNEXPLORED) {
               canSee = true;
               break;
            }
         }
         if(canSee == true) {
            break;
         }
      }
      
      return canSee;
   }*/
   /**
    * Prints out an area of the map. Used for debugging the model.
    */
   public void showMap() {
      System.out.println(xLoc);
      System.out.println(yLoc);
      for(int y = 12; y >= -12; y--) {
         for(int x = -12; x <= 12; x++) {
            char tile = world.get(new Point(x,y));
            System.out.print(tile);
         }
         System.out.println();
      }
   }
   /**
    * A private helper function for determining which tile is nearest.
    * @param start is the point from which we are travelling
    * @param goal is the point we are travelling to
    * @return the manhattan distance between these points.
    */
   private int manhattanDistance(Point start, Point goal) {
      return Math.abs((int)start.getX() - (int)goal.getX()) + Math.abs((int)start.getY() - (int)goal.getY());
   }
}

