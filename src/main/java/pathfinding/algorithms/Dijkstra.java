package pathfinding.algorithms;

public class Dijkstra extends AStar {

    public Dijkstra(int xMax, int yMax) {
        super(xMax, yMax);
    }

    @Override
    protected double heuristics(Node[] map, int x, int y, int endX, int endY) {
        return 0;
    }
}
