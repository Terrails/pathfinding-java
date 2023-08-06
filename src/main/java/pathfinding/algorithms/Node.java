package pathfinding.algorithms;

import pathfinding.MapField;

import java.awt.Point;

public class Node extends Point {

    public final MapField field;
    public int parentId;
    public double gCost, fCost;

    public Node(MapField field, int x, int y, int parentId) {
        super(x, y);
        this.field = field;
        this.parentId = parentId;
    }
}