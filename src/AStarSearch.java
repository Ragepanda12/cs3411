import java.awt.Point;
import java.util.*;

/*Derived from the wikipedia pseudocode on A* Search*/
public class AStarSearch {
   
   private Map<Point, Character> world;
   
   private static int INFINITY = 999999999; //large number for representing infinity
   
   private Point start;
   private Point goal;
   private Map<Point, Integer> fScore;
   private Map<Point, Integer> gScore;
   private Map<Point, Point> cameFrom;

   private boolean needToCutTree;
   
   public AStarSearch(Map<Point,Character> world, Point start, Point goal) {
      this.world = world;
      this.start = start;
      this.goal = goal;
      this.fScore = new HashMap<Point, Integer>();
      this.gScore = new HashMap<Point, Integer>();
      this.cameFrom = new HashMap<Point, Point>();
      this.needToCutTree = false;
   }
   private class FComparator implements Comparator<Point>{
      @Override
      public int compare(Point a, Point b) {
         return fScore.get(a) - fScore.get(b);
      }
   }
   
   public void aStar(boolean haveAxe, boolean haveKey, boolean haveRaft){
      PriorityQueue<Point> pq = new PriorityQueue<Point>(11, new FComparator());
      
      Set<Point> visited = new HashSet<Point>();
      
      for(int x = -Model.MAXIMUM_X; x <= Model.MAXIMUM_X; x++ ) {
         for(int y = -Model.MAXIMUM_Y; y <= Model.MAXIMUM_Y; y++) {
            fScore.put(new Point(x,y), INFINITY);
            gScore.put(new Point(x,y), INFINITY);
         }
      }
      
      gScore.put(this.start, 0);
      fScore.put(this.start, manhattanDistance(start,goal));
      
      pq.add(start);
      
      while(pq.size() != 0) {
         Point currTile = pq.poll();
         if(currTile.equals(goal)) {
            return;
         }
         visited.add(currTile);
         for (int i = 0; i < 4; i++) {
            int x = (int)currTile.getX();
            int y = (int)currTile.getY();
            switch(i) {
            case Model.UP:
               y += 1;
               break;
            case Model.RIGHT:
               x += 1;
               break;
            case Model.DOWN:
               y -= 1;
               break;
            case Model.LEFT:
               x -= 1;
               break;
            }
            Point nextTile = new Point(x,y);
            if(visited.contains(nextTile)) {
               continue;
            }
            if(this.world.get(start) == Model.WATER && this.world.get(goal) == Model.WATER && this.world.get(nextTile) != Model.WATER) {
               continue;
            }
            //Try to find a path without cutting down a tree first.
            if(needToCutTree == true) {
               if (!Model.canPotentiallyMoveOntoTile(world.get(nextTile), haveAxe, haveKey, haveRaft )) {
                  continue;             
               }
            }
            else {
               if (!Model.canPotentiallyMoveOntoTile(world.get(nextTile), false, haveKey, haveRaft )) {
                  continue;             
               }
            }
            int tentative_gScore = gScore.get(currTile) + 1;
            if (tentative_gScore >= gScore.get(nextTile)) {
               continue;
            }
            cameFrom.put(nextTile, currTile);
            gScore.put(nextTile, tentative_gScore);
            fScore.put(nextTile, tentative_gScore + manhattanDistance(nextTile, this.goal));
            if(!pq.contains(nextTile)) {
               pq.add(nextTile);
            }
         }
      }
      if(!reachable() && needToCutTree == false) {
         needToCutTree = true;
         aStar(haveAxe, haveKey, haveRaft);
      }
   }
   //Call this after calling aStar to get a linked list containing the path from start to goal
   //Returns empty linked list if there is no path
   public LinkedList<Point> reconstructPath(){
      LinkedList<Point> path = new LinkedList<Point>();
      Point curr = goal;
      while(cameFrom.get(curr) != null) {
         path.addFirst(curr);
         curr = cameFrom.get(curr);
      }
      return path;
   }
   
   public boolean reachable() {
      return (cameFrom.get(goal) != null);
   }
   
   private int manhattanDistance(Point start, Point goal) {
      return Math.abs((int)start.getX() - (int)goal.getX()) + Math.abs((int)start.getY() - (int)goal.getY());
   }
}
