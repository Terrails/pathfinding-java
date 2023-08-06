package pathfinding.algorithms;

import pathfinding.MapField;
import pathfinding.Main;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AStar {

    private static final int[][] NEIGHBOUR_COORDS = new int[][]{ {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1} };
    private static final double DIAGONAL_DISTANCE = Math.sqrt(2) - 2;

    public final List<Point> PATH_FIELDS = new ArrayList<>();
    public final List<Point> CHECKED_FIELDS = new ArrayList<>();

    public final int xMax, yMax;

    public AStar(int xMax, int yMax) {
        this.xMax = xMax;
        this.yMax = yMax;
    }

    public void findeWeg(int startX, int startY, int endX, int endY, MapField[] fieldMap) {
        Node[] nodeMap = createNodeMap(fieldMap);

        final int startIndex = calcIndex(startX, startY);
        nodeMap[startIndex].parentId = -1;
        nodeMap[startIndex].gCost = 0;
        nodeMap[startIndex].fCost = heuristics(nodeMap, startX, startY, endX, endY);

        PriorityQueue<Node> openQueue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        openQueue.add(nodeMap[startIndex]);

        Map<Integer, Node> closedMap = new LinkedHashMap<>();

        long startTime = System.currentTimeMillis();
        while (!openQueue.isEmpty()) {
            Node node = openQueue.peek();
            int index = calcIndex(node.x, node.y);

            if (node.x == endX && node.y == endY) {
                long timeDiff = System.currentTimeMillis() - startTime;
                int opsDone = closedMap.size() + 1; // +1, because last node was not added to closedMap
                double score = node.gCost;
                CHECKED_FIELDS.addAll(closedMap.values());
                PATH_FIELDS.addAll(fetchPath(closedMap, node));

                String output = """
                        Algorithm: %s
                        Time taken: %s
                        Ops done: %s
                        Path cost: %s
                        Path length: %s"""
                        .formatted(Main.SETTINGS_PANEL.USE_DIJKSTRA ? "Dijkstra" : "A*", timeDiff + "ms", opsDone, score, PATH_FIELDS.size());

                System.out.println(output);

                if (Main.SETTINGS_PANEL.LOG.getText().lines().count() >= 10) {
                    String string = Main.SETTINGS_PANEL.LOG.getText().lines().skip(6).collect(Collectors.joining("\n"));
                    Main.SETTINGS_PANEL.LOG.setText(string + "\n\n" + output);
                } else {
                    if (!Main.SETTINGS_PANEL.LOG.getText().isBlank()) {
                        Main.SETTINGS_PANEL.LOG.setText(Main.SETTINGS_PANEL.LOG.getText() + "\n\n");
                    }
                    Main.SETTINGS_PANEL.LOG.setText(Main.SETTINGS_PANEL.LOG.getText() + output);
                }
                return;
            }

            openQueue.remove(node);
            closedMap.put(index, node);

            for (int[] xy : NEIGHBOUR_COORDS) {
                boolean f = false;
                if (xy[0] != 0 && xy[1] != 0) {
                    if (!Main.SETTINGS_PANEL.DIAGONAL_MOVEMENT) continue;
                    f = true;
                }

                int x = node.x + xy[0];
                int y = node.y + xy[1];
                if (!isPointWithinBounds(x, y)) continue;

                int index_ = calcIndex(x, y);
                Node neighbour = nodeMap[index_];

                if (neighbour.field.isAccessible()) {
                    double tentative_gScore = node.gCost + neighbour.field.getWeight() + (f ? 0.5 : 0);

                    if (tentative_gScore < neighbour.gCost) {
                        neighbour.parentId = index;
                        neighbour.gCost = tentative_gScore;
                        neighbour.fCost = tentative_gScore + heuristics(nodeMap, x, y, endX, endY);
                        if (!openQueue.contains(neighbour)) {
                            openQueue.add(neighbour);
                        }
                    }
                }
            }
        }
        JOptionPane.showMessageDialog(Main.FRAME, "No Path Found!");
    }

    protected double heuristics(Node[] map, int x, int y, int endX, int endY) {
        double g = Double.parseDouble(Main.SETTINGS_PANEL.HEURISTICS_WEIGHT.getText());

        int dx = Math.abs(x - endX);
        int dy = Math.abs(y - endY);
        double heuristics;
        if (Main.SETTINGS_PANEL.DIAGONAL_MOVEMENT) {
            heuristics = (dx + dy) + DIAGONAL_DISTANCE * Math.min(dx, dy); // Octile
        } else {
            heuristics = dx + dy; // Manhattan
        }

        heuristics *= g;
        if (Main.SETTINGS_PANEL.TIE_BREAK.isSelected()) {
            heuristics *= (1.0 + (1.0 / 500.0));
        }

        return heuristics;
    }

    private int calcIndex(int x, int y) {
        return (y * this.xMax) + x;
    }

    private boolean isPointWithinBounds(int x, int y) {
        return ((x >= 0 && x < this.xMax) && (y >= 0 && y < this.yMax));
    }

    private ArrayList<Point> fetchPath(Map<Integer, Node> closed, Node node) {
        ArrayList<Point> lastPath = new ArrayList<>();

        Node parentNode = closed.get(node.parentId);
        while (parentNode.parentId != -1) {
            lastPath.add(parentNode);
            parentNode = closed.get(parentNode.parentId);
        }

        Collections.reverse(lastPath);
        return lastPath;
    }

    private Node[] createNodeMap(MapField[] fieldMap) {
        Node[] map = new Node[fieldMap.length];
        for (int x = 0; x < xMax; x++) {
            for (int y = 0; y < yMax; y++) {
                int index = calcIndex(x, y);
                MapField field = fieldMap[index];
                Node node = new Node(field, x, y, -2);
                node.gCost = Double.MAX_VALUE;
                node.fCost = Double.MAX_VALUE;
                map[index] = node;
            }
        }
        return map;
    }
}
