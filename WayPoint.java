import java.util.*;

class WayPoint {

    int idx;
    ArrayList<Integer> links;
    float x, y, z, radius; // relative position to mapunit. 
    MapUnit mapUnit = null;

    // used to construct the waypoint graph
    ArrayList<WayPoint> adj;
    ArrayList<WayPoint> inverts;
    boolean isStart = false;
    WayPoint backWp = null;
    float distToStart = Integer.MAX_VALUE;
    float posX, posY, posZ; // global position in the sublevel

    boolean visited = false; // for BFS
    boolean hasCarryableBehind = false; // for 

    WayPoint copy() {
        WayPoint wp = new WayPoint();
        wp.idx = idx;
        wp.links = new ArrayList<Integer>();
        for (Integer i: links) wp.links.add(i);
        wp.x = x;
        wp.y = y;
        wp.z = z;
        wp.radius = radius;
        return wp;
    }

    void wayPointPos() {
        mapUnit.wayPointPos(this);
    }

}
