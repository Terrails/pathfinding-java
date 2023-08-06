package pathfinding;

import library.FastNoiseLite;
import pathfinding.algorithms.AStar;
import pathfinding.algorithms.Dijkstra;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MapPanel extends JPanel {

    public final FastNoiseLite noise;
    public AStar pathfinding;
    public int xMax, yMax;

    private int startX, startY, endX, endY;
    private MapField[] noiseArray;
    private int[] noiseSW;

    public int animIndex;
    /**
     * A timer that gradually starts expanding the pathfinding algorithm and finally the path
     * Having this run on a constant speed would make it take ages on bigger map sizes,
     * so this is a weird attempt to make it faster depending on size
     */
    public final Timer animTimer = new Timer(1, new ActionListener() {
        int minSpeed, maxSpeed = -1;

        @Override
        public void actionPerformed(ActionEvent e) {
            int size = pathfinding.CHECKED_FIELDS.size() + pathfinding.PATH_FIELDS.size() - 2;

            if (minSpeed == -1 || maxSpeed == -1) {
                minSpeed = Math.max((size > 50000) ? (size / 4000) : ((size > 7500) ? (size / 1500) : (size / 750)), 1);
                maxSpeed = Math.max((size > 50000) ? 48 : ((size > 7500) ? 16 : 8), minSpeed);
            }

            if (animIndex < size) {
                int speed = Math.max(Math.min((int) ((animIndex / 750) * 1.5), maxSpeed), minSpeed);
                animIndex += speed;
            } else {
                animIndex = size;
                minSpeed = -1;
                maxSpeed = -1;
                animTimer.stop();
            }
        }
    });

    public MapPanel() {
        // FastNoiseLite
        this.noise = new FastNoiseLite();
        this.noise.SetSeed((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        this.noise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
        this.noise.SetFrequency(0.025f);
        this.noise.SetFractalType(FastNoiseLite.FractalType.PingPong);
        this.noise.SetFractalOctaves(11);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (pathfinding == null || pathfinding.PATH_FIELDS.isEmpty()) {
                    Point p = e.getPoint();
                    int x = p.x / Main.SETTINGS_PANEL.FIELD_SIZE;
                    int y = p.y / Main.SETTINGS_PANEL.FIELD_SIZE;

                    if (SwingUtilities.isLeftMouseButton(e)) {
                        MapPanel.this.startX = x;
                        MapPanel.this.startY = y;
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        MapPanel.this.endX = x;
                        MapPanel.this.endY = y;
                    }
                    MapPanel.this.repaint();
                }
            }
        });
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                MapPanel.this.noiseArray = null;
            }
        });
    }

    public void reset(boolean seed) {
        if (seed) {
            if (Main.SETTINGS_PANEL.NOISE_SEED.getText().isBlank()) {
                this.noise.SetSeed((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
            } else this.noise.SetSeed(Integer.parseInt(Main.SETTINGS_PANEL.NOISE_SEED.getText()));
        }
        this.xMax = this.getWidth() / Main.SETTINGS_PANEL.FIELD_SIZE;
        this.yMax = this.getHeight() / Main.SETTINGS_PANEL.FIELD_SIZE;
        this.noiseArray = null;
        this.animTimer.stop();
        this.animIndex = -1;
        this.startX = -1;
        this.startY = -1;
        this.endX = -1;
        this.endY = -1;
        if (this.pathfinding != null && (this.xMax != this.pathfinding.xMax || this.yMax != this.pathfinding.yMax)) {
            this.pathfinding = null;
        }
    }

    /**
     * Caches noise in an Array
     * Getting the noise value from noise#GetNoise on each paintComponent call would be really inefficient
     */
    public void cacheNoise() {
        this.reset(false);
        MapPanel.this.noise.SetFrequency(Main.SETTINGS_PANEL.NOISE_FREQUENCY);
        this.noiseArray = new MapField[this.xMax * this.yMax];
        this.noiseSW = new int[this.yMax * this.xMax];
        for (int y = 0; y < this.yMax; y++) {
            for (int x = 0; x < this.xMax; x++) {
                float noise = this.noise.GetNoise(x / (float) 2, y  / (float) 2); // returns value in -1 ~ 1 range.
                // Convert -1 ~ 1 into 0 ~ 1 range
                noise = (noise + 1.0f) / 2.0f;

                int index = (this.xMax * y) + x;
                if (noise < 0.25) {
                    this.noiseArray[index] = MapField.DEEP_WATER;
                } else if (noise < 0.31) {
                    this.noiseArray[index] = MapField.WATER;
                } else if (noise < 0.35) {
                    this.noiseArray[index] = MapField.SAND;
                } else if (noise < 0.65) {
                    this.noiseArray[index] = MapField.FLAT;
                } else if (noise < 0.8){
                    this.noiseArray[index] = MapField.FOREST;
                } else if (noise < 0.9) {
                    this.noiseArray[index] = MapField.MOUNTAIN;
                } else {
                    this.noiseArray[index] = MapField.MOUNTAINTOP;
                }

                this.noiseSW[index] = (int) (noise * 255);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.noiseArray == null)
            this.cacheNoise();

        this.paintNoise(g);
        this.paintPath(g);
        this.paintPoints(g);
        this.repaint();
    }

    private void paintNoise(Graphics g) {
        for (int y = 0; y < this.yMax; y++) {
            for (int x = 0; x < this.xMax; x++) {

                int index = (this.xMax * y) + x;
                if (Main.SETTINGS_PANEL.SEE_RAW_NOISE.isSelected()) {
                    g.setColor(new Color(noiseSW[index], noiseSW[index], noiseSW[index]));
                } else g.setColor(this.noiseArray[index].getColour());
                fillRect(g, x, y);
            }
        }
    }

    private void paintPath(Graphics g) {
        if (pathfinding != null && this.animIndex != -1) {
            for (int i = 0; i <= this.animIndex && i < pathfinding.CHECKED_FIELDS.size(); i++) {
                Point node = pathfinding.CHECKED_FIELDS.get(i);
                g.setColor(new Color(175, 100, 255, 175));
                fillRect(g, node.x, node.y);
            }

            for (int i = 0; i <= this.animIndex - pathfinding.CHECKED_FIELDS.size() + 1 && i < pathfinding.PATH_FIELDS.size(); i++) {
                Point node = pathfinding.PATH_FIELDS.get(i);
                g.setColor(new Color(255, 255,  0, 175));
                fillRect(g, node.x, node.y);
            }
        }
    }

    private void paintPoints(Graphics g) {
        if (startX != -1 && startY != -1) {
            g.setColor(new Color(21, 225, 245, 200));
            fillRect(g, startX, startY);
        }
        if (endX != -1 && endY != -1) {
            g.setColor(new Color(255, 0, 0, 200));
            fillRect(g, endX, endY);
        }
    }

    private static void fillRect(Graphics g, int x, int y) {
        g.fillRect(x * Main.SETTINGS_PANEL.FIELD_SIZE, y * Main.SETTINGS_PANEL.FIELD_SIZE, Main.SETTINGS_PANEL.FIELD_SIZE, Main.SETTINGS_PANEL.FIELD_SIZE);
    }

    public void pathfinding() {
        if (Main.SETTINGS_PANEL.USE_DIJKSTRA) {
            if (pathfinding == null || pathfinding.getClass() != Dijkstra.class) {
                pathfinding = new Dijkstra(xMax, yMax);
            }
        } else if (pathfinding == null || pathfinding.getClass() != AStar.class) {
            pathfinding = new AStar(xMax, yMax);
        }

        if (startX != -1 && startY != -1 && endX != -1 && endY != -1) {
            pathfinding.findeWeg(this.startX, this.startY, this.endX, this.endY, this.noiseArray);
            this.animIndex = -1;
            this.animTimer.start();
        }
    }
}
