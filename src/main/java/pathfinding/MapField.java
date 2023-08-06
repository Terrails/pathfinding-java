package pathfinding;

import javax.swing.*;
import java.awt.*;

public enum MapField {
    DEEP_WATER(new Color(0, 0, 255)),
    WATER(new Color(0, 196, 255)),
    SAND(new Color(255, 255, 0)),
    FLAT(new Color(0, 135, 0)),
    FOREST(new Color(42, 95, 42)),
    MOUNTAIN(new Color(129, 129, 129)),
    MOUNTAINTOP(new Color(255, 255, 255));

    private final Color colour;
    private final Icon icon;

    MapField(Color colour) {
        this.colour = colour;
        this.icon = new FieldIcon();
    }

    public Color getColour() {
        return this.colour;
    }

    public Icon getIcon() {
        return icon;
    }

    public double getWeight() {
        return switch (this) {
            case DEEP_WATER -> 6;
            case WATER -> 3;
            case SAND -> 1.5;
            case FLAT -> 1;
            case FOREST -> 2.5;
            case MOUNTAIN -> 6.5;
            case MOUNTAINTOP -> 7;
        };
    }

    public boolean isAccessible() {
        return Main.SETTINGS_PANEL.ACCESSIBLE_FIELDS[this.ordinal()];
    }

    private class FieldIcon implements Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(MapField.this.getColour());
            g.fillRect(x, y, 16, 16);
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }
}
